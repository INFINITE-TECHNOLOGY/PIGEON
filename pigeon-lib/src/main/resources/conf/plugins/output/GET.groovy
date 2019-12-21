package conf.plugins.output

import io.infinite.pigeon.conf.OutputQueue
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.springdatarest.entities.InputMessage
import io.infinite.pigeon.springdatarest.entities.OutputMessage

InputMessage inputMessage = binding.getVariable("inputMessage") as InputMessage
OutputMessage outputMessage = binding.getVariable("outputMessage") as OutputMessage
OutputQueue outputQueue = binding.getVariable("outputQueue") as OutputQueue
HttpRequest httpRequest = binding.getVariable("httpRequest") as HttpRequest

httpRequest.method = "GET"
httpRequest.url = httpRequest.url + (inputMessage.payload ?: "")