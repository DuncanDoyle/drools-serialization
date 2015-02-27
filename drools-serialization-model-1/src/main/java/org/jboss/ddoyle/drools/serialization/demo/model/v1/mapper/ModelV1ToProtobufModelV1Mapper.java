package org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper;

import java.util.Date;

import org.jboss.ddoyle.drools.serialization.demo.model.Event;
import org.jboss.ddoyle.drools.serialization.demo.model.proto.v1.Events.ProtoBufEvent;
import org.jboss.ddoyle.drools.serialization.demo.model.proto.v1.Events.ProtoBufEvent.Type;
import org.jboss.ddoyle.drools.serialization.demo.model.proto.v1.Events.ProtoBufSimpleEvent;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.SimpleEvent;

/**
 * Maps a version 1 of our Domain model to version 1 of our ProtoBuf model.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 * 
 */
public class ModelV1ToProtobufModelV1Mapper implements ModelToProtobufModelMapper {

	/*
	private final ExtensionRegistry extensionRegistry;

	public ModelV1ToProtobufModelV1Mapper() {

		//Register the extensions in our ProtoBuf file.
		extensionRegistry = ExtensionRegistry.newInstance();
		Events.registerAllExtensions(extensionRegistry);

	}
	*/

	public static ProtoBufEvent map(int droolsIdentityPlaceholder, SimpleEvent simpleEvent) {
		ProtoBufEvent.Builder eventBuilder = ProtoBufEvent.newBuilder();
		eventBuilder.setType(Type.SimpleEvent);
		eventBuilder.setDroolsIdentityPlaceholder(droolsIdentityPlaceholder);
		eventBuilder.setId(simpleEvent.getId());
		eventBuilder.setTimestamp(simpleEvent.getTimestamp().getTime());
		
		//builder.setExtension(extensionRegistry.findExtensionByName("bla").defaultInstance, Type.SimpleEvent);
		ProtoBufSimpleEvent.Builder simpleEventBuilder = ProtoBufSimpleEvent.newBuilder();
		
		//Here we could set the fields of the extension.
		//simpleEventBuilder.setTestMessage("This is just a test message to test extensions.");
		
		//Set the extention on the message.
		eventBuilder.setExtension(ProtoBufSimpleEvent.event, simpleEventBuilder.build());
		
		return eventBuilder.build();
	}

	/**
	 * Map from ProtoBuf back to Event.
	 * 
	 * @param event
	 * @return
	 */
	public static EventPlaceholderWrapper map(ProtoBufEvent protoBufEvent) {
		
		Type concreteType = protoBufEvent.getType();
		int droolsIdentityPlaceholder = protoBufEvent.getDroolsIdentityPlaceholder(); 
		Event event = null;

		switch (concreteType) {
		case SimpleEvent:
			String id = protoBufEvent.getId();
			long timestamp = protoBufEvent.getTimestamp();
			event = new SimpleEvent(id, new Date(timestamp));
			break;
		case OtherEvent:
			break;
		default:
			throw new IllegalArgumentException("Unexpected Event Type.");
		}
		return new EventPlaceholderWrapper(droolsIdentityPlaceholder, event);
		
	}
}
