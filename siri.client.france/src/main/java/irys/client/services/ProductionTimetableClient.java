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

import javax.xml.ws.Holder;

import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineDirectionStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.ProductionTimetableDeliveriesStructure;
import uk.org.siri.siri.ProductionTimetableRequestStructure;
import uk.org.siri.wsdl.ProductionTimetableError;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

/**
 * Methods required to implement an Production Timetable Service Proxy
 * 
 * @author michel
 *
 */
public class ProductionTimetableClient extends ServiceClient
{	
   /**
    * build a request to prepare Subscription request
    * 
    * @param lineIdArray filter on a list of lines (optional, may be null or empty) 
    * @param operatorId filter by line operator (optional, may be null) 
    * @param start start of period (optional, may be null) 
    * @param end end of period (optional, may be null) 
    * @param timestamp request timestamp (optional, current time if null)
    * @param messageIdentifier unique identifier used by server in responses (optional, generated if null)
    * @return the SIRI request fragment in SIRI XSD XMLBeans mapping format to insert in Subscription Request Choice
    */
   public ProductionTimetableRequestStructure buildRequest(String version,
         List<String> lineIds, 
         String operatorId, 
         GregorianCalendar start,
         GregorianCalendar end){
      ProductionTimetableRequestStructure request = factory.createProductionTimetableRequestStructure();      

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
            request.setLines(factory.createProductionTimetableRequestStructureLines());
            request.getLines().getLineDirection().add(lineDirRef );
         }
      }
      
      if (isDefined(operatorId)) 
      {
         OperatorRefStructure operatorRef = factory.createOperatorRefStructure();
         operatorRef.setValue(operatorId);
         request.getOperatorRef().add(operatorRef );
      }


      return request;
   }

   public ProductionTimetableRequestStructure buildRequest(String version,
         List<String> lineIds, 
         String operatorId, 
         String start,
         String end){
      return buildRequest(version,lineIds,operatorId,timeInDayToCalendar(start),timeInDayToCalendar(end));
      
   }
   
   
   /**
    * invoke ProductionTimetableService on a declared SIRI server
    * 
    * @param serverId the key used to fond the server's specific parameters in configuration files 
    * @param request a previous prepared SIRI request
    * @return the SIRI response in SIRI XSD XMLBeans mapping format 
    * @throws SiriException server or transport failure
    */
   public void getResponseDocument(WsServiceRequestInfoStructure serviceRequestInfo,
         ProductionTimetableRequestStructure request,
         Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
         Holder<ProductionTimetableDeliveriesStructure> answer) throws SiriException{
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.getProductionTimetable(serviceRequestInfo, request, requestExtension, serviceDeliveryInfo, answer, answerExtension);
      }
      catch (ProductionTimetableError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
