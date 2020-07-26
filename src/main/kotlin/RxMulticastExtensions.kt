import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun <T> Observable<T>.broadcast(bufferSize: Int = 1) = replay(bufferSize).refCount()

@Deprecated("use share()")
fun <T> Single<T>.broadcast() = toObservable().broadcast().singleOrError()

fun <T> Single<T>.share() = toObservable().share().firstOrError()
fun <T> Maybe<T>.share() = toObservable().share().firstElement()
fun Completable.share() = toObservable<Unit>().share().ignoreElements()

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