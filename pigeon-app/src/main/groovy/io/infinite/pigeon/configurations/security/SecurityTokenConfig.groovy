package io.infinite.pigeon.configurations.security

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletResponse
/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/SecurityTokenConfig.java
 */
@EnableWebSecurity
@Slf4j
class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

    @Value('${useAscend}')
    Boolean useAscend

    @Value('${ascendUrl}')
    String ascendUrl

    @Override
    @BlackBox
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        if (useAscend) {
            secured(httpSecurity)
        } else {
            unsecured(httpSecurity)
        }
    }

    void secured(HttpSecurity http) {
        log.info("Pigeon secured by Ascend.")
        JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(ascendUrl)
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint({
            req, rsp, e ->
                log.error("Exception in SecurityTokenConfig", e)
                rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        })
                .and()
                .addFilterBefore(jwtTokenAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeRequests()
                .antMatchers("/error/**").permitAll()
                .anyRequest().authenticated()
    }

    void unsecured(HttpSecurity http) {
        log.info("Pigeon is unsecured.")
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint({
            req, rsp, e ->
                log.error("Exception in SecurityTokenConfig", e)
                rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        })
                .and()
                .authorizeRequests()
                .anyRequest().permitAll()
    }

}