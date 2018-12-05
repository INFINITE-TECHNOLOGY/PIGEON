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
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.NONE)
@RestController
class TestSOAP11 {

    @PostMapping("/testSOAP11")
    TestSOAP11 post(HttpEntity<TestSOAP11> httpEntity) {
        TestSOAP11 testMessage = httpEntity.getBody()
        return testMessage
    }

}
