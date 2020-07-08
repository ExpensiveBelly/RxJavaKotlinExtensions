import io.reactivex.rxjava3.core.*

typealias RetryStrategy = (error: Throwable) -> Completable

fun <T> Flowable<T>.retryWith(strategy: RetryStrategy) =
    retryWhen { errors -> errors.flatMapSingle(useStrategy(strategy)) }
fun <T> Observable<T>.retryWith(strategy: RetryStrategy) =
    retryWhen { errors -> errors.flatMapSingle(useStrategy(strategy)) }
fun <T> Maybe<T>.retryWith(strategy: RetryStrategy) =
    retryWhen { errors -> errors.flatMapSingle(useStrategy(strategy)) }
fun <T> Single<T>.retryWith(strategy: RetryStrategy) =
    retryWhen { errors -> errors.flatMapSingle(useStrategy(strategy)) }
fun Completable.retryWith(strategy: RetryStrategy) =
    retryWhen { errors -> errors.flatMapSingle(useStrategy(strategy)) }

private fun useStrategy(strategy: RetryStrategy) =
    { throwable: Throwable -> strategy(throwable).toSingleDefault(Unit) }