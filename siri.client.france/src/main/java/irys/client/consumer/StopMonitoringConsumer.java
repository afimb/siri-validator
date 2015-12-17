package irys.client.consumer;

import uk.org.siri.siri.StopMonitoringDeliveryStructure;

public interface StopMonitoringConsumer
{
   void consume(StopMonitoringDeliveryStructure notification);
}
