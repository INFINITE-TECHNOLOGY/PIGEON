package io.infinite.tpn.other

import org.apache.commons.lang3.exception.ExceptionUtils
import org.codehaus.groovy.runtime.StackTraceUtils

class TpnException extends Exception {

    TpnException(String var1) {
        super(var1)
    }

    TpnException(Throwable var1) {
        super(StackTraceUtils.sanitize(var1))
    }

    String serialize() {
        return ExceptionUtils.getStackTrace(StackTraceUtils.sanitize(this))
    }
}