package io.infinite.pigeon.other

import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.http.HttpResponse
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.util.IOUtils
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox

@Slf4j
@BlackBox
class AwsResponseHandler implements HttpResponseHandler<AmazonWebServiceResponse<String>> {

    @Override
    AmazonWebServiceResponse<String> handle(HttpResponse response) throws IOException {
        AmazonWebServiceResponse<String> awsResponse = new AmazonWebServiceResponse<>()
        awsResponse.setResult((String) IOUtils.toString(response.getContent()))
        return awsResponse
    }

    @Override
    boolean needsConnectionLeftOpen() {
        return false
    }


}