package io.infinite.pigeon.springdatarest


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface OutputMessageRepository extends JpaRepository<OutputMessage, Long> {

    @Query("select coalesce(max(m.id), 0) from OutputMessage m")
    Long getMaxId()

    @Query("""select o from OutputMessage o where
        outputQueueName = :outputQueueName and
        status in :messageStatusList and
        id > :minId
        order by id asc""")
    LinkedHashSet<OutputMessage> masterQueryNormal(
            @Param("outputQueueName") String outputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("minId") Long minId)

    @Query("""select o from OutputMessage o where
        outputQueueName = :outputQueueName and
        status in :messageStatusList and
        attemptsCount <= :maxRetryCount and
        lastSendTime < :maxLastSendTime
        order by id asc""")
    LinkedHashSet<OutputMessage> masterQueryRetry(
            @Param("outputQueueName") String outputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("maxLastSendTime") Date maxLastSendTime)

    Set<OutputMessage> findByStatus(@Param("status") String status)

    @Query("""select o from OutputMessage o join o.inputMessage i where
        i.externalId = :externalId and i.sourceName = :sourceName""")
    LinkedHashSet<OutputMessage> searchByInputExternalIdAndSourceName(
            @Param("externalId") String externalId,
            @Param("sourceName") String sourceName)

}