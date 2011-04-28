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
import org.savara.activity.model.Context;
import org.savara.activity.model.Correlation;
import org.savara.common.config.Configuration;

import java.util.List;

/**
 * This interface represents the activity save
 * used to log activity events in a persistence save
 * and provide a query mechanism.
 *
 */
public interface ActivityStore {

	/**
     * This method pass the configuration object in.
	 * @param config
	 */
	public void setConfiguration(Configuration config);
	
	/**
	 * This method logs the activity record.
	 * 
	 * @param activity The activity
	 */
	public void save(Activity activity);


	/**
	 * This method retrieves the activity record
	 * associated with the supplied id.
	 * 
	 * @param id The activity id, or null if not found
	 */
	public Activity find(String id);


    /**
     * Find all activities that are associated with the correlations.
     * @param correlation
     * @return
     */
    public List<Activity> findByCorrelation(Correlation correlation);


    /**
     * Find all activities that have the context.
     * @param contexts
     * @return
     */
    public List<Activity> findByContext(List<Context> contexts);
	
	/**
	 * This method closes the log service.
	 */
	public void close();
	
}
