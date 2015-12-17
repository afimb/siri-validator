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
import irys.client.services.ProductionTimetableClient;
import irys.client.services.SiriException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.siri.ProductionTimetableDeliveriesStructure;
import uk.org.siri.siri.ProductionTimetableRequestStructure;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

public class PTCommand extends AbstractCommand {
	private String version = null;
	private String operatorId = null;
	private List<String> lineIds = new ArrayList<String>();
	private String start = null;
	private String end = null;

	private String requestFileName = "PTRequest";
	private String responseFileName = "PTResponse";

	private @Setter
	ProductionTimetableClient service;

	public PTCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		long startTime = System.currentTimeMillis();

		try {
			ProductionTimetableRequestStructure request = service.buildRequest(version, lineIds, operatorId, start, end);
			WsServiceRequestInfoStructure serviceRequestInfo = service.buildWsServiceRequestInfoStructure();

			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<ProductionTimetableDeliveriesStructure> answer = new Holder<ProductionTimetableDeliveriesStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.getResponseDocument(serviceRequestInfo, request, serviceDeliveryInfo, answer);
			if (isErrors()) {
				System.out.println("ProductionTimetable response has errors; see report or log for precisions");
			}
			if (isWarnings()) {
				System.out.println("ProductionTimetable response has warnings; see report or log for precisions");
			}

			if (!verbose) {
				System.out.println("ProductionTimetable executed");
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
		System.out.println("PTClient [-LineId  [lineId],[lineId+ (0/1) ");
		System.out.println("                  [-OperatorId operatorId](0/1) ");
		System.out.println("                  [-Start time](0/1) ");
		System.out.println("                  [-End time](0/1) ");
		System.out.println("");
		System.out.println(" operatorId = [operatorCode]:Company:[technicalId]");
		System.out.println(" lineId = [operatorCode]:Line:[technicalId]");
		System.out.println(" time = HH:MM");

		super.printHelp("PT");
	}

	public boolean parseArgs(String[] args) {
		version = null;
		operatorId = null;
		lineIds = new ArrayList<String>();
		start = null;
		end = null;

		boolean boperatorId = false;
		boolean blineId = false;
		boolean bstart = false;
		boolean bend = false;
		boolean bDir = false;
		boolean bFileIn = false;
		boolean bFileOut = false;

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

			if (args[i].equalsIgnoreCase("-LineId")) {
				if (blineId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				blineId = true;
				if ((i + 1) < args.length) {
					lineIds.addAll(Arrays.asList(args[++i].split(",")));
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
			if (args[i].equalsIgnoreCase("-End")) {
				if (bend) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bend = true;
				if ((i + 1) < args.length) {
					end = args[++i];
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
		return true;

	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) throws SiriException {
		if (!parseArgs(args))
			return null;

		AbstractServiceRequestStructure request = service.buildRequest(version, lineIds, operatorId, start, end);
		return request;

	}

}
