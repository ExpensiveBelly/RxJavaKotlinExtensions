import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

fun <T> Flowable<Flowable<T>>.flattenMerge() = flatMap { it }
fun <T> Flowable<Flowable<T>>.flattenLatest() = switchMap { it }
fun <T> Flowable<Flowable<T>>.flattenConcat() = concatMap { it }
fun <T> Flowable<out Iterable<T>>.flattenIterable() = flatMapIterable { it }

fun <T> Observable<Observable<T>>.flattenMerge() = flatMap { it }
fun <T> Observable<Observable<T>>.flattenLatest() = switchMap { it }
fun <T> Observable<Observable<T>>.flattenConcat() = concatMap { it }
fun <T> Observable<out Iterable<T>>.flattenIterable() = flatMapIterable { it }

fun <T> Single<Single<T>>.flatten() = flatMap { it }
fun <T> Maybe<Maybe<T>>.flatten() = flatMap { it }