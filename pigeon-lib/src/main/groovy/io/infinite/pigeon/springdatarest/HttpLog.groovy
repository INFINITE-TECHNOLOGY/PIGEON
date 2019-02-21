package io.infinite.pigeon.springdatarest

import groovy.transform.ToString

import javax.persistence.*

@Entity
@Table
@ToString(includeNames = true, includeFields = true)
class HttpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    Date requestDate
    @Lob
    String requestHeaders
    @Lob
    String requestBody
    String method
    @Lob
    String url
    String requestStatus
    @Lob
    String requestExceptionString
    Date responseDate
    @Lob
    String responseHeaders
    @Lob
    String responseBody
    Integer responseStatus
    Date insertDate = new Date()
    @ManyToOne(fetch = FetchType.EAGER)
    OutputMessage outputMessage
    String senderThreadName

}
