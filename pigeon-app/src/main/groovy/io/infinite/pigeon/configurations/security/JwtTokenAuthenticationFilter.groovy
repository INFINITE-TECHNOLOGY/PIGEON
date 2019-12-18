package io.infinite.pigeon.configurations.security

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.validation.model.AscendHttpRequest
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.HttpResponse
import io.infinite.pigeon.http.SenderDefaultHttps
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox
class JwtTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    String ascendUrl

    JwtTokenAuthenticationFilter(String ascendUrl, AuthenticationManager authenticationManager) {
        super("/pigeon/**")
        this.ascendUrl = ascendUrl
        this.authenticationManager = authenticationManager
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null){
                return failure(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing 'Authorization' header.")
            }
            if (!authorizationHeader.startsWith("Bearer ")) {
                return failure(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing 'Bearer' prefix.")
            }
            String incomingUrl
            if (request.getQueryString() != null) {
                incomingUrl = request.requestURL
                        .append('?')
                        .append(request.getQueryString())
                        .toString()
            } else {
                incomingUrl = request.requestURL
            }
            AscendHttpRequest ascendHttpRequest = new AscendHttpRequest(
                    authorizationHeader: authorizationHeader,
                    incomingUrl: incomingUrl,
                    method: request.method,
                    body: null as String
            )
            HttpRequest ascendRequest = new HttpRequest(
                    url: ascendUrl,
                    headers: ["content-type": "application/json"],
                    method: "POST",
                    body: objectMapper.writeValueAsString(ascendHttpRequest)
            )
            HttpResponse ascendResponse = new HttpResponse()
            new SenderDefaultHttps().sendHttpMessage(ascendRequest, ascendResponse)
            if (ascendResponse.status != 200) {
                return failure(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "\"There was an issue communicating with Authorization Server.")
            }
            AscendHttpRequest parsedAscendResponse = objectMapper.readValue(ascendResponse.body, AscendHttpRequest.class)
            if (parsedAscendResponse.status != 200) {
                return success(parsedAscendResponse, parsedAscendResponse)
                /*return failure(response, parsedAscendResponse.status,
                        parsedAscendResponse.statusDescription)*/
            }
            return success(parsedAscendResponse.authorization.identity, parsedAscendResponse.authorization.identity?.authentications)
        }
        catch (Exception e) {
            log.warn("Exception", e)
            return failure(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected exception")
        }
    }

    Authentication success(Object principal, Object credentials) {
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                new PreAuthenticatedAuthenticationToken(principal, credentials)
        preAuthenticatedAuthenticationToken.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
        getAuthenticationManager().authenticate(preAuthenticatedAuthenticationToken)
        return preAuthenticatedAuthenticationToken
    }

    Authentication failure(HttpServletResponse httpServletResponse, Integer httpCode, String message) {
        SecurityContextHolder.clearContext()
        httpServletResponse.sendError(httpCode, message)
        return null
    }

}