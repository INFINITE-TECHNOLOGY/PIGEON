package io.infinite.pigeon.springdatarest

import groovy.transform.CompileStatic
import groovy.transform.ToString

import javax.persistence.*

@Entity
@Table(name = "OutputMessages")
@ToString(includeNames = true, includeFields = true, excludes = "httpLogs")
@CompileStatic
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

    String threadName

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