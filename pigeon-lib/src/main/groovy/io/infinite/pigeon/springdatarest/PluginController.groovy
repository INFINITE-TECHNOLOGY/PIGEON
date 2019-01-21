package io.infinite.pigeon.springdatarest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Memoized
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
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
class PluginController {

    @Value('${pigeonInputPluginsDir}')
    String pigeonInputPluginsDir

    @Autowired
    InputMessageRepository inputMessageRepository

    @PostMapping(value = "/pigeon/plugins/input/*", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    ResponseEntity<CustomResponse> post(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI()
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        return groovyScriptEngine.run(pluginName + ".groovy", binding) as ResponseEntity<CustomResponse>
    }

    @GetMapping(value = "/pigeon/plugins/input/*", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    ResponseEntity<CustomResponse> get(HttpServletRequest httpServletRequest) {
        String path = httpServletRequest.getRequestURI()
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        return groovyScriptEngine.run(pluginName + ".groovy", binding) as ResponseEntity<CustomResponse>
    }

    @Memoized
    GroovyScriptEngine getGroovyScriptEngine() {
        return new GroovyScriptEngine(pigeonInputPluginsDir, ClassLoader.getSystemClassLoader())
    }

}
