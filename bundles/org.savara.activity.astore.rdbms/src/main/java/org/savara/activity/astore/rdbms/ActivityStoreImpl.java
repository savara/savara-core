/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.activity.astore.rdbms;

import org.savara.activity.ActivityStore;
import org.savara.activity.astore.rdbms.model.CorrelationIDEntity;
import org.savara.activity.model.Activity;
import org.savara.common.config.Configuration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: Jeff Yu
 * @date: 20/04/11
 */
public class ActivityStoreImpl implements ActivityStore {

    public  static final String TRANSACTION_MANAGER_JNDI_NAME = "transaction.manager.jndi.name";

	private static final Logger logger = Logger.getLogger(ActivityStoreImpl.class.toString());

    private TransactionManager txManager;

    private static EntityManagerFactory emf;

    private EntityManager entityManager;

    private boolean isInitialized = false;

    private Configuration configuration;

    private TxContext txContext;

    public  ActivityStoreImpl() {

    }

    public ActivityStoreImpl(TransactionManager manager) {
        this.txManager = manager;
    }


    protected void saveCorrelationIDEntity(CorrelationIDEntity entity) {

    }


    private void initialize() {
		if (emf == null) {
			Map<String, String> props = new HashMap<String, String>();
			//TODO: convert some of configuration properties into props map.
			emf = Persistence.createEntityManagerFactory("activity-store-unit", props);
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
			txContext = new JPAJTAContext(txManager, entityManager);
		} else {
			txContext = new JPANonTxContext(entityManager);
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

    public void setConfiguration(Configuration config) {
        this.configuration = config;
    }

    public void store(Activity activity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Activity queryById(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
