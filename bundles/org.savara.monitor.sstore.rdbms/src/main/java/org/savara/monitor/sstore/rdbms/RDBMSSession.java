/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.monitor.sstore.rdbms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * 
 * @author Jeff Yu
 *
 */
@Entity
@Table(name="SAVARA_SESSION")
@NamedQueries({
	@NamedQuery(name=RDBMSSession.GET_SESSION_BY_KEY, query="select s from RDBMSSession as s where s.protocolName = :name and s.protocolRole = :role and s.conversationInstanceId = :id"),
	@NamedQuery(name=RDBMSSession.REMOVE_SESSION_BY_KEY, query="delete from RDBMSSession as s where s.protocolName = :name and s.protocolRole = :role and s.conversationInstanceId = :id")
})
public class RDBMSSession implements Serializable{
	
	public static final String GET_SESSION_BY_KEY = "GET_SESSION_BY_KEY";
	
	public static final String REMOVE_SESSION_BY_KEY = "REMOVE_SESSION_BY_KEY";
	
	private static final long serialVersionUID = 5514308301188696320L;

	private static final Logger logger = Logger.getLogger(RDBMSSession.class.toString()) ;
	
	private long id;

	private String protocolName;
	
	private String protocolRole;
	
	private String conversationInstanceId;
	
	private Serializable session;	
	
	
	@Id
	@Column(name="ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}

	
	@Column(name="PROTOCOL_NAME")
	public String getProtocolName() {
		return protocolName;
	}


	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	@Column(name="PROTOCOL_ROLE")
	public String getProtocolRole() {
		return protocolRole;
	}


	public void setProtocolRole(String protocolRole) {
		this.protocolRole = protocolRole;
	}

	@Column(name="CONVERSATION_INSTANCE_ID")
	public String getConversationInstanceId() {
		return conversationInstanceId;
	}


	public void setConversationInstanceId(String conversationInstanceId) {
		this.conversationInstanceId = conversationInstanceId;
	}

	@Transient
	public Serializable getSession() {
		return session;
	}


	public void setSession(Serializable session) {
		this.session = session;
	}

	@Lob
	@Column(name = "SESSION_IN_BYTE")
	public byte[] getByteSession() {
		if (this.session != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = null;
			try {
				os = new ObjectOutputStream(bos);
				os.writeObject(this.session);
				return bos.toByteArray();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error in getting session in byte.");
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error in closing ObjectOutputStream.");
				}
			}
		}
		return null;
	}


	public void setByteSession(byte[] byteSession) {
		if (byteSession != null && byteSession.length > 0) {
			ByteArrayInputStream bis = new ByteArrayInputStream(byteSession);
			ObjectInputStream is = null;
			try {
				is = new ObjectInputStream(bis);
				this.session = (Serializable)is.readObject();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error in set session in byte");
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Error in closing ObjectInputStream.");
					}
				}
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RDBMSSession) {
			RDBMSSession s = (RDBMSSession) o;
			if (s.getId() == this.id) {
				return true;
			}
		}
		return false;
	}
	
	
}
