package io.infinite.pigeon.web.controllers


import groovy.transform.Memoized
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.repositories.InputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@BlackBox(level = BlackBoxLevel.METHOD)
class PluginHttpController {

    @Value('${pigeonInputPluginsHttpDir}')
    String pigeonInputPluginsHttpDir

    @Autowired
    InputMessageRepository inputMessageRepository

    @PostMapping(value = "/plugins/input/http/*")
    @ResponseBody
    String post(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @RequestBody String requestBody) {
        String path = httpServletRequest.requestURI
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("httpServletResponse", httpServletResponse)
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        binding.setVariable("requestBody", requestBody)
        return groovyScriptEngine.run(pluginName + ".groovy", binding)
    }

    @GetMapping(value = "/plugins/input/http/*")
    @ResponseBody
    String get(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String path = httpServletRequest.requestURI
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("httpServletResponse", httpServletResponse)
        binding.setVariable("requestBody", "")
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        return groovyScriptEngine.run(pluginName + ".groovy", binding)
    }

    @Memoized
    GroovyScriptEngine getGroovyScriptEngine() {
        return new GroovyScriptEngine(pigeonInputPluginsHttpDir, this.class.classLoader)
    }

}
