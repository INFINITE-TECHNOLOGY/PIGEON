package io.infinite.tpn.springdatarest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface HttpLogRepository extends JpaRepository<HttpLog, Long> {


}
