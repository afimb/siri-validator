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
import javax.xml.ws.Holder;

import uk.org.siri.siri.CheckStatusRequestStructure;
import uk.org.siri.siri.CheckStatusResponseBodyStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.wsdl.CheckStatusError;

/**
 * Methods required to implement a Check Status Service Proxy

 * @author michel
 *
 */
public class CheckStatusClient extends ServiceClient
{

   public CheckStatusClient()
   {
      super();

   }
   

   public CheckStatusRequestStructure buildRequest()
   {
      CheckStatusRequestStructure request = factory.createCheckStatusRequestStructure();
      populateServiceInfoStructure(request);
      return request;
   }
   
   public void checkStatus(
         CheckStatusRequestStructure request,
         Holder<ProducerResponseEndpointStructure> checkStatusAnswerInfo,
         Holder<CheckStatusResponseBodyStructure> answer) throws CheckStatusError
         {

      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      port.checkStatus(request, requestExtension, checkStatusAnswerInfo,answer, answerExtension);

         }


}
