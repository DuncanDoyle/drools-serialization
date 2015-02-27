package org.jboss.ddoyle.drools.demo;

import static org.jboss.ddoyle.drools.demo.session.KieSessionUtil.insertAndAdvance;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.jboss.ddoyle.drools.demo.io.FactsLoader;
import org.jboss.ddoyle.drools.demo.serialization.KieSessionLoader;
import org.jboss.ddoyle.drools.demo.serialization.ProtoBufFileKieSessionLoader;
import org.jboss.ddoyle.drools.serialization.demo.model.v1.mapper.ModelV2ToProtobufModelV1Mapper;
import org.jboss.ddoyle.drools.serialization.demo.model.v2.SimpleEvent;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class Main {

	private static final String DROOLS_SERIALIZED_KIESESSION_FILENAME = "/tmp/drools-sessions/protobuf-session.dsk";

	public static void main(String[] args) {

		FactsLoader factsLoader = new FactsLoader();
		InputStream eventsFileStream = Main.class.getClassLoader().getResourceAsStream("events.csv");
		List<SimpleEvent> events = factsLoader.loadFacts(eventsFileStream);
		
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.getKieClasspathContainer();
		KieBase newKieBase = kieContainer.getKieBase();

		KieSession kieSession = loadKieSession(newKieBase);
		
		try {
			for (SimpleEvent nextEvent : events) {
				insertAndAdvance(kieSession, nextEvent);
				kieSession.fireAllRules();
			}
		} finally {
			kieSession.dispose();
		}
	}
	
	private static void saveKieSession(KieSession kieSession) {
		KieSessionLoader loader = new ProtoBufFileKieSessionLoader(new File(DROOLS_SERIALIZED_KIESESSION_FILENAME), new ModelV2ToProtobufModelV1Mapper());
		loader.save(kieSession);
	}

	private static KieSession loadKieSession(KieBase newKieBase) {
		KieSessionLoader loader = new ProtoBufFileKieSessionLoader(new File(DROOLS_SERIALIZED_KIESESSION_FILENAME), new ModelV2ToProtobufModelV1Mapper());
		return loader.load(newKieBase);
	}

}
