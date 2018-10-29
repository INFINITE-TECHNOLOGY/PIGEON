package io.infinite.tpn.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface SourceMessageRepository extends JpaRepository<SourceMessage, Long> {

    Set<SourceMessage> findByQueueNameAndStatus(String queueName, String status)

}
