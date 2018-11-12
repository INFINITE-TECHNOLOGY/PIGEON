package io.infinite.tpn.springdatarest


import javax.persistence.*

@Entity
@Table(name = "OutputMessages")
class OutputMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    @ManyToOne(fetch = FetchType.EAGER)
    InputMessage inputMessage

    String outputQueueName

    String url

    Integer retryCount = 0

    String status

    String threadName

    Date lastSendTime

    OutputMessage() {
    }

    OutputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage
    }
}