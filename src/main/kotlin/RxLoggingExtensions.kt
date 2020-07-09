@file:Suppress("NOTHING_TO_INLINE", "IMPLICIT_CAST_TO_ANY")

import io.reactivex.rxjava3.core.*

/**
 * https://gist.github.com/tomaszpolanski/99c37c388e06e57ef72a5c8e752b8c2c
 */

inline fun <reified T> printEvent(tag: String, success: T?, error: Throwable?) =
    when {
        success == null && error == null -> RxLog.d(tag, "Complete") /* Only with Maybe */
        success != null -> RxLog.d(tag, "Success $success")
        error != null -> RxLog.d(tag, "Error $error")
        else -> -1 /* Cannot happen*/
    }

inline fun printEvent(tag: String, error: Throwable?) =
    when {
        error != null -> RxLog.d(tag, "Error $error")
        else -> RxLog.d(tag, "Complete")
    }

/**
 * Example usage of [log]:
 *   Single.timer(1, TimeUnit.SECONDS).log().subscribe ({ }, { })
 */

inline fun tag() =
    Thread.currentThread().stackTrace
        .first { it.fileName.endsWith(".kt") }
        .let { stack -> "${stack.fileName.removeSuffix(".kt")}::${stack.methodName}:${stack.lineNumber}" }

inline fun <reified T> Single<T>.log(tag: String = tag()): Single<T> {
    return doOnEvent { success, error -> printEvent(tag, success, error) }
        .doOnSubscribe { RxLog.d(tag, "Subscribe") }
        .doOnDispose { RxLog.d(tag, "Dispose") }
}

inline fun <reified T> Maybe<T>.log(tag: String = tag()): Maybe<T> {
    return doOnEvent { success, error -> printEvent(tag, success, error) }
        .doOnSubscribe { RxLog.d(tag, "Subscribe") }
        .doOnDispose { RxLog.d(tag, "Dispose") }
}

inline fun Completable.log(tag: String = tag()): Completable {
    return doOnEvent { printEvent(tag, it) }
        .doOnSubscribe { RxLog.d(tag, "Subscribe") }
        .doOnDispose { RxLog.d(tag, "Dispose") }
}

inline fun <reified T> Observable<T>.log(tag: String = tag()): Observable<T> {
    return doOnEach { RxLog.d(tag, "Each $it") }
        .doOnSubscribe { RxLog.d(tag, "Subscribe") }
        .doOnDispose { RxLog.d(tag, "Dispose") }
}

inline fun <reified T> Flowable<T>.log(tag: String = tag()): Flowable<T> {
    return doOnEach { RxLog.d(tag, "Each $it") }
        .doOnSubscribe { RxLog.d(tag, "Subscribe") }
        .doOnCancel { RxLog.d(tag, "Cancel") }
}


object RxLog {
    fun d(tag: String, message: String) {
        println("$tag : $message")
    }
}