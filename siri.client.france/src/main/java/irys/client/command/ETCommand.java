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
import irys.client.services.EstimatedTimetableClient;
import irys.client.services.ServiceClient;
import irys.client.services.SiriException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.EstimatedTimetableDeliveriesStructure;
import uk.org.siri.siri.EstimatedTimetableRequestStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;
import uk.org.siri.wsdl.WsServiceRequestInfoStructure;

public class ETCommand extends AbstractCommand {
	private String version = null;
	private String operatorId = null;
	private List<String> lineIds = new ArrayList<String>();
	private String direction = null;
	private String timetableVersionId = null;
	private int preview = ServiceClient.UNDEFINED_NUMBER;
	private String detailLevel = null;

	private String requestFileName = "ETRequest";
	private String responseFileName = "ETResponse";

	private @Setter
	EstimatedTimetableClient service;

	public ETCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		long startTime = System.currentTimeMillis();

		try {
			EstimatedTimetableRequestStructure request = service.buildRequest(
					version, lineIds, direction, timetableVersionId,
					operatorId, preview, detailLevel);
			WsServiceRequestInfoStructure serviceRequestInfo = service
					.buildWsServiceRequestInfoStructure();

			Holder<ProducerResponseEndpointStructure> serviceDeliveryInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<EstimatedTimetableDeliveriesStructure> answer = new Holder<EstimatedTimetableDeliveriesStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.getResponseDocument(serviceRequestInfo, request,
					serviceDeliveryInfo, answer);
			if (isErrors())
			{
				System.out.println("EstimatedTimetable response has errors; see report or log for precisions");
			}
			if (isWarnings())
			{
				System.out.println("EstimatedTimetable response has warnings; see report or log for precisions");
			}

			if (!verbose) {
				System.out.println("EstimatedTimetable executed");
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
		System.out.println("ETClient [-LineId  [lineId],[lineId+ (0/1) ");
		System.out.println("                  [-Direction direction](0/1) ");
		System.out.println("                  [-OperatorId operatorId](0/1) ");
		System.out
				.println("                  [-Preview duration](0/1) (in minutes)");
		// System.out.println("                  [-TmVersion TMVersionId](0/1) ");
		System.out
				.println("                  [-DetailLevel M(inimum),B(asic),N(ormal),C(alls),F(ull)](0/1) ");
		System.out.println("");
		System.out
				.println(" operatorId = [operatorCode]:Company:[technicalId]");
		System.out.println(" lineId = [operatorCode]:Line:[technicalId]");
		System.out.println(" direction = Left|Right");

		super.printHelp("ET");
	}

	public boolean parseArgs(String[] args) {
		version = null;
		operatorId = null;
		lineIds.clear();
		direction = null;
		timetableVersionId = null;
		preview = ServiceClient.UNDEFINED_NUMBER;
		detailLevel = null;

		boolean boperatorId = false;
		boolean blineId = false;
		boolean bdirection = false;
		// boolean bTMId = false;
		boolean bpreview = false;
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

			// if (args[i].equalsIgnoreCase("-TmVersion"))
			// {
			// if (bTMId)
			// {
			// printHelp("Option "+args[i]+alreadySet);
			// return false;
			// }
			// bTMId = true;
			// if ((i + 1) < args.length)
			// {
			// timetableVersionId = args[++i];
			// }
			// else
			// {
			// printHelp(missingArgument+args[i]);
			// return false;
			// }
			// continue;
			// }
			if (args[i].equalsIgnoreCase("-Direction")) {
				if (bdirection) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bdirection = true;
				if ((i + 1) < args.length) {
					direction = args[++i];
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
		return true;

	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args)
			throws SiriException {
		if (!parseArgs(args))
			return null;

		AbstractServiceRequestStructure request = service.buildRequest(version,
				lineIds, direction, timetableVersionId, operatorId, preview,
				detailLevel);
		return request;

	}

}
