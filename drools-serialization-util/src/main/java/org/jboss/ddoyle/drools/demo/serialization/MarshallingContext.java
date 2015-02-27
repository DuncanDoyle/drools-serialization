package org.jboss.ddoyle.drools.demo.serialization;

import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.marshalling.ObjectMarshallingStrategyStore;

public class MarshallingContext {

	
	private ObjectMarshallingStrategyAcceptor acceptor;
	
	private ObjectMarshallingStrategy strategy;
	
	private Marshaller marshaller;
	
	
	public MarshallingContext(Marshaller marshaller) {
		ObjectMarshallingStrategyStore strategyStore = marshaller.getMarshallingConfiguration().getObjectMarshallingStrategyStore();
	}
}
