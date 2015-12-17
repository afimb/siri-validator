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
import irys.client.consumer.SiriConsumer;
import irys.client.services.SiriException;
import irys.client.services.SubscriptionClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.Duration;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;

import lombok.Getter;
import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.AbstractSubscriptionStructure;
import uk.org.siri.siri.RequestStructure;
import uk.org.siri.siri.ResponseEndpointStructure;
import uk.org.siri.siri.SiriSubscriptionRequestStructure;
import uk.org.siri.siri.SubscriptionResponseBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionRequestBodyStructure;
import uk.org.siri.siri.TerminateSubscriptionResponseStructure;
import uk.org.siri.wsdl.WsSubscriptionRequestInfoStructure;

public class SubscribeCommand extends AbstractCommand {

	private static long subscriptionCount = 1;
	@Getter
	@Setter
	private Map<String, AbstractCommand> services;

	private String version = null;
	private String notify = null;
	@Getter
	@Setter
	private String notifyAddress = null;
	@Getter
	@Setter
	private boolean notifyLog = true;
	private GregorianCalendar validUntil = null;
	private Duration changeBeforeTime = null;
	private String askedService = null;
	private String subscriptionId = null;
	private String subscriptionRequestFileName = "SubscriptionRequest";
	private String subscriptionResponseFileName = "SubscriptionResponse";
	private String terminateSubscriptionRequestFileName = "TerminateSubscriptionRequest";
	private String terminateSubscriptionResponseFileName = "TerminateSubscriptionResponse";

	@Setter
	private SubscriptionClient service;
	@Getter
	@Setter
	private Endpoint consumer = null;
	private SiriConsumer siriConsumer;
	private Duration updateInterval;

	/**
    * 
    */
	public SubscribeCommand() {
		super();
		verbose = true;
	}

	public void printHelp(String errorMsg) {

		if (errorMsg.length() > 0) {
			System.out.println("SYNTAX ERROR : " + errorMsg);
		}
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("Subscribe -Notify url -ValidUntil dateTime ");
		System.out.println("                   [-ChangeBefore time] ");
		System.out.println("                   [-UpdateInterval time] (for VehicleMonitoring only");
		System.out.println("                   -Service ZZClient ClientArgs+ ");

		System.out.println(" dateTime : yyyy/mm/dd-hh:mm");
		System.out.println(" time : sss ");
		System.out
				.println(" ZZClient : GMClient, SMClient, VMClient, ETClient or PTClient, followed by their respective arguments\n");
		if (!isConsoleMode())
			System.out.print("client.sh ");
		System.out.println("Unsubscribe -SubscriptionId [id|all]");

		super.printHelp("[Terminate]Subscription");

	}

	public SiriSubscriptionRequestStructure getSubscriptionRequest(String[] args) throws SiriException {
		if (!parseArgs(args))
			return null;
		SiriSubscriptionRequestStructure request = service.buildSubcriptionRequest(notify);
		AbstractCommand command = null;
		if (askedService.equalsIgnoreCase("SMClient")) {
			command = services.get("SMClient");
		} else if (askedService.equalsIgnoreCase("GMClient")) {
			command = services.get("GMClient");
		} else if (askedService.equalsIgnoreCase("VMClient")) {
			command = services.get("VMClient");
		} else if (askedService.equalsIgnoreCase("ETClient")) {
			command = services.get("ETClient");
		} else if (askedService.equalsIgnoreCase("PTClient")) {
			command = services.get("PTClient");
		} else {
			return null;
		}
		command.setConsoleMode(isConsoleMode());
		AbstractServiceRequestStructure structure = command.getRequest(args);

		if (structure == null)
			return null;
		String subId = "" + subscriptionCount++;
		AbstractSubscriptionStructure subRequest = service.buildSubRequest(request, new GregorianCalendar(), structure,
				validUntil, true, changeBeforeTime, updateInterval, subId);

		return request;
	}

