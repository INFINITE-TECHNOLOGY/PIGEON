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

    Integer attemptsCount = 0

    String status

    String threadName

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