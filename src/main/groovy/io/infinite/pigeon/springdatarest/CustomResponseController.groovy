package io.infinite.pigeon.springdatarest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.infinite.blackbox.BlackBox
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType

@JsonIgnoreProperties(ignoreUnknown = true)
@Component
@XmlAccessorType(XmlAccessType.NONE)
@RestController
@BlackBox
class CustomResponseController {

    static GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine("${System.getProperty("confDir", ".")}/plugins/", ClassLoader.getSystemClassLoader())

    @PostMapping(value = "/customResponse/*", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    ResponseEntity<CustomResponse> post(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI()
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        CustomResponse customResponse = new CustomResponse()
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("customResponse", customResponse)
        groovyScriptEngine.run(pluginName + ".groovy", binding)
        return new ResponseEntity(customResponse, HttpStatus.valueOf(customResponse.getStatus())) //todo - move to script
    }

}
