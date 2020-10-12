import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RxUpdaterExtensionsKtTest {

    @Test
    fun `update executes action upon value`() {
        val list = mutableListOf<Int>()
        val subject = PublishSubject.create<Int>()
        subject.update { list += it + 1 }.test()

        subject.onNext(3)
        assertEquals(listOf(4), list)
        subject.onNext(-1)
        assertEquals(listOf(4, 0), list)
    }

    @Test
    fun `updateOnScheduler will executes action on scheduler`() {
        val list = mutableListOf<Int>()
        val scheduler = TestScheduler()
        val subject = PublishSubject.create<Int>()
        subject.updateOnScheduler(scheduler) { list += it + 1 }.test()

        subject.onNext(3)
        assertTrue(list.isEmpty())
        scheduler.triggerActions()
        assertEquals(listOf(4), list)
    }

    @Test
    fun `updateOnScheduler will not cancel if busy processing`() {
        val list = mutableListOf<Int>()
        val scheduler = TestScheduler()
        val subject = PublishSubject.create<Int>()
        subject.updateOnScheduler(scheduler) { list += it + 1 }.test()

        subject.onNext(3)
        assertTrue(list.isEmpty())
        subject.onNext(0)
        scheduler.triggerActions()
        assertEquals(listOf(1), list)
    }

    @Test
    fun `updateWhenChanged executes action upon different value`() {
        val list = mutableListOf<Int>()
        val subject = PublishSubject.create<Int>()
        subject.updateChanges { list += it + 1 }.test()

        subject.onNext(3)
        assertEquals(listOf(4), list)
        subject.onNext(-1)
        assertEquals(listOf(4, 0), list)
        subject.onNext(-1)
        assertEquals(listOf(4, 0), list)
        subject.onNext(3)
        assertEquals(listOf(4, 0, 4), list)
    }

    @Test
    fun `updateOnSchedulerWhenChanged executes action upon different value`() {
        val list = mutableListOf<Int>()
        val scheduler = TestScheduler()
        val subject = PublishSubject.create<Int>()
        subject.updateOnSchedulerWhenChanged(scheduler) { list += it + 1 }.test()

        subject.onNext(3)
        assertTrue(list.isEmpty())
        scheduler.triggerActions()
        assertEquals(listOf(4), list)
        subject.onNext(-1)
        assertEquals(listOf(4), list)
        scheduler.triggerActions()
        assertEquals(listOf(4, 0), list)
        subject.onNext(-1)
        scheduler.triggerActions()
        assertEquals(listOf(4, 0), list)
        subject.onNext(3)
        scheduler.triggerActions()
        assertEquals(listOf(4, 0, 4), list)
    }

    @Test
    fun `updateOnSchedulerWhenChanged will not re-display the latest if the same as the last successful`() {
        val list = mutableListOf<Int>()
        val scheduler = TestScheduler()
        val subject = PublishSubject.create<Int>()
        subject.updateOnSchedulerWhenChanged(scheduler) { list += it + 1 }.test()

        subject.onNext(3)
        scheduler.triggerActions()
        assertEquals(listOf(4), list)
        subject.onNext(0)
        subject.onNext(3)
        scheduler.triggerActions()
        assertEquals(listOf(4), list)
    }

    @Test
    fun `updateOnSchedulerWhenChanged on different observers will not be influenced by each other`() {
        val list = mutableListOf<Int>()
        val scheduler = TestScheduler()
        val subject = PublishSubject.create<Int>()
        val observable = subject.updateOnSchedulerWhenChanged(scheduler) { synchronized(list) { list += it + 1 } }
        observable.test()

        subject.onNext(3)
        scheduler.triggerActions()

        observable.test()
        assertEquals(listOf(4), list)

        subject.onNext(3)
        scheduler.triggerActions()
        assertEquals(listOf(4, 4), list)

        subject.onNext(5)
        scheduler.triggerActions()
        assertEquals(listOf(4, 4, 6, 6), list)
    }
}