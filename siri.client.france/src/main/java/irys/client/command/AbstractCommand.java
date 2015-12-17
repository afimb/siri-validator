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

import irys.client.services.SiriErrorCallback;
import irys.client.services.SiriException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import lombok.Getter;
import lombok.Setter;
import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.ObjectFactory;

public abstract class AbstractCommand implements SiriErrorCallback {

	protected static String missingArgument = "missing argument(s) for ";

	protected static String alreadySet = " already set";

	private @Getter
	@Setter
	String outDirectory = ".";

	private @Getter
	@Setter
	boolean consoleMode = false;

	protected @Setter
	@Getter
	boolean verbose = false;

	protected static JAXBContext context = null;

	private static String lock = "lock";

	private @Getter
	@Setter
	boolean errors;

	private @Getter
	@Setter
	boolean warnings;

	public AbstractCommand() {
		synchronized (lock) {
			if (context == null) {
				try {
					context = JAXBContext.newInstance(ObjectFactory.class);
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(2);
				}
			}
		}
	}

	public abstract void call(String[] args);

	public abstract AbstractServiceRequestStructure getRequest(String[] args) throws SiriException;

	/**
	 * convert a duration in millisecond to literal
	 * 
	 * the returned format depends on the duration : <br>
	 * if duration > 1 hour, format is HH h MM m SS s <br>
	 * else if duration > 1 minute , format is MM m SS s <br>
	 * else if duration > 1 second , format is SS s <br>
	 * else (duration < 1 second) format is LLL ms
	 * 
	 * @param duration
	 *           the duration to convert
	 * @return the duration
	 */
	public static String getTimeAsString(long duration) {
		long d = duration;
		long milli = d % 1000;
		d /= 1000;
		long sec = d % 60;
		d /= 60;
		long min = d % 60;
		d /= 60;
		long hour = d;

		String res = "";
		if (hour > 0)
			res += hour + " h " + min + " m " + sec + " s ";
		else if (min > 0)
			res += min + " m " + sec + " s ";
		else if (sec > 0)
			res += sec + " s ";
		res += milli + " ms";
		return res;
	}

	public void printHelp(String prefix) {
		System.out.println("");
		System.out.println("common options :");
		System.out.println("                -[help]");
		System.out.println("                -in [requestFileName]");
		System.out.println("                -out [responseFileName]");
		System.out.println("                -d [outputDirectory]");
		System.out.println("                -version [siriLocalAgreementVersion]");
		System.out.println("");
		System.out.println("requestFileName : file name for saving siri request (default = " + prefix + "Request.xml)");
		System.out.println("responsFileName : file name for saving siri response (default = " + prefix + "Response.xml)");
		System.out.println("outputDirectory : directory target for saved files (default = current directory)");
		System.out
				.println("siriLocalAgreementVersion : request version as siriVersion[country-localAgreementName-localAgreementVersion]");
		System.out.println("                            (default = value set in properties file)");
		System.out.println("");
		System.out.println(" all option names are case-insensitive");

	}

}
