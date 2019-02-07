package io.infinite.pigeon.http

import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.ClientConfiguration
import com.amazonaws.DefaultRequest
import com.amazonaws.Request
import com.amazonaws.Response
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpMethodName
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.AwsErrorResponseHandler
import io.infinite.pigeon.other.AwsResponseHandler
import io.infinite.pigeon.other.MessageStatuses
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@CompileStatic
class SenderAWS extends SenderAbstract {

    private final transient Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    SenderAWS(HttpRequest httpRequest) {
        super(httpRequest)
    }

    @Override
    void sendHttpMessage() {
        if (httpRequest.httpProperties.get("awsAccessKey") == null || httpRequest.httpProperties.get("awsSecretKey") == null) {
            log.warn("Configuration: One of the AWS keys is null")
        }
        if (httpRequest.httpProperties.get("awsServiceName") == null) {
            log.warn("Configuration: AWS service name is null")
        }
        if (httpRequest.httpProperties.get("awsResourceName") == null) {
            log.warn("Configuration: AWS resource name is null")
        }
        Request<Void> awsRequest = new DefaultRequest<Void>(httpRequest.httpProperties.get("awsServiceName") as String)
        awsRequest.setHttpMethod(HttpMethodName.valueOf(httpRequest.getMethod()))
        awsRequest.setEndpoint(URI.create(httpRequest.getUrl()))
        awsRequest.setContent(new com.amazonaws.util.StringInputStream(httpRequest.getBody()))
        awsRequest.setHeaders(httpRequest.getHeaders())
        AWS4Signer signer = new AWS4Signer()
        signer.setRegionName(httpRequest.httpProperties.get("awsRegion") as String)
        signer.setServiceName(awsRequest.getServiceName())
        awsRequest.setResourcePath(httpRequest.httpProperties.get("awsResourceName") as String)
        signer.sign(awsRequest, new BasicAWSCredentials(httpRequest.httpProperties.get("awsAccessKey") as String, httpRequest.httpProperties.get("awsSecretKey") as String))
        Response<AmazonWebServiceResponse<String>> awsResponse
        try {
            log.info("Sending AWS Request")
            log.info(awsRequest.toString())
            log.info(httpRequest.toString())
            awsResponse = new AmazonHttpClient(new ClientConfiguration())
                    .requestExecutionBuilder()
                    .executionContext(new ExecutionContext(true))
                    .request(awsRequest)
                    .errorResponseHandler(new AwsErrorResponseHandler())
                    .execute(new AwsResponseHandler())
            if (awsResponse.getAwsResponse()?.getResult() != null) {
                httpResponse.setBody(awsResponse.getAwsResponse().getResult())
            }
            for (headerName in awsResponse.httpResponse.headers.keySet()) {
                httpResponse.getHeaders().put(headerName, awsResponse.httpResponse.headers.get(headerName))
            }
            httpRequest.setRequestStatus(MessageStatuses.DELIVERED.value())
            httpResponse.setStatus(awsResponse.httpResponse.statusCode)
        } catch (AmazonServiceException amazonServiceException) {
            log.warn("AmazonServiceException: " + amazonServiceException.statusCode)
            httpRequest.setRequestStatus(MessageStatuses.FAILED_RESPONSE.value())
            httpResponse.setStatus(amazonServiceException.statusCode)
            for (l_header_name in amazonServiceException.httpHeaders.keySet()) {
                httpResponse.getHeaders().put(l_header_name, amazonServiceException.httpHeaders.get(l_header_name))
            }
            httpResponse.setBody(amazonServiceException.getErrorMessage())
        } finally {
            log.info("Received response data:")
            log.info(httpResponse.toString())
        }
    }

}