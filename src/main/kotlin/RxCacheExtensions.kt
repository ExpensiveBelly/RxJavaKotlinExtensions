import io.reactivex.rxjava3.core.Single
import java.util.concurrent.atomic.AtomicReference

fun <T> Single<T>.broadcast() = toObservable().replay(1).refCount().singleOrError()

/**
 * Works like .cache() operator, except errors will not be cached
 * Warning: This cache will be in memory and has no means of invalidation.
 */
fun <T> Single<T>.cacheValues(): Single<T> {
    val reference = AtomicReference<T>()

    val referenceShared = this.doOnSuccess { reference.set(it) }.broadcast()

    return Single.defer {
        val value = reference.get()
        if (value != null) Single.just(value)
        else referenceShared
    }
}