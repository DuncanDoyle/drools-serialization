package org.jboss.ddoyle.drools.demo.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the given object from the given (CSV) file. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public abstract class AbstractFactsLoader<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFactsLoader.class);
	
	public List<T> loadFacts(File eventsFile) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(eventsFile));
		} catch (FileNotFoundException fnfe) {
			String message = "File not found.";
			LOGGER.error(message, fnfe);
			throw new IllegalArgumentException(message, fnfe);
		}
		return loadFacts(br);
		
	}
	
	public List<T> loadFacts(InputStream eventsInputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(eventsInputStream));
		return loadFacts(br);
		
	}
	
	private List<T> loadFacts(BufferedReader reader) {
		List<T> eventList = new ArrayList<>();
		try {
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				if (!nextLine.startsWith("#")) {
					T event = readFact(nextLine);
					if (event != null) {
						eventList.add(event);
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Got an IO exception while reading events.", ioe);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					// Swallowing exception, not much we can do here.
					LOGGER.error("Unable to close reader.", ioe);
				}
			}
		}
		return eventList;
	}
	

	/**
	 * Reads the fact {@link T} from the given String.
	 */
	protected abstract T readFact(String line);
	

}
