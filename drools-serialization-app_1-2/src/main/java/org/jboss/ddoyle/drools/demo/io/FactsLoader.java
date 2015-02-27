package org.jboss.ddoyle.drools.demo.io;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jboss.ddoyle.drools.serialization.demo.model.v1.SimpleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactsLoader extends AbstractFactsLoader<SimpleEvent> {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd:HHmmssSSS");
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FactsLoader.class);
	
	@Override
	protected SimpleEvent readFact(String line) {
		String[] eventData = line.split(",");
		if (eventData.length != 2) {
			String message = "Unable to parse string: " + line;
			LOGGER.error(message);
			throw new IllegalArgumentException(message);
		}
		SimpleEvent event = null;
		try {
			event = new SimpleEvent(eventData[0], DATE_FORMAT.parse(eventData[1].trim()));
		} catch (NumberFormatException nfe) {
			LOGGER.error("Error parsing line: " + line, nfe);
		} catch (ParseException pe) {
			LOGGER.error("Error parsing line: " + line, pe);
		} 
		return event;

	}

}
