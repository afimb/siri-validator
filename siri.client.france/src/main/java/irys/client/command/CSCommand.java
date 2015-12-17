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
import irys.client.services.CheckStatusClient;

import javax.xml.ws.Holder;

import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.CheckStatusRequestStructure;
import uk.org.siri.siri.CheckStatusResponseBodyStructure;
import uk.org.siri.siri.ProducerResponseEndpointStructure;

public class CSCommand extends AbstractCommand {

	private @Setter
	CheckStatusClient service;

	private String version = null;
	private String requestFileName = "CSRequest";
	private String responseFileName = "CSResponse";

	/**
	 * 
	 */
	public CSCommand() {
		super();
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;
		try {
			CheckStatusRequestStructure request = service.buildRequest();
			if (version != null) {
				request.setVersion(version);
			}

			Holder<ProducerResponseEndpointStructure> checkStatusAnswerInfo = new Holder<ProducerResponseEndpointStructure>();
			Holder<CheckStatusResponseBodyStructure> answer = new Holder<CheckStatusResponseBodyStructure>();
			String timestamp = SiriClientUtil.nowFile();
			String reqFileName = requestFileName + "_" + timestamp + ".xml";
			String repFileName = responseFileName + "_" + timestamp + ".xml";
			service.startTrace(reqFileName, repFileName, this, verbose);
			service.checkStatus(request, checkStatusAnswerInfo, answer);
			if (isErrors()) {
				System.out.println("CheckStatus respones has errors; see report or log for precisions");
			}
			if (isWarnings()) {
				System.out.println("CheckStatus respones has warnings; see report or log for precisions");
			}
			if (!verbose) {
				System.out.println("CheckStatus executed");
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
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("CSClient  ");
		super.printHelp("CS");
	}

	public boolean parseArgs(String[] args) {

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
				printHelp("");
				return false;
			}
		}
		return true;
	}

	@Override
	public AbstractServiceRequestStructure getRequest(String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
