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

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;

import lombok.Getter;
import lombok.Setter;
import uk.org.siri.siri.DestinationRefStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.GeneralMessageDeliveriesStructure;
import uk.org.siri.siri.GeneralMessageRequestStructure;
import uk.org.siri.siri.GroupOfLinesRefStructure;
import uk.org.siri.siri.InfoChannelRefStructure;
import uk.org.siri.siri.JourneyPatternRefStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.RouteRefStructure;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.wsdl.GeneralMessageError;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

/**
 * Methods required to implement a General Messaging Service Proxy
 * <p/>
 * <b>Note upon infochannel values :</b><br/>
 * SIRI France Requirement is ambiguous on InfoChannel coding and 2
 * possibilities may be encountered on SIRI servers :
 * 
 * <ol>
 * <li>String coded : Perturbation, Information and Commercial</li>
 * <li>Numeric coded : 1, 2, 3</li>
 * </ol>
 * 
 * The SIRI client interface may manage number encoding for transparency on
 * client side, a specific parameter <i>isInfoChannelEncoded</i> is available
 * for this purpose<br/>
 * If not used, the client side has to manage the infochannel values, the proxy
 * make no control
 * 
 * @author michel
 * 
 */

public class GeneralMessageClient extends ServiceClient {

	/**
	 * Allowed types for General Message affectation filtering
	 */
	public enum IDFItemRefFilterType {
		/**
		 * no affectation filtering required
		 */
		None,
		/**
		 * journeyPattern filtering
		 */
		JourneyPatternRef,
		/**
		 * lines filtering
		 */
		LineRef,
		/**
		 * stop points filtering
		 */
		StopRef,
		/**
		 * route filtering
		 */
		RouteRef,
		/**
		 * destination filtering
		 */
		DestinationRef,
		/**
		 * group Of Line filtering
		 */
		GroupOfLineRef
	};


