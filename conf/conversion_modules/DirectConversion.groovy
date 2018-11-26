import io.infinite.tpn.springdatarest.InputMessage
import io.infinite.tpn.springdatarest.OutputMessage
import io.infinite.tpn.http.HttpRequest
import io.infinite.tpn.conf.OutputQueue

InputMessage inputMessage = binding.getVariable("inputMessage") as InputMessage
OutputMessage outputMessage = binding.getVariable("outputMessage") as OutputMessage
OutputQueue outputQueue = binding.getVariable("outputQueue") as OutputQueue
HttpRequest httpRequest = binding.getVariable("httpRequest") as HttpRequest

httpRequest.method = "POST"
httpRequest.headers = [
        "content-type": "application/json"
]
httpRequest.body = inputMessage.payload