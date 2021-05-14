import com.github.fsbarata.functional.data.maybe.Optional
import com.github.fsbarata.functional.data.maybe.Some
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.ofType
import java.util.concurrent.TimeUnit

fun <T, U> Observable<T>.mapNotNull(transform: (T) -> U?): Observable<U> = flatMapMaybe(transformToMaybe(transform))
fun <T, U> Single<T>.mapNotNull(mapper: (T) -> U?): Maybe<U> = flatMapMaybe { mapper(it).toMaybe() }
fun <T, U> Maybe<T>.mapNotNull(transform: (T) -> U?): Maybe<U> = flatMap(transformToMaybe(transform))
private fun <T, U> transformToMaybe(transform: (T) -> U?): (T) -> Maybe<U> = { transform(it).toMaybe() }

fun <T> Observable<Optional<T>>.filterNotNull(): Observable<T> = ofType<Some<T>>().map { it.value }

@Suppress("UNCHECKED_CAST")
fun <T> List<Single<T>>.zip(defaultIfEmpty: Single<List<T>> = Single.just(emptyList())): Single<List<T>> =
	if (isNotEmpty()) Single.zip(this) { args -> args.map { it as T } }
	else defaultIfEmpty

@Suppress("UNCHECKED_CAST")
fun <T> List<Maybe<T>>.zip(): Maybe<List<T>> = Maybe.zip(this) { args -> args.map { it as T } }

@Suppress("UNCHECKED_CAST")
fun <T> List<Observable<T>>.zip(): Observable<List<T>> =
	Observable.zip<T, List<T>>(this) { args -> args.map { it as T } }

@Suppress("UNCHECKED_CAST")
fun <T> List<Observable<T>>.combineLatest(): Observable<List<T>> =
	Observable.combineLatest<T, List<T>>(this) { args -> args.map { it as T } }

inline fun <reified U: Any> Observable<*>.filterOf(): Observable<U> =
	flatMap {
		if (it is U) Observable.just(it)
		else Observable.empty<U>()
	}

fun <T> Maybe<T>.delayIfEmpty(time: Long, timeUnit: TimeUnit, scheduler: Scheduler): Maybe<T> =
	switchIfEmpty(Single.timer(time, timeUnit, scheduler).ignoreElement().toMaybe())

fun <T> Observable<T>.aggregate(): Observable<List<T>> =
	scan(emptyList<T>()) { list, value -> list + value }.skip(1)

