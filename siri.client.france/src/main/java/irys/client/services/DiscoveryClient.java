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

import javax.xml.ws.Holder;

import uk.org.siri.siri.AbstractDiscoveryRequestStructure;
import uk.org.siri.siri.ConnectionLinksDeliveryStructure;
import uk.org.siri.siri.ConnectionLinksDiscoveryRequestStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LinesDeliveryStructure;
import uk.org.siri.siri.LinesDiscoveryRequestStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.ParticipantRefStructure;
import uk.org.siri.siri.StopPointsDeliveryStructure;
import uk.org.siri.siri.StopPointsDiscoveryRequestStructure;
import uk.org.siri.wsdl.ConnectionLinksDiscoveryError;
import uk.org.siri.wsdl.LinesDiscoveryError;
import uk.org.siri.wsdl.StopPointsDiscoveryError;

/**
 * Methods required to implement a Discovery Service Proxy
 * 
 * @author michel
 *
 */
public class DiscoveryClient extends ServiceClient
{
   /**
    * populate the Service Request Info structure
    * 
    * @param serviceRequestInfo the structure to populate
    */
   private void populateServiceInfoStructure(AbstractDiscoveryRequestStructure serviceRequestInfo)  
   {
      serviceRequestInfo.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(new GregorianCalendar()));
      ParticipantRefStructure requestorRef = factory.createParticipantRefStructure();
      requestorRef.setValue(requestorRefValue);
      serviceRequestInfo.setRequestorRef(requestorRef);
      MessageQualifierStructure id =  factory.createMessageQualifierStructure();
      id.setValue(requestIdentifierPrefix + getRequestNumber());
      serviceRequestInfo.setMessageIdentifier(id);
   }


//   @XmlElement(name = "BoundingBox")
//   @XmlElement(name = "Circle")
//   @XmlElement(name = "PlaceRef")
//   @XmlElement(name = "LineDirectionRef")
//   @XmlElement(name = "OperatorRef")
//   @XmlElement(name = "Language", defaultValue = "en")
//   @XmlElement(name = "LinesDetailLevel", defaultValue = "normal")
//   @XmlAttribute(name = "version")
   public LinesDiscoveryRequestStructure buildLineRequest(String version)
   {
      LinesDiscoveryRequestStructure request = factory.createLinesDiscoveryRequestStructure();
      populateServiceInfoStructure(request);
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }
      return request;
   }
   
//   @XmlElement(name = "BoundingBox")
//   @XmlElement(name = "Circle")
//   @XmlElement(name = "PlaceRef")
//   @XmlElement(name = "OperatorRef")
//   @XmlElement(name = "LineRef")
//   @XmlElement(name = "Language", defaultValue = "en")
//   @XmlElement(name = "StopPointsDetailLevel", defaultValue = "normal")
//   @XmlAttribute(name = "version")
   public StopPointsDiscoveryRequestStructure buildStopRequest(String version)
   {
      StopPointsDiscoveryRequestStructure request = factory.createStopPointsDiscoveryRequestStructure();
      populateServiceInfoStructure(request);
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }
      return request;
   }

//   @XmlElement(name = "BoundingBox")
//   @XmlElement(name = "Circle")
//   @XmlElement(name = "PlaceRef")
//   @XmlElement(name = "LineRef")
//   @XmlElement(name = "OperatorRef")
//   @XmlElement(name = "ConnectionLinksDetailLevel", defaultValue = "normal")
//   @XmlAttribute(name = "version", required = true)
   public ConnectionLinksDiscoveryRequestStructure buildConnectionLinkRequest(String version)
   {
      ConnectionLinksDiscoveryRequestStructure request = factory.createConnectionLinksDiscoveryRequestStructure();
      populateServiceInfoStructure(request);
      if (version != null)
      {
         request.setVersion(version);
      }
      else
      {
         request.setVersion(this.version);
      }
      return request;
   }

   public void stopPointsDiscovery(
         StopPointsDiscoveryRequestStructure request,
         Holder<StopPointsDeliveryStructure> answer)
         throws StopPointsDiscoveryError
         {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      port.stopPointsDiscovery(request, requestExtension,answer, answerExtension);
      
         }

   public void linesDiscovery(
         LinesDiscoveryRequestStructure request,
         Holder<LinesDeliveryStructure> answer)
         throws LinesDiscoveryError
         {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      port.linesDiscovery(request, requestExtension,answer, answerExtension);
      
         }

   public void connectionLinksDiscovery(
         ConnectionLinksDiscoveryRequestStructure request, 
         Holder<ConnectionLinksDeliveryStructure> answer) throws ConnectionLinksDiscoveryError
         {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      port.connectionLinksDiscovery(request, requestExtension,answer, answerExtension);
      
         }

}
