import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

/**
 * Null-safe behavior subject
 */

class BehaviorSubjectWithDefault<T> private constructor(
    private val behaviorSubject: BehaviorSubject<T>
) : Subject<T>(), Observer<T> by behaviorSubject {

    val value: T get() = checkNotNull(behaviorSubject.value)

    override fun subscribeActual(observer: Observer<in T>) {
        behaviorSubject.subscribe(observer)
    }

    override fun hasObservers(): Boolean = behaviorSubject.hasObservers()
    override fun hasThrowable(): Boolean = behaviorSubject.hasThrowable()
    override fun hasComplete(): Boolean = behaviorSubject.hasComplete()
    override fun getThrowable(): Throwable? = behaviorSubject.throwable

    companion object {
        fun <T> create(initialValue: T) =
            BehaviorSubjectWithDefault(BehaviorSubject.createDefault(initialValue))
    }
}