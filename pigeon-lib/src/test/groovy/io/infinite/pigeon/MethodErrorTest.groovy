package io.infinite.pigeon

import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.testng.annotations.Test

class MethodErrorTest {

    @BlackBox(level = CarburetorLevel.ERROR)
    def test(String arg1) {
        println "Before exception"
        throw new Exception("Method threw an exception here")
    }

    @BlackBox(level = CarburetorLevel.ERROR, suppressExceptions = true)
    def test2(String arg1) {
        test(arg1)
    }

    @Test
    void test() {
        new MethodErrorTest().test2("abcd")
    }

}