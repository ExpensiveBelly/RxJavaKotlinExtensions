import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

fun <T> delayTransformation(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()): (T) -> Single<T> =
    delayTransformation<T>({ delay }, timeUnit, scheduler)

fun <T> delayTransformation(delayProvider: (T) -> Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()): (T) -> Single<T> = { obj: T ->
    val delay = delayProvider(obj)
    if (delay > 0) Single.timer(delayProvider(obj), timeUnit, scheduler).map { obj }
    else Single.just(obj)
}

fun <T> variableDelayTransformation(initialDelayMs: Long, scheduler: Scheduler = Schedulers.computation(), delayUpdater: (Long) -> Long) =
    AtomicLong(initialDelayMs).let { counter ->
        { obj: T ->
            val count = counter.get()
            val newCount = delayUpdater(count)
            counter.set(newCount)
            Single.timer(newCount, TimeUnit.MILLISECONDS, scheduler).map { obj }
        }
    }

fun <T> combine(vararg transformations: (T) -> Single<T>): (T) -> Single<out T> = { obj ->
    transformations.fold(Single.just(obj)) { single, transformation -> single.flatMap(transformation) }
}