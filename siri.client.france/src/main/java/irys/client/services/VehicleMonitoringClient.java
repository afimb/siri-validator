/**
 *   Siri Product - Produit SIRI
 *  
 *   a set of tools for easy application building with 
 *   respect of the France Siri Local Agreement
 *
 *   un ensemble d'outils facilitant la realisation d'applications
 *   respectant le profil France de la norme SIRI
 * 
 *   Copyright DRYADE 2009-2010
 */
package irys.client.services;

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.ws.Holder;

import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveriesStructure;
import uk.org.siri.siri.VehicleMonitoringDetailEnumeration;
import uk.org.siri.siri.VehicleMonitoringRequestStructure;
import uk.org.siri.siri.VehicleRefStructure;
import uk.org.siri.wsdl.VehicleMonitoringError;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

/**
 * Methods required to implement a Vehicle Monitoring Service Proxy
 * <p>
 * @author michel
 *
 */
public class VehicleMonitoringClient extends ServiceClient
{	
   /**
    * prepare a VehicleMonitoringRequest for recurrent usage
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param vehicleId SIRI reference for the vehicle to monitor (optional, may be null)
    * @param lineId filter on a line reference (optional, may be null)
    * @param maxVehicle filter on maximum calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param timestamp request timestamp (optional, current time if null)
    * @param messageIdentifier unique identifier used by server in responses (optional, generated if null)
    * @return the SIRI request in SIRI XSD XMLBeans mapping format 
    * @throws SiriException unknown serverId
    * 
    */
   public VehicleMonitoringRequestStructure buildRequest(String version, String vehicleId, String lineId,
         String direction, int maxVehicle, Long maxPrevious, Long maxOnwards,
         String detailLevel) throws SiriException
         {
      VehicleMonitoringRequestStructure request = factory.createVehicleMonitoringRequestStructure();

      request.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(new GregorianCalendar()));
      
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }

      if (isDefined(vehicleId))
      {
         VehicleRefStructure value = factory.createVehicleRefStructure();
         value.setValue(vehicleId);
         request.setVehicleRef(value );
      }
      else if (isDefined(lineId))
      {
         LineRefStructure value = factory.createLineRefStructure();
         value.setValue(lineId);
         request.setLineRef(value);
         if (maxVehicle > UNDEFINED_NUMBER)
         {
            request.setMaximumVehicles(BigInteger.valueOf(maxVehicle));
         }
         if (isDefined(direction))
         {
            DirectionRefStructure directionRef = factory.createDirectionRefStructure();
            directionRef.setValue(direction);
            request.setDirectionRef(directionRef );
         }
      }
      
      if (isDefined(detailLevel))
      {
         request.setVehicleMonitoringDetailLevel(VehicleMonitoringDetailEnumeration.valueOf(detailLevel));
      }
      
      if (maxPrevious != null || maxOnwards != null)
      {
         VehicleMonitoringRequestStructure.MaximumNumberOfCalls maxCalls = factory.createVehicleMonitoringRequestStructureMaximumNumberOfCalls();
         if (maxPrevious == null) 
         {
            maxCalls.setPrevious(BigInteger.ZERO);
         }
         else if ( maxPrevious != 0)
         {
           maxCalls.setPrevious(BigInteger.valueOf(maxPrevious));           
         }
         if (maxOnwards == null) 
         {
            maxCalls.setOnwards(BigInteger.ZERO);
         }
         else if ( maxOnwards != 0)
         {
           maxCalls.setOnwards(BigInteger.valueOf(maxOnwards));           
         }
         request.setMaximumNumberOfCalls(maxCalls);
      }
      return request;
         }
   /**
    * invoke VehicleMonitoringService on a declared SIRI server
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param request
    * @return the SIRI response in SIRI XSD XMLBeans mapping format 
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument( WsServiceRequestInfoStructure serviceRequestInfo,
         VehicleMonitoringRequestStructure request,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<VehicleMonitoringDeliveriesStructure> answer) throws SiriException
         {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.getVehicleMonitoring(serviceRequestInfo, request, requestExtension, serviceDeliveryInfo, answer, answerExtension);
      }
      catch (VehicleMonitoringError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }


}
