package io.infinite.pigeon.springdatarest

import groovy.transform.ToString
import io.infinite.pigeon.http.HttpResponse

@ToString(includeNames = true, includeFields = true, includeSuper = true)
class CustomResponse extends HttpResponse {

    String exceptionString

}
