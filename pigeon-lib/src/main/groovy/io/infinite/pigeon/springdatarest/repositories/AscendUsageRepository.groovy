package io.infinite.pigeon.springdatarest.repositories

import io.infinite.ascend.validation.repositories.UsageRepository
import io.infinite.pigeon.springdatarest.entities.AscendUsage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AscendUsageRepository extends JpaRepository<AscendUsage, Long>, UsageRepository {

    Set<AscendUsage> findByAuthorizationId(@Param("authorizationId") UUID authorizationId)

}
