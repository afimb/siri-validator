package irys.client.consumer;

import irys.client.common.SiriClientUtil;
import irys.client.services.SiriErrorHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.HandlerChain;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.RequestWrapper;

import org.w3c.dom.Element;

import uk.org.siri.siri.CheckStatusResponseBodyStructure;
import uk.org.siri.siri.CheckStatusResponseBodyStructure.ErrorCondition;
import uk.org.siri.siri.ConnectionMonitoringDeliveriesStructure;
import uk.org.siri.siri.ConnectionTimetableDeliveriesStructure;
import uk.org.siri.siri.DataReadyRequestStructure;
import uk.org.siri.siri.DatedTimetableVersionFrameStructure;
import uk.org.siri.siri.DatedVehicleJourneyStructure;
import uk.org.siri.siri.DestinationRefStructure;
import uk.org.siri.siri.EstimatedCallStructure;
import uk.org.siri.siri.EstimatedTimetableDeliveriesStructure;
import uk.org.siri.siri.EstimatedTimetableDeliveryStructure;
import uk.org.siri.siri.EstimatedVehicleJourneyStructure;
import uk.org.siri.siri.EstimatedVersionFrameStructure;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.FacilityMonitoringDeliveriesStructure;
import uk.org.siri.siri.GeneralMessageDeliveriesStructure;
import uk.org.siri.siri.GeneralMessageDeliveryStructure;
import uk.org.siri.siri.GroupOfLinesRefStructure;
import uk.org.siri.siri.InfoMessageCancellationStructure;
import uk.org.siri.siri.InfoMessageStructure;
import uk.org.siri.siri.JourneyPatternRefStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredStopVisitCancellationStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.ProducerRequestEndpointStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.ProductionTimetableDeliveriesStructure;
import uk.org.siri.siri.ProductionTimetableDeliveryStructure;
import uk.org.siri.siri.RecordedCallStructure;
import uk.org.siri.siri.RouteRefStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri.SituationExchangeDeliveriesStructure;
import uk.org.siri.siri.StopMonitoringDeliveriesStructure;
import uk.org.siri.siri.StopMonitoringDeliveryStructure;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.siri.StopTimetableDeliveriesStructure;
import uk.org.siri.siri.SubscriptionTerminatedNotificationStructure;
import uk.org.siri.siri.VehicleActivityCancellationStructure;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveriesStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;
import uk.org.siri.wsdl.SiriConsumerDocPort;

import com.sun.xml.ws.developer.SchemaValidation;

@SchemaValidation(handler = SiriErrorHandler.class)
@HandlerChain(file = "handlers.xml")
@WebService(portName = "SiriConsumerDocPort", serviceName = "SiriConsumerDocServices",
		targetNamespace = "http://wsdl.siri.org.uk")
public class SiriConsumer implements SiriConsumerDocPort {

	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private HeartbeatConsumer heartBeatConsumer = null;

	private boolean notifyLog = true;

	private String siriVersion = "";

	private boolean verbose;

	// no setter annotation in this class
	@WebMethod(exclude = true)
	public void setNotifyLog(boolean value) {
		this.notifyLog = value;
	}

	@WebMethod(exclude = true)
	public void setSiriVersion(String value) {
		this.siriVersion = value;
	}

	@WebMethod(exclude = true)
	public void setVerbose(boolean value) {
		this.verbose = value;
	}

	@WebMethod(exclude = true)
	public void setHeartbeatConsumer(HeartbeatConsumer heartBeatConsumer) {
		this.heartBeatConsumer = heartBeatConsumer;
	}

	private ConcurrentHashMap<String, StopMonitoringConsumer> stopMonitoringConsumers = new ConcurrentHashMap<String, StopMonitoringConsumer>();

	@WebMethod(exclude = true)
	public void addStopMonitoringConsumer(String subscriptionRef, StopMonitoringConsumer consumer) {
		stopMonitoringConsumers.put(subscriptionRef, consumer);
	}

	@WebMethod(exclude = true)
	public void removeConsumer(String subscriptionRef) {
		stopMonitoringConsumers.remove(subscriptionRef);
	}

	@WebMethod(exclude = true)
	public boolean hasConsumers() {
		return !stopMonitoringConsumers.isEmpty();
	}

