package io.infinite.pigeon.springdatarest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface InputMessageRepository extends JpaRepository<InputMessage, Long> {

    @Query("""select i from InputMessage i where
        inputQueueName = :inputQueueName and
        status in :messageStatusList""")
    Set<InputMessage> findByInputQueueNameAndStatus(
            @Param("inputQueueName") String inputQueueName,
            @Param("messageStatusList") List<String> messageStatusList
    )

    Set<InputMessage> findByExternalIdAndSourceName(
            @Param("externalId") String externalId,
            @Param("sourceName") String sourceName
    )

    @Query("select count(i.id) from InputMessage i where sourceName = :sourceName and inputQueueName = :inputQueueName and externalId = :externalId and id <> :id and status = :status")
    Integer findDuplicates(
            @Param("sourceName") String sourceName,
            @Param("inputQueueName") String inputQueueName,
            @Param("externalId") String externalId,
            @Param("id") Long id,
            @Param("status") String status
    )

    @Query("""select i from InputMessage i where status in :messageStatusList""")
    Set<InputMessage> findByMessageStatusList(@Param("messageStatusList") List<String> messageStatusList)

}
