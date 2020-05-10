package io.infinite.pigeon.other

import io.infinite.supplies.ast.exceptions.RuntimeException

class PigeonException extends RuntimeException {

    PigeonException(String message) {
        super(message)
    }

}