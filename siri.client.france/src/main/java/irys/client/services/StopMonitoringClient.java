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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import uk.org.siri.siri.DestinationRefStructure;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.MonitoringRefStructure;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.StopMonitoringDeliveriesStructure;
import uk.org.siri.siri.StopMonitoringDetailEnumeration;
import uk.org.siri.siri.StopMonitoringFilterStructure;
import uk.org.siri.siri.StopMonitoringFilterStructure.MaximumNumberOfCalls;
import uk.org.siri.siri.StopMonitoringRequestStructure;
import uk.org.siri.siri.StopVisitTypeEnumeration;
import uk.org.siri.wsdl.StopMonitoringError;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

/**
 * Methods required to implement a Stop Monitoring Service Proxy
 * <p>
 * <b>Note on Multiple Stop Monitoring </b><br/>
 * 
 * A SIRI server may not implement this method, so to manage this possibility, 
 * a configuration parameter <i>isMultipleStopMonitoredSupported</i> is set 
 * and a specific method is available to let the client invoke the MultipleStopMonitoring 
 * only if available.
 * <br/><br/>
 * When the GetMultipleStopMonitoring is available, the way to invoke it is : 
 * <ol>
 * <li>multiple calls to getFilterStructure : one for each set of parameters</li>
 * <li>call GetResponseDocument with the list of FilterStucture </li>
 * </ol>
 * @author michel
 *
 */
