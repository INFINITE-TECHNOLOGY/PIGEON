package io.infinite.pigeon.springdatarest

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
class TestSOAP12 {

    @PostMapping("/testSOAP12")
    TestSOAP12 post(HttpEntity<TestSOAP12> httpEntity) {
        TestSOAP12 testMessage = httpEntity.getBody()
        return testMessage
    }

}
