package io.infinite.pigeon.springdatarest.configurations.security

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.other.AscendException
import io.infinite.ascend.validation.model.AscendHttpRequest
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.HttpResponse
import io.infinite.pigeon.http.SenderDefaultHttps
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox
class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            AscendHttpRequest ascendHttpRequest = new AscendHttpRequest(
                    authorizationHeader: authorizationHeader,
                    incomingUrl: request.requestURL
                            .append('?')
                            .append(request.getQueryString())
                            .toString(),
                    method: request.method,
                    body: null as String
            )
            HttpRequest ascendRequest = new HttpRequest(
                    url: "https://ascend-gfs.herokuapp.com/ascend/validation",
                    headers: ["content-type": "application/json"],
                    method: "POST",
                    body: objectMapper.writeValueAsString(ascendHttpRequest)
            )
            HttpResponse ascendResponse = new HttpResponse()
            new SenderDefaultHttps().sendHttpMessage(ascendRequest, ascendResponse)
            if (ascendResponse.status != 200) {
                throw new AscendException("Failed Ascend HTTP status")
            }
            AscendHttpRequest ascendHttpResponse = objectMapper.readValue(ascendResponse.body, AscendHttpRequest.class)
            if (ascendResponse.status != 200) {
                throw new AscendException("Unauthorized.")
            }
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(ascendHttpResponse.authorization.identity, ascendHttpResponse.authorization.identity?.authentications)
            preAuthenticatedAuthenticationToken.setAuthenticated(true)
            SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
        }
        catch (Exception e) {
            log.warn("Exception during validation", e)
            SecurityContextHolder.clearContext()
        } finally {
            chain.doFilter(request, response)
        }
    }


}