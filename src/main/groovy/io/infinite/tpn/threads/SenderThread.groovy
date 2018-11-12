package io.infinite.tpn.threads

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.tpn.conf.OutputQueue
import io.infinite.tpn.http.HttpMessage
import io.infinite.tpn.springdatarest.InputMessageRepository
import io.infinite.tpn.springdatarest.OutputMessage
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.LinkedBlockingQueue

class SenderThread extends Thread {

    @Autowired
    InputMessageRepository inputMessageRepository

    OutputQueue outputQueue

    LinkedBlockingQueue<OutputMessage> sendingQueue = new LinkedBlockingQueue<>()

    GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine()

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    SenderThread(OutputQueue outputQueue) {
        setName(outputQueue.getUrl())
        this.outputQueue = outputQueue
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    void run() {
        while (true) {
            while (!sendingQueue.isEmpty()) {
                sendMessage(sendingQueue.poll())
            }
            synchronized (this) {
                wait()
            }
        }
    }

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    void sendMessage(OutputMessage outputMessage) {
        Binding binding = new Binding()
        HttpMessage httpMessage = new HttpMessage()
        binding.setVariable("outputMessage", outputMessage)
        binding.setVariable("inputMessage", outputMessage.getInputMessage())
        binding.setVariable("httpMessage", httpMessage)
        groovyScriptEngine.run(outputQueue.getConversionModuleName(), binding)
    }

}
