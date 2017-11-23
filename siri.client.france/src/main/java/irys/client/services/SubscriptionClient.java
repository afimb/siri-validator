package irys.client.services;
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


import java.util.GregorianCalendar;

import javax.xml.datatype.Duration;
import javax.xml.ws.Holder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.AbstractSubscriptionStructure;
import uk.org.siri.siri.ConnectionMonitoringRequestStructure;
import uk.org.siri.siri.ConnectionMonitoringSubscriptionRequestStructure;
import uk.org.siri.siri.ConnectionTimetableRequestStructure;
import uk.org.siri.siri.ConnectionTimetableSubscriptionStructure;
import uk.org.siri.siri.EstimatedTimetableRequestStructure;
import uk.org.siri.siri.EstimatedTimetableSubscriptionStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.FacilityMonitoringRequestStructure;
import uk.org.siri.siri.FacilityMonitoringSubscriptionStructure;
import uk.org.siri.siri.GeneralMessageRequestStructure;
import uk.org.siri.siri.GeneralMessageSubscriptionStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.ParticipantRefStructure;
import uk.org.siri.siri.ProductionTimetableRequestStructure;
import uk.org.siri.siri.ProductionTimetableSubscriptionRequest;
import uk.org.siri.siri.RequestStructure;
import uk.org.siri.siri.ResponseEndpointStructure;
import uk.org.siri.siri.SiriSubscriptionRequestStructure;
import uk.org.siri.siri.SituationExchangeRequestStructure;
import uk.org.siri.siri.SituationExchangeSubscriptionStructure;
import uk.org.siri.siri.StopMonitoringRequestStructure;
import uk.org.siri.siri.StopMonitoringSubscriptionStructure;
import uk.org.siri.siri.StopTimetableRequestStructure;
import uk.org.siri.siri.StopTimetableSubscriptionStructure;
import uk.org.siri.siri.SubscriptionQualifierStructure;
import uk.org.siri.siri.SubscriptionResponseBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionRequestBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionResponseStructure;
import uk.org.siri.siri.VehicleMonitoringRequestStructure;
import uk.org.siri.siri.VehicleMonitoringSubscriptionStructure;
import uk.org.siri.wsdl.DeleteSubscriptionError;
import uk.org.siri.wsdl.SubscriptionError;
import uk.org.siri.wsdl.WsSubscriptionRequestInfoStructure;


/**
 * @author michel
 *
 */
@Log4j
public class SubscriptionClient extends ServiceClient
{

	@Getter @Setter private String subscriptionIdentifierPrefix ;
	@Getter @Setter private String subscriptionIdentifierSuffix ;

   public AbstractSubscriptionStructure buildSubRequest(SiriSubscriptionRequestStructure subscriptionRequest,GregorianCalendar requestTimestamp, AbstractServiceRequestStructure structure, String  initialTerminationTime, boolean incrementalUpdates, int changeBeforeUpdates,int updateInterval,String subscriptionId)
   {
      return buildSubRequest(subscriptionRequest,requestTimestamp, structure,  timeInDayToCalendar(initialTerminationTime), incrementalUpdates, secondsToDuration(changeBeforeUpdates), secondsToDuration(updateInterval), subscriptionId);
   }

	public AbstractSubscriptionStructure buildSubRequest(SiriSubscriptionRequestStructure subscriptionRequest, GregorianCalendar requestTimestamp, AbstractServiceRequestStructure structure, GregorianCalendar initialTerminationTime, boolean incrementalUpdates, Duration changeBeforeUpdates,Duration updateInterval,String subscriptionId)
	{

		AbstractSubscriptionStructure abstractRequest = null;
		if (structure instanceof GeneralMessageRequestStructure )
		{
			// multiple subscription allowed : no control
			structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			GeneralMessageSubscriptionStructure request = factory.createGeneralMessageSubscriptionStructure();

			request.setGeneralMessageRequest((GeneralMessageRequestStructure)structure);
			subscriptionRequest.getGeneralMessageSubscriptionRequest().add(request);
			abstractRequest = request;

		}
		else if (structure instanceof StopMonitoringRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));

