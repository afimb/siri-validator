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
import irys.client.services.GeneralMessageClient;
import irys.client.services.GeneralMessageClient.IDFItemRefFilterType;
import irys.client.services.SiriException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.GeneralMessageDeliveriesStructure;
import uk.org.siri.siri.GeneralMessageRequestStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

public class GMCommand extends AbstractCommand {

	private String version = null;
	private List<String> infoChannels = new ArrayList<String>();
	private String language = null;
	private Map<String, List<String>> itemRefs = new HashMap<String, List<String>>();
	private List<IDFItemRefFilterType> extensionFilterTypes = new ArrayList<IDFItemRefFilterType>();

	private @Setter
	GeneralMessageClient service;

	private String requestFileName = "GMRequest";
	private String responseFileName = "GMResponse";

	/**
    * 
    */
	public GMCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;
		try {
			GeneralMessageRequestStructure request = service.buildRequest(version, infoChannels, language,
					extensionFilterTypes, itemRefs);
			WsServiceRequestInfoStructure serviceRequestInfo = service.buildWsServiceRequestInfoStructure();
			MessageQualifierStructure id = serviceRequestInfo.getMessageIdentifier();
			request.setMessageIdentifier(id);

			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<GeneralMessageDeliveriesStructure> answer = new Holder<GeneralMessageDeliveriesStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.getResponseDocument(serviceRequestInfo, request, serviceDeliveryInfo, answer);
			if (isErrors()) {
				System.out.println("GeneralMessage response has errors; see report or log for precisions");
			}
			if (isWarnings()) {
				System.out.println("GeneralMessage response has warnings; see report or log for precisions");
			}

			if (!verbose) {
				System.out.println("GeneralMessage executed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	private boolean parseArgs(String[] args) {
		version = null;
		infoChannels.clear();
		language = null;
		itemRefs.clear();
		extensionFilterTypes.clear();
		boolean bIn = false;
		boolean bOut = false;

		setOutDirectory(null);

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-in")) {
				if ((i + 1) < args.length) {
					if (!bIn) {
						requestFileName = args[++i];
						bIn = true;
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-out")) {
				if ((i + 1) < args.length) {
					if (!bOut) {
						responseFileName = args[++i];
						bOut = true;
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-d")) {
				if ((i + 1) < args.length) {
					if (getOutDirectory() == null) {
						setOutDirectory(args[++i]);
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-InfoChannel")) {
				if ((i + 1) < args.length) {
					if (infoChannels.isEmpty()) {
						infoChannels.addAll(Arrays.asList(args[++i].split(",")));
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-RouteRef")) {
				if (itemRefs.containsKey(IDFItemRefFilterType.StopRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.LineRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.JourneyPatternRef.toString())) {
					printHelp("Option '-RouteRef' in conflit with '-StopRef', '-LineRef' or '-JourneyPatternRef'");
					return false;
				}
				if ((i + 1) < args.length) {
					if (itemRefs.get(IDFItemRefFilterType.RouteRef.toString()) == null) {
						extensionFilterTypes.add(IDFItemRefFilterType.RouteRef);
						itemRefs.put(IDFItemRefFilterType.RouteRef.toString(), new ArrayList<String>());
						itemRefs.get(IDFItemRefFilterType.RouteRef.toString()).addAll(Arrays.asList(args[++i].split(",")));
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-LineRef")) {
				if (itemRefs.containsKey(IDFItemRefFilterType.StopRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.RouteRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.JourneyPatternRef.toString())) {
					printHelp("Option '-LineRef' in conflit with '-StopRef', '-RouteRef' or '-JourneyPatternRef'");
					return false;
				}
				if ((i + 1) < args.length) {
					if (!itemRefs.containsKey(IDFItemRefFilterType.LineRef.toString())) {
						extensionFilterTypes.add(IDFItemRefFilterType.LineRef);
						itemRefs.put(IDFItemRefFilterType.LineRef.toString(), new ArrayList<String>());
						itemRefs.get(IDFItemRefFilterType.LineRef.toString()).addAll(Arrays.asList(args[++i].split(",")));
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-StopRef")) {
				if (itemRefs.containsKey(IDFItemRefFilterType.LineRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.RouteRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.JourneyPatternRef.toString())) {
					printHelp("Option '-StopRef' in conflit with '-LineRef', '-RouteRef' or '-JourneyPatternRef'");
					return false;
				}
				if ((i + 1) < args.length) {
					if (!itemRefs.containsKey(IDFItemRefFilterType.StopRef.toString())) {
						extensionFilterTypes.add(IDFItemRefFilterType.StopRef);
						itemRefs.put(IDFItemRefFilterType.StopRef.toString(), new ArrayList<String>());
						itemRefs.get(IDFItemRefFilterType.StopRef.toString()).addAll(Arrays.asList(args[++i].split(",")));
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-JourneyPatternRef")) {
				if (itemRefs.containsKey(IDFItemRefFilterType.LineRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.StopRef.toString())
						|| itemRefs.containsKey(IDFItemRefFilterType.RouteRef.toString())) {
					printHelp("Option '-JourneyPatternRef' in conflit with '-LineRef', '-StopRef' or '-RouteRef'");
					return false;
				}
				if ((i + 1) < args.length) {
					if (!itemRefs.containsKey(IDFItemRefFilterType.JourneyPatternRef.toString())) {
						extensionFilterTypes.add(IDFItemRefFilterType.JourneyPatternRef);
						itemRefs.put(IDFItemRefFilterType.JourneyPatternRef.toString(), new ArrayList<String>());
						itemRefs.get(IDFItemRefFilterType.JourneyPatternRef.toString()).addAll(
								Arrays.asList(args[++i].split(",")));
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].equalsIgnoreCase("-lang")) {
				if ((i + 1) < args.length) {
					if (language == null) {
						language = args[++i];
					} else {
						printHelp("Option " + args[i] + alreadySet);
						return false;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			} else if (args[i].toLowerCase().startsWith("-v")) {
				verbose = true;
				continue;
			} else if (args[i].equalsIgnoreCase("-nv")) {
				verbose = false;
				continue;
			} else if (args[i].toLowerCase().startsWith("-h")) {
				printHelp("");
				return false;
			}
		}

		if ("default".equalsIgnoreCase(version))
			version = null;
		if (getOutDirectory() == null)
			setOutDirectory(".");

		return true;
	}

	public void printHelp(String errorMsg) {

		if (errorMsg.length() > 0) {
			System.out.println("SYNTAX ERROR : " + errorMsg);
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("GMClient [-InfoChannels [Perturbation],[Information],[Commercial] ](0/*)");
		System.out.println("                   [-lang arg2](0/1) ");
		System.out.println("                   [-MessageType [type],[type]+ (0/1) ");
		System.out.println("                   [-LineRef [lineId],[lineId]+ (0/1) ");
		System.out.println("                   [-StopRef [stopId],[stopId]+ (0/1) ");
		System.out.println("                   [-JourneyPatternRef [journeyPatternId],[journeyPatternId]+ (0/1) ");
		System.out.println("                   [-RouteRef [routeId],[routeId]+ (0/1) ");
		System.out.println(" -LineRef, -StopRef, -JourneyPatternRef and -RouteRef are choice options");
		System.out.println("");
		System.out.println(" stopId = [operatorCode]:StopPoint:[SP|BP|Q]]:[technicalId][:LOC]");
		System.out.println(" lineId = [operatorCode]:Line:[technicalId][:LOC]");
		System.out.println(" journeyPatternId = [operatorCode]:JourneyPattern:[technicalId][:LOC]");
		System.out.println(" routeId = [operatorCode]:Route:[technicalId][:LOC]");
		super.printHelp("GM");
	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) throws SiriException {
		if (!parseArgs(args))
			return null;
		AbstractServiceRequestStructure request = service.buildRequest(version, infoChannels, language,
				extensionFilterTypes, itemRefs);
		return request;

	}

}
