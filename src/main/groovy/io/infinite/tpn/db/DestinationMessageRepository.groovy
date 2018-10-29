package io.infinite.tpn.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface DestinationMessageRepository extends JpaRepository<DestinationMessage, Long> {


}
