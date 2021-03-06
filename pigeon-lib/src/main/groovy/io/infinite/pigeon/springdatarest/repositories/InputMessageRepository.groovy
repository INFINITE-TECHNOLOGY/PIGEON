package io.infinite.pigeon.springdatarest.repositories

import io.infinite.pigeon.springdatarest.entities.InputMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface InputMessageRepository extends JpaRepository<InputMessage, Long> {

    @Query("""select i from InputMessage i
        where inputQueueName = :inputQueueName
        and status in :messageStatusList""")
    Set<InputMessage> findByInputQueueNameAndStatus(
            @Param("inputQueueName") String inputQueueName,
            @Param("messageStatusList") List<String> messageStatusList
    )

    Set<InputMessage> findByExternalIdAndSourceName(
            String externalId,
            String sourceName
    )

    @Query("""select count(i.id) from InputMessage i
            where sourceName = :sourceName 
            and inputQueueName = :inputQueueName
            and externalId = :externalId
            and id <> :id
            and status = :status""")
    Integer findDuplicates(
            @Param("sourceName") String sourceName,
            @Param("inputQueueName") String inputQueueName,
            @Param("externalId") String externalId,
            @Param("id") Long id,
            @Param("status") String status
    )

    @Query("""select i from InputMessage i
            where status in :messageStatusList""")
    Set<InputMessage> findByMessageStatusList(
            @Param("messageStatusList") List<String> messageStatusList
    )

    @Query("""select i from InputMessage i
            where status in :messageStatusList
            and i.inputQueueName = :inputQueueName""")
    Set<InputMessage> findByStatusListAndInputQueueName(
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("inputQueueName") String inputQueueName
    )

    @Transactional
    @Modifying(clearAutomatically=true, flushAutomatically = true)
    @Query("""update InputMessage i
        set instanceUUID = :instanceUUID
        where i.inputQueueName = :inputQueueName
        and i.status in :messageStatusList
        and i.instanceUUID is null""")
    Integer markForSplit(
            @Param("inputQueueName") String inputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("instanceUUID") UUID instanceUUID
    )

    @Query("""select i from InputMessage i
        where i.inputQueueName = :inputQueueName
        and i.status in :messageStatusList
        and i.instanceUUID = :instanceUUID
        order by i.id asc""")
    LinkedHashSet<InputMessage> selectForSplit(
            @Param("inputQueueName") String inputQueueName,
            @Param("messageStatusList") List<String> messageStatusList,
            @Param("instanceUUID") UUID instanceUUID
    )

}
