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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.developer.SchemaValidationFeature;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import uk.org.siri.siri.AbstractServiceDeliveryStructure;
import uk.org.siri.siri.AccessNotAllowedErrorStructure;
import uk.org.siri.siri.CapabilityNotSupportedErrorStructure;
import uk.org.siri.siri.ContextualisedRequestStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.ObjectFactory;
import uk.org.siri.siri.OtherErrorStructure;
import uk.org.siri.siri.ParticipantRefStructure;
import uk.org.siri.siri.RequestStructure;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.wsdl.SiriProducerDocPort;
import uk.org.siri.wsdl.SiriProducerDocServices;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

@Log4j
public class ServiceClient {
	/**
	 * Constant value for optional numeric arguments
	 */
	public static final int UNDEFINED_NUMBER = -100000;

	/**
	 * Available services in SIRI
	 */
	public static enum Service {
		/**
		 * General Messaging
		 */
		GeneralMessageService,
		/**
		 * Stop Monitoring
		 */
		StopMonitoringService,
		/**
		 * Check Status
		 */
		CheckStatusService,
		/**
		 * Data Supply : not implemented
		 */
		DataSupplyService,
		/**
		 * Subscription : implementation in progress
		 */
		SubscriptionService,
		/**
		 * Capabilities : not implemented
		 */
		CapabilitiesService,
		/**
		 * Connection Monitoring : not implemented
		 */
		ConnectionMonitoringService,
		/**
		 * Connection Timetable : not implemented
		 */
		ConnectionTimetableService,
		/**
		 * Estimated Timetable
		 */
		EstimatedTimetableService,
		/**
		 * Facility Monitoring : not implemented
		 */
		FacilityMonitoringService,
		/**
		 * Production Timetable
		 */
		ProductionTimetableService,
		/**
		 * Stop Timetable : not implemented
		 */
		StopTimetableService,
		/**
		 * Situation Exchange : not implemented
		 */
		SituationExchangeService,
		/**
		 * Vehicle Monitoring
		 */
		VehicleMonitoringService,
		/**
		 * SiriService : in progress
		 */
		SiriService,
		/**
		 * Discovery Service
		 */
		DiscoveryService
	};

	@Setter
	protected String serverUrl;
	@Setter
	@Getter
	protected String version;
	@Setter
	protected String requestIdentifierPrefix;
	@Setter
	protected String proxyName = "";
	@Setter
	protected String proxyPort = "8080";
	@Setter
	protected String requestorRefValue;
	@Setter
	protected String authUser;
	@Setter
	protected String authPassword;
	@Setter
	protected boolean isRequestCompressionRequired;
	@Setter
	protected boolean isResponseCompressionAllowed;

	private static int requestNumber = 0;
	@Setter
	protected long soapTimeOut = 90000;
	@Setter
	protected boolean validation = false;

	protected static SiriProducerDocServices services;

	protected static SiriProducerDocPort port;

	protected static DatatypeFactory xmlFactory;

	protected static ObjectFactory factory;
	protected static uk.org.siri.wsdl.siri.ObjectFactory idfFactory;

	// protected static scma.siri.ObjectFactory scmaFactory;

	static LoggingHandler traceHandler;

	private static boolean initialized = false;

	public ServiceClient() {
	}

	public void startTrace(String requestFileName, String responseFileName,SiriErrorCallback callBack,
			boolean verbose) {
		traceHandler.init(requestFileName, responseFileName, callBack, verbose);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void init() throws Exception {
		if (initialized)
			return;

		initialized = true;
		xmlFactory = DatatypeFactory.newInstance();
		factory = new ObjectFactory();
		idfFactory = new uk.org.siri.wsdl.siri.ObjectFactory();
		services = new SiriProducerDocServices();
		port = services.getSiriWSPort(new SchemaValidationFeature(
				SiriErrorHandler.class)); 
		BindingProvider prov = (BindingProvider) port;
		Map<String, Object> context = prov.getRequestContext();
		context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serverUrl);
		Map headers = new HashMap();
		if (isRequestCompressionRequired) {
			headers.put("Content-Encoding", Collections.singletonList("gzip"));
		}
		if (isResponseCompressionAllowed) {
			headers.put("Accept-Encoding", Collections.singletonList("gzip"));
		}
		if (!headers.isEmpty()) {
			context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
		}
		if (isDefined(authUser) && isDefined(authPassword)) {
			context.put(BindingProvider.USERNAME_PROPERTY, authUser);
			context.put(BindingProvider.PASSWORD_PROPERTY, authPassword);
		}
		if (isDefined(proxyName) && isDefined(proxyPort)) {
			context.put("http.proxyHost", proxyName);
			context.put("http.proxyPort", proxyPort);
		}

		Binding binding = prov.getBinding();
		List<Handler> oldHandlerList = binding.getHandlerChain();
		traceHandler = new LoggingHandler();
		List<Handler>  handlerList = new ArrayList<Handler>();
		handlerList.add(traceHandler);
		if (oldHandlerList != null) {
			log.info("complete handler list");
			handlerList.addAll(oldHandlerList);
		}
		binding.setHandlerChain(handlerList);
		
	}

