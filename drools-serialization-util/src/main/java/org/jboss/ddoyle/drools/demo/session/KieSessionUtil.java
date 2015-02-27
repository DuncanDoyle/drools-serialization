package org.jboss.ddoyle.drools.demo.session;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.jboss.ddoyle.drools.serialization.demo.model.Event;
import org.kie.api.runtime.KieSession;

/**
 * Utility methods for the {@link KieSession}. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class KieSessionUtil {
	
	public static void insertAndAdvance(KieSession kieSession, Event event) {
		kieSession.insert(event);
		// Advance the clock if required.
		PseudoClockScheduler clock = kieSession.getSessionClock();
		long advanceTime = event.getTimestamp().getTime() - clock.getCurrentTime();
		if (advanceTime > 0) {
			clock.advanceTime(advanceTime, TimeUnit.MILLISECONDS);
		}
	}

}
