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
package irys.client.command;

import irys.client.common.SiriClientUtil;
import irys.client.services.ServiceClient;
import irys.client.services.SiriException;
import irys.client.services.StopMonitoringClient;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.xml.sax.SAXParseException;

import lombok.Setter;

import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.StopMonitoringDeliveriesStructure;
import uk.org.siri.siri.StopMonitoringRequestStructure;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

public class SMCommand extends AbstractCommand {

	private String version = null;
	private String stopId = null;
	private String lineId = null;
	private String destId = null;
	private String directionId = null;
	private String operatorId = null;
	private String start = null;
	private String typeVisit = null;
	private String detailLevel = null;
	private int preview = ServiceClient.UNDEFINED_NUMBER;
	private int maxStop = ServiceClient.UNDEFINED_NUMBER;
	private int minStLine = ServiceClient.UNDEFINED_NUMBER;
	private Long maxPrevious = null;
	private Long maxOnwards = null;

	private String requestFileName = "SMRequest";
	private String responseFileName = "SMResponse";

	private @Setter
	StopMonitoringClient service;

	public SMCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		long startTime = System.currentTimeMillis();

		try {
			StopMonitoringRequestStructure request = service.buildRequest(version, stopId, lineId, directionId, destId,
					operatorId, start, preview, typeVisit, maxStop, minStLine, maxPrevious, maxOnwards, detailLevel);
			WsServiceRequestInfoStructure serviceRequestInfo = service.buildWsServiceRequestInfoStructure();
			MessageQualifierStructure id = serviceRequestInfo.getMessageIdentifier();
			request.setMessageIdentifier(id);

			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<StopMonitoringDeliveriesStructure> answer = new Holder<StopMonitoringDeliveriesStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.getResponseDocument(serviceRequestInfo, request, serviceDeliveryInfo, answer);
			if (isErrors()) {
				System.out.println("StopMonitoring response has errors; see report or log for precisions");
			}
			if (isWarnings()) {
				System.out.println("StopMonitoring response has warnings; see report or log for precisions");
			}

			if (!verbose) {
				System.out.println("StopMonitoring executed");
			}

		} catch (Exception e) {
			if (e instanceof WebServiceException) {
				Throwable cause = e.getCause();
				if (cause instanceof SAXParseException) {
					// log validation
				} else {
					// log web service error
				}
			} else {
				// log other problem
			}
			e.printStackTrace();
		} catch (Error e) {
			// log failure
			e.printStackTrace();
		} finally {
			long endTime = System.currentTimeMillis();
			long duree = endTime - startTime;
			System.out.println("durée = " + getTimeAsString(duree));
		}

	}

	public void printHelp(String errorMsg) {
		if (errorMsg.length() > 0) {
			System.out.println("ERREUR DE SYNTAXE : " + errorMsg);
			System.out.println("");
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("SMClient -StopId [stopId] ");
		System.out.println("                  [-LineId lineId](0/1) ");
		System.out.println("                  [-DestId stopId](0/1) ");
		System.out.println("                  [-DirectionId dirId](0/1) ");
		System.out.println("                  [-OperatorId operatorId](0/1) ");
		System.out.println("                  [-Start time](0/1) ");
		System.out.println("                  [-Preview duration](0/1) (in minutes)");
		System.out.println("                  [-TypeVisit all|arrivals|departures](0/1) ");
		System.out.println("                  [-MaxStop maximumStopPoint](0/1) ");
		System.out.println("                  [-MinStLine minimumStopPointPerLine](0/1) ");
		System.out.println("                  [-Onward nbCalls](0/1), 0 for all");
		System.out.println("                  [-Previous nbCalls](0/1), 0 for all ");
		System.out.println("                  [-DetailLevel M(inimum),B(asic),N(ormal),C(alls),F(ull)](0/1) ");
		System.out.println("");
		System.out.println(" stopId = [operatorCode]:StopPoint:[SP|BP|Q]]:[technicalId][:LOC]");
		System.out.println(" lineId = [operatorCode]:Line:[technicalId][:LOC]");
		System.out.println(" dirId = [Left|Right]");
		System.out.println(" operatorId = [operatorCode]:Company:[technicalId][:LOC]");
		System.out.println(" time = HH:MM");
		System.out.println(" duration = MMM");
		System.out.println("");
		super.printHelp("SM");

	}

	private void clearArgs() {
		version = null;
		stopId = null;
		lineId = null;
		destId = null;
		operatorId = null;
		directionId = null;
		start = null;
		typeVisit = null;
		detailLevel = null;
		preview = ServiceClient.UNDEFINED_NUMBER;
		maxStop = ServiceClient.UNDEFINED_NUMBER;
		minStLine = ServiceClient.UNDEFINED_NUMBER;
		maxPrevious = null;
		maxOnwards = null;

	}

	public boolean parseArgs(String[] args) {

		boolean bstopId = false;
		boolean blineId = false;
		boolean bdestId = false;
		boolean bdirId = false;
		boolean boperatorId = false;
		boolean bstart = false;
		boolean bpreview = false;
		boolean btypeVisit = false;
		boolean bmaxStop = false;
		boolean bminStLine = false;
		boolean bOnward = false;
		boolean bPrevious = false;
		boolean bDir = false;
		boolean bFileIn = false;
		boolean bFileOut = false;
		boolean bDetailLevel = false;

		clearArgs();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-in")) {
				if (bFileIn) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bFileIn = true;
				if ((i + 1) < args.length) {
					requestFileName = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-out")) {
				if (bFileOut) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bFileIn = true;
				if ((i + 1) < args.length) {
					responseFileName = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-d")) {
				if (bDir) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bDir = true;
				if ((i + 1) < args.length) {
					setOutDirectory(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-StopId")) {
				if (bstopId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bstopId = true;
				if ((i + 1) < args.length) {
					stopId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-LineId")) {
				if (blineId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				blineId = true;
				if ((i + 1) < args.length) {
					lineId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-DirectionId")) {
				if (bdirId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bdirId = true;
				if ((i + 1) < args.length) {
					directionId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-DestId")) {
				if (bdestId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bdestId = true;
				if ((i + 1) < args.length) {
					destId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-OperatorId")) {
				if (boperatorId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				boperatorId = true;
				if ((i + 1) < args.length) {
					operatorId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-Start")) {
				if (bstart) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bstart = true;
				if ((i + 1) < args.length) {
					start = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-Preview")) {
				if (bpreview) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bpreview = true;
				if ((i + 1) < args.length) {
					preview = Integer.parseInt(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-TypeVisit")) {
				if (btypeVisit) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				btypeVisit = true;
				if ((i + 1) < args.length) {
					typeVisit = args[++i].toLowerCase();
					if (!typeVisit.equals("all") && !typeVisit.equals("arrivals") && !typeVisit.equals("departures")) {
						printHelp("argument(s) invalide(s) pour '-TypeVisit'");
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-MaxStop")) {
				if (bmaxStop) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bmaxStop = true;
				if ((i + 1) < args.length) {
					maxStop = Integer.parseInt(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-MinStLine")) {
				if (bminStLine) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bminStLine = true;
				if ((i + 1) < args.length) {
					minStLine = Integer.parseInt(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-Previous")) {
				if (bPrevious) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bPrevious = true;
				if ((i + 1) < args.length) {
					maxPrevious = Long.parseLong(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-Onward")) {
				if (bOnward) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bOnward = true;
				if ((i + 1) < args.length) {
					maxOnwards = Long.parseLong(args[++i]);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-DetailLevel")) {
				if (bDetailLevel) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bDetailLevel = true;
				if ((i + 1) < args.length) {
					String dl = args[++i];
					detailLevel = dl.substring(0, 1).toLowerCase();
					if (detailLevel.equals("m"))
						detailLevel = "minimum";
					else if (detailLevel.equals("b"))
						detailLevel = "basic";
					else if (detailLevel.equals("n"))
						detailLevel = "normal";
					else if (detailLevel.equals("c"))
						detailLevel = "calls";
					else if (detailLevel.equals("f"))
						detailLevel = "full";
					else {
						printHelp("argument inconnu pour '-DetailLevel' " + dl);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-v")) {
				// mode bavard
				verbose = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-nv")) {
				verbose = false;
				continue;
			}

			if (args[i].startsWith("-h")) {
				// aide : on sort
				printHelp("");
				return false;
			}
		}

		if (!bstopId) {
			printHelp("StopId non fourni ");
			return false;
		}
		return true;
	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) throws SiriException {
		if (!parseArgs(args))
			return null;

		AbstractServiceRequestStructure request = service.buildRequest(version, stopId, lineId, directionId, destId,
				operatorId, start, preview, typeVisit, maxStop, minStLine, maxPrevious, maxOnwards, detailLevel);
		return request;

	}

}
