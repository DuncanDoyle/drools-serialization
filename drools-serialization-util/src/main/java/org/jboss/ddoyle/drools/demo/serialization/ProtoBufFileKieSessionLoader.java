package org.jboss.ddoyle.drools.demo.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.core.marshalling.impl.IdentityPlaceholderResolverStrategy;
import org.jboss.ddoyle.drools.serialization.demo.model.proto.v1.Events.ProtoBufEvent;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.SimpleEvent;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper.EventPlaceholderWrapper;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper.ModelToProtobufModelMapper;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper.ModelV1ToProtobufModelV1Mapper;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper.ModelV2ToProtobufModelV1Mapper;
import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.runtime.KieSession;
import org.kie.internal.marshalling.MarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marshaller which uses {@link IdentityPlaceholderResolverStrategy} to serialize the facts using ProtoBufs.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class ProtoBufFileKieSessionLoader extends AbstractFileKieSessionLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtoBufFileKieSessionLoader.class);

	ObjectMarshallingStrategyAcceptor acceptor = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });

	MyIdentityPlaceholderResolverStrategy strategy = new MyIdentityPlaceholderResolverStrategy(acceptor);
	
	ModelToProtobufModelMapper mapper; 

	public ProtoBufFileKieSessionLoader(File file, ModelToProtobufModelMapper mapper) {
		super(file);
		this.mapper = mapper;
	}

	/**
	 * Overriding the save method, as we need to plug-in our custom logic to save the actual facts and id->object map after the session has
	 * been saved.
	 * 
	 */
	@Override
	public void save(KieSession kieSession) {
		save(kieSession, getFile());
		saveFacts(strategy.getIds());
		LOGGER.debug("Succesfully saved KieSession.");

	}

	/**
	 * Overriding the load method, as we need to plug-in our custom logic to load the actual facts and id->object map after the session has
	 * been saved.
	 * 
	 */
	@Override
	public KieSession load() {
		/*
		 * Before we start loading, we need to load the facts from the Protobuf file, populate the id -> Object map of our
		 * identityPlaceholderResolverStrategy and only after that can we load the session
		 * 
		 * TODO: Small problem with this approach is that the correct functioning of the marshaller depends on what we set here .... Maybe
		 * we should split loading and storing in 2 different implementations .... and do a check on whether the
		 * IdentityPlaceholderResolverStrategy is correctly initialized before returning the marshaller ...
		 */

		// Set the constructed map on the marshaller strategy.
		strategy.setIds(loadFactsAndIds());
		// Unmarshall the session.
		return load(getFile());
	}
	
	@Override
	public KieSession load(KieBase kieBase) {
		strategy.setIds(loadFactsAndIds());
		return load(getFile(), kieBase);
	}

	private Map<Integer, Object> loadFactsAndIds() {
		Map<Integer, Object> idToFactsMap = new HashMap<>();

		// First load the data from the protobuf file.
		String factsFilePath = getFactsFileName(getFile());
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(factsFilePath));
			ProtoBufEvent nextEvent = null;
			while ((nextEvent = ProtoBufEvent.parseDelimitedFrom(inputStream)) != null) {
				EventPlaceholderWrapper nextEventWrapper = null;
				if (mapper instanceof ModelV1ToProtobufModelV1Mapper) {
					nextEventWrapper = ModelV1ToProtobufModelV1Mapper.map(nextEvent);
				} else if (mapper instanceof ModelV2ToProtobufModelV1Mapper) {
					nextEventWrapper = ModelV2ToProtobufModelV1Mapper.map(nextEvent);
				}
				idToFactsMap.put(nextEventWrapper.getDroolsIdentityPlaceholder(), nextEventWrapper.getEvent());
			}
		} catch (IOException ioe) {
			String message = "Problem while loading facts from file.";
			LOGGER.error(message);
			throw new RuntimeException(message, ioe);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Not much we can do here, so logging and swallowing exception.
					LOGGER.warn("Unable to close inputstream.");
				}
			}
		}
		return idToFactsMap;

	}

	private void saveFacts(Map<Integer, Object> idToFactsMap) {
		// Create a new a facts file.
		String factsFilePath = getFactsFileName(getFile());
		File factsFile = new File(factsFilePath);
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(factsFile);

			Set<Map.Entry<Integer, Object>> idToFactsEntries = idToFactsMap.entrySet();
			for (Map.Entry<Integer, Object> nextIdToFactsEntry : idToFactsEntries) {
				Integer nextKey = nextIdToFactsEntry.getKey();
				Object nextEvent = nextIdToFactsEntry.getValue();

				System.out.println("Storing fact: '" + nextIdToFactsEntry.getValue() + "' with id: '" + nextIdToFactsEntry.getKey() + "'.");
				// TODO: We need to change this implementation to use something like a Visitor pattern that generates abstract ProtoBuf
				// messages.
				// The problem with this implementation is that it requires knowledge of our domain and ProtoBuf model. But good enough for
				// now,
				if (!(nextEvent instanceof SimpleEvent)) {
					throw new IllegalArgumentException("UnsupportedEventType.");
				}
				ProtoBufEvent protoBufEventMessage;
				if (mapper instanceof ModelV1ToProtobufModelV1Mapper) {
					 protoBufEventMessage = ((ModelV1ToProtobufModelV1Mapper) mapper).map(nextKey, (SimpleEvent) nextEvent);
				} else if (mapper instanceof ModelV2ToProtobufModelV1Mapper) {
					protoBufEventMessage = ((ModelV2ToProtobufModelV1Mapper) mapper).map(nextKey, (org.jboss.ddoyle.drools.serialization.demo.model.v2.SimpleEvent) nextEvent);
				} else {
					throw new IllegalStateException("Unsupported mapper configured.");
				}
				protoBufEventMessage.writeDelimitedTo(outputStream);
			}
		} catch (IOException ioe) {
			String message = "Error writing facts to file.";
			LOGGER.error(message);
			throw new RuntimeException(message, ioe);

		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOGGER.warn("Unable to close OutputStream.");
					// Not much we can do here, so swallowing excepion.
				}
			}
		}
	}

	@Override
	protected Marshaller createMarshaller(KieBase kieBase) {
		// Define which classes we want to marshall

		Marshaller marshaller = MarshallerFactory.newMarshaller(kieBase, new ObjectMarshallingStrategy[] { strategy });
		return marshaller;
	}

	/*
	 * Map<Integer, Object> ids = ((MyIdentityPlaceholderResolverStrategy) strategy).getIds(); Set<Map.Entry<Integer, Object>> idEntries =
	 * ids.entrySet();
	 * 
	 * for (Map.Entry<Integer, Object> nextEntry: idEntries) { System.out.println("Id: " + nextEntry.getKey() + ", object: " +
	 * nextEntry.getValue()); }
	 */

	private String getFactsFileName(File file) {
		String fileCanonicalPath = "";
		try {
			fileCanonicalPath = file.getCanonicalPath();
		} catch (IOException e) {
			String message = "Unable to get canonical path for file.";
			LOGGER.error(message);
			throw new RuntimeException(message);
		}
		return fileCanonicalPath + ".facts";

	}
}
