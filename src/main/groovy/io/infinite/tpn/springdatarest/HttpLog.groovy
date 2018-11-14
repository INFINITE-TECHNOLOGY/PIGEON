package io.infinite.tpn.springdatarest

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table
class HttpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    Date requestDate
    String requestHeaders
    String requestBody
    String method
    String url
    String requestStatus
    @Lob
    String requestExceptionString
    Date responseDate
    String responseHeaders
    String responseBody
    Integer responseStatus

}
