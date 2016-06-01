package irys.client.consumer;

import irys.client.services.SiriErrorHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.validation.Schema;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.BasicConfigurator;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

@Log4j
public class Validator {

	private enum HEADERS {
		service, id, level, format, parent, xpath, test
	}

	@Data
	private class Record {
		private String service;
		private String id;
		private String level;
		private String format;
		private XPathExpression<Element> parent;
		private XPathExpression xpath;
		private String test;
		private Pattern pattern;
	}

	public enum TEST {
		NOT_EXIST, EXIST, PATTERN
	}

	private static Map<String, List<Record>> config;

	private Schema schema;

	public Validator() {
		try {
			initialize();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void validateSchema(SOAPMessageContext context, Path path)
			throws Exception {

		Path report = Paths.get(path.getFileName() + ".report");

		BufferedWriter writer = Files.newBufferedWriter(report,
				StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		try {

			writer.write("Validation : " + path);
			writer.newLine();

			if (context.containsKey(SiriErrorHandler.ERRORS)) {
				List<String> messages = (List<String>) context
						.get(SiriErrorHandler.ERRORS);
				for (String message : messages) {
					writer.write(message);
					writer.newLine();
				}
			}

			if (context.containsKey(SiriErrorHandler.WARNINGS)) {
				List<String> messages = (List<String>) context
						.get(SiriErrorHandler.WARNINGS);
				for (String message : messages) {
					writer.write(message);
					writer.newLine();
				}
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

	}

	public void validateProfile(Path path, String xpath) throws Exception {

		Path report = Paths.get(path.getFileName() + ".report");

		BufferedWriter writer = Files.newBufferedWriter(report,
				StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		try {

			SAXBuilder builder = new SAXBuilder();

			builder.setJDOMFactory(new LocatedJDOMFactory());
			File file = path.toFile();
			Document document = builder.build(file);
			Element root = document.getRootElement();
			XPathExpression<Element> expression = XPathFactory.instance()
					.compile(xpath, Filters.element());
			Element service = expression.evaluateFirst(root);

			List<Record> list = config.get(service.getName());
			if (list != null) {
				for (Record record : list) {
					List<Element> nodes = record.getParent().evaluate((root));
					for (Element parent : nodes) {

						Object object = record.getXpath().evaluateFirst(parent);
						if (object instanceof Attribute) {
							Attribute attribute = (Attribute) object;

							Matcher matcher = record.getPattern().matcher(
									attribute.getValue());
							if (!matcher.matches()) {
								this.trace(writer, record,
										(LocatedElement) parent, attribute);
							}
						} else {
							LocatedElement element = (LocatedElement) object;

							if (record.getTest().equals(TEST.EXIST.name())) {
								if (element == null) {
									this.trace(writer, record,
											(LocatedElement) parent);
								}
							} else if (record.getTest().equals(
									TEST.NOT_EXIST.name())) {
								if (element != null) {
									this.trace(writer, record, element);
								}
							} else {
								if (element != null) {
									Matcher matcher = record.getPattern()
											.matcher(element.getValue());
									if (!matcher.matches()) {
										this.trace(writer, record, element);
									}
								}
							}
						}

					}
				}
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void initialize() throws Exception {
		if (config == null) {
			BasicConfigurator.configure();
			log.info("[DSU] initialize");
//			ClassLoader loader = Thread.currentThread().getContextClassLoader();
//			InputStreamReader reader = new InputStreamReader(
//					loader.getResourceAsStream("config.csv"));
			Path path = Paths.get("config.csv");
			InputStreamReader reader = new InputStreamReader(Files.newInputStream(path));
			
			config = new HashMap<String, List<Record>>();
			Iterable<CSVRecord> records = CSVFormat.EXCEL
					.withHeader(HEADERS.class).withSkipHeaderRecord()
					.parse(reader);
			XPathFactory factory = XPathFactory.instance();

			for (CSVRecord record : records) {
				Record item = new Record();
				item.setService(record.get(HEADERS.service));
				item.setId(record.get(HEADERS.id));
				item.setLevel(record.get(HEADERS.level));
				item.setFormat(record.get(HEADERS.format));
				String parent = record.get(HEADERS.parent);
				item.setParent(factory.compile(parent, Filters.element()));
				String xpath = record.get(HEADERS.xpath);
				item.setXpath(factory.compile(xpath));
				item.setTest(record.get(HEADERS.test));
				if (!record.get(HEADERS.test).equals(TEST.EXIST.name())
						&& !record.get(HEADERS.test).equals(
								TEST.NOT_EXIST.name())) {
					item.setTest(TEST.PATTERN.name());
					item.setPattern(Pattern.compile(record.get(HEADERS.test)));
				} else {
					item.setTest(record.get(HEADERS.test));
				}

				List<Record> list = config.get(item.getService());
				if (list == null) {
					list = new ArrayList<Record>();
				}
				list.add(item);
				config.put(item.getService(), list);
			}
		}
	}

	private void trace(BufferedWriter writer, Record record,
			LocatedElement element) throws IOException {
		String message = MessageFormat.format(record.getFormat(),
				record.getLevel(), record.getId(),
				new Integer(element.getLine()),
				new Integer(element.getColumn()), element.getValue());
		log.info(message);
		writer.write(message);
		writer.newLine();
	}

	private void trace(BufferedWriter writer, Record record,
			LocatedElement element, Attribute attribute) throws IOException {
		String message = MessageFormat.format(record.getFormat(),
				record.getLevel(), record.getId(),
				new Integer(element.getLine()),
				new Integer(element.getColumn()), attribute.getValue());
		log.info(message);
		writer.write(message);
		writer.newLine();
	}

}
