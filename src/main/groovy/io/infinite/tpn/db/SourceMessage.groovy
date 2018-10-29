package io.infinite.tpn.db

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "messages")
class SourceMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "tpn_internal_unique_id")
    Long id

    @Column(name = "TXN_ID")
    String sourceId

    @Column(name = "SOURCE")
    String sourceName

    @Column(name = "PAYLOAD")
    String payload

    @Column(name = "ENDPOINT")
    String queueName

    @Column(name = "STATUS")
    String status

}
