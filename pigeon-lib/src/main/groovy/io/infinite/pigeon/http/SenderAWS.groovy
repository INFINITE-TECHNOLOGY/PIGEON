package io.infinite.pigeon.http

import com.amazonaws.*
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpMethodName
import com.amazonaws.util.StringInputStream
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.AwsErrorResponseHandler
import io.infinite.pigeon.other.AwsResponseHandler
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.supplies.ast.exceptions.ExceptionUtils

@BlackBox
@ToString(includeNames = true, includeFields = true, includeSuper = true)
@Slf4j
class SenderAWS extends SenderAbstract {


    @Override
    void sendHttpMessage(HttpRequest httpRequest, HttpResponse httpResponse) {
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
        awsRequest.httpMethod = HttpMethodName.valueOf(httpRequest.method)
        awsRequest.endpoint = URI.create(httpRequest.url)
        awsRequest.content = new StringInputStream(httpRequest.body)
        awsRequest.headers = httpRequest.headers
        AWS4Signer signer = new AWS4Signer()
        signer.regionName = httpRequest.httpProperties.get("awsRegion") as String
        signer.serviceName = awsRequest.serviceName
        awsRequest.resourcePath = httpRequest.httpProperties.get("awsResourceName") as String
        signer.sign(awsRequest, new BasicAWSCredentials(httpRequest.httpProperties.get("awsAccessKey") as String, httpRequest.httpProperties.get("awsSecretKey") as String))
        Response<AmazonWebServiceResponse<String>> awsResponse
        try {
            log.trace("Sending AWS Request")
            log.trace(awsRequest.toString())
            log.trace(httpRequest.toString())
            ClientConfiguration clientConfiguration = new ClientConfiguration()
            if (httpRequest.httpProperties?.get("proxyHost") != null && httpRequest.httpProperties?.get("proxyPort") != null) {
                clientConfiguration.proxyHost = httpRequest.httpProperties?.get("proxyHost")?.toString()
                clientConfiguration.proxyPort = httpRequest.httpProperties?.get("proxyPort") as Integer
            }
            awsResponse = new AmazonHttpClient(clientConfiguration)
                    .requestExecutionBuilder()
                    .executionContext(new ExecutionContext(true))
                    .request(awsRequest)
                    .errorResponseHandler(new AwsErrorResponseHandler())
                    .execute(new AwsResponseHandler())
            if (awsResponse.awsResponse?.result != null) {
                httpResponse.body = awsResponse.awsResponse.result
            }
            for (headerName in awsResponse.httpResponse.headers.keySet()) {
                httpResponse.headers.put(headerName, awsResponse.httpResponse.headers.get(headerName))
            }
            httpRequest.requestStatus = MessageStatuses.DELIVERED.value()
            httpResponse.status = awsResponse.httpResponse.statusCode
        } catch (AmazonServiceException amazonServiceException) {
            log.warn("AmazonServiceException: " + amazonServiceException.statusCode)
            httpRequest.requestStatus = MessageStatuses.FAILED_RESPONSE.value()
            httpRequest.exceptionString = new ExceptionUtils().stacktrace(amazonServiceException)
            httpResponse.status = amazonServiceException.statusCode
            for (httpHeader in amazonServiceException.httpHeaders.keySet()) {
                httpResponse.headers.put(httpHeader, amazonServiceException.httpHeaders.get(httpHeader))
            }
            httpResponse.body = amazonServiceException.errorMessage
        } catch (Exception e) {
            fail(httpRequest, e, MessageStatuses.EXCEPTION)
        } finally {
            log.trace("Received response data:")
            log.trace(httpResponse.toString())
        }
    }

}