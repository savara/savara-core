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
package org.savara.activity;

import org.savara.activity.model.Activity;
import org.savara.common.config.Configuration;

/**
 * This interface represents the activity store 
 * used to log activity events in a persistence store
 * and provide a query mechanism.
 *
 */
public interface ActivityStore {

	/**
     * This method pass the configuratioin object in.
	 * @param config
	 */
	public void setConfiguration(Configuration config);
	
	/**
	 * This method logs the activity record.
	 * 
	 * @param activity The activity
	 */
	public void store(Activity activity);
	
	/**
	 * This method retrieves the activity record
	 * associated with the supplied id.
	 * 
	 * @param id The activity id, or null if not found
	 */
	public Activity queryById(String id);
	
	/**
	 * This method closes the log service.
	 */
	public void close();
	
}
