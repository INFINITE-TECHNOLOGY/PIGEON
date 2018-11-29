package io.infinite.tpn.springdatarest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
@Component
@XmlRootElement(name = "Envelope", namespace = "http://www.w3.org/2003/05/soap-envelope")
@XmlAccessorType(XmlAccessType.NONE)
@RestController
class TestMessage {

    @PostMapping("/testMessages")
    TestMessage post(HttpEntity<TestMessage> httpEntity) {
        TestMessage testMessage = httpEntity.getBody()
        return testMessage
    }

}