	/**
	 * get a request number
	 * 
	 * @return the next value of a counter shared by every proxy
	 */
	public static int getRequestNumber() {
		return requestNumber++;
	}

	public static void setRequestNumber(int requestNumber) {
		ServiceClient.requestNumber = requestNumber;
	}

	/**
	 * 
	 * 
	 * @param requestIdentifierPrefix
	 * @return
	 */
	public WsServiceRequestInfoStructure buildWsServiceRequestInfoStructure() {
		return buildWsServiceRequestInfoStructure(requestIdentifierPrefix);
	}

	/**
	 * 
	 * 
	 * @param requestIdentifierPrefix
	 * @return
	 */
	public WsServiceRequestInfoStructure buildWsServiceRequestInfoStructure(
			String requestIdentifierPrefix) {
		WsServiceRequestInfoStructure serviceRequestInfo = new WsServiceRequestInfoStructure();
		populateServiceInfoStructure(serviceRequestInfo,
				requestIdentifierPrefix);
		return serviceRequestInfo;
	}

	/**
	 * populate the Service Request Info structure
	 * 
	 * @param serviceRequestInfo
	 *            the structure to populate
	 * @param requestIdentifierPrefix
	 *            a prefix for the message identifier
	 * @throws SiriException
	 *             unknown server id
	 */
	public void populateServiceInfoStructure(
			ContextualisedRequestStructure serviceRequestInfo,
			String requestIdentifierPrefix) {
		serviceRequestInfo.setRequestTimestamp(xmlFactory
				.newXMLGregorianCalendar(new GregorianCalendar()));
		ParticipantRefStructure requestorRef = factory
				.createParticipantRefStructure();
		requestorRef.setValue(requestorRefValue);
		serviceRequestInfo.setRequestorRef(requestorRef);
		MessageQualifierStructure id = factory
				.createMessageQualifierStructure();
		id.setValue(requestIdentifierPrefix + getRequestNumber());
		serviceRequestInfo.setMessageIdentifier(id);

	}

	/**
	 * populate the Service Request Info structure
	 * 
	 * @param serviceRequestInfo
	 *            the structure to populate
	 * @param messageIdentfier
	 *            a preset message identifier
	 * @throws SiriException
	 *             unknown server id
	 */
	public void populateServiceInfoStructure(
			ContextualisedRequestStructure serviceRequestInfo,
			MessageQualifierStructure messageIdentfier) {
		serviceRequestInfo.setRequestTimestamp(xmlFactory
				.newXMLGregorianCalendar(new GregorianCalendar()));
		ParticipantRefStructure requestorRef = factory
				.createParticipantRefStructure();
		requestorRef.setValue(requestorRefValue);
		serviceRequestInfo.setRequestorRef(requestorRef);
		serviceRequestInfo.setMessageIdentifier(messageIdentfier);
	}

	/**
	 * populate the Service Request Info structure
	 * 
	 * @param serviceRequestInfo
	 *            the structure to populate
	 */
	public void populateServiceInfoStructure(RequestStructure serviceRequestInfo) {
		serviceRequestInfo.setRequestTimestamp(xmlFactory
				.newXMLGregorianCalendar(new GregorianCalendar()));
		ParticipantRefStructure requestorRef = factory
				.createParticipantRefStructure();
		requestorRef.setValue(requestorRefValue);
		serviceRequestInfo.setRequestorRef(requestorRef);
		MessageQualifierStructure id = factory
				.createMessageQualifierStructure();
		id.setValue(requestIdentifierPrefix + getRequestNumber());
		serviceRequestInfo.setMessageIdentifier(id);
	}

