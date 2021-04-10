import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

fun <T> Observable<T>.retryWith(transformation: (Throwable) -> Single<*>): Observable<T> =
	retryWhen { errors -> errors.flatMapSingle(transformation) }

fun <T> Single<T>.retryWith(transformation: (Throwable) -> Single<*>): Single<T> =
	retryWhen { errors -> errors.flatMapSingle(transformation) }

fun <T> Maybe<T>.retryWith(transformation: (Throwable) -> Single<*>): Maybe<T> =
	retryWhen { errors -> errors.flatMapSingle(transformation) }

fun Completable.retryWith(transformation: (Throwable) -> Single<*>): Completable =
	retryWhen { errors -> errors.flatMapSingle(transformation) }

fun <T> Observable<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
	retryWith(delayTransformation(delay, timeUnit, scheduler))

fun Completable.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
	retryWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Single<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
	retryWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Maybe<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
	retryWith(delayTransformation(delay, timeUnit, scheduler))

fun predicateErrorTransformation(predicate: (Throwable) -> Boolean) = { throwable: Throwable ->
	if (predicate(throwable)) Single.error(throwable)
	else Single.just(throwable)
}

fun <T> Observable<T>.retryDelayedUntil(
    delay: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation(),
    throwablePredicate: ((Throwable) -> Boolean),
): Observable<T> =
	retryWith(
        combine(
            delayTransformation(delay, timeUnit, scheduler),
            predicateErrorTransformation(throwablePredicate)
        )
    )

fun countErrorTransformation(
    limit: Int,
    start: Int = 0,
    updater: (Int) -> Int = { it + 1 },
): (Throwable) -> @NonNull Single<Throwable> =
	AtomicInteger(start).let { counter ->
		{ throwable: Throwable ->
			val count = counter.get()
			val newCount = updater(count)
			counter.set(newCount)
			if (newCount > limit) Single.error(throwable)
			else Single.just(throwable)
		}
	}

fun <T> Observable<T>.retryReset(handler: (Observable<Throwable>) -> ObservableSource<*>): Observable<T> =
	Single.fromCallable { PublishSubject.create<Unit>() }
		.flatMapObservable { signal ->
			this.doOnNext { signal.onNext(Unit) }
				.retryWhen {
					Observable.defer { handler(it) }
						.takeUntil(signal)
						.repeat()
				}
		}

fun timeoutErrorTransformation(
    timeout: Long,
    timeUnit: TimeUnit,
    timeMsSupplier: () -> Long = { System.currentTimeMillis() },
): (Throwable) -> @NonNull Single<Throwable> =
	@Suppress("ReplaceSingleLineLet")
	timeMsSupplier().let { timeBefore ->
		predicateErrorTransformation { _ ->
			timeMsSupplier() - timeBefore > timeUnit.toMillis(
                timeout
            )
		}
	}

fun exponentialBackoffTransformation(
    initialDelayMs: Long = 100L,
    capDelayMs: Long = 30000L,
    multiplier: Long = 2L,
    scheduler: Scheduler = Schedulers.computation(),
): (Throwable) -> @NonNull Single<Throwable> =
	variableDelayTransformation<Throwable>(initialDelayMs, scheduler) { delayMs ->
		min(
            capDelayMs,
            delayMs * multiplier
        )
	}

fun <T> Observable<T>.onErrorReturnAndThrow(value: T): Observable<T> =
	onErrorResumeNext { error: Throwable -> Observable.just(value).concatWith(Observable.error(error)) }