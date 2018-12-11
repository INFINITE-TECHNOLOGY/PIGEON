package io.infinite.pigeon.springdatarest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType

@JsonIgnoreProperties(ignoreUnknown = true)
@Component
@XmlAccessorType(XmlAccessType.NONE)
@RestController
class TestJSON {

    @PostMapping("/testJSON")
    TestJSON post(HttpEntity<TestJSON> httpEntity) {
        TestJSON testMessage = httpEntity.getBody()
        return testMessage
    }

}