	/**
	 * invoke GeneralMessageService on a declared SIRI server
	 * 
	 * @param serverId
	 *            the key used to fond the server's specific parameters in
	 *            configuration files
	 * @param request
	 *            a previous prepared SIRI request
	 * @return the SIRI response in SIRI XSD XMLBeans mapping format
	 * @throws SiriException
	 *             server or transport failure
	 */
	public void getResponseDocument(
			WsServiceRequestInfoStructure serviceRequestInfo,
			GeneralMessageRequestStructure request,
			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo,
			Holder<GeneralMessageDeliveriesStructure> answer)
			throws SiriException {
		ExtensionsStructure requestExtension = factory
				.createExtensionsStructure();
		Holder<ExtensionsStructure> answerExtension = new Holder<ExtensionsStructure>();
		try {
			port.getGeneralMessage(serviceRequestInfo, request,
					requestExtension, serviceDeliveryInfo, answer,
					answerExtension);
			
		} catch (GeneralMessageError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * prepare a GeneralMessageRequest for recurrent usage
	 * 
	 * @param serverId
	 *            the key used to fond the server's specific parameters in
	 *            configuration files
	 * @param infoChannels
	 *            filter on a list of Info Channels
	 * @param language
	 *            filter on a specific language (FR, EN, ...)
	 * @param extensionFilterTypes
	 *            filters on affectation : set the reference types
	 * @param itemRefs
	 *            filter on affectation : list of asked affectations by
	 *            reference type
	 * @return the SIRI request in SIRI XSD XMLBeans mapping format
	 * @throws SiriException
	 *             unknown serverId
	 */
	public GeneralMessageRequestStructure buildRequest(String version,
			List<String> infoChannels, String language,
			List<IDFItemRefFilterType> extensionFilterTypes,
			Map<String, List<String>> itemRefs) throws SiriException {
		GeneralMessageRequestStructure request = factory
				.createGeneralMessageRequestStructure();

		request.setRequestTimestamp(xmlFactory
				.newXMLGregorianCalendar(new GregorianCalendar()));

		if (version != null) {
			request.setVersion(version);
		} else {
			request.setVersion(this.version);
		}

		if (infoChannels != null) {
			for (String channel : infoChannels) {
				InfoChannelRefStructure infoChannel = factory
						.createInfoChannelRefStructure();
				infoChannel.setValue(channel);
				request.getInfoChannelRef().add(infoChannel);
			}
		}
		if (isDefined(language)) {
			request.setLanguage(language);
		}
		if (extensionFilterTypes.size() > 0) {
			if (version.endsWith("2.4"))
				populateExtension2_4(extensionFilterTypes, itemRefs, request);
			else
			    populateExtension2_2(extensionFilterTypes, itemRefs, request);
		}
		return request;
	}

	/**
	 * @param extensionFilterTypes
	 * @param itemRefs
	 * @param request
	 */
	private void populateExtension2_2(
			List<IDFItemRefFilterType> extensionFilterTypes,
			Map<String, List<String>> itemRefs,
			GeneralMessageRequestStructure request) {
		uk.org.siri.siri.IDFGeneralMessageRequestFilterStructure idfExtension = factory
				.createIDFGeneralMessageRequestFilterStructure();
		if (extensionFilterTypes.contains(IDFItemRefFilterType.LineRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.LineRef
					.toString())) {
				LineRefStructure ref = factory.createLineRefStructure();
				ref.setValue(item);
				idfExtension.getLineRef().add(ref);
			}
		}
		if (extensionFilterTypes
				.contains(IDFItemRefFilterType.JourneyPatternRef)) {
			for (String item : itemRefs
					.get(IDFItemRefFilterType.JourneyPatternRef.toString())) {
				JourneyPatternRefStructure ref = factory
						.createJourneyPatternRefStructure();
				ref.setValue(item);
				idfExtension.getJourneyPatternRef().add(ref);
			}

		}
		if (extensionFilterTypes.contains(IDFItemRefFilterType.StopRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.StopRef
					.toString())) {
				StopPointRefStructure ref = factory
						.createStopPointRefStructure();
				ref.setValue(item);
				idfExtension.getStopPointRef().add(ref );
			}
		}
		
		if (extensionFilterTypes.contains(IDFItemRefFilterType.RouteRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.RouteRef
					.toString())) {
				RouteRefStructure ref= factory
						.createRouteRefStructure();
				ref.setValue(item);
				idfExtension.getRouteRef().add(ref);
			}
		}
		ExtensionsStructure extension = factory.createExtensionsStructure();
		JAXBElement<uk.org.siri.siri.IDFGeneralMessageRequestFilterStructure> idfExtElement = factory
				.createIDFGeneralMessageRequestFilter(idfExtension);
		extension.getAny().add(idfExtElement);
		request.setExtensions(extension);
	}
	/**
	 * @param extensionFilterTypes
	 * @param itemRefs
	 * @param request
	 */
	private void populateExtension2_4(
			List<IDFItemRefFilterType> extensionFilterTypes,
			Map<String, List<String>> itemRefs,
			GeneralMessageRequestStructure request) {
		uk.org.siri.wsdl.siri.IDFGeneralMessageRequestFilterStructure idfExtension = idfFactory
				.createIDFGeneralMessageRequestFilterStructure();
		if (extensionFilterTypes.contains(IDFItemRefFilterType.LineRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.LineRef
					.toString())) {
				LineRefStructure ref = factory.createLineRefStructure();
				ref.setValue(item);
				idfExtension.getLineRef().add(ref);
			}
		}
		if (extensionFilterTypes
				.contains(IDFItemRefFilterType.JourneyPatternRef)) {
			for (String item : itemRefs
					.get(IDFItemRefFilterType.JourneyPatternRef.toString())) {
				JourneyPatternRefStructure ref = factory
						.createJourneyPatternRefStructure();
				ref.setValue(item);
				idfExtension.getJourneyPatternRef().add(ref);
			}

		}
		if (extensionFilterTypes.contains(IDFItemRefFilterType.StopRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.StopRef
					.toString())) {
				StopPointRefStructure ref = factory
						.createStopPointRefStructure();
				ref.setValue(item);
				idfExtension.getStopPointRef().add(ref );
			}
		}
		
		if (extensionFilterTypes.contains(IDFItemRefFilterType.RouteRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.RouteRef
					.toString())) {
				RouteRefStructure ref= factory
						.createRouteRefStructure();
				ref.setValue(item);
				idfExtension.getRouteRef().add(ref);
			}
		}
		if (extensionFilterTypes.contains(IDFItemRefFilterType.DestinationRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.DestinationRef
					.toString())) {
				DestinationRefStructure ref= factory
						.createDestinationRefStructure();
				ref.setValue(item);
				idfExtension.getDestinationRef().add(ref);
			}
		}
		if (extensionFilterTypes.contains(IDFItemRefFilterType.GroupOfLineRef)) {
			for (String item : itemRefs.get(IDFItemRefFilterType.GroupOfLineRef
					.toString())) {
				GroupOfLinesRefStructure ref= factory
						.createGroupOfLinesRefStructure();
				ref.setValue(item);
				idfExtension.getGroupOfLinesRef().add(ref);
			}
		}
		ExtensionsStructure extension = factory.createExtensionsStructure();
		JAXBElement<uk.org.siri.wsdl.siri.IDFGeneralMessageRequestFilterStructure> idfExtElement = idfFactory
				.createIDFGeneralMessageRequestFilter(idfExtension);
		extension.getAny().add(idfExtElement);
		request.setExtensions(extension);
	}

}
