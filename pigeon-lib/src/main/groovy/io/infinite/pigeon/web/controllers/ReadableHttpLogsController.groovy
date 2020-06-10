package io.infinite.pigeon.web.controllers


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.pigeon.entities.HttpLog
import io.infinite.pigeon.repositories.HttpLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

@Controller
@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
class ReadableHttpLogsController {

    @Value('${pigeonInputPluginsHttpDir}')
    String pigeonInputPluginsHttpDir

    @Autowired
    HttpLogRepository httpLogRepository

    @GetMapping(value = "/readableHttpLogs")
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

    @GetMapping(value = "/readableHttpLogs/search/findByInputMessageId")
    @ResponseBody
    Set<HttpLog> findByInputMessageId(HttpServletRequest httpServletRequest) {
        String inputMessageId = httpServletRequest.getParameter("inputMessageId")
        Set<HttpLog> httpLogs = httpLogRepository.findByInputMessageId(Long.valueOf(inputMessageId))
        httpLogs.each {
            it.outputMessage = null
            it.requestBody = it.requestBody?.replace("\r", "")
            it.responseBody = it.responseBody?.replace("\r", "")
        }
        return httpLogs
    }

}