public class StopMonitoringClient extends ServiceClient
{	
   /**
    * prepare a StopMonitoringRequest for recurrent usage
    * 
    * @param version SIRI and profile version (null if default one)
    * @param stopId SIRI reference for the stop point to monitor (mandatory) 
    * @param lineId filter on a line reference (optional, may be null)
    * @param destId filter on a destination stop point reference (optional, may be null)
    * @param operatorId filter on a line operator reference (optional, may be null)
    * @param start filter on a start time (optional, null value for 'now')
    * @param preview filter on an interval (optional, null value for 'until end of service')
    * @param typeVisit filter on Arrivals/Departures/All times (optional, null for 'Departures')
    * @param maxStop filter on maximum calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param minStLine filter on minimum calls per line returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param onWard filter on onward calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param detailLevel filter on detailLevel returned (optional, null to ignore) 
    * @return the SIRI request in SIRI XSD XMLBeans mapping format 
    * 
    */
   public StopMonitoringRequestStructure buildRequest(String version, String stopId, String lineId, String directionId, String destId,
         String operatorId, GregorianCalendar start, Duration preview,
         String typeVisit, int maxStop, int minStLine, Long maxPrevious, Long maxOnwards,
         String detailLevel)
         {
      StopMonitoringRequestStructure request = factory.createStopMonitoringRequestStructure();
      
      request.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(new GregorianCalendar()));
      
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }

      MonitoringRefStructure monitoringRef = factory.createMonitoringRefStructure();
      monitoringRef.setValue(stopId);
      request.setMonitoringRef(monitoringRef );
      

      if (isDefined(lineId))
      {
         LineRefStructure lineRef = factory.createLineRefStructure();
         lineRef.setValue(lineId);
         request.setLineRef(lineRef );
      }
      
      if (isDefined(directionId))
      {
         DirectionRefStructure dirRef = factory.createDirectionRefStructure();
         dirRef.setValue(directionId);
         request.setDirectionRef(dirRef );
      }

      if (isDefined(destId))
      {
         DestinationRefStructure destRef = factory.createDestinationRefStructure();
         destRef.setValue(destId);
         request.setDestinationRef(destRef );
      }
      
      if (isDefined(operatorId))
      {
         OperatorRefStructure operatorRef = factory.createOperatorRefStructure();
         operatorRef.setValue(operatorId);
         request.setOperatorRef(operatorRef );
      }
      
      if (start != null)
      {
         XMLGregorianCalendar value = xmlFactory.newXMLGregorianCalendar(start);
         request.setStartTime(value );
      }
      
      if (preview != null)
      {
         request.setPreviewInterval(preview);
      }
      
      if (typeVisit != null)
      {
         request.setStopVisitTypes(StopVisitTypeEnumeration.fromValue(typeVisit));
      }
      
      if (maxStop > UNDEFINED_NUMBER)
      {
         request.setMaximumStopVisits(BigInteger.valueOf((long) maxStop));
      }
      
      if (minStLine > UNDEFINED_NUMBER)
      {
         request.setMinimumStopVisitsPerLine(BigInteger.valueOf((long) minStLine));
      }

      if (maxPrevious != null || maxOnwards != null)
      {
         MaximumNumberOfCalls maxCalls = factory.createStopMonitoringFilterStructureMaximumNumberOfCalls();
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
      
      if (isDefined(detailLevel))
      {
         request.setStopMonitoringDetailLevel(StopMonitoringDetailEnumeration.valueOf(detailLevel));
      }

      return request;
         }
    /**
    * prepare a StopMonitoringRequest for recurrent usage
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param stopId SIRI reference for the stop point to monitor (mandatory) 
    * @param lineId filter on a line reference (optional, may be null)
    * @param directionId filter on a direction reference (optional, may be null)
    * @param destId filter on a destination stop point reference (optional, may be null)
    * @param operatorId filter on a line operator reference (optional, may be null)
    * @param start filter on a start time (optional, null value for 'now')
    * @param preview filter on an interval in minutes (optional, must be SiriInterface.UNDEFINED_NUMBER for 'until end of service')
    * @param typeVisit filter on Arrivals/Departures/All times (optional, null for 'Departures')
    * @param maxStop filter on maximum calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param minStLine filter on minimum calls per line returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param onWard filter on onward calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param detailLevel filter on detailLevel returned (optional, null to ignore) 
    * @return the SIRI request in SIRI XSD XMLBeans mapping format 
    * @throws SiriException unknown serverId
    */
   public StopMonitoringRequestStructure buildRequest(String version, String stopId, String lineId, String directionId, String destId,
         String operatorId, String start, int preview,
         String typeVisit, int maxStop, int minStLine, Long maxPrevious, Long maxOnwards, String detailLevel)
   {
      return buildRequest(version, stopId, lineId, directionId, destId, operatorId, timeInDayToCalendar(start), toDuration(preview), typeVisit, maxStop, minStLine, maxPrevious,  maxOnwards, detailLevel);
   }

   /**
    * invoke StopMonitoringService
    * 
    * @param request
    * @param serviceDeliveryInfo
    * @param answer
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument(WsServiceRequestInfoStructure serviceRequestInfo,
         StopMonitoringRequestStructure request,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<StopMonitoringDeliveriesStructure> answer) throws SiriException
         {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.getStopMonitoring(serviceRequestInfo, request, requestExtension, serviceDeliveryInfo, answer, answerExtension);
      }
      catch (StopMonitoringError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }

   /**
    * prepare a FilterStructure form MultipleStopMonitoring call
    * 
    * @param stopId SIRI reference for the stop point to monitor (mandatory) 
    * @param lineId filter on a line reference (optional, may be null)
    * @param destId filter on a destination stop point reference (optional, may be null)
    * @param operatorId filter on a line operator reference (optional, may be null)
    * @param start filter on a start time (optional, null value for 'now')
    * @param preview filter on an interval in minutes (optional, must be SiriInterface.UNDEFINED_NUMBER for 'until end of service')
    * @param typeVisit filter on Arrivals/Departures/All times (optional, null for 'Departures')
    * @param maxStop filter on maximum calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param minStLine filter on minimum calls per line returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param onWard filter on onward calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param detailLevel filter on detailLevel returned (optional, null to ignore) 
    * @return the SIRI request in SIRI XSD XMLBeans mapping format 
    */
   public StopMonitoringFilterStructure buildFilter(String stopId, String lineId, String destId, String operatorId, String start,
         int preview, String typeVisit, int maxStop, int minStLine, Long maxPrevious, Long maxOnwards, String detailLevel){
      return buildFilter(stopId, lineId, destId, operatorId, timeInDayToCalendar(start), toDuration(preview), typeVisit, maxStop, minStLine,  maxPrevious,  maxOnwards, detailLevel);
   }
   /**
    * prepare a FilterStructure form MultipleStopMonitoring call
    * 
    * @param stopId SIRI reference for the stop point to monitor (mandatory) 
    * @param lineId filter on a line reference (optional, may be null)
    * @param destId filter on a destination stop point reference (optional, may be null)
    * @param operatorId filter on a line operator reference (optional, may be null)
    * @param start filter on a start time (optional, null value for 'now')
    * @param preview filter on an interval (optional, null value for 'until end of service')
    * @param typeVisit filter on Arrivals/Departures/All times (optional, null for 'Departures')
    * @param maxStop filter on maximum calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param minStLine filter on minimum calls per line returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param onWard filter on onward calls returned (optional, must be SiriInterface.UNDEFINED_NUMBER to ignore) 
    * @param detailLevel filter on detailLevel returned (optional, null to ignore) 
    * @return the SIRI request in SIRI XSD XMLBeans mapping format 
    */
   public StopMonitoringFilterStructure buildFilter(String stopId, String lineId, String destId, String operatorId, 
         GregorianCalendar start, Duration preview, String typeVisit, int maxStop, int minStLine, Long maxPrevious, Long maxOnwards, String detailLevel){
      StopMonitoringFilterStructure request = factory.createStopMonitoringFilterStructure();
      
      MonitoringRefStructure monitoringRef = factory.createMonitoringRefStructure();
      monitoringRef.setValue(stopId);
      request.setMonitoringRef(monitoringRef );
      

      if (isDefined(lineId))
      {
         LineRefStructure lineRef = factory.createLineRefStructure();
         lineRef.setValue(lineId);
         request.setLineRef(lineRef );
      }
      
      if (isDefined(destId))
      {
         DestinationRefStructure destRef = factory.createDestinationRefStructure();
         destRef.setValue(destId);
         request.setDestinationRef(destRef );
      }
      
      if (isDefined(operatorId))
      {
         OperatorRefStructure operatorRef = factory.createOperatorRefStructure();
         operatorRef.setValue(operatorId);
         request.setOperatorRef(operatorRef );
      }
      
      if (start != null)
      {
         XMLGregorianCalendar value = xmlFactory.newXMLGregorianCalendar(start);
         request.setStartTime(value );
      }
      
      if (preview != null)
      {
         request.setPreviewInterval(preview);
      }
      
      if (typeVisit != null)
      {
         request.setStopVisitTypes(StopVisitTypeEnumeration.fromValue(typeVisit));
      }
      
      if (maxStop > UNDEFINED_NUMBER)
      {
         request.setMaximumStopVisits(BigInteger.valueOf((long) maxStop));
      }
      
      if (minStLine > UNDEFINED_NUMBER)
      {
         request.setMinimumStopVisitsPerLine(BigInteger.valueOf((long) minStLine));
      }

      if (maxPrevious != null || maxOnwards != null)
      {
         MaximumNumberOfCalls maxCalls = factory.createStopMonitoringFilterStructureMaximumNumberOfCalls();
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
      
      if (isDefined(detailLevel))
      {
         request.setStopMonitoringDetailLevel(StopMonitoringDetailEnumeration.valueOf(detailLevel));
      }
      return request;
   }
   /**
    * invoke MultipleStopMonitoringService on a declared SIRI server
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param requests list of prepared StopMonitoring filters
    * @return the SIRI response in SIRI XSD XMLBeans mapping format 
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument(String serverId,List<StopMonitoringFilterStructure> requests,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<StopMonitoringDeliveriesStructure> answer) throws SiriException{
   }

   /**
    * invoke MultipleStopMonitoringService on a declared SIRI server
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param requests list of prepared StopMonitoring filters
    * @param timestamp request timestamp (optional, current time if null)
    * @param messageIdentifier unique identifier used by server in responses (optional, generated if null)
    * @return the SIRI response in SIRI XSD XMLBeans mapping format 
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument(String serverId,List<StopMonitoringFilterStructure> requests, Calendar timestamp, MessageQualifierStructure messageIdentifier,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<StopMonitoringDeliveriesStructure> answer ) throws SiriException{
   }

}
