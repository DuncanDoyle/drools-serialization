package org.jboss.ddoyle.drools.demo;

import static org.jboss.ddoyle.drools.demo.session.KieSessionUtil.insertAndAdvance;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.jboss.ddoyle.drools.demo.io.FactsLoader;
import org.jboss.ddoyle.drools.demo.serialization.DefaultFileKieSessionLoader;
import org.jboss.ddoyle.drools.demo.serialization.KieSessionLoader;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.SimpleEvent;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class Main {

	//private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd:HH.mm.ss.SSS");

	private static final String DROOLS_SERIALIZED_KIESESSION_FILENAME = "/tmp/drools-sessions/serialized-session.dsk";

	public static void main(String[] args) {

		FactsLoader factsLoader = new FactsLoader();
		InputStream eventsFileStream = Main.class.getClassLoader().getResourceAsStream("events.csv");
		List<SimpleEvent> events = factsLoader.loadFacts(eventsFileStream);

		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.getKieClasspathContainer();

		KieSession kieSession = kieContainer.newKieSession();
		
		try {
			for (SimpleEvent nextEvent : events) {
				insertAndAdvance(kieSession, nextEvent);
				kieSession.fireAllRules();
			}
			saveKieSession(kieSession);
		} finally {
			kieSession.dispose();
		}
	}

	

	private static void saveKieSession(KieSession kieSession) {

		KieSessionLoader loader = new DefaultFileKieSessionLoader(new File(DROOLS_SERIALIZED_KIESESSION_FILENAME));
		loader.save(kieSession);
	}

	private static KieSession loadKieSession() {
		KieSessionLoader loader = new DefaultFileKieSessionLoader(new File(DROOLS_SERIALIZED_KIESESSION_FILENAME));
		return loader.load();
	}

}
