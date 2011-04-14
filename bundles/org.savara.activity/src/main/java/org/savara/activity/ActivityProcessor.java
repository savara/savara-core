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

/**
 * This interface represents the activity processor
 * responsible for managing a set of analysers and validators,
 * and applying a supplied activity against them. The analysers
 * are applied prior to the validators, to derived additional
 * information that may be required during validation.
 */
public interface ActivityProcessor {

	/**
	 * This method returns the list of activity analysers.
	 * 
	 * @return The analysers
	 */
	public java.util.List<ActivityAnalyser> getAnalysers();
	
	/**
	 * This method sets the list of activity analysers.
	 * 
	 * @param analysers The analysers
	 */
	public void setAnalysers(java.util.List<ActivityAnalyser> analysers);
	
	/**
	 * This method returns the list of activity filters.
	 * 
	 * @return The filters
	 */
	public java.util.List<ActivityFilter> getFilters();
	
	/**
	 * This method sets the list of activity filters.
	 * 
	 * @param filters The filters
	 */
	public void setFilters(java.util.List<ActivityFilter> filters);
	
	/**
	 * This method returns the list of activity validators.
	 * 
	 * @return The validators
	 */
	public java.util.List<ActivityValidator> getValidators();
	
	/**
	 * This method sets the list of activity validators.
	 * 
	 * @param validators The validators
	 */
	public void setValidators(java.util.List<ActivityValidator> validators);
	
	/**
	 * This method returns the list of activity stores.
	 * 
	 * @return The stores
	 */
	public java.util.List<ActivityStore> getStores();
	
	/**
	 * This method sets the list of activity stores.
	 * 
	 * @param stores The stores
	 */
	public void setStores(java.util.List<ActivityStore> stores);
	
	/**
	 * This method returns the list of activity notifiers.
	 * 
	 * @return The notifiers
	 */
	public java.util.List<ActivityNotifier> getNotifiers();
	
	/**
	 * This method sets the list of activity notifiers.
	 * 
	 * @param notifiers The notifiers
	 */
	public void setNotifiers(java.util.List<ActivityNotifier> notifiers);
	
	/**
	 * This method processes the supplied activity event against
	 * any predefined analysers and validators.
	 * 
	 * @param activity The activity event to be processed
	 */
	public void process(Activity activity);
	
}
