import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.repeatWith(transformation: (Any) -> Single<Any>): Observable<T> = repeatWhen { it.flatMapSingle(transformation) }
fun <T> Single<T>.repeatWith(transformation: (Any) -> Single<Any>): Flowable<T> = repeatWhen { it.flatMapSingle(transformation) }
fun <T> Maybe<T>.repeatWith(transformation: (Any) -> Single<Any>): Flowable<T> = repeatWhen { it.flatMapSingle(transformation) }
fun Completable.repeatWith(transformation: (Any) -> Single<Any>): Completable = repeatWhen { it.flatMapSingle(transformation) }

fun <T> Observable<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
    repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Single<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
    repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Maybe<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
    repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun Completable.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
    repeatWith(delayTransformation(delay, timeUnit, scheduler))