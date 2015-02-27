package org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper;

import org.jboss.ddoyle.drools.serialization.demo.model.Event;

/**
 * Wraps an event and it's ID in a <code>Drools</code> serialized session.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class EventPlaceholderWrapper {

	private final int droolsIdentityPlaceholder;
	private final Event event;

	public EventPlaceholderWrapper(final int droolsIdentityPlaceholder, final Event event) {
		this.droolsIdentityPlaceholder = droolsIdentityPlaceholder;
		this.event = event;
	}

	public int getDroolsIdentityPlaceholder() {
		return droolsIdentityPlaceholder;
	}

	public Event getEvent() {
		return event;
	}
	
}
