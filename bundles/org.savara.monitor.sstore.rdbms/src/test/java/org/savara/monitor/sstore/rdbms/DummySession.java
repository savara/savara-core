package org.savara.monitor.sstore.rdbms;

import java.io.Serializable;

public class DummySession implements Serializable{

	private static final long serialVersionUID = -6624913741004684358L;
	
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
