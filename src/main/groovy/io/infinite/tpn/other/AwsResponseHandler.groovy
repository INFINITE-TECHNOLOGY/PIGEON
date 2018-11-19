package io.infinite.tpn.other

import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.http.HttpResponse
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.util.IOUtils
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxEngine
import io.infinite.blackbox.BlackBoxLevel

@Slf4j
class AwsResponseHandler implements HttpResponseHandler<AmazonWebServiceResponse<String>> {

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    AmazonWebServiceResponse<String> handle(HttpResponse response) throws IOException {
        AmazonWebServiceResponse<String> awsResponse = new AmazonWebServiceResponse<>()
        awsResponse.setResult((String) IOUtils.toString(response.getContent()))
        return awsResponse
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    boolean needsConnectionLeftOpen() {
        return false
    }


}