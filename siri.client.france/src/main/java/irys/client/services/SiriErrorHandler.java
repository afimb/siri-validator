package irys.client.services;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.ws.developer.ValidationErrorHandler;

@Log4j
public class SiriErrorHandler extends ValidationErrorHandler{
	
	public static final String ERRORS = "siri-xsd-validation-errors";
	public static final String WARNINGS = "siri-xsd-validation-warnings";

	@SuppressWarnings("unchecked")
	@Override
	public void error(SAXParseException e) throws SAXException {
		List<String> errors = (List<String>) packet.invocationProperties.get(ERRORS);
		if (errors == null)
		{
			errors = new ArrayList<>();
			packet.invocationProperties.put(ERRORS,errors);
		}
		errors.add(e.getMessage());
		log.error("Sax error "+e.getMessage());
		// throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		// log.error("Sax fatal "+e.getMessage());
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void warning(SAXParseException e) throws SAXException {
		log.error("Sax warning "+e.getMessage());
		List<String> errors = (List<String>) packet.invocationProperties.get(WARNINGS);
		if (errors == null)
		{
			errors = new ArrayList<>();
			packet.invocationProperties.put(WARNINGS,errors);
		}
		errors.add(e.getMessage());
	}

}
