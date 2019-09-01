package io.infinite.pigeon.springdatarest.configurations.security

import io.infinite.ascend.validation.AccessJwtManager
import io.infinite.ascend.validation.AuthorizationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.repositories.AscendUsageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/SecurityTokenConfig.java
 */
@EnableWebSecurity
class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

    AuthorizationValidator authorizationValidator

    @Autowired
    AscendUsageRepository ascendUsageRepository

    @Value('${jwtAccessKeystorePath}')
    String jwtAccessKeystorePath

    @Value('${jwtAccessKeystoreType}')
    String jwtAccessKeystoreType

    @Value('${jwtAccessKeystorePassword}')
    String jwtAccessKeystorePassword

    @Value('${jwtAccessKeystoreAlias}')
    String jwtAccessKeystoreAlias

    @PostConstruct
    void init() {
        authorizationValidator = new AuthorizationValidator(
                jwtManager: new AccessJwtManager(
                        jwtAccessKeystorePath,
                        jwtAccessKeystoreType,
                        jwtAccessKeystorePassword,
                        jwtAccessKeystoreAlias
                ),
                usageRepository: ascendUsageRepository)
    }

    @Override
    @BlackBox
    protected void configure(HttpSecurity http) throws Exception {
        JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(
                authorizationValidator: authorizationValidator)
        http
                .csrf().disable()
        // make sure we use stateless session session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
        // handle an authorized attempts
                .exceptionHandling().authenticationEntryPoint({
            req, rsp, e ->
                rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        })
                .and()
        // Add a filter to validate the tokens with every request
                .addFilterAfter(jwtTokenAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)
        // authorization requests config
                .authorizeRequests()
                .anyRequest().authenticated()
    }

}