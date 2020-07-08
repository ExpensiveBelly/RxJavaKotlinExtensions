import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import org.junit.Assert.assertTrue
import org.junit.Test

class RxOperatorsExtensionsKtTest {

    @Test
    fun `concatScanEager publishes first value transformed`() {
        concatScanEager(
            Single.just("value"),
            Observable.empty<Int>()
        ) { accumulated, newValue -> accumulated + newValue }
            .test()
            .assertValue("value")
    }

    @Test
    fun `concatScanEager scans through values`() {

        concatScanEager(
            Single.just("value"),
            Observable.just(1, 2, 3)
        ) { accumulated, newValue -> accumulated + newValue }
            .test()
            .assertValues("value", "value1", "value12", "value123")
    }

    @Test
    fun `concatScanEager subscribes to observable before single emits`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()

        concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        // then
        assertTrue(single.hasObservers())
        assertTrue(observable.hasObservers())
    }

    @Test
    fun `concatScanEager observable emits first, initial value is used from single and then emission is used`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        observable.onNext(1)
        single.onSuccess("value")

        observer.assertValues("value", "value1")
    }

    @Test
    fun `concatScanEager single errors, outputs error`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()
        val error = Exception()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        // when
        single.onError(error)

        // then
        observer.assertNoValues().assertError(error)
    }

    @Test
    fun `concatScanEager observable errors, outputs error after single emits`() {
        val single = SingleSubject.create<String>()
        val observable = PublishSubject.create<Int>()
        val error = Exception()

        val observer = concatScanEager(single, observable) { accumulated, newValue -> accumulated + newValue }.test()

        observable.onError(error)
        observer.assertNoErrors()
        single.onSuccess("value")

        observer.assertValue("value").assertError(error)
    }
}