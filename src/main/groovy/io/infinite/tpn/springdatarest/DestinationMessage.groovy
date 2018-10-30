package io.infinite.tpn.springdatarest


import javax.persistence.*

@Entity
@Table(name = "DestinationMessages")
class DestinationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    @ManyToOne(fetch = FetchType.EAGER)
    SourceMessage sourceMessage

    String subscriberName

    String url

    Integer retryCount

    String status

    String threadName

    DestinationMessage() {
    }

    DestinationMessage(SourceMessage sourceMessage) {
        this.sourceMessage = sourceMessage
    }
}