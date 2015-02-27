package org.jboss.ddoyle.drools.demo.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KieSessionLoader} implementation which persists the {@link KieSession} to, and loads the {@link KieSession} from, a file.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public abstract class AbstractFileKieSessionLoader implements KieSessionLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(KieSessionLoader.class);

	private File file;

	public AbstractFileKieSessionLoader(File file) {
		this.file = file;
	}

	public void save(KieSession kieSession) {
		save(kieSession, file);
	}

	public KieSession load() {
		return load(file);
	}
	
	public KieSession load(KieBase kieBase) {
		return load(file, kieBase);
	}

	protected void save(KieSession kieSession, File file) {
		try {
			LOGGER.info("Saving the KieSession to file: " + file.getCanonicalPath());
		} catch (IOException ioe) {
			LOGGER.error("Error retrieving the canonical path of the file.", ioe);
			//Not much we can do here, just an error while logging. So swallow the exception.
			//BTW, if we get an error here, we will probably get errors somewhere further down as well.....
		}
		// TODO: Might want to buffer the writes.
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file);	
			oos = new ObjectOutputStream(fos);
			oos.writeObject(kieSession.getKieBase());
			/*
			 * It seems that the Marshaller does not persist the actual SessionClock, which is a problem when using the PseudoClock, so we
			 * persist the SessionConfiguration, Environment and clock time to be able to restore the pseudo-clock (if it's used).
			 */
			KieSessionConfiguration kieSessionConfiguration = kieSession.getSessionConfiguration();
			oos.writeObject(kieSessionConfiguration);
			
			Marshaller marshaller = createMarshaller(kieSession.getKieBase());
			
			marshaller.marshall(fos, kieSession);
			
		} catch (FileNotFoundException fnfe) {
			String errorMessage = "Cannot find file to save KieSession.";
			LOGGER.error(errorMessage, fnfe);
			throw new RuntimeException(errorMessage, fnfe);
		} catch (IOException ioe) {
			String errorMessage = "Unable to save KieSession.";
			LOGGER.error(errorMessage, ioe);
			throw new RuntimeException(errorMessage, ioe);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					LOGGER.warn("Unable to close ObjectOutputStream.");
					// Not much we can do here, so swallowing excepion.
				}
			} else {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						LOGGER.warn("Unable to close FileOutputStream.");
						// Not much we can here, so swallowing exception.
					}
				}
			}
		}
		LOGGER.info("Succesfully saved KieSession to file.");
	}

	protected KieSession load(File file) {
		return load(file, null);
	}
	
	protected KieSession load(File file, KieBase kieBase) {
		try {
			LOGGER.info("Loading KieSession from file: " + file.getCanonicalPath());
		} catch (IOException ioe) {
			LOGGER.error("Error retrieving the canonical path of the file.", ioe);
			//Not much we can do here, just an error while logging. So swallow the exception.
			//BTW, if we get an error here, we will probably get errors somewhere further down as well.....
		}
		
		// TODO: We might want to buffer the reads ..
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);

			//If no new KieBase has been passed, we use the serialized one.
			if (kieBase == null) {
				kieBase = (KieBase) ois.readObject();
			} else {
				ois.readObject();
			}
			
			/*
			 * The KieSessionConfiguration contains, among other things the session clock.
			 * If we were using the PseudoClock, the correct time is already set when deserializing the KieSessionConfiguration.
			 */
			KieSessionConfiguration kieSessionConfiguration = (KieSessionConfiguration) ois.readObject();
			
			Marshaller marshaller = createMarshaller(kieBase);
			
			KieSession kieSession = marshaller.unmarshall(fis, kieSessionConfiguration, null);
			return kieSession;
		} catch (FileNotFoundException fnfe) {
			String errorMessage = "Cannot find file to load KieSession.";
			LOGGER.error(errorMessage, fnfe);
			throw new RuntimeException(errorMessage, fnfe);
		} catch (ClassNotFoundException cnfe) {
			String errorMessage = "Error loading serialized KieBase from file.";
			LOGGER.error(errorMessage, cnfe);
			throw new RuntimeException(errorMessage, cnfe);
		} catch (IOException ioe) {
			String errorMessage = "Error loading stored KieSession.";
			LOGGER.error(errorMessage, ioe);
			throw new RuntimeException(errorMessage, ioe);
		} finally {
			if (ois != null) {
				// This will also close the FIS.
				try {
					ois.close();
				} catch (IOException e) {
					LOGGER.warn("Unable to close ObjectInputStream.");
					// Not much we can do here, so swallowing exception.
				}
			} else {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						LOGGER.warn("Unable to close FileInputStream.");
						// Not much we can do here, so swallowing exception.
					}
				}
			}

		}
		
	}
	
	/**
	 * FactoryMethod which creates the proper {@link Marshaller}
	 * 
	 * @param kieBase the {@link KieBase} for which we need to create the {@link Marshaller}
	 * @return the {@link Marshaller}.
	 */
	protected abstract Marshaller createMarshaller(KieBase kieBase);
	
	protected File getFile() {
		return this.file;
	}
	
	
}
