package org.jboss.ddoyle.drools.serialization.demo.model;

import java.io.Serializable;
import java.util.Date;


public interface Event extends Serializable {
	
	public abstract String getId();
	
	public abstract Date getTimestamp();

}
