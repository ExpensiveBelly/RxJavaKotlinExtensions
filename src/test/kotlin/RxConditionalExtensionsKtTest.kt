import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.*
import org.junit.Test

class RxConditionalExtensionsKtTest {
    val observable = BehaviorSubject.create<Boolean>()
    val subject = PublishSubject.create<Int>()

    @Test
    fun `when true will subscribe`() {
        val testObserver = observable.whileTrue { subject }.test()

        subject.onNext(1)
        observable.onNext(true)
        subject.onNext(2)

        assertTrue(subject.hasObservers())
        testObserver.assertValue(2)
        testObserver.assertNotComplete()
    }

    @Test
    fun `when not true will not subscribe`() {
        val testObserver = observable.whileTrue { subject }.test()

        subject.onNext(1)
        observable.onNext(false)
        subject.onNext(2)

        assertFalse(subject.hasObservers())
        testObserver.assertNoValues()
    }

    @Test
    fun `when it becomes false will unsubscribe`() {
        val testObserver = observable.whileTrue { subject }.test()

        subject.onNext(1)
        observable.onNext(true)
        subject.onNext(2)
        subject.onNext(3)
        observable.onNext(false)
        subject.onNext(4)
        subject.onNext(5)

        assertFalse(subject.hasObservers())
        testObserver.assertValues(2, 3)
    }

    @Test
    fun `when it becomes true again will resubscribe`() {
        val testObserver = observable.whileTrue { subject }.test()

        observable.onNext(false)
        subject.onNext(2)
        observable.onNext(true)
        subject.onNext(3)
        observable.onNext(false)
        subject.onNext(4)
        subject.onNext(5)
        observable.onNext(true)
        subject.onNext(6)
        subject.onNext(7)

        assertTrue(subject.hasObservers())
        testObserver.assertValues(3, 6, 7)
    }
}