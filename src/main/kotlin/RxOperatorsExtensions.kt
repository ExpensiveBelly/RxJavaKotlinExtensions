import com.github.fsbarata.functional.data.either.Either
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.withLatestFrom
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@Suppress("UNCHECKED_CAST")
fun <T> zip(iterable: List<Single<T>>, defaultWhenEmpty: List<T>?): Single<List<T>> =
	if (defaultWhenEmpty == null || iterable.isNotEmpty()) Single.zip(iterable) { (it as Array<T>).asList() }
	else Single.just(defaultWhenEmpty)

@Suppress("UNCHECKED_CAST")
fun <T> combineLatest(
	collection: Collection<Observable<T>>,
	defaultIfEmpty: List<T> = emptyList(),
): Observable<List<T>> =
	if (collection.isEmpty()) Observable.just(defaultIfEmpty) else Observable.combineLatest(collection) { (it as Array<T>).asList() }

fun <T: Any> Observable<T>.valve(valveSource: Observable<Boolean>): Observable<T> =
	withLatestFrom(valveSource.distinctUntilChanged())
		.mapNotNull { (value, valve) -> value.takeIf { valve } }

fun <T> Observable<T>.debounceAfterFirst(
	time: Long,
	timeUnit: TimeUnit,
	scheduler: Scheduler = Schedulers.computation(),
): Observable<T> =
	publish { obs ->
		obs.firstElement().toObservable().concatWith(obs.skip(1).debounce(time, timeUnit, scheduler))
	}

fun <T, U> concatScanEager(
	initialValueSingle: Single<T>,
	valuesObservable: Observable<U>,
	accumulator: (T, U) -> T,
): Observable<T> =
	Observable.concatArrayEager(
		initialValueSingle.map { Either.Left(it) }.toObservable(),
		valuesObservable.map { Either.Right(it) }
	)
		.scan { leftValue, rightValue ->
			Either.Left(
				accumulator(
					(leftValue as Either.Left).value,
					(rightValue as Either.Right).value
				)
			)
		}
		.map { (it as Either.Left).value }

fun <T, R> Observable<T>.zipWithNext(f: (T, T) -> R) =
	bufferExact(2, 1).map { (a: T, b: T) -> f(a, b) }

fun <T> Observable<T>.bufferExact(count: Int, skip: Int) =
	buffer(count, skip).filter { it.size == count }

/*
`skipLastBut(0)` is equivalent to `skipLast(1)`
 */

fun <T> Observable<T>.skipLastBut(n: Int): Observable<T> = skipLast(n + 1).concatWith(takeLast(n))

fun <T, U> Observable<T>.toggleMap(f: (T) -> Observable<U>) =
	publish { obs -> obs.flatMapDrop { f(it).takeUntil(obs) } }