package irys.client.consumer;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.extern.log4j.Log4j;

import org.w3c.dom.Node;

@Log4j
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String FILENAME = "filename";
	private static final String SEP = "-";
	private static SimpleDateFormat DF = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH-mm-ss-SSS");
	private XPathFactory factory = XPathFactory.newInstance();

	@Override
	public boolean handleMessage(SOAPMessageContext context) {

		try {
			SOAPMessage message = context.getMessage();
			String filename = buildFileName(message);
			if (filename != null) {
				context.put(FILENAME, filename);
				Path path = Paths.get(filename);
				prettyPrint(message, path);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {

		return true;
	}

	@Override
	public void close(MessageContext context) {

	}

	@Override
	public Set<QName> getHeaders() {

		return null;
	}

	private void prettyPrint(SOAPMessage message, Path path) throws Exception {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			Source source = message.getSOAPPart().getContent();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
			Files.write(path, out.toByteArray());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	private String buildFileName(SOAPMessage message) throws SOAPException,
			XPathExpressionException {
		String result = null;

		XPath path = factory.newXPath();

		SOAPBody body = message.getSOAPBody();
		Node root = body.getFirstChild();
		if (root != null) {
			Node delivery = (Node) path.evaluate("//ServiceDeliveryInfo/*[1]",
					root, XPathConstants.NODE);
			if (delivery != null) {
				Node node = (Node) path.evaluate("//Notification/*[1]", root,
						XPathConstants.NODE);
				String service = node.getLocalName();
				String subscriptionRef = path.evaluate(
						"//*[local-name() = 'SubscriptionRef']", node);
				subscriptionRef = subscriptionRef.replaceAll(":", "_");
				result = Service.getValue(service) + SEP
						+ subscriptionRef + SEP + DF.format(new Date())
						+ ".xml";
			}
		}
		return result;
	}

	enum Service {
		CM("ConnectionMonitoringDelivery"), 
		CT("ConnectionTimetableDelivery"), 
		ET("EstimatedTimetableDelivery"), 
		FM("FacilityMonitoringDelivery"), 
		GM("GeneralMessageDelivery"), 
		PT("ProductionTimetableDelivery"), 
		SM("StopMonitoringDelivery"), 
		ST("StopTimetableDelivery"), 
		SX("SituationExchangeDelivery"), 
		VM("VehicleMonitoringDelivery");

		private String value;

		private Service(String value) {
			this.value = value;
		}

		public static String getValue(String key) {
			String result = null;
			for (Service item : Service.values()) {
				if (item.value.equals(key)) {
					result = item.name();
					break;
				}
			}
			return result;
		}
	}
}
