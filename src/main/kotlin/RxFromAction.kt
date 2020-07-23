import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

fun rxCompletable(action: () -> Unit) = Completable.fromAction(action)
fun <T : Any> rxSingle(callable: () -> T) = Single.fromCallable(callable)
fun <T : Any> rxMaybe(callable: () -> T?) = Maybe.defer { callable.toMaybe() }