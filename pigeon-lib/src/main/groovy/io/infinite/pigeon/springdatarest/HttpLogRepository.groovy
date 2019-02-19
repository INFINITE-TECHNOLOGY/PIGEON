package io.infinite.pigeon.springdatarest

import groovy.transform.CompileStatic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
@CompileStatic
interface HttpLogRepository extends JpaRepository<HttpLog, Long> {


}
