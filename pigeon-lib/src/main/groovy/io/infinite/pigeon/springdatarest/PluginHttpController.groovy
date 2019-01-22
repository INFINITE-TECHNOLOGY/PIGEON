package io.infinite.pigeon.springdatarest

import groovy.transform.Memoized
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class PluginHttpController {

    @Value('${pigeonInputPluginsHttpDir}')
    String pigeonInputPluginsHttpDir

    @Autowired
    InputMessageRepository inputMessageRepository

    @PostMapping(value = "/pigeon/plugins/input/http/*")
    @ResponseBody
    String post(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String path = httpServletRequest.getRequestURI()
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("httpServletResponse", httpServletResponse)
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        return groovyScriptEngine.run(pluginName + ".groovy", binding)
    }

    @GetMapping(value = "/pigeon/plugins/input/http/*")
    @ResponseBody
    String get(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String path = httpServletRequest.getRequestURI()
        String pluginName = path.substring(path.lastIndexOf('/') + 1)
        Binding binding = new Binding()
        binding.setVariable("httpServletRequest", httpServletRequest)
        binding.setVariable("httpServletResponse", httpServletResponse)
        binding.setVariable("inputMessageRepository", inputMessageRepository)
        return groovyScriptEngine.run(pluginName + ".groovy", binding)
    }

    @Memoized
    GroovyScriptEngine getGroovyScriptEngine() {
        return new GroovyScriptEngine(pigeonInputPluginsHttpDir, ClassLoader.getSystemClassLoader())
    }

}
