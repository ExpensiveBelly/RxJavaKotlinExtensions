import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> reactiveProperty(behaviorSubject: BehaviorSubjectWithDefault<T>) =
	object: ReadWriteProperty<Any?, T> {
		override fun getValue(thisRef: Any?, property: KProperty<*>): T =
			behaviorSubject.value

		override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
			behaviorSubject.onNext(value)
		}
	}
