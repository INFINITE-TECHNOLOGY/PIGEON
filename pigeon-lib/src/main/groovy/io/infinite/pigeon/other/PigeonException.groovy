package io.infinite.pigeon.other

import groovy.transform.CompileStatic
import io.infinite.supplies.ast.exceptions.RuntimeException

@CompileStatic
class PigeonException extends RuntimeException {

    PigeonException(String var1) {
        super(var1)
    }

}