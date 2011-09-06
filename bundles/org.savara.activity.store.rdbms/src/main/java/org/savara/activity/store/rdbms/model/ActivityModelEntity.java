/**
 * 
 */
package org.savara.activity.store.rdbms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * 
 * @author jeffyu
 *
 */
@Entity
@Table(name = "SAVARA_ACT_MODEL")
public class ActivityModelEntity {
	
	@Id
	@Column(name="ID")
	@GeneratedValue
	private long id;
	
	@Lob
	@Column(name="MODEL")
	private String model;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

}
