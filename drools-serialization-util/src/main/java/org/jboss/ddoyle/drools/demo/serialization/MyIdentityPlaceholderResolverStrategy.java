package org.jboss.ddoyle.drools.demo.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;

/**
 * 
 * Custom implementation of the default <code>Drools</code> {@link org.drools.core.marshalling.impl.IdentityPlaceholderResolverStrategy}
 * which does provide access to id and object maps.
 * 
 * See https://issues.jboss.org/browse/DROOLS-575
 * 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 * 
 */
public class MyIdentityPlaceholderResolverStrategy implements ObjectMarshallingStrategy {

	private Map<Integer, Object> ids;
	private Map<Object, Integer> objects;

	private ObjectMarshallingStrategyAcceptor acceptor;

	public MyIdentityPlaceholderResolverStrategy(ObjectMarshallingStrategyAcceptor acceptor) {
		this.acceptor = acceptor;
		this.ids = new HashMap<Integer, Object>();
		this.objects = new IdentityHashMap<Object, Integer>();
	}

	public MyIdentityPlaceholderResolverStrategy(ObjectMarshallingStrategyAcceptor acceptor, Map<Integer, Object> ids) {
		this(acceptor);
		setIds(ids);
	}

	public Object read(ObjectInputStream os) throws IOException, ClassNotFoundException {
		int id = os.readInt();
		return ids.get(id);
	}

	public void write(ObjectOutputStream os, Object object) throws IOException {
		Integer id = (Integer) objects.get(object);
		if (id == null) {
			id = ids.size();
			ids.put(id, object);
			objects.put(object, id);
		}
		os.writeInt(id);
	}

	public boolean accept(Object object) {
		return this.acceptor.accept(object);
	}

	public byte[] marshal(Context context, ObjectOutputStream os, Object object) {
		Integer id = (Integer) objects.get(object);
		if (id == null) {
			id = ids.size();
			ids.put(id, object);
			objects.put(object, id);
		}
		return intToByteArray(id.intValue());
	}

	public Object unmarshal(Context context, ObjectInputStream is, byte[] object, ClassLoader classloader) {
		return ids.get(byteArrayToInt(object));
	}

	private final byte[] intToByteArray(int value) {
		return new byte[] { (byte) ((value >>> 24) & 0xFF), (byte) ((value >>> 16) & 0xFF), (byte) ((value >>> 8) & 0xFF),
				(byte) (value & 0xFF) };
	}

	private final int byteArrayToInt(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
	}

	public Context createContext() {
		// no need for context
		return null;
	}

	public Map<Integer, Object> getIds() {
		return ids;
	}

	public void setIds(Map<Integer, Object> ids) {
		this.ids = ids;
		objects.clear();
		for (Map.Entry<Integer, Object> idEntry : ids.entrySet()) {
			objects.put(idEntry.getValue(), idEntry.getKey());
		}
	}

}
