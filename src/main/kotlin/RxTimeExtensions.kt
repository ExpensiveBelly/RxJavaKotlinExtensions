import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.time.Duration
import java.util.concurrent.TimeUnit

fun <T : Any> Single<T>.delay(delay: Duration): Single<T> =
    this.delay(delay.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Observable<T>.delay(delay: Duration): Observable<T> =
    this.delay(delay.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Flowable<T>.delay(delay: Duration): Flowable<T> =
    this.delay(delay.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Single<T>.timeout(timeout: Duration): Single<T> =
    this.timeout(timeout.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Observable<T>.debounce(interval: Duration): Observable<T> =
    this.debounce(interval.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Observable<T>.throttleLatest(duration: Duration): Observable<T> =
    this.throttleLatest(duration.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Single<T>.delaySubscription(delay: Duration): Single<T> =
    this.delaySubscription(delay.toMillis(), TimeUnit.MILLISECONDS)

fun <T : Any> Observable<T>.buffer(delay: Duration): Observable<List<T>> =
    this.buffer(delay.toMillis(), TimeUnit.MILLISECONDS)