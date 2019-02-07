package io.infinite.pigeon.other

import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.http.HttpResponse
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.util.IOUtils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@BlackBox
@CompileStatic
class AwsResponseHandler implements HttpResponseHandler<AmazonWebServiceResponse<String>> {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

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