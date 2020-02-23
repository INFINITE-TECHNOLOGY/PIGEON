package io.infinite.pigeon.configurations.security

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.validation.model.AscendHttpRequest
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox
class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    String ascendUrl

    JwtTokenAuthenticationFilter(String ascendUrl) {
        this.ascendUrl = ascendUrl
    }

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null) {
                failure(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing 'Authorization' header.")
                return
            }
            if (!authorizationHeader.startsWith("Bearer ")) {
                failure(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing 'Bearer' prefix.")
                return
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
                failure(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "\"There was an issue communicating with Authorization Server.")
                return
            }
            AscendHttpRequest parsedAscendResponse = objectMapper.readValue(ascendResponse.body, AscendHttpRequest.class)
            if (parsedAscendResponse.status != 200) {
                failure(response, parsedAscendResponse.status,
                        parsedAscendResponse.statusDescription)
                return
            }
            success(parsedAscendResponse.authorization?.identity, parsedAscendResponse.authorization?.identity?.authentications)
        } catch (Exception e) {
            log.warn("Authentication exception", e)
            failure(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication exception")
            return
        }
        chain.doFilter(request, response)
    }

    void success(Object principal, Object credentials) {
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                new PreAuthenticatedAuthenticationToken(principal, credentials)
        preAuthenticatedAuthenticationToken.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
    }

    void failure(HttpServletResponse httpServletResponse, Integer httpCode, String message) {
        SecurityContextHolder.clearContext()
        httpServletResponse.sendError(httpCode, message)
    }

}