	@Override
	public void call(String[] args) {
		if (!parseArgs(args))
			return;

		try {
			String timestamp = SiriClientUtil.nowFile();
			if (subscriptionId != null) {
				String reqFileName = terminateSubscriptionRequestFileName + "_" + subscriptionId + "_" + timestamp + ".xml";
				String repFileName = terminateSubscriptionResponseFileName + "_" + subscriptionId + "_" + timestamp
						+ ".xml";
				service.startTrace(reqFileName, repFileName, this, verbose);
				RequestStructure deleteSubscriptionInfo = service.buildDeleteSubscriptionInfo();
				TerminateSubscriptionRequestBodyStructure request = service
						.buildTerminateSubcriptionRequest(subscriptionId);
				Holder<ResponseEndpointStructure> deleteSubscriptionAnswerInfo = new Holder<ResponseEndpointStructure>();
				Holder<TerminateSubscriptionResponseStructure> answer = new Holder<TerminateSubscriptionResponseStructure>();
				service.deleteSubscription(deleteSubscriptionInfo, request, deleteSubscriptionAnswerInfo, answer);
				if (isErrors()) {
					System.out.println("DeleteSubscription response has errors; see report or log for precisions");
				}
				if (isWarnings()) {
					System.out.println("DeleteSubscription response has warnings; see report or log for precisions");
				}
				if (!verbose) {
					System.out.println("Subscription executed");
				}
			} else {
				SiriSubscriptionRequestStructure request = service.buildSubcriptionRequest(notify);

				AbstractCommand command = null;
				String tag = askedService.substring(0, 2);
				if (askedService.equalsIgnoreCase("SMClient")) {
					command = services.get("SMClient");
				} else if (askedService.equalsIgnoreCase("GMClient")) {
					command = services.get("GMClient");
				} else if (askedService.equalsIgnoreCase("VMClient")) {
					command = services.get("VMClient");
				} else if (askedService.equalsIgnoreCase("ETClient")) {
					command = services.get("ETClient");
				} else if (askedService.equalsIgnoreCase("PTClient")) {
					command = services.get("PTClient");
				} else if (askedService.equalsIgnoreCase("CMClient")) {
					command = services.get("CMClient");
				} else {
					printHelp("unavailable service " + askedService);
					return;
				}
				command.setConsoleMode(isConsoleMode());
				AbstractServiceRequestStructure structure = command.getRequest(args);

				if (structure == null)
					return;
				String subId = "" + subscriptionCount++;
				AbstractSubscriptionStructure subRequest = service.buildSubRequest(request, new GregorianCalendar(),
						structure, validUntil, true, changeBeforeTime, updateInterval, subId);
				// start consumer
				try {
					if (consumer == null) {
						if (notifyLog)
							System.out.println("demarrage consumer sur " + notify + " avec traces");
						else
							System.out.println("demarrage consumer sur " + notify + " sans traces");
						notifyAddress = notify;
						siriConsumer = new SiriConsumer();
						siriConsumer.setNotifyLog(notifyLog);
						siriConsumer.setSiriVersion(service.getVersion());
						consumer = Endpoint.create(siriConsumer);
						SOAPBinding binding = (SOAPBinding) consumer.getBinding();
						binding.setMTOMEnabled(true);
						consumer.publish(notify);
					}
					siriConsumer.setVerbose(verbose);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				WsSubscriptionRequestInfoStructure subscriptionRequestInfo = service
						.buildWsSubscriptionRequestInfoStructure(notify);
				Holder<ResponseEndpointStructure> subscriptionAnswerInfo = new Holder<ResponseEndpointStructure>();
				Holder<SubscriptionResponseBodyStructure> answer = new Holder<SubscriptionResponseBodyStructure>();

				String reqFileName = subscriptionRequestFileName + "_" + tag + "_" + subId + "_" + timestamp + ".xml";
				String repFileName = subscriptionResponseFileName + "_" + tag + "_" + subId + "_" + timestamp + ".xml";
				service.startTrace(reqFileName, repFileName, this, verbose);
				service.subscribe(subscriptionRequestInfo, request, subscriptionAnswerInfo, answer);
				if (isErrors()) {
					System.out.println("Subscription response has errors; see report or log for precisions");
				}
				if (isWarnings()) {
					System.out.println("Subscription response has warnings; see report or log for precisions");
				}

				if (!verbose) {
					System.out.println("Subscription executed");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

	}

	private boolean parseArgs(String[] args) {
		notify = notifyAddress;
		validUntil = new GregorianCalendar();
		changeBeforeTime = null;
		updateInterval = null;
		askedService = null;
		subscriptionId = null;
		version = null;

		boolean bNotify = false;
		boolean bService = false;
		boolean bValid = false;
		boolean bChange = false;
		boolean bUpdate = false;
		boolean bSubscribe = false;
		boolean bUnsubscribe = false;
		boolean bSubId = false;

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd-HH:mm");

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("Subscribe")) {
				if (bSubscribe) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				if (bUnsubscribe) {
					printHelp("Option 'Subscribe' en conflit avec 'Unsubscribe'");
					return false;
				}
				bSubscribe = true;
			}
			if (args[i].equalsIgnoreCase("Unsubscribe")) {
				if (bUnsubscribe) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				if (bSubscribe) {
					printHelp("Option 'Unsubscribe' en conflit avec 'Subscribe'");
					return false;
				}
				bUnsubscribe = true;
			}
			if (args[i].equalsIgnoreCase("-Notify")) {
				if (!bSubscribe) {
					printHelp("Option '-Notify' demande Subscribe");
					return false;
				}
				if (bNotify) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bNotify = true;
				if ((i + 1) < args.length) {
					notify = args[++i];
					if (consumer != null && notify != notifyAddress) {
						printHelp("Option '-Notify' différent d'un précédent abonnement, ignoré");
						notify = notifyAddress;
					}
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}
			if (args[i].equalsIgnoreCase("-ValidUntil")) {
				if (!bSubscribe) {
					printHelp("Option '-ValidUntil'  only for Subscribe");
					return false;
				}
				if (bValid) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bValid = true;
				if ((i + 1) < args.length) {
					try {
						Date date = fmt.parse(args[++i]);
						validUntil.setTime(date);
					} catch (ParseException e) {
						printHelp("syntaxe argument invalide pour '-ValidUntil'");
						return false;
					}

				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}
			if (args[i].equalsIgnoreCase("-ChangeBefore")) {
				if (!bSubscribe) {
					printHelp("Option '-ChangeBefore'  only for Subscribe");
					return false;
				}
				if (bChange) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bChange = true;
				if ((i + 1) < args.length) {
					int value = Integer.parseInt(args[++i]);
					changeBeforeTime = service.secondsToDuration(value);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}
			if (args[i].equalsIgnoreCase("-UpdateInterval")) {
				if (!bSubscribe) {
					printHelp("Option '-UpdateInterval'  only for Subscribe");
					return false;
				}
				if (bUpdate) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bUpdate = true;
				if ((i + 1) < args.length) {
					int value = Integer.parseInt(args[++i]);
					updateInterval = service.secondsToDuration(value);
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}
			if (args[i].equalsIgnoreCase("-Service")) {
				if (!bSubscribe) {
					printHelp("Option '-Service'  only for Subscribe");
					return false;
				}
				if (bService) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bService = true;
				if ((i + 1) < args.length) {
					askedService = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}
			if (args[i].equalsIgnoreCase("-SubscriptionId")) {
				if (!bUnsubscribe) {
					printHelp("Option '-SubscriptionId'  only for Unsubscribe");
					return false;
				}
				if (bSubId) {
					printHelp("Option " + args[i] + alreadySet);
					return false;
				}
				bSubId = true;
				if ((i + 1) < args.length) {
					subscriptionId = args[++i];
				} else {
					printHelp(missingArgument + args[i]);
					return false;
				}
			}

			if (args[i].toLowerCase().startsWith("-v")) {
				verbose = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-nv")) {
				verbose = false;
				continue;
			}

			if (args[i].toLowerCase().startsWith("-h")) {
				printHelp("");
				return false;
			}

		}
		if (!bValid) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, 3);
			validUntil.setTime(c.getTime());
		}
		if (!bNotify && notify != null)
			bNotify = true;
		if (bSubscribe && (!bNotify || !bService)) {
			printHelp("option(s) obligatoire(s) manquante(s) -Notify et/ou -Service");
			return false;
		}
		if (bUnsubscribe && (!bSubId)) {
			printHelp("option obligatoire manquante -SubscriptionId");
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
