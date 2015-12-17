/**
 *   SIRI Product - Produit SIRI
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

import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.Duration;
import javax.xml.ws.Holder;

import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.EstimatedTimetableDeliveriesStructure;
import uk.org.siri.siri.EstimatedTimetableDetailEnumeration;
import uk.org.siri.siri.EstimatedTimetableRequestStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineDirectionStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.VersionRefStructure;
import uk.org.siri.wsdl.EstimatedTimetableError;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

/**
 * Methods required to implement an Estimated Timetable Service Proxy
 * 
 * @author michel
 *
 */
public class EstimatedTimetableClient extends ServiceClient
{	
   /**
    * build a request to prepare Subscription request
    * 
    * @param lineIdArray filter on a list of lines (optional, may be null or empty) 
    * @param timetableVersionId filter by version of the timetable set (optional, may be null)
    * @param operatorId filter by line operator (optional, may be null) 
    * @param preview filter on an interval (optional, may be null) 
    * @param timestamp request timestamp (optional, current time if null)
    * @param messageIdentifier unique identifier used by server in responses (optional, generated if null)
    * @return the SIRI request fragment in SIRI XSD XMLBeans mapping format to insert in Subscription Request Choice
    */
   public EstimatedTimetableRequestStructure buildRequest(String version,
         List<String> lineIds, 
         String direction,
         String timetableVersionId, 
         String operatorId, 
         Duration preview, 
         String detailLevel){
      EstimatedTimetableRequestStructure request = factory.createEstimatedTimetableRequestStructure();      

      request.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(new GregorianCalendar()));
      
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }
      if (!lineIds.isEmpty())
      {
         for (String lineId : lineIds)
         {
            LineDirectionStructure lineDirRef = factory.createLineDirectionStructure();
            LineRefStructure lineRef = factory.createLineRefStructure();
            lineRef.setValue(lineId);
            lineDirRef.setLineRef(lineRef );
            if (direction != null)
            {
               DirectionRefStructure dirRef = factory.createDirectionRefStructure();
               dirRef.setValue(direction);
               lineDirRef.setDirectionRef(dirRef );
            }
            request.setLines(factory.createEstimatedTimetableRequestStructureLines());
            request.getLines().getLineDirection().add(lineDirRef );
         }
      }
      if (isDefined(timetableVersionId))
      {
         VersionRefStructure value = factory.createVersionRefStructure();
         value.setValue(timetableVersionId);
         request.setTimetableVersionRef(value );
      }
      
      if (isDefined(operatorId)) 
      {
         OperatorRefStructure operatorRef = factory.createOperatorRefStructure();
         operatorRef.setValue(operatorId);
         request.getOperatorRef().add(operatorRef );
      }

      if (preview != null)
      {
         request.setPreviewInterval(preview);
      }

      
      if (isDefined(detailLevel))
      {
         request.setEstimatedTimetableDetailLevel(EstimatedTimetableDetailEnumeration.valueOf(detailLevel));
      }

      return request;
   }

   public EstimatedTimetableRequestStructure buildRequest(String version,
         List<String> lineIds, 
         String direction,
         String timetableVersionId, 
         String operatorId, 
         int preview, 
         String detailLevel){
      return buildRequest(version, lineIds, direction, timetableVersionId, operatorId, toDuration(preview),detailLevel);
   }
   /**
    * invoke EstimatedTimetableService on a declared SIRI server
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param request a previous prepared SIRI request
    * @return the SIRI response in SIRI XSD XMLBeans mapping format 
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument(WsServiceRequestInfoStructure serviceRequestInfo,
         EstimatedTimetableRequestStructure request,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<EstimatedTimetableDeliveriesStructure> answer) throws SiriException{
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.getEstimatedTimetable(serviceRequestInfo, request, requestExtension, serviceDeliveryInfo, answer, answerExtension);
      }
      catch (EstimatedTimetableError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
