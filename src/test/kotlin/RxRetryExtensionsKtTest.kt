import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.zipWith
import org.junit.Test
import java.io.IOException

class RxRetryExtensionsKtTest {

    @Test
    fun retryReset_withCount_resetsUponSuccess() {
        var count = 0
        Single.defer {
            if (count++ % 3 == 0) Single.just(0)
            else Single.error(IOException())
        }
            .repeat()
            .toObservable()
            .retryReset { errors ->
                errors.zipWith(
                    Observable.range(1, 2).concatWith(Single.just(0))
                )
                    .flatMapSingle { (throwable, errorCount) ->
                        if (errorCount == 0) Single.error(throwable)
                        else Single.just(0)
                    }
            }
            .take(3)
            .test()
            .assertValues(0, 0, 0)
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun count_error_transformations_retry_specified_amount_of_times() {
        Single.just(1)
            .retryWith(countErrorTransformation(3))
    }
}