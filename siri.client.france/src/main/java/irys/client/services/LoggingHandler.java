package irys.client.services;

import irys.client.consumer.Validator;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import lombok.extern.log4j.Log4j;

@Log4j
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	private PrintStream requestOS = null;
	private PrintStream responseOS = null;
	private PrintStream validationOS = null;
	private SiriErrorCallback callBack = null;
	private boolean verbose = false;
	private String responseFileName;

	public void init(String requestFileName, String responseFileName, SiriErrorCallback callBack, boolean verbose) {
		this.verbose = verbose;
		this.callBack = callBack;
		this.responseFileName = responseFileName;
		
		if (requestFileName == null) {
			requestOS = null;
		} else {
			try {
				requestOS = new PrintStream(new FileOutputStream(requestFileName));
			} catch (FileNotFoundException e) {
				log.error("unable to create file " + requestFileName, e);
				requestOS = null;
			}
		}
		if (responseFileName == null) {
			responseOS = null;
			validationOS = null;

		} else {
			try {
				responseOS = new PrintStream(new FileOutputStream(responseFileName));
			} catch (FileNotFoundException e) {
				log.error("unable to create file " + responseFileName, e);
				responseOS = null;
			}
			try {
				validationOS = new PrintStream(new FileOutputStream(responseFileName + ".report"));
			} catch (FileNotFoundException e) {
				log.error("unable to create file " + responseFileName + ".report", e);
				validationOS = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean handleMessage(SOAPMessageContext c) {
		SOAPMessage msg = c.getMessage();

		boolean request = ((Boolean) c.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
		String type = "";
		try {
			if (request) { // This is a request message.
				// Write the message to the output stream
				type = "request";
				if (requestOS != null)
					prettyPrint(msg, requestOS);
			} else { // This is the response message
				type = "response";
				if (responseOS != null)
					prettyPrint(msg, responseOS);
				if (c.containsKey(SiriErrorHandler.ERRORS)) {
					if (callBack != null)
						callBack.setErrors(true);
					validationOS.println("wsdl or xsd errors :");
					List<String> msgs = (List<String>) c.get(SiriErrorHandler.ERRORS);
					for (String val : msgs) {
						validationOS.println("  " + val);
					}
				} else {
					if (callBack != null)
						callBack.setErrors(false);
					validationOS.println("no wsdl or xsd errors");
				}
				if (c.containsKey(SiriErrorHandler.WARNINGS)) {
					if (callBack != null)
						callBack.setWarnings(true);
					validationOS.println("wsdl or xsd warnings :");
					List<String> msgs = (List<String>) c.get(SiriErrorHandler.WARNINGS);
					for (String val : msgs) {
						validationOS.println("  " + val);
					}
				} else {
					if (callBack != null)
						callBack.setWarnings(false);
					validationOS.println("no wsdl or xsd warnings");
				}
				
				
				Validator validator = new Validator();				
				validator.validateProfile(Paths.get(this.responseFileName), "//*[local-name() = 'Answer']/*[1]");
				
			}
			if (verbose)
				prettyPrint(msg, System.out);
		} catch (Exception e) {
			log.error("cannot save " + type + " message", e);
		}
		return true;
	}

	public boolean handleFault(SOAPMessageContext c) {
		SOAPMessage msg = c.getMessage();

		try {
			if (responseOS != null)
				prettyPrint(msg, responseOS);
			log.error("fault message saved");

		} catch (Exception e) {
			log.error("cannot save fault message");
		}
		return true;
	}

	public void close(MessageContext c) {
		if (requestOS != null)
			requestOS.close();
		requestOS = null;
		if (responseOS != null)
			responseOS.close();
		responseOS = null;
		if (validationOS != null)
			validationOS.close();
		validationOS = null;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getHeaders() {
		// Not required for logging
		return null;
	}

	private void prettyPrint(SOAPMessage soapMessage, PrintStream out) throws Exception {

		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf = tff.newTransformer();

		// Set formatting
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		Source sc = soapMessage.getSOAPPart().getContent();

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(streamOut);
		tf.transform(sc, result);

		String strMessage = streamOut.toString();
		out.println(strMessage);
	}

}
