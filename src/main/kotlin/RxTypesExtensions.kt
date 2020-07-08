import io.reactivex.rxjava3.core.Maybe

fun <T> T?.toMaybe(): Maybe<T> = when (this) {
    null -> Maybe.empty()
    else -> Maybe.just(this)
}