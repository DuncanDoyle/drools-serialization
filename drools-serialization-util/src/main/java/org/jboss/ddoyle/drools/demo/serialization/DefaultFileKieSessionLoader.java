package org.jboss.ddoyle.drools.demo.serialization;

import java.io.File;

import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.internal.marshalling.MarshallerFactory;

public class DefaultFileKieSessionLoader extends AbstractFileKieSessionLoader  {
	
	public DefaultFileKieSessionLoader(File file) {
		super(file);
	}

	@Override
	protected Marshaller createMarshaller(KieBase kieBase) {
		ObjectMarshallingStrategyAcceptor acceptor = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });
		ObjectMarshallingStrategy strategy = MarshallerFactory.newSerializeMarshallingStrategy(acceptor);
		Marshaller marshaller = MarshallerFactory.newMarshaller(kieBase, new ObjectMarshallingStrategy[] { strategy });
		return marshaller;
	}

}
