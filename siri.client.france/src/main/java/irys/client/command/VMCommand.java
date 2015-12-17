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
import irys.client.services.VehicleMonitoringClient;
import irys.client.services.SiriException;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveriesStructure;
import uk.org.siri.siri.VehicleMonitoringRequestStructure;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

public class VMCommand extends AbstractCommand {
	private String version = null;
	private String vehicleId = null;
	private String lineId = null;
	private String direction = null;
	private int maxVehicle = ServiceClient.UNDEFINED_NUMBER;
	private String detailLevel = null;
	private Long maxPrevious = null;
	private Long maxOnwards = null;

	private String requestFileName = "VMRequest";
	private String responseFileName = "VMResponse";

	private @Setter
	VehicleMonitoringClient service;

	public VMCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		long startTime = System.currentTimeMillis();

		try {
			VehicleMonitoringRequestStructure request = service.buildRequest(version, vehicleId, lineId, direction,
					maxVehicle, maxPrevious, maxOnwards, detailLevel);
			WsServiceRequestInfoStructure serviceRequestInfo = service.buildWsServiceRequestInfoStructure();

			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<VehicleMonitoringDeliveriesStructure> answer = new Holder<VehicleMonitoringDeliveriesStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.getResponseDocument(serviceRequestInfo, request, serviceDeliveryInfo, answer);
			if (isErrors()) {
				System.out.println("VehicleMonitoring response has errors; see report or log for precisions");
			}
			if (isWarnings()) {
				System.out.println("VehicleMonitoring response has warnings; see report or log for precisions");
			}

			if (!verbose) {
				System.out.println("VehicleMonitoring executed");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		} finally {
			long endTime = System.currentTimeMillis();
			long duree = endTime - startTime;
			System.out.println("durée = " + getTimeAsString(duree));
		}

	}

	public void printHelp(String errorMsg) {
		if (errorMsg.length() > 0) {
			System.out.println("SYNTAX ERROR : " + errorMsg);
			System.out.println("");
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("VMClient[-VehicleId [vehicleId](0/1) ");
		System.out.println("                  [-LineId lineId](0/1) ");
		System.out.println("                  [-Direction direction](0/1) ");
		System.out.println("                  [-MaxVehicle number](0/1) ");
		System.out.println("                  [-Onward nbCalls](0/1), 0 for all");
		System.out.println("                  [-Previous nbCalls](0/1), 0 for all ");
		System.out.println("                  [-DetailLevel M(inimum),B(asic),N(ormal),C(alls),F(ull)](0/1) ");
		System.out.println("");
		System.out.println(" vehicleId = [operatorCode]:Vehicle:[technicalId]");
		System.out.println(" lineId = [operatorCode]:Line:[technicalId]");
		System.out.println(" direction = [Left|Right]");
		super.printHelp("VM");
	}

	public boolean parseArgs(String[] args) {
		version = null;
		vehicleId = null;
		lineId = null;
		maxVehicle = ServiceClient.UNDEFINED_NUMBER;
		detailLevel = null;
		direction = null;
		maxPrevious = null;
		maxOnwards = null;

		boolean bvehicleId = false;
		boolean blineId = false;
		boolean bDirection = false;
		boolean bmaxVehicle = false;
		boolean bOnward = false;
		boolean bPrevious = false;
		boolean bDir = false;
		boolean bFileIn = false;
		boolean bFileOut = false;
		boolean bDetailLevel = false;

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

			if (args[i].equalsIgnoreCase("-VehicleId")) {
				if (bvehicleId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bvehicleId = true;
				if ((i + 1) < args.length) {
					vehicleId = args[++i];
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
			if (args[i].equalsIgnoreCase("-Direction")) {
				if (bDirection) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bDirection = true;
				if ((i + 1) < args.length) {
					direction = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
				continue;
			}

			if (args[i].equalsIgnoreCase("-MaxVehicle")) {
				if (bmaxVehicle) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bmaxVehicle = true;
				if ((i + 1) < args.length) {
					maxVehicle = Integer.parseInt(args[++i]);
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

		if (!blineId && !bvehicleId) {
			printHelp("LineId ou VehicleId non fourni ");
			return false;
		}
		return true;
	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) throws SiriException {
		if (!parseArgs(args))
			return null;

		AbstractServiceRequestStructure request = service.buildRequest(version, vehicleId, lineId, direction, maxVehicle,
				maxPrevious, maxOnwards, detailLevel);
		return request;

	}

}
