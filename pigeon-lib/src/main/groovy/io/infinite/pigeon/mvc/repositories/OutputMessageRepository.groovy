package io.infinite.pigeon.mvc.repositories

import io.infinite.pigeon.mvc.entities.OutputMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface OutputMessageRepository extends JpaRepository<OutputMessage, Long> {

    @Query("select coalesce(max(m.id), 0) from OutputMessage m")
    Long getMaxId()

    @Query("""select o from OutputMessage o
        where outputQueueName = :outputQueueName
        and status in :messageStatusList
        and attemptsCount < :maxRetryCount
        and lastSendTime < :maxLastSendTime
        order by id asc""")
    LinkedHashSet<OutputMessage> masterQueryRetry(
            @Param("outputQueueName") String outputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("maxLastSendTime") Date maxLastSendTime
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

}