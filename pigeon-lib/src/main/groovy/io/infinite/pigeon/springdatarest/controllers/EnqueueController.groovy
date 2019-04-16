package io.infinite.pigeon.springdatarest.controllers

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.pigeon.other.MessageStatuses
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.repositories.InputMessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletRequest

@Controller
@BlackBox
@Slf4j
class EnqueueController {

    @Value('${pigeonInputPluginsHttpDir}')
    String pigeonInputPluginsHttpDir

    @Autowired
    InputMessageRepository inputMessageRepository

    @PostMapping(value = "/pigeon/enqueue")
    @ResponseBody
    EnqueueResponse post(HttpServletRequest httpServletRequest) {
        String payload = httpServletRequest.getReader().getText()
        return any(httpServletRequest, payload)
    }

    @Value('${disableDuplicateChecks:false}')
    Boolean disableDuplicateChecks

    @GetMapping(value = "/pigeon/enqueue")
    @ResponseBody
    EnqueueResponse get(HttpServletRequest httpServletRequest) {
        String payload = httpServletRequest.getParameter("payload")
        return any(httpServletRequest, payload)
    }

    EnqueueResponse any(HttpServletRequest httpServletRequest, String payload) {
        String externalId = httpServletRequest.getParameter("externalId") ?: httpServletRequest.getParameter("txn_id")
        String sourceName = httpServletRequest.getParameter("sourceName") ?: httpServletRequest.getParameter("source")
        String inputQueueName = httpServletRequest.getParameter("inputQueueName") ?: httpServletRequest.getParameter("endpoint")
        InputMessage inputMessage = new InputMessage()
        inputMessage.externalId = externalId
        if (inputMessage.externalId == null && disableDuplicateChecks) {
            inputMessage.externalId = System.currentTimeMillis().toString()
        }
        inputMessage.sourceName = sourceName
        inputMessage.inputQueueName = inputQueueName
        inputMessage.payload = payload
        inputMessage.status = MessageStatuses.NEW
        inputMessage.queryParams = new JsonBuilder(httpServletRequest.getParameterMap()).toString()
        inputMessageRepository.saveAndFlush(inputMessage)
        EnqueueResponse enqueueResponse = new EnqueueResponse()
        enqueueResponse.result = "Enqueued successfully"
        String port = (httpServletRequest.serverPort == 443 ? "" : ":$httpServletRequest.serverPort")
        enqueueResponse.inputMessageUrl = "/pigeon/inputMessages/$inputMessage.id"
        enqueueResponse.readableHttpLogsUrl = "/pigeon/readableHttpLogs/search/findByInputMessageId?format=yaml&inputMessageId=${inputMessage.id}"
        return enqueueResponse
    }

}
