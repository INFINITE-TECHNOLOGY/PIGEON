package io.infinite.pigeon.springdatarest.repositories

import io.infinite.pigeon.springdatarest.entities.OutputMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface OutputMessageRepository extends JpaRepository<OutputMessage, Long> {

    @Query("select coalesce(max(m.id), 0) from OutputMessage m")
    Long getMaxId()

    @Transactional
    @Modifying(clearAutomatically=true, flushAutomatically = true)
    @Query("""update OutputMessage o
        set instanceUUID = :instanceUUID
        where outputQueueName = :outputQueueName
        and status in :messageStatusList
        and attemptsCount < :maxRetryCount
        and lastSendTime < :maxLastSendTime""")
    Integer markForRetry(
            @Param("outputQueueName") String outputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("maxLastSendTime") Date maxLastSendTime,
            @Param("instanceUUID") UUID instanceUUID
    )

    @Query("""Select o from OutputMessage o
        where outputQueueName = :outputQueueName
        and status in :messageStatusList
        and attemptsCount < :maxRetryCount
        and lastSendTime < :maxLastSendTime
        and instanceUUID = :instanceUUID
        order by id asc""")
    LinkedHashSet<OutputMessage> selectForRetry(
            @Param("outputQueueName") String outputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("maxLastSendTime") Date maxLastSendTime,
            @Param("instanceUUID") UUID instanceUUID
    )

    @Query("""select o from OutputMessage o
        where status in :messageStatusList""")
    Set<OutputMessage> findByMessageStatusList(
            @Param("messageStatusList") List<String> messageStatusList
    )

    @Query("""select o from OutputMessage o
        join o.inputMessage i
        where i.externalId = :externalId
        and i.sourceName = :sourceName""")
    LinkedHashSet<OutputMessage> findByInputExternalIdAndSourceName(
            @Param("externalId") String externalId,
            @Param("sourceName") String sourceName
    )

    @Query("""select o from OutputMessage o
        where status in :messageStatusList
        and outputQueueName = :outputQueueName""")
    Set<OutputMessage> findByStatusListAndOutputQueueName(
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("outputQueueName") String outputQueueName
    )

}