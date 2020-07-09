import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.exceptions.CompositeException

fun <T> Observable<T>.transformErrors(transform: (Throwable) -> Throwable) =
    onErrorResumeNext { error: Throwable -> Observable.error(transform(error)) }

fun <T> Observable<T>.onErrorComplete(predicate: (Throwable) -> Boolean = { true }) =
    onErrorResumeNext { throwable: Throwable ->
        if (predicate(throwable)) Observable.empty()
        else Observable.error(throwable)
    }

fun <T> Observable<T>.onErrorResume(f: (Throwable) -> Observable<T>) = onErrorResumeNext(f)

fun <T> Observable<T>.onErrorReturnAndThrow(valueGenerator: (Throwable) -> T?) =
    onErrorResume {
        valueGenerator(it).toMaybe()
            .toObservable()
            .concatWith(Completable.error(it))
    }

/**
 * CompositeExceptions can occur when combining multiple parallel observables that have errors at a
 * similar time frame. Operators such as merge, concatEager and switchOnNext are examples of those
 * which can generate these errors.
 * This method will reduce CompositeExceptions when they contain the only one class of exceptions.
 */
fun <T> Observable<T>.sanitiseCompositeException() = transformErrors { error: Throwable ->
    if (error is CompositeException) error.mergeIfSame()
    else error
}

fun CompositeException.mergeIfSame(): Throwable {
    val distinctExceptions = exceptions.distinct()
    return if (distinctExceptions.size == 1) distinctExceptions[0]
    else CompositeException(distinctExceptions)
}