	/**
	 * 
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyDataReady", action = "NotifyDataReady")
	@Oneway
	@RequestWrapper(localName = "NotifyDataReady", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsDataReadyNotificationStructure")
	public void notifyDataReady(
			@WebParam(name = "Notification", targetNamespace = "") DataReadyRequestStructure notification, @WebParam(
					name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyDataReady()");
	}

	/**
	 * 
	 * @param siriExtension
	 * @param notification
	 * @param heartbeatNotifyInfo
	 */
	@WebMethod(operationName = "NotifyHeartbeat", action = "NotifyHeartbeat")
	@Oneway
	@RequestWrapper(localName = "NotifyHeartbeat", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsHeartbeatNotificationStructure")
	public void notifyHeartbeat(
			@WebParam(name = "HeartbeatNotifyInfo", targetNamespace = "") ProducerRequestEndpointStructure heartbeatNotifyInfo,
			@WebParam(name = "Notification", targetNamespace = "") CheckStatusResponseBodyStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		if (heartBeatConsumer != null)
			heartBeatConsumer.consume(heartbeatNotifyInfo, notification);
		else {
			Boolean status = notification.isStatus();
			System.out.println(now() + " SiriConsumer.notifyHeartbeat(" + status + ")");
			if (!status) {
				print(notification.getErrorCondition());
			}

		}
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyProductionTimetable", action = "GetProductionTimetable")
	@Oneway
	@RequestWrapper(localName = "NotifyProductionTimetable", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsProductionTimetableNotificationStructure")
	public void notifyProductionTimetable(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") ProductionTimetableDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyProductionTimetable()");
		for (ProductionTimetableDeliveryStructure delivery : notification.getProductionTimetableDelivery()) {
			String subscriptionRef = delivery.getSubscriptionRef().getValue();
			ServiceDelivery sd = new ServiceDelivery();
			sd.getProductionTimetableDelivery().add(delivery);
			save("PT", subscriptionRef, sd);

			if (!delivery.isStatus()) {
				System.out.println("Notification failed : ");
				print(delivery.getErrorCondition());

			}

			for (DatedTimetableVersionFrameStructure frame : delivery.getDatedTimetableVersionFrame()) {
				System.out.println(" LineRef : " + frame.getLineRef().getValue() + " directionRef : "
						+ frame.getDirectionRef().getValue());
				for (DatedVehicleJourneyStructure dvj : frame.getDatedVehicleJourney()) {
					System.out.println("   DatedVehicleJourney : "
							+ dvj.getFramedVehicleJourneyRef().getDatedVehicleJourneyRef() + " status "
							+ (dvj.isSetCancellation() && dvj.isCancellation() ? "cancelled" : "active"));
				}

			}
		}
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyEstimatedTimetable", action = "GetEstimatedTimetable")
	@Oneway
	@RequestWrapper(localName = "NotifyEstimatedTimetable", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsEstimatedTimetableNotificationStructure")
	public void notifyEstimatedTimetable(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") EstimatedTimetableDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyEstimatedTimetable()");
		for (EstimatedTimetableDeliveryStructure delivery : notification.getEstimatedTimetableDelivery()) {
			String subscriptionRef = delivery.getSubscriptionRef().getValue();
			ServiceDelivery sd = new ServiceDelivery();
			sd.getEstimatedTimetableDelivery().add(delivery);
			save("ET", subscriptionRef, sd);

			if (!delivery.isStatus()) {
				System.out.println("Notification failed : ");
				print(delivery.getErrorCondition());

			}
			if (verbose) {
				for (EstimatedVersionFrameStructure frame : delivery.getEstimatedJourneyVersionFrame()) {
					System.out.println("EstimatedVersionFrame : ");
					for (EstimatedVehicleJourneyStructure evj : frame.getEstimatedVehicleJourney()) {
						System.out.println("--EstimatedVehicleJourney : ");
						System.out.println("  +-- DataFrameRef        = "
								+ evj.getFramedVehicleJourneyRef().getDataFrameRef());
						System.out.println("  +-- DatedVehicleJourney = "
								+ evj.getFramedVehicleJourneyRef().getDatedVehicleJourneyRef());
						System.out.println("  +-- Line            = " + evj.getLineRef().getValue());
						System.out.println("  +-- Direction       = " + evj.getDirectionRef().getValue());
						if (evj.isSetCancellation())
							System.out.println("  +-- Cancellation    = " + evj.isCancellation());

						if (evj.isSetRecordedCalls()) {
							for (RecordedCallStructure call : evj.getRecordedCalls().getRecordedCall()) {
								System.out.println("--RecordedCall : " + call.getOrder());
								System.out.println("  +-- StopPointRef    = " + call.getStopPointRef().getValue());
								if (call.getAimedArrivalTime() != null)
									System.out.println("  +-- AimedArrivalTime      = "
											+ dateTimeFormat.format(call.getAimedArrivalTime().toGregorianCalendar().getTime()));
								if (call.getActualArrivalTime() != null)
									System.out.println("  +-- ActualArrivalTime        = "
											+ dateTimeFormat.format(call.getActualArrivalTime().toGregorianCalendar().getTime()));
								if (call.getAimedDepartureTime() != null)
									System.out.println("  +-- AimedDepartureTime    = "
											+ dateTimeFormat.format(call.getAimedDepartureTime().toGregorianCalendar().getTime()));
								if (call.getActualDepartureTime() != null)
									System.out
											.println("  +-- ActualDepartureTime      = "
													+ dateTimeFormat.format(call.getActualDepartureTime().toGregorianCalendar()
															.getTime()));
							}
						}
						if (evj.isSetEstimatedCalls()) {
							for (EstimatedCallStructure call : evj.getEstimatedCalls().getEstimatedCall()) {
								System.out.println("--EstimatedCall : " + call.getOrder());
								System.out.println("  +-- StopPointRef    = " + call.getStopPointRef().getValue());
								if (call.isSetCancellation())
									System.out.println("  +-- Cancellation    = " + call.isCancellation());

								if (call.getArrivalStatus() != null) {
									System.out.println("  +-- ArrivalStatus   = " + call.getArrivalStatus());
									if (call.getAimedArrivalTime() != null)
										System.out
												.println("  +-- AimedArrivalTime      = "
														+ dateTimeFormat.format(call.getAimedArrivalTime().toGregorianCalendar()
																.getTime()));
									if (call.getExpectedArrivalTime() != null)
										System.out.println("  +-- ExpectedArrivalTime        = "
												+ dateTimeFormat.format(call.getExpectedArrivalTime().toGregorianCalendar()
														.getTime()));
									if (call.getArrivalStopAssignment() != null) {
										if (call.getArrivalStopAssignment().getAimedQuayRef() != null)
											System.out.println("  +-- ArrivalAimedQuayRef      = "
													+ call.getArrivalStopAssignment().getAimedQuayRef().getValue());
										if (call.getArrivalStopAssignment().getActualQuayRef() != null)
											System.out.println("  +-- ArrivalActualQuayRef     = "
													+ call.getArrivalStopAssignment().getActualQuayRef().getValue());
										if (call.getArrivalStopAssignment().getExpectedQuayRef() != null)
											System.out.println("  +-- ArrivalExpectedQuayRef   = "
													+ call.getArrivalStopAssignment().getExpectedQuayRef().getValue());
									}
								}
								if (call.getArrivalProximityText() != null) {
									System.out.println("  +-- ArrivalProximityText     = "
											+ call.getArrivalProximityText().getValue());
								}
								if (call.getDepartureStatus() != null) {
									System.out.println("  +-- DepartureStatus = " + call.getDepartureStatus());
									if (call.getAimedDepartureTime() != null)
										System.out.println("  +-- AimedDepartureTime    = "
												+ dateTimeFormat.format(call.getAimedDepartureTime().toGregorianCalendar()
														.getTime()));
									if (call.getExpectedDepartureTime() != null)
										System.out.println("  +-- ExpectedDepartureTime      = "
												+ dateTimeFormat.format(call.getExpectedDepartureTime().toGregorianCalendar()
														.getTime()));
									if (call.getDepartureStopAssignment() != null) {
										if (call.getDepartureStopAssignment().getAimedQuayRef() != null)
											System.out.println("  +-- DepartureAimedQuayRef    = "
													+ call.getDepartureStopAssignment().getAimedQuayRef().getValue());
										if (call.getDepartureStopAssignment().getActualQuayRef() != null)
											System.out.println("  +-- DepartureActualQuayRef   = "
													+ call.getDepartureStopAssignment().getActualQuayRef().getValue());
										if (call.getDepartureStopAssignment().getExpectedQuayRef() != null)
											System.out.println("  +-- DepartureExpectedQuayRef = "
													+ call.getDepartureStopAssignment().getExpectedQuayRef().getValue());
									}
								}
							}
						}
						System.out.println("");
					}
				}
			} else {
				for (EstimatedVersionFrameStructure frame : delivery.getEstimatedJourneyVersionFrame()) {
					System.out.println("EstimatedVersionFrame : ");
					for (EstimatedVehicleJourneyStructure evj : frame.getEstimatedVehicleJourney()) {
						System.out.println("   EstimatedVehicleJourney : "
								+ evj.getFramedVehicleJourneyRef().getDatedVehicleJourneyRef() + " directionRef : "
								+ evj.getDirectionRef().getValue() + " status "
								+ (evj.isCancellation() != null && evj.isCancellation() ? "cancelled" : "active"));
					}

				}

			}
		}
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyStopTimetable", action = "GetStopTimetable")
	@Oneway
	@RequestWrapper(localName = "NotifyStopTimetable", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsStopTimetableNotificationStructure")
	public void notifyStopTimetable(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") StopTimetableDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyStopTimetable()");
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyStopMonitoring", action = "GetStopMonitoring")
	@Oneway
	@RequestWrapper(localName = "NotifyStopMonitoring", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsStopMonitoringNotificationStructure")
	public void notifyStopMonitoring(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") StopMonitoringDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {

		System.out.println(now() + " SiriConsumer.notifyStopMonitoring()");
		for (StopMonitoringDeliveryStructure delivery : notification.getStopMonitoringDelivery()) {
			if (!delivery.isStatus()) {
				System.out.println("Notification failed : ");
				print(delivery.getErrorCondition());

			}
			String subscriptionRef = delivery.getSubscriptionRef().getValue();
			if (stopMonitoringConsumers.containsKey(subscriptionRef)) {
				StopMonitoringConsumer consumer = stopMonitoringConsumers.get(subscriptionRef);
				consumer.consume(delivery);
			} else {
				ServiceDelivery sd = new ServiceDelivery();
				sd.getStopMonitoringDelivery().add(delivery);
				save("SM", subscriptionRef, sd);
				if (verbose) {

					for (MonitoredStopVisitCancellationStructure msvc : delivery.getMonitoredStopVisitCancellation()) {
						System.out.println("--MonitoredStopVisitCancellation : ");
						System.out.println("  +-- MonitoredRef    = " + msvc.getMonitoringRef().getValue());
						System.out.println("  +-- ItemRef         = " + msvc.getItemRef());
					}
					for (MonitoredStopVisitStructure msv : delivery.getMonitoredStopVisit()) {
						MonitoredVehicleJourneyStructure mvj = msv.getMonitoredVehicleJourney();
						MonitoredCallStructure call = mvj.getMonitoredCall();
						System.out.println("--MonitoredStopVisit : ");
						System.out.println("  +-- MonitoredRef    = " + msv.getMonitoringRef().getValue());
						System.out.println("  +-- ItemIdentifier  = " + msv.getItemIdentifier());
						System.out.println("  +-- Line            = " + mvj.getPublishedLineName().get(0).getValue());
						System.out.println("  +-- Destination     = " + mvj.getDestinationName().get(0).getValue());

						if (call == null) {
							System.out.println("  +-- ERROR   : MonitoredCall is null");
						} else {
							if (call.getArrivalStatus() != null) {
								System.out.println("  +-- ArrivalStatus   = " + call.getArrivalStatus());
								if (call.getExpectedArrivalTime() != null)
									System.out
											.println("  +-- ExpectedArrivalTime      = "
													+ dateTimeFormat.format(call.getExpectedArrivalTime().toGregorianCalendar()
															.getTime()));
								if (call.getActualArrivalTime() != null)
									System.out.println("  +-- ActualArrivalTime        = "
											+ dateTimeFormat.format(call.getActualArrivalTime().toGregorianCalendar().getTime()));
								if (call.getArrivalStopAssignment() != null) {
									if (call.getArrivalStopAssignment().getAimedQuayRef() != null)
										System.out.println("  +-- ArrivalAimedQuayRef      = "
												+ call.getArrivalStopAssignment().getAimedQuayRef().getValue());
									if (call.getArrivalStopAssignment().getActualQuayRef() != null)
										System.out.println("  +-- ArrivalActualQuayRef     = "
												+ call.getArrivalStopAssignment().getActualQuayRef().getValue());
									if (call.getArrivalStopAssignment().getExpectedQuayRef() != null)
										System.out.println("  +-- ArrivalExpectedQuayRef   = "
												+ call.getArrivalStopAssignment().getExpectedQuayRef().getValue());
								}
							}
							if (call.getArrivalProximityText() != null) {
								System.out.println("  +-- ArrivalProximityText     = "
										+ call.getArrivalProximityText().getValue());
							}
							if (call.getDepartureStatus() != null) {
								System.out.println("  +-- DepartureStatus = " + call.getDepartureStatus());
								if (call.getExpectedDepartureTime() != null)
									System.out.println("  +-- ExpectedDepartureTime    = "
											+ dateTimeFormat.format(call.getExpectedDepartureTime().toGregorianCalendar()
													.getTime()));
								if (call.getActualDepartureTime() != null)
									System.out
											.println("  +-- ActualDepartureTime      = "
													+ dateTimeFormat.format(call.getActualDepartureTime().toGregorianCalendar()
															.getTime()));
								if (call.getDepartureStopAssignment() != null) {
									if (call.getDepartureStopAssignment().getAimedQuayRef() != null)
										System.out.println("  +-- DepartureAimedQuayRef    = "
												+ call.getDepartureStopAssignment().getAimedQuayRef().getValue());
									if (call.getDepartureStopAssignment().getActualQuayRef() != null)
										System.out.println("  +-- DepartureActualQuayRef   = "
												+ call.getDepartureStopAssignment().getActualQuayRef().getValue());
									if (call.getDepartureStopAssignment().getExpectedQuayRef() != null)
										System.out.println("  +-- DepartureExpectedQuayRef = "
												+ call.getDepartureStopAssignment().getExpectedQuayRef().getValue());
								}
							}
						}
						System.out.println("");
					}
				} else {
					if (!delivery.getMonitoredStopVisitCancellation().isEmpty())
						System.out.println("--MonitoredStopVisitCancellation : ");
					for (MonitoredStopVisitCancellationStructure msvc : delivery.getMonitoredStopVisitCancellation()) {
						System.out.println("  MonitoringRef " + msvc.getMonitoringRef().getValue()
								+ " DatedVehicleJourney = " + msvc.getVehicleJourneyRef().getDatedVehicleJourneyRef()
								+ " Direction = " + msvc.getDirectionRef().getValue());
					}
					if (!delivery.getMonitoredStopVisit().isEmpty())
						System.out.println("--MonitoredStopVisit: ");
					for (MonitoredStopVisitStructure msv : delivery.getMonitoredStopVisit()) {
						System.out.println("  MonitoringRef " + msv.getMonitoringRef().getValue() + " DatedVehicleJourney = "
								+ msv.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef()
								+ " Direction = " + msv.getMonitoredVehicleJourney().getDirectionRef().getValue());
						MonitoredCallStructure mc = msv.getMonitoredVehicleJourney().getMonitoredCall();
						String quay = mc.getDepartureStopAssignment().getAimedQuayRef().getValue();
						if (mc.getDepartureStopAssignment().getExpectedQuayRef() != null)
							quay = mc.getDepartureStopAssignment().getExpectedQuayRef().getValue();
						else if (mc.getDepartureStopAssignment().getActualQuayRef() != null)
							quay = mc.getDepartureStopAssignment().getActualQuayRef().getValue();
						XMLGregorianCalendar time = mc.getExpectedArrivalTime();
						String timeLabel = ", ExpectedArrivalTime ";
						if (time == null) {
							time = mc.getActualArrivalTime();
							timeLabel = ", ActualArrivalTime ";
						}
						if (time == null) {
							time = mc.getAimedArrivalTime();
							timeLabel = ", AimedArrivalTime ";
						}
						String timeValue = "undefined";
						if (time == null) {
							timeLabel = ", ArrivalTime ";
						} else {
							timeValue = dateTimeFormat.format(time.toGregorianCalendar().getTime());
						}
						System.out.println("    monitoredCall " + mc.getStopPointRef().getValue() + ", arrivalStatus "
								+ mc.getArrivalStatus().name() + ", departureStatus " + mc.getDepartureStatus().name()
								+ ", quay " + quay + timeLabel + timeValue);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyVehicleMonitoring", action = "GetVehicleMonitoring")
	@Oneway
	@RequestWrapper(localName = "NotifyVehicleMonitoring", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsVehicleMonitoringNotificationStructure")
	public void notifyVehicleMonitoring(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") VehicleMonitoringDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyVehicleMonitoring()");

		for (VehicleMonitoringDeliveryStructure delivery : notification.getVehicleMonitoringDelivery()) {
			String subscriptionRef = delivery.getSubscriptionRef().getValue();
			ServiceDelivery sd = new ServiceDelivery();
			sd.getVehicleMonitoringDelivery().add(delivery);
			save("VM", subscriptionRef, sd);

			if (!delivery.isStatus()) {
				System.out.println("Notification failed : ");
				print(delivery.getErrorCondition());

			}
			if (verbose) {
				for (VehicleActivityCancellationStructure vac : delivery.getVehicleActivityCancellation()) {
					System.out.println("--VehicleActivityCancellation : ");
					System.out.println("  +-- VehicleMonitoringRef    = " + vac.getVehicleMonitoringRef().getValue());
					System.out.println("  +-- ItemRef                   = " + vac.getItemRef());
				}
				for (VehicleActivityStructure va : delivery.getVehicleActivity()) {
					System.out.println("--VehicleActivity : ");
					MonitoredVehicleJourneyStructure mvj = va.getMonitoredVehicleJourney();
					MonitoredCallStructure call = mvj.getMonitoredCall();
					System.out.println("--MonitoredStopVisit : ");
					System.out.println("  +-- VehicleMonitoringRef = " + va.getVehicleMonitoringRef().getValue());
					System.out.println("  +-- ItemIdentifier       = " + va.getItemIdentifier());
					System.out.println("  +-- Line                 = " + mvj.getPublishedLineName().get(0).getValue());
					System.out.println("  +-- Destination          = " + mvj.getDestinationName().get(0).getValue());
					System.out.println("  +-- LinkDistance         = " + va.getProgressBetweenStops().getLinkDistance());
					System.out.println("  +-- Percentage           = " + va.getProgressBetweenStops().getPercentage());
					if (call == null) {
						System.out.println("  +-- ERROR   : MonitoredCall is null");
					} else {
						System.out.println("  +-- StopPointRef         = " + call.getStopPointRef().getValue());
						if (call.getArrivalStatus() != null) {
							System.out.println("  +-- ArrivalStatus        = " + call.getArrivalStatus());
							if (call.getExpectedArrivalTime() != null)
								System.out.println("  +-- ExpectedArrivalTime      = "
										+ dateTimeFormat.format(call.getExpectedArrivalTime().toGregorianCalendar().getTime()));
							if (call.getActualArrivalTime() != null)
								System.out.println("  +-- ActualArrivalTime        = "
										+ dateTimeFormat.format(call.getActualArrivalTime().toGregorianCalendar().getTime()));
							if (call.getArrivalStopAssignment() != null) {
								if (call.getArrivalStopAssignment().getAimedQuayRef() != null)
									System.out.println("  +-- ArrivalAimedQuayRef      = "
											+ call.getArrivalStopAssignment().getAimedQuayRef().getValue());
								if (call.getArrivalStopAssignment().getActualQuayRef() != null)
									System.out.println("  +-- ArrivalActualQuayRef     = "
											+ call.getArrivalStopAssignment().getActualQuayRef().getValue());
								if (call.getArrivalStopAssignment().getExpectedQuayRef() != null)
									System.out.println("  +-- ArrivalExpectedQuayRef   = "
											+ call.getArrivalStopAssignment().getExpectedQuayRef().getValue());
							}
						}
						if (call.getArrivalProximityText() != null) {
							System.out
									.println("  +-- ArrivalProximityText     = " + call.getArrivalProximityText().getValue());
						}
						if (call.getDepartureStatus() != null) {
							System.out.println("  +-- DepartureStatus      = " + call.getDepartureStatus());
							if (call.getExpectedDepartureTime() != null)
								System.out.println("  +-- ExpectedDepartureTime    = "
										+ dateTimeFormat.format(call.getExpectedDepartureTime().toGregorianCalendar().getTime()));
							if (call.getActualDepartureTime() != null)
								System.out.println("  +-- ActualDepartureTime      = "
										+ dateTimeFormat.format(call.getActualDepartureTime().toGregorianCalendar().getTime()));
							if (call.getDepartureStopAssignment() != null) {
								if (call.getDepartureStopAssignment().getAimedQuayRef() != null)
									System.out.println("  +-- DepartureAimedQuayRef    = "
											+ call.getDepartureStopAssignment().getAimedQuayRef().getValue());
								if (call.getDepartureStopAssignment().getActualQuayRef() != null)
									System.out.println("  +-- DepartureActualQuayRef   = "
											+ call.getDepartureStopAssignment().getActualQuayRef().getValue());
								if (call.getDepartureStopAssignment().getExpectedQuayRef() != null)
									System.out.println("  +-- DepartureExpectedQuayRef = "
											+ call.getDepartureStopAssignment().getExpectedQuayRef().getValue());
							}
						}
					}
					System.out.println("");
				}
			} else {
				if (!delivery.getVehicleActivityCancellation().isEmpty())
					System.out.println("--VehicleActivityCancellation : ");
				for (VehicleActivityCancellationStructure vac : delivery.getVehicleActivityCancellation()) {
					System.out.println("  VehicleMonitoringRef " + vac.getVehicleMonitoringRef().getValue()
							+ "DatedVehicleJourney = " + vac.getVehicleJourneyRef().getDatedVehicleJourneyRef());
				}
				if (!delivery.getVehicleActivity().isEmpty())
					System.out.println("--VehicleActivity: ");
				for (VehicleActivityStructure va : delivery.getVehicleActivity()) {
					System.out.println("  VehicleMonitoringRef " + va.getVehicleMonitoringRef().getValue()
							+ "DatedVehicleJourney = "
							+ va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef());
					System.out.println("    monitoredCall "
							+ va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef().getValue());
				}
			}
		}
	}

	private void print(ErrorCondition errorCondition) {
		if (errorCondition == null) {
			System.out.println("ErrorCondition null !!");
			return;
		}
		if (errorCondition.isSetDescription()) {
			System.out.println("Description :" + errorCondition.getDescription().getValue());
		}
		if (errorCondition.isSetOtherError()) {
			System.out.println("OtherError :" + errorCondition.getOtherError().getErrorText());
		}
		if (errorCondition.isSetServiceNotAvailableError()) {
			System.out.println("ServiceNotAvailableError :" + errorCondition.getServiceNotAvailableError().getErrorText());
		}

	}

	private void print(ServiceDeliveryErrorConditionStructure errorCondition) {

		if (errorCondition.isSetAccessNotAllowedError()) {
			System.out.println("AccessNotAllowedError :" + errorCondition.getAccessNotAllowedError().getErrorText());
		}
		if (errorCondition.isSetAllowedResourceUsageExceededError()) {
			System.out.println("AllowedResourceUsageExceededError :"
					+ errorCondition.getAllowedResourceUsageExceededError().getErrorText());
		}
		if (errorCondition.isSetBeyondDataHorizon()) {
			System.out.println("BeyondDataHorizon :" + errorCondition.getBeyondDataHorizon().getErrorText());
		}
		if (errorCondition.isSetCapabilityNotSupportedError()) {
			System.out.println("CapabilityNotSupportedError :"
					+ errorCondition.getCapabilityNotSupportedError().getErrorText());
		}
		if (errorCondition.isSetEndpointDeniedAccessError()) {
			System.out.println("EndpointDeniedAccessError :"
					+ errorCondition.getEndpointDeniedAccessError().getErrorText());
		}
		if (errorCondition.isSetEndpointNotAvailableAccessError()) {
			System.out.println("EndpointNotAvailableAccessError :"
					+ errorCondition.getEndpointNotAvailableAccessError().getErrorText());
		}
		if (errorCondition.isSetInvalidDataReferencesError()) {
			System.out.println("InvalidDataReferencesError :"
					+ errorCondition.getInvalidDataReferencesError().getErrorText());
		}
		if (errorCondition.isSetNoInfoForTopicError()) {
			System.out.println("NoInfoForTopicError :" + errorCondition.getNoInfoForTopicError().getErrorText());
		}
		if (errorCondition.isSetOtherError()) {
			System.out.println("OtherError :" + errorCondition.getOtherError().getErrorText());
		}
		if (errorCondition.isSetParametersIgnoredError()) {
			System.out.println("ParametersIgnoredError :" + errorCondition.getParametersIgnoredError().getErrorText());
		}
		if (errorCondition.isSetServiceNotAvailableError()) {
			System.out.println("ServiceNotAvailableError :" + errorCondition.getServiceNotAvailableError().getErrorText());
		}
		if (errorCondition.isSetUnapprovedKeyAccessError()) {
			System.out.println("UnapprovedKeyAccessError :" + errorCondition.getUnapprovedKeyAccessError().getErrorText());
		}
		if (errorCondition.isSetUnknownEndpointError()) {
			System.out.println("UnknownEndpointError :" + errorCondition.getUnknownEndpointError().getErrorText());
		}
		if (errorCondition.isSetUnknownExtensionsError()) {
			System.out.println("UnknownExtensionsError :" + errorCondition.getUnknownExtensionsError().getErrorText());
		}
		if (errorCondition.isSetUnknownParticipantError()) {
			System.out.println("UnknownParticipantError :" + errorCondition.getUnknownParticipantError().getErrorText());
		}
		if (errorCondition.isSetDescription()) {
			System.out.println("Description :" + errorCondition.getDescription().getValue());
		}

		return;
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyConnectionTimetable", action = "GetConnectionTimetable")
	@Oneway
	@RequestWrapper(localName = "NotifyConnectionTimetable", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsConnectionTimetableNotificationStructure")
	public void notifyConnectionTimetable(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") ConnectionTimetableDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyConnectionTimetable()");
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyConnectionMonitoring", action = "GetConnectionMonitoring")
	@Oneway
	@RequestWrapper(localName = "NotifyConnectionMonitoring", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsConnectionMonitoringNotificationStructure")
	public void notifyConnectionMonitoring(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") ConnectionMonitoringDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyConnectionMonitoring()");
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyGeneralMessage", action = "GetGeneralMessage")
	@Oneway
	@RequestWrapper(localName = "NotifyGeneralMessage", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsGeneralMessageNotificationStructure")
	public void notifyGeneralMessage(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") GeneralMessageDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyGeneralMessage()");
		System.out.println("delivery size = " + notification.getGeneralMessageDelivery().size());
		for (GeneralMessageDeliveryStructure delivery : notification.getGeneralMessageDelivery()) {
			String subscriptionRef = delivery.getSubscriptionRef().getValue();
			ServiceDelivery sd = new ServiceDelivery();
			sd.getGeneralMessageDelivery().add(delivery);
			save("GM", subscriptionRef, sd);
			if (!delivery.isStatus()) {
				System.out.println("Notification failed : ");
				print(delivery.getErrorCondition());

			}

			for (InfoMessageCancellationStructure infoMessage : delivery.getGeneralMessageCancellation()) {
				System.out.println("--InfoMessageCancellation : ");
				System.out.println("  +-- InfoMessageIdentifier = " + infoMessage.getInfoMessageIdentifier());
				System.out.println("  +-- InfoChannel           = " + infoMessage.getInfoChannelRef().getValue());
				System.out.println("  +-- ItemRef               = " + infoMessage.getItemRef());

			}
			for (InfoMessageStructure infoMessage : delivery.getGeneralMessage()) {

				System.out.println("--InfoMessage : ");
				System.out.println("  +-- InfoMessageIdentifier = " + infoMessage.getInfoMessageIdentifier());
				System.out.println("  +-- ItemIdentifier        = " + infoMessage.getItemIdentifier());
				System.out.println("  +-- InfoChannel           = " + infoMessage.getInfoChannelRef().getValue());
				System.out.println("  +-- ValidUntilTime        = "
						+ dateTimeFormat.format(infoMessage.getValidUntilTime().toGregorianCalendar().getTime()));
				// extension
				Element content = (Element) infoMessage.getContent();
				if (content != null) {
					try {
						if (siriVersion.endsWith("2.4")) {
							printGM24(content);
						} else {
							printGM22(content);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else
					System.out.println("no content");
			}
		}

	}

	/**
	 * @param content
	 * @throws Exception
	 */
	private void printGM22(Element content) throws Exception {
		IDFAdapter22 adapter = new IDFAdapter22();
		uk.org.siri.siri.IDFGeneralMessageStructure idfContent = adapter.unmarshal(content);
		for (uk.org.siri.siri.IDFMessageStructure message : idfContent.getMessage()) {
			System.out.println("  +-- MessageType        = " + message.getMessageType().name());
			System.out.println("  +-- MessageText        = " + message.getMessageText());
		}
		for (LineRefStructure ref : idfContent.getLineRef()) {
			System.out.println("  +-- LineRef        = " + ref.getValue());
		}
		for (StopPointRefStructure ref : idfContent.getStopPointRef()) {
			System.out.println("  +-- StopPointRef        = " + ref.getValue());
		}
		for (JourneyPatternRefStructure ref : idfContent.getJourneyPatternRef()) {
			System.out.println("  +-- JourneyPatternRef        = " + ref.getValue());
		}
		for (RouteRefStructure ref : idfContent.getRouteRef()) {
			System.out.println("  +-- RouteRef        = " + ref.getValue());
		}
	}

	/**
	 * @param content
	 * @throws Exception
	 */
	private void printGM24(Element content) throws Exception {
		IDFAdapter24 adapter = new IDFAdapter24();
		uk.org.siri.wsdl.siri.IDFGeneralMessageStructure idfContent = adapter.unmarshal(content);
		for (uk.org.siri.wsdl.siri.IDFMessageStructure message : idfContent.getMessage()) {
			System.out.println("  +-- MessageType         = " + message.getMessageType().name());
			System.out.println("  +-- MessageText         = " + message.getMessageText());
			if (message.getNumberOfLines() != null)
				System.out.println("  +-- NumberOfLines       = " + message.getNumberOfLines());
			if (message.getNumberOfCharPerLine() != null)
				System.out.println("  +-- NumberOfCharPerLine = " + message.getNumberOfCharPerLine());
		}
		for (LineRefStructure ref : idfContent.getLineRef()) {
			System.out.println("  +-- LineRef           = " + ref.getValue());
		}
		for (StopPointRefStructure ref : idfContent.getStopPointRef()) {
			System.out.println("  +-- StopPointRef      = " + ref.getValue());
		}
		for (JourneyPatternRefStructure ref : idfContent.getJourneyPatternRef()) {
			System.out.println("  +-- JourneyPatternRef = " + ref.getValue());
		}
		for (RouteRefStructure ref : idfContent.getRouteRef()) {
			System.out.println("  +-- RouteRef          = " + ref.getValue());
		}
		for (DestinationRefStructure ref : idfContent.getDestinationRef()) {
			System.out.println("  +-- DestinationRef    = " + ref.getValue());
		}
		for (GroupOfLinesRefStructure ref : idfContent.getGroupOfLinesRef()) {
			System.out.println("  +-- GroupOfLinesRef   = " + ref.getValue());
		}
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifyFacilityMonitoring", action = "GetFacilityMonitoring")
	@Oneway
	@RequestWrapper(localName = "NotifyFacilityMonitoring", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsFacilityMonitoringNotificationStructure")
	public void notifyFacilityMonitoring(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") FacilityMonitoringDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifyFacilityMonitoring()");
	}

	/**
	 * 
	 * @param serviceDeliveryInfo
	 * @param siriExtension
	 * @param notification
	 */
	@WebMethod(operationName = "NotifySituationExchange", action = "GetSituationExchange")
	@Oneway
	@RequestWrapper(localName = "NotifySituationExchange", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsSituationExchangeNotificationStructure")
	public void notifySituationExchange(
			@WebParam(name = "ServiceDeliveryInfo", targetNamespace = "") ProducerResponseEndpointStructure serviceDeliveryInfo,
			@WebParam(name = "Notification", targetNamespace = "") SituationExchangeDeliveriesStructure notification,
			@WebParam(name = "SiriExtension", targetNamespace = "") ExtensionsStructure siriExtension) {
		System.out.println(now() + " SiriConsumer.notifySituationExchange()");
	}

	@Override
	@WebMethod(operationName = "NotifySubscriptionTerminated", action = "NotifySubscriptionTerminated")
	@Oneway
	@RequestWrapper(localName = "NotifySubscriptionTerminated", targetNamespace = "http://wsdl.siri.org.uk",
			className = "uk.org.siri.wsdl.WsSubscriptionTerminatedNotificationStructure")
	public void notifySubscriptionTerminated(
			@WebParam(name = "Notification", targetNamespace = "") SubscriptionTerminatedNotificationStructure notification) {
		System.out.println("SiriConsumer.notifySubscriptionTerminated()");

	}

	private synchronized String now() {
		return dateTimeFormat.format(new Date());
	}

	Map<String, JAXBContext> contexts = new HashMap<String, JAXBContext>();

	private void save(String service, String subscriptionRef, ServiceDelivery data) {
		if (!notifyLog)
			return;
		String name = subscriptionRef.replaceAll(":", "-");
		File f = new File(service + "_" + name + "_" + SiriClientUtil.nowFile() + ".xml");
		try {
			JAXBContext context = null;
			synchronized (contexts) {
				context = contexts.get(data.getClass().getName());
				if (context == null) {
					context = JAXBContext.newInstance(data.getClass());
					contexts.put(data.getClass().getName(), context);
				}
			}
			FileOutputStream stream = new FileOutputStream(f, true);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); // NOI18N
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(data, stream);
			stream.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
