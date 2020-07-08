import arrow.core.Either
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.withLatestFrom
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> zip(iterable: List<Single<T>>, defaultWhenEmpty: List<T>?) =
    if (defaultWhenEmpty == null || iterable.isNotEmpty()) Single.zip(iterable) { (it as Array<T>).asList() }
    else Single.just(defaultWhenEmpty)

fun <T> combineLatest(iterable: Iterable<Observable<T>>) =
    Observable.combineLatest(iterable) { (it as Array<T>).asList() }

fun <T : Any> Observable<T>.valve(valveSource: Observable<Boolean>): Observable<T> =
    withLatestFrom(valveSource.distinctUntilChanged())
        .mapNotNull { (value, valve) -> value.takeIf { valve } }

fun <T> Observable<T>.debounceAfterFirst(
    time: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
) =
    firstElement().toObservable().concatWith(skip(1).debounce(time, timeUnit, scheduler))

fun <T, U> concatScanEager(
    initialValueSingle: Single<T>,
    valuesObservable: Observable<U>,
    accumulator: (T, U) -> T
): Observable<T> =
    Observable.concatArrayEager(
        initialValueSingle.map { Either.Left(it) }.toObservable(),
        valuesObservable.map { Either.Right(it) }
    )
        .scan { leftValue, rightValue ->
            Either.Left(
                accumulator(
                    (leftValue as Either.Left).a,
                    (rightValue as Either.Right).b
                )
            )
        }
        .map { (it as Either.Left).a }