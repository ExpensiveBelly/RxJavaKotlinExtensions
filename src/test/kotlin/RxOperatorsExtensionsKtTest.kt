import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import org.junit.Assert.assertTrue
import org.junit.Test

class RxOperatorsExtensionsKtTest {

    @Test
    fun `concatScanEager publishes first value transformed`() {
        concatScanEager(
            Single.just("value"),
            Observable.empty<Int>()
        ) { accumulated, newValue -> accumulated + newValue }
            .test()
            .assertValue("value")
    }

    @Test
    fun `concatScanEager scans through values`() {

        concatScanEager(
            Single.just("value"),
            Observable.just(1, 2, 3)
        ) { accumulated, newValue -> accumulated + newValue }
            .test()
            .assertValues("value", "value1", "value12", "value123")
    }

    @Test
    fun `concatScanEager subscribes to observable before single emits`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()

        concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        // then
        assertTrue(single.hasObservers())
        assertTrue(observable.hasObservers())
    }

    @Test
    fun `concatScanEager observable emits first, initial value is used from single and then emission is used`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        observable.onNext(1)
        single.onSuccess("value")

        observer.assertValues("value", "value1")
    }

    @Test
    fun `concatScanEager single errors, outputs error`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()
        val error = Exception()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        // when
        single.onError(error)

        // then
        observer.assertNoValues().assertError(error)
    }

    @Test
    fun `concatScanEager observable errors, outputs error after single emits`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()
        val error = Exception()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        observable.onError(error)
        observer.assertNoErrors()
        single.onSuccess("value")

        observer.assertValue("value").assertError(error)
    }

    @Test
    fun `zip waits for all results and puts them in a list in the same order`() {
        val scheduler = TestScheduler()
        val subject = SingleSubject.create<Long>()
        val observer = zip(
            listOf(
                subject,
                Single.just(10L)
            ), defaultWhenEmpty = null
        )
            .test()

        observer.assertNoValues()
        subject.onSuccess(3L)
        observer.assertValue(listOf(3L, 10L))
    }

    @Test
    fun `zip defaults when list is empty`() {
        zip(emptyList(), defaultWhenEmpty = listOf(3L, 8L))
            .test()
            .assertValue(listOf(3L, 8L))
    }

    @Test
    fun `zip throws error when empty and no default value`() {
        zip(emptyList<Single<Unit>>(), defaultWhenEmpty = null)
            .test()
            .assertError(NoSuchElementException::class.java)
    }

    @Test
    fun `zip emits error when any single is error`() {
        val error = Throwable()
        zip(
            listOf(
                Single.just(1L),
                Single.error(error)
            ), defaultWhenEmpty = null
        )
            .test()
            .assertError(error)
    }

    @Test
    fun `combineLatest waits for all results and puts them in a list in the same order`() {
        val subject1 = PublishSubject.create<Long>()
        val subject2 = PublishSubject.create<Long>()
        val observer = combineLatest(
            listOf(
                subject1,
                subject2
            )
        )
            .test()

        observer.assertNoValues()
        subject1.onNext(5L)
        subject1.onNext(3L)
        subject2.onNext(10L)
        subject2.onNext(1L)
        subject2.onNext(1L)
        subject1.onNext(11L)

        observer.assertValues(
            listOf(3L, 10L),
            listOf(3L, 1L),
            listOf(3L, 1L),
            listOf(11L, 1L)
        )
    }

    @Test
    fun `combineLatest completes when list is empty`() {
        combineLatest(emptyList<Observable<Unit>>())
            .test()
            .assertNoValues()
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `combineLatest emits error when any observable is error`() {
        val error = Throwable()
        val subject2 = PublishSubject.create<Long>()
        val observer = combineLatest(
            listOf(
                Observable.just(1L),
                subject2
            )
        ).test()

        subject2.onNext(10L)
        subject2.onError(error)

        observer.assertError(error)
    }

    @Test
    fun `combineLatest completes when any observable is empty or when all complete`() {
        val subject2 = PublishSubject.create<Long>()
        combineLatest(
            listOf(
                Observable.empty<Long>(),
                subject2
            )
        )
            .test()
            .assertComplete()

        val observer = combineLatest(
            listOf(
                Observable.just(1L),
                subject2
            )
        ).test()

        subject2.onNext(10L)
        subject2.onComplete()

        observer.assertValueCount(1)
        observer.assertComplete()
    }
}