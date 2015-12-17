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
import irys.client.services.DiscoveryClient;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.ConnectionLinksDeliveryStructure;
import uk.org.siri.siri.ConnectionLinksDiscoveryRequestStructure;
import uk.org.siri.siri.LinesDeliveryStructure;
import uk.org.siri.siri.LinesDiscoveryRequestStructure;
import uk.org.siri.siri.StopPointsDeliveryStructure;
import uk.org.siri.siri.StopPointsDiscoveryRequestStructure;

public class DSCommand extends AbstractCommand {
	private @Setter
	DiscoveryClient service;

	private String version = null;
	private boolean stopDiscovery;
	private boolean lineDiscovery;
	private boolean connectionLinkDiscovery;
	private String requestLineFileName = "LineDiscoveryRequest";
	private String responseLineFileName = "LineDiscoveryResponse";
	private String requestStopFileName = "StopDiscoveryRequest";
	private String responseStopFileName = "StopDiscoveryResponse";
	private String requestConnectionLinkFileName = "ConnectionLinkDiscoveryRequest";
	private String responseConnectionLinkFileName = "ConnectionLinkDiscoveryResponse";

	/**
	 * 
	 */
	public DSCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		try {

			String timestamp = SiriClientUtil.nowFile();
			if (lineDiscovery) {
				LinesDiscoveryRequestStructure request = service
						.buildLineRequest(version);
				Holder<LinesDeliveryStructure> answer = new Holder<LinesDeliveryStructure>();
				String reqFileName = requestLineFileName + "_" + timestamp + ".xml";
				String repFileName = responseLineFileName + timestamp + ".xml";
				service.startTrace(reqFileName, repFileName, this,verbose);
				service.linesDiscovery(request, answer);
				if (isErrors())
				{
					System.out.println("Lines Discovery response has errors; see report or log for precisions");
				}
				if (isWarnings())
				{
					System.out.println("Lines Discovery response has warnings; see report or log for precisions");
				}
				if (!verbose) {
					System.out.println("Lines Discovery executed");
				}
			} else if (stopDiscovery) {
				StopPointsDiscoveryRequestStructure request = service
						.buildStopRequest(version);
				Holder<StopPointsDeliveryStructure> answer = new Holder<StopPointsDeliveryStructure>();
				String reqFileName = requestStopFileName + "_" + timestamp + ".xml";
				String repFileName = responseStopFileName + "_" + timestamp + ".xml";
				service.startTrace(reqFileName, repFileName, this,verbose);
				service.stopPointsDiscovery(request, answer);
				if (isErrors())
				{
					System.out.println("Stops Discovery response has errors; see report or log for precisions");
				}
				if (isWarnings())
				{
					System.out.println("Stops Discovery response has warnings; see report or log for precisions");
				}
				if (!verbose) {
					System.out.println("Stops Discovery executed");
				}
			} else {
				ConnectionLinksDiscoveryRequestStructure request = service
						.buildConnectionLinkRequest(version);
				Holder<ConnectionLinksDeliveryStructure> answer = new Holder<ConnectionLinksDeliveryStructure>();
				String reqFileName = requestConnectionLinkFileName + "_" + timestamp + ".xml";
				String repFileName = responseConnectionLinkFileName + "_" + timestamp + ".xml";
				service.startTrace(reqFileName, repFileName,this, verbose);
				service.connectionLinksDiscovery(request, answer);
				if (isErrors())
				{
					System.out.println("ConnectionLinks Discovery response has errors; see report or log for precisions");
				}
				if (isWarnings())
				{
					System.out.println("ConnectionLinks Discovery response has warnings; see report or log for precisions");
				}
				if (!verbose) {
					System.out.println("ConnectionLinks executed");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	public void printHelp(String errorMsg) {
		if (errorMsg.length() > 0) {
			System.out.println("SYNTAX ERROR : " + errorMsg);
			System.out.println("");
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("DSClient [-Stop | -Line | -ConnectionLink] ");
		super.printHelp("XxxDiscovery");
	}

	public boolean parseArgs(String[] args) {
		version = null;
		stopDiscovery = false;
		lineDiscovery = false;
		connectionLinkDiscovery = false;
		boolean bDir = false;
		boolean bFileIn = false;
		boolean bFileOut = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-StopDiscovery")
					|| args[i].equalsIgnoreCase("-Stop")) {
				if (stopDiscovery) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				stopDiscovery = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-LineDiscovery")
					|| args[i].equalsIgnoreCase("-Line")) {
				if (lineDiscovery) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				lineDiscovery = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-ConnectionLinkDiscovery")
					|| args[i].equalsIgnoreCase("-ConnectionLink")) {
				if (connectionLinkDiscovery) {
					printHelp("Option '-ConnectionLink' already set");
					return false;
				}
				connectionLinkDiscovery = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-in")) {
				if (bFileIn) {
					printHelp("Option '-in' en double");
					return false;
				}
				bFileIn = true;
				if ((i + 1) < args.length) {
					if (lineDiscovery)
						requestLineFileName = args[++i];
					else if (stopDiscovery)
						requestStopFileName = args[++i];
					else
						requestConnectionLinkFileName = args[++i];
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
					if (lineDiscovery)
						responseLineFileName = args[++i];
					else if (stopDiscovery)
						responseStopFileName = args[++i];
					else
						responseConnectionLinkFileName = args[++i];
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

		if (!stopDiscovery && !lineDiscovery && !connectionLinkDiscovery) {
			printHelp("missing option -Stop or -Line or -ConnectionLink");
			return false;
		}
		if ((stopDiscovery && (lineDiscovery || connectionLinkDiscovery))
				|| (lineDiscovery && connectionLinkDiscovery)) {
			printHelp("only one of the options -Stop,  -Line , -ConnectionLink can be set at once");
			return false;
		}
		return true;
	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
