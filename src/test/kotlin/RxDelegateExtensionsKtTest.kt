import org.junit.Test

class RxDelegateExtensionsKtTest {

	private val numberSubject = BehaviorSubjectWithDefault.create(0)
	private var number by reactiveProperty(numberSubject)

	@Test
	fun `updating number also makes the subject emit`() {
		val testObserver = numberSubject.test()
		number = 1
		testObserver.assertValues(0, 1)
	}
}