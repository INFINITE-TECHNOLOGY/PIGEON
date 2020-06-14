package io.infinite.pigeon.springdatarest.entities

import groovy.transform.ToString
import io.infinite.pigeon.springdatarest.services.PigeonService

import javax.persistence.*

@Entity
@Table(name = "OutputMessages")
@ToString(includeNames = true, includeFields = true, excludes = "httpLogs")
class OutputMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    @ManyToOne(fetch = FetchType.EAGER)
    InputMessage inputMessage

    String outputQueueName

    @Lob
    String url

    Integer attemptsCount = 0

    String status

    String outputThreadName

    String lastSenderThreadName

    UUID instanceUUID = PigeonService.staticUUID

    Date insertTime = new Date()

    Date lastSendTime

    @Lob
    String exceptionString

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "Output2httplog")
    Set<HttpLog> httpLogs = new HashSet<>()

    OutputMessage() {
    }

    OutputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage
    }

}