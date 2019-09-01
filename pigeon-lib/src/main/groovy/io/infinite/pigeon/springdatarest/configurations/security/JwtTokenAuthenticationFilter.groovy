package io.infinite.pigeon.springdatarest.configurations.security


import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.validation.AuthorizationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
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

    AuthorizationValidator authorizationValidator

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            Authorization authorization = authorizationValidator.validateAuthorizationHeader(authorizationHeader, request)
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(authorization.identity, authorization.identity?.authentications)
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