	/**
	 * convert a preview interval from integer (minutes) to Duration object
	 * 
	 * @param preview
	 *            preview interval in minutes
	 * @return preview interval in GDuration format
	 */
	public Duration toDuration(int preview) {
		if (preview > UNDEFINED_NUMBER) {
			return xmlFactory.newDuration((long) preview * 60000L);
		}
		return null;
	}

	/**
	 * convert a preview interval from integer (seconds) to Duration object
	 * 
	 * @param preview
	 *            value in seconds
	 * @return preview interval in GDuration format
	 */
	public Duration secondsToDuration(int preview) {
		if (preview > UNDEFINED_NUMBER) {
			return xmlFactory.newDuration((long) preview * 1000L);
		}
		return null;
	}

	public GregorianCalendar timeInDayToCalendar(String date) {
		if (date != null && !date.equals("")) {
			try {
				String[] t = date.split(":");
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
				if (t.length > 1) {
					cal.set(Calendar.MINUTE, Integer.parseInt(t[1]));
				} else {
					cal.set(Calendar.MINUTE, 0);
				}
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				GregorianCalendar gcal = new GregorianCalendar();
				gcal.setTimeInMillis(cal.getTimeInMillis());
				return gcal;
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"invalid syntax for time 'hh:mm' required");
			}
		}
		return null;
	}

	/**
	 * tool for converting eventual exceptions in SIRI deliveries in
	 * SiriException <br/>
	 * this tool scans a delivery array and create a map with entries only on
	 * ranks with ErrorCondition set; <br/>
	 * the error condition is converted in SiriException for easier way to
	 * process it <br/>
	 * <b>NOTE:</b>the warnings (ErrorCondition without status set to false) are
	 * ignored.
	 * 
	 * @param deliveries
	 *            answers from a SIRI Service
	 * @return a map between the rank in the array where a status is false and
	 *         the error condition converted in SiriException
	 */
	Map<Integer, SiriException> convertToException(
			AbstractServiceDeliveryStructure[] deliveries) {
		Map<Integer, SiriException> exceptions = new HashMap<Integer, SiriException>();
		for (int i = 0; i < deliveries.length; i++) {
			AbstractServiceDeliveryStructure delivery = deliveries[i];
			if (!delivery.isStatus()) {
				ServiceDeliveryErrorConditionStructure errorCondition = delivery
						.getErrorCondition();
				SiriException e = null;
				if (errorCondition.getAccessNotAllowedError() != null) {
					AccessNotAllowedErrorStructure detail = errorCondition
							.getAccessNotAllowedError();
					e = new SiriException(
							SiriException.ERROR_CODE.ACCESS_NOT_ALLOWED_ERROR,
							detail.getErrorText());
				} else if (errorCondition.getCapabilityNotSupportedError() != null) {
					CapabilityNotSupportedErrorStructure detail = errorCondition
							.getCapabilityNotSupportedError();
					e = new SiriException(
							SiriException.ERROR_CODE.CAPABILITY_NOT_SUPPORTED_ERROR,
							detail.getErrorText());
				} else if (errorCondition.getOtherError() != null) {
					OtherErrorStructure detail = errorCondition.getOtherError();
					String message = detail.getErrorText();
					if (message.startsWith("[")) {
						// String codeName = message.substring(1,
						// message.indexOf("]"));
						SiriException.ERROR_CODE code = SiriException.ERROR_CODE.OTHER_ERROR;
						// message = message.substring(message.indexOf("]") +
						// 1);
						e = new SiriException(code, message);
					} else {
						e = new SiriException(
								SiriException.ERROR_CODE.OTHER_ERROR, message);
					}
				} else {
					e = new SiriException(SiriException.ERROR_CODE.OTHER_ERROR,
							errorCondition.toString());
				}
				exceptions.put(Integer.valueOf(i), e);
			}
		}
		return exceptions;

	}

	protected boolean isDefined(String value) {
		return value != null && !value.trim().isEmpty();
	}

}
