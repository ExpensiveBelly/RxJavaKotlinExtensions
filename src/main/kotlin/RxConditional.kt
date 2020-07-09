import io.reactivex.rxjava3.core.Observable

fun <T> Observable<Boolean>.whileTrue(observableProvider: () -> Observable<T>): Observable<T> =
    filter(true::equals)
        .firstOrError()
        .ignoreElement()
        .andThen(Observable.defer { observableProvider().takeUntil(filter(false::equals)) })
        .repeat()

fun <T> Observable<Boolean>.whileFalse(observableProvider: () -> Observable<T>): Observable<T> =
    map(Boolean::not).whileTrue(observableProvider)
