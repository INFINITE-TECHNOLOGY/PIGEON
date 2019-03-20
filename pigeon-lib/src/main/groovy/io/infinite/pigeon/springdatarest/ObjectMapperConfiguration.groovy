package io.infinite.pigeon.springdatarest


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Taken from: https://blog.jdriven.com/2016/11/handling-yaml-format-rest-spring-boot/
 */
@Configuration
class ObjectMapperConfiguration {

    //This is our default JSON ObjectMapper. Add @Primary to inject is as default bean.
    @Bean
    @Primary
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
        //Enable or disable features
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        return objectMapper
    }

    @Bean
    ObjectMapper yamlObjectMapper() {
        ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory())
        //Enable or disable features
        return yamlObjectMapper
    }
}