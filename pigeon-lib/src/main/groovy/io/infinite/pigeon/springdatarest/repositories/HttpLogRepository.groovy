package io.infinite.pigeon.springdatarest.repositories

import io.infinite.pigeon.springdatarest.entities.HttpLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface HttpLogRepository extends JpaRepository<HttpLog, Long> {

    @Query("""select l from HttpLog l
        join l.outputMessage o
        join o.inputMessage i
        where i.externalId = :externalId and i.sourceName = :sourceName
    """)
    Set<HttpLog> findByExternalIdAndSourceName(
            @Param("externalId") String externalId,
            @Param("sourceName") String sourceName
    )

}
