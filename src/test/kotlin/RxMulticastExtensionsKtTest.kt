import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

class RxMulticastExtensionsKtTest {

    @Test
    fun should_cache_value_when_more_subscribers_subscribe() {
        val testScheduler = TestScheduler()
        val text = "Hello"
        val singleCached =
            Single.defer { Single.just(text).delay(100, TimeUnit.MILLISECONDS, testScheduler) }
                .broadcast(duration = Duration.ofMillis(1))

        singleCached.test().assertNoValues().also { testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS) }
            .assertValue(text)
        singleCached.test().assertValue(text)
    }

    @Test
    fun should_reset_the_cached_value_if_no_subscribers_subscribed() {
        val text1 = "Hello"
        val text2 = "World"
        var firstTime = true
        val singleCached =
            Single.defer { Single.just(if (firstTime) text1 else text2).also { firstTime = false } }.broadcast(duration = Duration.ZERO)

        singleCached.test().assertValue(text1)
        singleCached.test().assertValue(text2)

    }

    @Test
    fun should_share_atomic_reference_value_when_multiple_subscribers_subscribe() {
        val testScheduler = TestScheduler()
        val text = "Hello"
        val singleCached =
            Single.defer { Single.just(text).delay(100, TimeUnit.MILLISECONDS, testScheduler) }.cacheValues(timeout = Duration.ofMillis(1000))

        val subscriber1 = singleCached.test()
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        subscriber1.assertNoValues()

        val subscriber2 = singleCached.test()
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)
        subscriber1.assertValue(text)
        subscriber2.assertValue(text)
    }

    @Test
    fun should_remain_subscribed_during_timeout_even_if_no_subscribers_and_replay_the_element_when_received_while_there_are_no_subscribers() {
        val testScheduler = TestScheduler()
        val text = "Hello"
        val singleCached =
            Single.defer { Single.just(text).delay(100, TimeUnit.MILLISECONDS, testScheduler) }.cacheValues(timeout = Duration.ofMillis(150))

        val subscriber1 = singleCached.subscribe()
        testScheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS)

        val subscriber2 = singleCached.subscribe()
        testScheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS)

        subscriber1.dispose()
        subscriber2.dispose()

        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS)

        val subscriber3 = singleCached.subscribe()
        subscriber3.dispose()

        singleCached.test().assertValue(text)
    }

    @Test
    fun should_remain_subscribed_during_timeout_even_if_no_subscribers_and_replay_the_element_when_received_while_there_is_a_subscriber() {
        val testScheduler = TestScheduler()
        val text = "Hello"
        val singleCached =
            Single.defer { Single.just(text).delay(100, TimeUnit.MILLISECONDS, testScheduler) }.cacheValues(timeout = Duration.ofMillis(150))

        val subscriber1 = singleCached.subscribe()
        testScheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS)

        val subscriber2 = singleCached.subscribe()
        testScheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS)

        subscriber1.dispose()
        subscriber2.dispose()

        testScheduler.advanceTimeBy(40, TimeUnit.MILLISECONDS)
        val subscriber3 = singleCached.test()
        testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS)
        subscriber3.dispose()

        singleCached.test().assertValue(text)
    }

    @Test
    fun should_not_persist_errors_and_try_again_when_using_cache_atomic_reference() {
        val text = "Hello"
        var firstTime = true
        val singleCachedError = Single.defer {
            if (firstTime) {
                Single.error<Throwable>(IllegalStateException()).doOnError { firstTime = false }
            } else {
                Single.just(text)
            }
        }.cacheValues()

        singleCachedError.test().assertError(IllegalStateException::class.java)
        singleCachedError.test().assertValue(text)
        singleCachedError.test().assertValue(text)
    }

    @Test
    fun should_cache_atomic_reference_even_if_unsubscribed_and_share_the_same_value() {
        val text = "Hello"
        val singleCached = Single.defer { Single.just(text) }.cacheValues()

        val disposable = singleCached.subscribe()

        disposable.dispose()

        singleCached.test().assertValue(text)
    }

    /**
     * What's the difference between `cacheAtomicReference` and `cache` operator?
     */

    @Test
    fun `cache_should_re-throw_the_error_whereas_cacheAtomicReference_should_not`() {
        val text = "Hello"
        var firstTimeCacheAtomicReference = true
        val singleCachedAtomicReference = Single.defer {
            if (firstTimeCacheAtomicReference) {
                Single.error<Throwable>(IllegalStateException()).doOnError { firstTimeCacheAtomicReference = false }
            } else {
                Single.just(text)
            }
        }.cacheValues()

        var firstTimeCache = true
        val singleCached = Single.defer {
            if (firstTimeCache) {
                Single.error<Throwable>(IllegalStateException()).doOnError { firstTimeCache = false }
            } else {
                Single.just(text)
            }
        }.cache()

        singleCachedAtomicReference.test().assertError(IllegalStateException::class.java)
        // Subsequent subscription triggers the source again
        singleCachedAtomicReference.test().assertValue(text)

        singleCached.test().assertError(IllegalStateException::class.java)
        // Subsequent subscription throws error again
        singleCached.test().assertError(IllegalStateException::class.java)
    }

    @Test
    fun cache_with_on_terminate_detach_should_not_re_throw_the_error() {
        val text = "Hello"
        var firstTimeCacheAtomicReference = true
        val singleCachedAtomicReference = Single.defer {
            if (firstTimeCacheAtomicReference) {
                Single.error<Throwable>(IllegalStateException()).doOnError { firstTimeCacheAtomicReference = false }
            } else {
                Single.just(text)
            }
        }.cacheValues()

        var firstTimeCache = true
        val singleCached = Single.defer {
            if (firstTimeCache) {
                Single.error<Throwable>(IllegalStateException()).doOnError { firstTimeCache = false }
                    .onTerminateDetach()
            } else {
                Single.just(text)
            }
        }
            .onTerminateDetach()
            .cache()
            .onTerminateDetach()

        singleCachedAtomicReference.test().assertError(IllegalStateException::class.java)
        // Subsequent subscription triggers the source again
        singleCachedAtomicReference.test().assertValue(text)

        singleCached.test().assertError(IllegalStateException::class.java)
        // Subsequent subscription throws error again
        singleCached.test().assertError(IllegalStateException::class.java)
    }
}