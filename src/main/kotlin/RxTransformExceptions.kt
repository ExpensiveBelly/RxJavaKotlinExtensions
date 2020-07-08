import arrow.core.Option
import arrow.core.Some
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.withLatestFrom

fun <T, U> Observable<T>.mapNotNull(transform: (T) -> U?) = flatMapMaybe(transformToMaybe(transform))

fun <T, U> Maybe<T>.mapNotNull(transform: (T) -> U?) = flatMap(transformToMaybe(transform))

private fun <T, U> transformToMaybe(transform: (T) -> U?): (T) -> Maybe<U> = { transform(it).toMaybe() }

fun <T> Observable<Option<T>>.filterNotNull(): Observable<T> = filterType<Some<T>>().map { it.t }

inline fun <reified T> Observable<in T>.filterType(): Observable<T> = mapNotNull { it as? T }

