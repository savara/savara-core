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
package org.savara.activity.astore.rdbms;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author Jeff Yu
 *
 */
public class JPAJTAContext implements TxContext {
	
	private static Logger logger = Logger.getLogger(JPAJTAContext.class.getName());
	
	private TransactionManager txManager;
	
	private EntityManager em;
	
	public JPAJTAContext(TransactionManager txManager, EntityManager em) {
		this.txManager = txManager;
		this.em = em;
	}
	
	public void begin() {
		try {
			if (txManager.getStatus() == Status.STATUS_ACTIVE) {
				em.joinTransaction();
			}
		}catch (SystemException e) {
			logger.log(Level.SEVERE, "error in joining in transaction");
			throw new RuntimeException(e);
		}

	}

	public void commit() {

	}

	public void rollback() {
		try {
			if (txManager.getStatus() == Status.STATUS_ACTIVE) {
				txManager.setRollbackOnly();
			}
		}catch (SystemException e) {
			logger.log(Level.SEVERE, "unable to set roll back.");
			throw new RuntimeException(e);
		}

	}

}
