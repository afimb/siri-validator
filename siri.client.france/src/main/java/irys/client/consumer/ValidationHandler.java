package irys.client.consumer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import lombok.extern.log4j.Log4j;

@Log4j
public class ValidationHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			String filename = (String) context.get(LoggingHandler.FILENAME);

			if (filename != null) {
				log.info("[DSU] validate : " + filename);

				Path path = Paths.get(filename);
				Validator validator = new Validator();
				validator.validateSchema(context, path);
				validator.validateProfile(path, "//*[local-name() = 'Notification']/*[1]");
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

}
