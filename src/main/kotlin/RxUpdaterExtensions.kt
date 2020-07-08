import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicReference

fun <T> Observable<T>.update(updater: (T) -> Unit): Completable =
    doOnNext { updater(it) }.ignoreElements()

fun <T> Observable<T>.updateChanges(updater: (T) -> Unit) =
    distinctUntilChanged().update(updater)

fun <T> Flowable<T>.update(updater: (T) -> Unit): Completable =
    doOnNext { updater(it) }.ignoreElements()

fun <T> Flowable<T>.updateChanges(updater: (T) -> Unit) =
    distinctUntilChanged().update(updater)

fun <T> Observable<T>.updateOnScheduler(scheduler: Scheduler, updater: (T) -> Unit): Completable =
    switchMapCompletable { onScheduler(scheduler) { updater(it) } }

private fun onScheduler(scheduler: Scheduler, action: () -> Unit) =
    Completable.fromAction(action).subscribeOn(scheduler)

fun <T> Observable<T>.updateChangesOnScheduler(scheduler: Scheduler, updater: (T) -> Unit): Completable =
    Single.fromCallable { AtomicReference<T>(null) }
        .flatMapCompletable { reference ->
            updateOnScheduler(scheduler) {
                if (reference.get() != it) {
                    updater(it)
                    reference.set(it)
                }
            }
        }

fun <T> Observable<T>.updateChangesOnComputation(updater: (T) -> Unit) =
    updateChangesOnScheduler(Schedulers.computation(), updater)

fun <T> Single<T>.update(updater: (T) -> Unit): Completable =
    doOnSuccess { updater(it) }.ignoreElement()

fun <T> Maybe<T>.update(updater: (T) -> Unit): Completable =
    doOnSuccess { updater(it) }.ignoreElement()

fun <T> Observable<T>.completeOnFirst() = firstOrError().ignoreElement()
