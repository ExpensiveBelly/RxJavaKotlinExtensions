import hu.akarnokd.rxjava3.operators.ObservableTransformers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Predicate

/*
https://github.com/akarnokd/RxJavaExtensions
 */

fun <T, R> Observable<T>.flatMapDrop(mapper: (T) -> Observable<R>): Observable<R> =
    compose(ObservableTransformers.flatMapDrop(mapper))

fun <T, R> Observable<T>.flatMapLatest(mapper: (T) -> Observable<R>): Observable<R> =
    compose(ObservableTransformers.flatMapLatest(mapper))

fun <T> Observable<T>.valve(valveSource: Observable<Boolean>, defaultOpen: Boolean): Observable<T> =
    compose(ObservableTransformers.valve(valveSource, defaultOpen))

fun <T> Observable<T>.indexOf(predicate: Predicate<T>): Observable<Long> =
    compose(ObservableTransformers.indexOf(predicate))

fun <T, R> Observable<T>.errorJump(transformer: (Observable<T>) -> Observable<R>): Observable<R> =
    compose(ObservableTransformers.errorJump(transformer))

fun <T> Observable<T>.observeOnDrop(scheduler: Scheduler) =
    compose(ObservableTransformers.observeOnDrop(scheduler))

fun <T> Observable<T>.observeOnLatest(scheduler: Scheduler) =
    compose(ObservableTransformers.observeOnLatest(scheduler))