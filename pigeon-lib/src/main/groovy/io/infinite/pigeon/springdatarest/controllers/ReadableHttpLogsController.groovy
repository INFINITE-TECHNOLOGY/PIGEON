package io.infinite.pigeon.springdatarest.controllers


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.springdatarest.entities.HttpLog
import io.infinite.pigeon.springdatarest.repositories.HttpLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

@Controller
@BlackBox
@Slf4j
class ReadableHttpLogsController {

    @Value('${pigeonInputPluginsHttpDir}')
    String pigeonInputPluginsHttpDir

    @Autowired
    HttpLogRepository httpLogRepository

    @GetMapping(value = "/pigeon/readableHttpLogs")
    @ResponseBody
    Set<HttpLog> get(HttpServletRequest httpServletRequest) {
        String externalId = httpServletRequest.getParameter("externalId")
        String sourceName = httpServletRequest.getParameter("sourceName")
        Set<HttpLog> httpLogs = httpLogRepository.findByExternalIdAndSourceName(externalId, sourceName)
        httpLogs.each {
            it.outputMessage = null
            it.requestBody = it.requestBody?.replace("\r", "")
            it.responseBody = it.responseBody?.replace("\r", "")
        }
        return httpLogs
    }

}
