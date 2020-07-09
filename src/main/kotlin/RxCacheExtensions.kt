import io.reactivex.rxjava3.core.Single
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun <T> Single<T>.broadcast(): Single<T> = toObservable().replay(1).refCount().singleOrError()

/**
 * Works like .cache() operator, except errors will not be cached
 * Warning: This cache will be in memory and has no means of invalidation.
 */
fun <T> Single<T>.cacheValuesIndefinitely(): Single<T> {
    val reference = AtomicReference<T>()

    val referenceShared = this.doOnSuccess { reference.set(it) }.broadcast()

    return Single.defer {
        val value = reference.get()
        if (value != null) Single.just(value)
        else referenceShared
    }
}

fun <T> Single<T>.cacheValuesFor(duration: Duration): Single<T> =
    toObservable().replay(1).refCount(duration.toMillis(), TimeUnit.MILLISECONDS).firstOrError()