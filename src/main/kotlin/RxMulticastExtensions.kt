import com.jakewharton.rx3.replayingShare
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.time.Duration
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.broadcast(bufferSize: Int = 1, duration: Duration = Duration.ZERO): Observable<T> =
    replay(bufferSize).refCount(duration.toNanos(), TimeUnit.NANOSECONDS)

fun <T> Single<T>.broadcast(duration: Duration = Duration.ZERO): Single<T> =
    toObservable().replay(1).refCount(duration.toNanos(), TimeUnit.NANOSECONDS).firstOrError()

fun <T> Single<T>.share(): Single<T> = toObservable().share().firstOrError()
fun <T> Maybe<T>.share(): Maybe<T> = toObservable().share().firstElement()
fun Completable.share(): Completable = toObservable<Unit>().share().ignoreElements()

/**
 * Works like .cache() operator, except errors will not be cached
 * Warning: This cache will be in memory and has no means of invalidation.
 */

fun <T> Single<T>.cacheValues(timeout: Duration = Duration.ZERO): Single<T> =
    toObservable()
        .replay(1).refCount(timeout.toNanos(), TimeUnit.NANOSECONDS)
        .replayingShare()
        .firstOrError()