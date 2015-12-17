package irys.client.consumer;

import irys.client.common.SiriClientUtil;
import irys.client.services.SiriErrorHandler;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
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
public class NotificationLoggingHandler implements SOAPHandler<SOAPMessageContext> {

	public NotificationLoggingHandler() {
		log.info("instancied");
	}
	@SuppressWarnings("unchecked")
	public boolean handleMessage(SOAPMessageContext c) {
		SOAPMessage msg = c.getMessage();

		boolean request = !((Boolean) c.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
		String requestFileName = null;
		
		log.info("processing soap message : request = "+request);
		boolean problem = c.containsKey(SiriErrorHandler.WARNINGS) || c.containsKey(SiriErrorHandler.ERRORS);

		try {
			if (request && problem) { // This is a request message with problems .
				// Write the message to the file

				requestFileName = buildFileName(msg);

				try {
					PrintStream requestOS = new PrintStream(new FileOutputStream(requestFileName));
					prettyPrint(msg, requestOS);
					requestOS.close();
				} catch (FileNotFoundException e) {
					log.error("unable to create file " + requestFileName, e);
				}

				try {
					PrintStream validationOS = new PrintStream(new FileOutputStream(requestFileName + ".report"));
					if (c.containsKey(SiriErrorHandler.ERRORS)) {
						validationOS.println("wsdl or xsd errors :");
						List<String> msgs = (List<String>) c.get(SiriErrorHandler.ERRORS);
						for (String val : msgs) {
							validationOS.println("  " + val);
						}
					} else {
						validationOS.println("no wsdl or xsd errors");
					}
					if (c.containsKey(SiriErrorHandler.WARNINGS)) {
						validationOS.println("wsdl or xsd warnings :");
						List<String> msgs = (List<String>) c.get(SiriErrorHandler.WARNINGS);
						for (String val : msgs) {
							validationOS.println("  " + val);
						}
					} else {
						validationOS.println("no wsdl or xsd warnings");
					}
					validationOS.close();
				} catch (FileNotFoundException e) {
					log.error("unable to create file " + requestFileName + ".report", e);
				}
			}
		} catch (Exception e) {
			log.error("cannot save notification message");
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private String buildFileName(SOAPMessage msg) {
		try {
			SOAPBody body = msg.getSOAPBody();
			
			for (Iterator<SOAPElement> iterator = body.getChildElements(); iterator.hasNext();) {
				SOAPElement type =  iterator.next();
				return type.getElementName().getLocalName()+"_"+SiriClientUtil.nowFile()+".xml";
			}
			
		} catch (SOAPException e) {
			return "InvalidSoapMessage_"+SiriClientUtil.nowFile()+".xml";
		}
		return "EmptyBodyMessage_"+SiriClientUtil.nowFile()+".xml";
	}

	public boolean handleFault(SOAPMessageContext c) {

		return true;
	}

	public void close(MessageContext c) {

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
