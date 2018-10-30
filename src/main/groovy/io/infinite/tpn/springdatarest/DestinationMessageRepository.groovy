package io.infinite.tpn.springdatarest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface DestinationMessageRepository extends JpaRepository<DestinationMessage, Long> {

    Set<DestinationMessage> findBySubscriberNameAndStatus(String subscriberName, String status)

    @Query("select coalesce(max(m.id), 0) from DestinationMessage m")
    Long getMaxId()

    @Query("")
    LinkedHashSet<DestinationMessage> masterQueryWithNoRetries(String subscriberName, Long minTpnId)

    @Query("")
    LinkedHashSet<DestinationMessage> masterQueryWithRetries(String subscriberName, Integer maxRetryCount, Integer resendIntervalSeconds)

}