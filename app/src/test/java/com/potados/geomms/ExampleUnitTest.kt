package com.potados.geomms

import io.reactivex.Flowable
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun rxTest() {
        Flowable.just(System.currentTimeMillis())
            .doOnNext { println("item emitted: $it") }
            .doOnNext { println("item emitted: $it") }
            .subscribe({ println("yeah: $it") }, { println("error: ${it.message}") })
    }
}
