package irys.client.consumer;

import uk.org.siri.siri.CheckStatusResponseBodyStructure;
import uk.org.siri.siri.ProducerRequestEndpointStructure;

public interface HeartbeatConsumer
{
void consume(ProducerRequestEndpointStructure heartbeatNotifyInfo, CheckStatusResponseBodyStructure notification);
}
