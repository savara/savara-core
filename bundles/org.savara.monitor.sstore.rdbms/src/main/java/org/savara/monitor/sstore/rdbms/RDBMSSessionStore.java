/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import org.savara.common.config.Configuration;
import org.savara.monitor.ConversationInstanceId;
import org.savara.monitor.SessionStore;
import org.savara.protocol.ProtocolId;

/**
 * This class defines the RDBMS implementation of the SessionStore
 * interface.
 * 
 * @author Jeff Yu
 *
 */
public class RDBMSSessionStore implements SessionStore {
		
	public static final String TRANSACTION_MANAGER_JNDI_NAME = "transaction.manager.jndi.name";
	
	private static final Logger logger = Logger.getLogger(RDBMSSessionStore.class.toString());
	
	
	private static  EntityManagerFactory emf;
	
	private EntityManager entityManager;
	
	private TransactionManager txManager;
	
	private TxContext tx;

    private Configuration configuration;

    private boolean isInitialized = false;
	
	public RDBMSSessionStore(TransactionManager txManager) {
		this.txManager = txManager;
	}
	
	public RDBMSSessionStore() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#setConfiguration(org.savara.common.configuration.Configuration)
	 */
	public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
	}


    private void initialization() {
		if (emf == null) {
			Map<String, String> props = new HashMap<String, String>();
			//TODO: convert some of configuration properties into props map.
			emf = Persistence.createEntityManagerFactory("savara-monitor-session", props);
		}
		if (entityManager == null || !entityManager.isOpen()) {
			entityManager = emf.createEntityManager();
		}

		if (configuration != null) {
			String txManagerJndiName = configuration.getProperty(TRANSACTION_MANAGER_JNDI_NAME);
			if (txManagerJndiName != null) {
				getTransactionManagerFromJNDI(txManagerJndiName);
			}
		}
		if (txManager != null) {
			tx = new JPAJTAContext(txManager, entityManager);
		} else {
			tx = new JPANonTxContext(entityManager);
		}
        this.isInitialized = true;
    }

	private void getTransactionManagerFromJNDI(String txManagerJndiName) {
		try {
			txManager = (TransactionManager) InitialContext.doLookup(txManagerJndiName);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Error in getting Transaction Manager from JNDI: " + txManagerJndiName);
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#create(org.savara.protocol.ProtocolId, org.savara.monitor.ConversationInstanceId)
	 */
	public java.io.Serializable create(ProtocolId pid, ConversationInstanceId cid, Serializable session) {

        if (!isInitialized) {
            initialization();
        }

		RDBMSSession psession = new RDBMSSession();
		
		psession.setProtocolName(pid.getName());
		psession.setProtocolRole(pid.getRole());
		psession.setConversationInstanceId(cid.getId());		
		psession.setSession(session);
		
		Serializable s = this.find(pid, cid);
		if (s != null) {
			throw new IllegalArgumentException("The session with [" + pid + cid +"] already existed.");
		}
		
		try {		
			tx.begin();
			entityManager.persist(psession);
			tx.commit();
			return psession.getSession();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error in creating a session");
			tx.rollback();
			throw new RuntimeException(e);
		} 
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#find(org.savara.protocol.ProtocolId, org.savara.monitor.ConversationInstanceId)
	 */
	public java.io.Serializable find(ProtocolId pid, ConversationInstanceId cid) {

        if (!isInitialized) {
            initialization();
        }

        try {
			tx.begin();
			List<?> sessions = entityManager.createNamedQuery(RDBMSSession.GET_SESSION_BY_KEY)
								.setParameter("name", pid.getName())
								.setParameter("role", pid.getRole())
								.setParameter("id", cid.getId())
								.getResultList();
			tx.commit();
			if (sessions == null || sessions.size() < 1) {
				return null;
			}
			if (sessions.size() > 1) {
				throw new IllegalArgumentException("Found two sessions with [" + pid + cid + "]");
			}
			RDBMSSession session = (RDBMSSession)sessions.get(0);
			return session.getSession();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error in finding a session");
			tx.rollback();
			throw new RuntimeException(e);
		} 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#remove(org.savara.protocol.ProtocolId, org.savara.monitor.ConversationInstanceId)
	 */
	public void remove(ProtocolId pid, ConversationInstanceId cid) {
		if (!isInitialized) {
            initialization();
        }

        try {
			tx.begin();
			entityManager.createNamedQuery(RDBMSSession.REMOVE_SESSION_BY_KEY)
							.setParameter("name", pid.getName())
							.setParameter("role", pid.getRole())
							.setParameter("id", cid.getId())
							.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error in removing a session");
			tx.rollback();
			throw new RuntimeException(e);
		} 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#update(org.savara.protocol.ProtocolId, org.savara.monitor.ConversationInstanceId, java.io.Serializable)
	 */
	public void update(ProtocolId pid, ConversationInstanceId cid,
					java.io.Serializable session)  {
		if (!isInitialized) {
            initialization();
        }
        try {
			tx.begin();
			RDBMSSession s = (RDBMSSession) entityManager.createNamedQuery(RDBMSSession.GET_SESSION_BY_KEY)
								.setParameter("name", pid.getName())
								.setParameter("role", pid.getRole())
								.setParameter("id", cid.getId())
								.getSingleResult();
			s.setSession(session);
			entityManager.persist(s);
			tx.commit();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error in updating a session");
			tx.rollback();
			throw new RuntimeException(e);
		} 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.savara.monitor.SessionStore#close()
	 */
	public void close() {
        if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
		if (emf != null) {
			emf.close();
			emf = null;
		}
	}
	
	
}
