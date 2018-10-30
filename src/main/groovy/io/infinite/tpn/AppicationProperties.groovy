package io.infinite.tpn

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@PropertySource(value = "application.properties")
class AppicationProperties {

    @Value('${blackbox.mode}')
    public String blackboxMode

    @Value('${tpn.splitter.poll.period.milliseconds}')
    public Long splitterPollPeriodMilliseconds

    @Value('${tpn.master.poll.period.milliseconds}')
    public Long masterPollPeriodMilliseconds

}