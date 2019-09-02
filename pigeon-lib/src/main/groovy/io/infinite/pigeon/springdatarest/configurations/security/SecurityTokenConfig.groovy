package io.infinite.pigeon.springdatarest.configurations.security

import groovy.util.logging.Slf4j
import io.infinite.ascend.validation.AccessJwtManager
import io.infinite.ascend.validation.AuthorizationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.HttpResponse
import io.infinite.pigeon.http.SenderAbstract
import io.infinite.pigeon.other.PigeonException
import io.infinite.pigeon.springdatarest.repositories.AscendUsageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.annotation.PostConstruct
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletResponse
/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/SecurityTokenConfig.java
 */
@EnableWebSecurity
@Slf4j
class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

    AuthorizationValidator authorizationValidator

    @Autowired
    AscendUsageRepository ascendUsageRepository

    @Value('${ascendPublicKeyUrl}')
    String ascendPublicKeyUrl

    @Value('${ascendSenderClassName}')
    String ascendSenderClassName

    @PostConstruct
    void init() {
        log.info("Sit back and relax, while Pigeon is getting its trusted Ascend Access Public Key.")
        SenderAbstract senderAbstract = Class.forName(ascendSenderClassName).newInstance() as SenderAbstract
        HttpRequest httpRequest = new HttpRequest(
                url: ascendPublicKeyUrl,
                method: "GET"
        )
        HttpResponse httpResponse = new HttpResponse()
        senderAbstract.sendHttpMessage(httpRequest, httpResponse)
        if (httpResponse.status != 200) {
            throw new PigeonException("Unable to get trusted Ascend Public Key")
        }
        log.info("Pigeon trusted Ascend Key is: " + httpResponse.body)
        String trustedAscendAccessPublicKeyBytesString = httpResponse.body
        byte[] trustedAscendAccessPublicKeyBytes = Base64.getDecoder().decode(trustedAscendAccessPublicKeyBytesString)
        SecretKey trustedAscendAccessPublicKey = new SecretKeySpec(trustedAscendAccessPublicKeyBytes, 0, trustedAscendAccessPublicKeyBytes.length, "AES")
        authorizationValidator = new AuthorizationValidator(
                jwtManager: new AccessJwtManager(
                        trustedAscendAccessPublicKey
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