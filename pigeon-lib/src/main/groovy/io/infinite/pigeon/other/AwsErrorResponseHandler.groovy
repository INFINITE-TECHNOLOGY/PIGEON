package io.infinite.pigeon.other

import com.amazonaws.AmazonServiceException
import com.amazonaws.http.HttpResponse
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.services.s3.Headers
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.util.IOUtils
import io.infinite.blackbox.BlackBox

@BlackBox
class AwsErrorResponseHandler implements HttpResponseHandler<AmazonServiceException> {

    @Override
    AmazonServiceException handle(HttpResponse errorResponse) throws IOException {
        String requestId = errorResponse.getHeaders().get(Headers.REQUEST_ID)
        String extendedRequestId = errorResponse.getHeaders().get(Headers.EXTENDED_REQUEST_ID)
        AmazonS3Exception ase = new AmazonS3Exception(errorResponse.statusText)
        ase.statusCode = errorResponse.statusCode
        ase.requestId = requestId
        ase.extendedRequestId = extendedRequestId
        fillInErrorType(ase, errorResponse)
        ase.errorMessage = ((String) IOUtils.toString(errorResponse.content))
        ase.httpHeaders = errorResponse.headers
        return ase
    }

    @Override
    boolean needsConnectionLeftOpen() {
        return false
    }

    /**
     * Fills in the AWS error type information in the specified
     * AmazonServiceException by looking at the HTTP status code in the error
     * response. S3 error responses don't explicitly declare a sender or client
     * fault like other AWS services, so we have to use the HTTP status code to
     * infer this information.
     *
     * @param ase
     *            The AmazonServiceException to populate with error type
     *            information.
     * @param errorResponse
     *            The HTTP error response to use to determine the right error
     *            type to set.
     */
    private void fillInErrorType(AmazonServiceException ase, HttpResponse errorResponse) {
        if (errorResponse.statusCode >= 500) {
            ase.errorType = AmazonServiceException.ErrorType.Service
        } else {
            ase.errorType = AmazonServiceException.ErrorType.Client
        }
    }


}