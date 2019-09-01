package io.infinite.pigeon.springdatarest.entities

import groovy.transform.ToString
import io.infinite.ascend.validation.entities.Usage

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class AscendUsage extends Usage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long authorizationUsageId

    @Column(nullable = false)
    UUID authorizationId

    Date usageDate

}