			StopMonitoringSubscriptionStructure request = factory.createStopMonitoringSubscriptionStructure();
			request.setStopMonitoringRequest((StopMonitoringRequestStructure) structure);
			if (changeBeforeUpdates != null)
				request.setChangeBeforeUpdates(changeBeforeUpdates);
			request.setIncrementalUpdates(incrementalUpdates);
         subscriptionRequest.getStopMonitoringSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof ConnectionMonitoringRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			ConnectionMonitoringSubscriptionRequestStructure request = factory.createConnectionMonitoringSubscriptionRequestStructure();
			request.setConnectionMonitoringRequest((ConnectionMonitoringRequestStructure) structure);
			if (changeBeforeUpdates != null)
				request.setChangeBeforeUpdates(changeBeforeUpdates);
         subscriptionRequest.getConnectionMonitoringSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof ConnectionTimetableRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			ConnectionTimetableSubscriptionStructure request = factory.createConnectionTimetableSubscriptionStructure();
			request.setConnectionTimetableRequest( (ConnectionTimetableRequestStructure) structure);
         subscriptionRequest.getConnectionTimetableSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof EstimatedTimetableRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			EstimatedTimetableSubscriptionStructure request = factory.createEstimatedTimetableSubscriptionStructure();
			request.setEstimatedTimetableRequest( (EstimatedTimetableRequestStructure) structure);
			if (changeBeforeUpdates != null)
				request.setChangeBeforeUpdates(changeBeforeUpdates);
			request.setIncrementalUpdates(incrementalUpdates);
         subscriptionRequest.getEstimatedTimetableSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof FacilityMonitoringRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			FacilityMonitoringSubscriptionStructure request = factory.createFacilityMonitoringSubscriptionStructure();
			request.setFacilityMonitoringRequest((FacilityMonitoringRequestStructure) structure);
			request.setIncrementalUpdates(incrementalUpdates);
         subscriptionRequest.getFacilityMonitoringSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof ProductionTimetableRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			ProductionTimetableSubscriptionRequest request = factory.createProductionTimetableSubscriptionRequest();
			request.setProductionTimetableRequest((ProductionTimetableRequestStructure) structure);
         subscriptionRequest.getProductionTimetableSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof SituationExchangeRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			SituationExchangeSubscriptionStructure request = factory.createSituationExchangeSubscriptionStructure();
			request.setSituationExchangeRequest((SituationExchangeRequestStructure) structure);
         subscriptionRequest.getSituationExchangeSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof StopTimetableRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			StopTimetableSubscriptionStructure request = factory.createStopTimetableSubscriptionStructure();
			request.setStopTimetableRequest((StopTimetableRequestStructure) structure);
         subscriptionRequest.getStopTimetableSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else if (structure instanceof VehicleMonitoringRequestStructure)
		{
			// multiple subscription allowed : no control
         structure.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(requestTimestamp));
			VehicleMonitoringSubscriptionStructure request = factory.createVehicleMonitoringSubscriptionStructure();
			request.setVehicleMonitoringRequest((VehicleMonitoringRequestStructure) structure);
			if (changeBeforeUpdates != null)
				request.setChangeBeforeUpdates(changeBeforeUpdates);
         if (updateInterval != null)
            request.setUpdateInterval(updateInterval);
			request.setIncrementalUpdates(incrementalUpdates);
         subscriptionRequest.getVehicleMonitoringSubscriptionRequest().add(request);
			abstractRequest = request;
		}
		else
		{
		   log.error("unknown service "+structure.getClass().getSimpleName());
		   return null;
		}

		abstractRequest.setInitialTerminationTime(xmlFactory.newXMLGregorianCalendar(initialTerminationTime));
		SubscriptionQualifierStructure subscriptionIdentifier = factory.createSubscriptionQualifierStructure();
		subscriptionIdentifier.setValue(subscriptionIdentifierPrefix+subscriptionId+subscriptionIdentifierSuffix);
		abstractRequest.setSubscriptionIdentifier(subscriptionIdentifier);
		
		return abstractRequest;

	}



	public SiriSubscriptionRequestStructure buildSubcriptionRequest(String consumerAddress) throws SiriException
	{
		SiriSubscriptionRequestStructure request = factory.createSiriSubscriptionRequestStructure();
      return request ;
	}
	
   public TerminateSubscriptionRequestBodyStructure buildTerminateSubcriptionRequest(String subscriptionId) throws SiriException
   {
      TerminateSubscriptionRequestBodyStructure request = factory.createTerminateSubscriptionRequestBodyStructure();
      if (subscriptionId.equalsIgnoreCase("all"))
      {
         request.setAll("");
      }
      else
      {
         SubscriptionQualifierStructure ref = factory.createSubscriptionQualifierStructure();
         ref.setValue(subscriptionId);
         request.getSubscriptionRef().add(ref );
      }
      return request ;
   }

	public void subscribe(WsSubscriptionRequestInfoStructure subscriptionRequestInfo, SiriSubscriptionRequestStructure request, Holder<ResponseEndpointStructure> subscriptionAnswerInfo, Holder<SubscriptionResponseBodyStructure> answer) throws SiriException
	{
		ExtensionsStructure requestExtension = factory.createExtensionsStructure();
		
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.subscribe(subscriptionRequestInfo, request, requestExtension, subscriptionAnswerInfo, answer, answerExtension);
      }
      catch (SubscriptionError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

	}		


   public void deleteSubscription(RequestStructure deleteSubscriptionInfo, TerminateSubscriptionRequestBodyStructure request, Holder<ResponseEndpointStructure> deleteSubscriptionAnswerInfo, Holder<TerminateSubscriptionResponseStructure> answer) throws SiriException
   {
      ExtensionsStructure requestExtension = factory.createExtensionsStructure();
      Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
      try
      {
         port.deleteSubscription(deleteSubscriptionInfo, request, requestExtension, deleteSubscriptionAnswerInfo, answer, answerExtension);
      }
      catch (DeleteSubscriptionError e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public WsSubscriptionRequestInfoStructure buildWsSubscriptionRequestInfoStructure(String consumerAddress)
   {
      WsSubscriptionRequestInfoStructure serviceRequestInfo = new WsSubscriptionRequestInfoStructure();
      serviceRequestInfo.setConsumerAddress(consumerAddress);
      populateRequestStructure(serviceRequestInfo);
      return serviceRequestInfo;
   }

   public RequestStructure buildDeleteSubscriptionInfo()
   {
      RequestStructure serviceRequestInfo = new RequestStructure();
      populateRequestStructure(serviceRequestInfo);
      return serviceRequestInfo;
   }     
   
   private void populateRequestStructure(RequestStructure request)
   {
      request.setRequestTimestamp(xmlFactory.newXMLGregorianCalendar(new GregorianCalendar()));
      ParticipantRefStructure requestorRef = factory.createParticipantRefStructure();
      requestorRef.setValue(requestorRefValue);
      request.setRequestorRef(requestorRef);
      MessageQualifierStructure id =  factory.createMessageQualifierStructure();
      id.setValue(requestIdentifierPrefix + getRequestNumber()+requestIdentifierSuffix);
      request.setMessageIdentifier(id);
      
   }


}
