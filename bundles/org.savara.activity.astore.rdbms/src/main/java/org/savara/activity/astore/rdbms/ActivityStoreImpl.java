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
import org.savara.activity.model.Activity;
import org.savara.common.config.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import java.util.logging.Logger;

/**
 * @author: Jeff Yu
 * @date: 20/04/11
 */
public class ActivityStoreImpl implements ActivityStore {

    public  static final String TRANSACTION_MANAGER_JNDI_NAME = "transaction.manager.jndi.name";

	private static final Logger logger = Logger.getLogger(ActivityStoreImpl.class.toString());

    private TransactionManager txMgr;

    private static EntityManagerFactory emf;

    public  ActivityStoreImpl() {

    }

    public ActivityStoreImpl(TransactionManager manager) {
        this.txMgr = manager;
    }

    public void initialize(Configuration config) {

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
