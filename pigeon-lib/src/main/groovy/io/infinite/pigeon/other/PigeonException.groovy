package io.infinite.pigeon.other

import org.apache.commons.lang3.exception.ExceptionUtils
import org.codehaus.groovy.runtime.StackTraceUtils

class PigeonException extends Exception {

    PigeonException(String var1) {
        super(var1)
    }

    PigeonException(Throwable var1) {
        super(StackTraceUtils.sanitize(var1))
    }

    String serialize() {
        return ExceptionUtils.getStackTrace(StackTraceUtils.sanitize(this))
    }
}