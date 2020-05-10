package io.infinite.pigeon.mvc.repositories

import io.infinite.pigeon.mvc.entities.HttpLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface HttpLogRepository extends JpaRepository<HttpLog, Long> {

    @Query("""select h from HttpLog h
        join h.outputMessage o
        join o.inputMessage i
        where i.externalId = :externalId
         and i.sourceName = :sourceName""")
    Set<HttpLog> findByExternalIdAndSourceName(
            @Param("externalId") String externalId,
            @Param("sourceName") String sourceName
    )

    @Query("""select h from HttpLog h
        join h.outputMessage o
        join o.inputMessage i
        where i.id = :inputMessageId""")
    Set<HttpLog> findByInputMessageId(
            @Param("inputMessageId") Long inputMessageId
    )

}
