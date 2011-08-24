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

/**
 * 
 * @author Jeff Yu
 *
 */
public class JPANonTxContext implements TxContext {
	
	private EntityManager em;
	
	public JPANonTxContext(EntityManager em) {
		this.em = em;
	}

	/* (non-Javadoc)
	 * @see org.savara.monitor.sstore.rdbms.TxContext#begin()
	 */
	public void begin() {
		em.getTransaction().begin();
	}

	/* (non-Javadoc)
	 * @see org.savara.monitor.sstore.rdbms.TxContext#commit()
	 */
	public void commit() {
		em.getTransaction().commit();
	}

	/* (non-Javadoc)
	 * @see org.savara.monitor.sstore.rdbms.TxContext#rollback()
	 */
	public void rollback() {
		em.getTransaction().rollback();
	}

}
