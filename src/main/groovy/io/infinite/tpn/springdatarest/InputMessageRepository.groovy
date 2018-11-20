package io.infinite.tpn.springdatarest

import io.infinite.tpn.other.MessageStatuses
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface InputMessageRepository extends JpaRepository<InputMessage, Long> {

    Set<InputMessage> findByInputQueueNameAndStatus(String inputQueueName, String status)

    @Query("select count(i.id) from InputMessage i where sourceName = :sourceName and inputQueueName = :inputQueueName and externalId = :externalId and id <> :id and status = :status")
    Integer findDuplicates(
            @Param("sourceName") String sourceName,
            @Param("inputQueueName") String inputQueueName,
            @Param("externalId") String externalId,
            @Param("id") Long id,
            @Param("status") String status
    )

}
