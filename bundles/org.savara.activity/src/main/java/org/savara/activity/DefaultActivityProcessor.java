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
 * This class implements a default activity validation manager
 * responsible for managing a set of analysers and validators,
 * and applying a supplied activity against them. The analysers
 * are applied prior to the validators, to derived additional
 * information that may be required during validation.
 */
public class DefaultActivityProcessor implements ActivityProcessor {

	private java.util.List<ActivityAnalyser> m_analysers=null;
	private java.util.List<ActivityFilter> m_filters=null;
	private java.util.List<ActivityValidator> m_validators=null;
	private java.util.List<ActivityStore> m_stores=null;
	private java.util.List<ActivityNotifier> m_notifiers=null;
	
	/**
	 * This method returns the list of activity analysers.
	 * 
	 * @return The analysers
	 */
	public java.util.List<ActivityAnalyser> getAnalysers() {
		if (m_analysers == null) {
			m_analysers = new java.util.Vector<ActivityAnalyser>();
		}
		return(m_analysers);
	}
	
	/**
	 * This method sets the list of activity analysers.
	 * 
	 * @param analysers The analysers
	 */
	public void setAnalysers(java.util.List<ActivityAnalyser> analysers) {
		m_analysers = analysers;
	}
	
	/**
	 * This method returns the list of activity filters.
	 * 
	 * @return The filters
	 */
	public java.util.List<ActivityFilter> getFilters() {
		if (m_filters == null) {
			m_filters = new java.util.Vector<ActivityFilter>();
		}
		return(m_filters);
	}
	
	/**
	 * This method sets the list of activity filters.
	 * 
	 * @param filters The filters
	 */
	public void setFilters(java.util.List<ActivityFilter> filters) {
		m_filters = filters;
	}
	
	/**
	 * This method returns the list of activity validators.
	 * 
	 * @return The validators
	 */
	public java.util.List<ActivityValidator> getValidators() {
		if (m_validators == null) {
			m_validators = new java.util.Vector<ActivityValidator>();
		}
		return(m_validators);
	}
	
	/**
	 * This method sets the list of activity validators.
	 * 
	 * @param validators The validators
	 */
	public void setValidators(java.util.List<ActivityValidator> validators) {
		m_validators = validators;
	}
	
	/**
	 * This method returns the list of activity stores.
	 * 
	 * @return The stores
	 */
	public java.util.List<ActivityStore> getStores() {
		if (m_stores == null) {
			m_stores = new java.util.Vector<ActivityStore>();
		}
		return(m_stores);
	}
	
	/**
	 * This method sets the list of activity stores.
	 * 
	 * @param stores The stores
	 */
	public void setStores(java.util.List<ActivityStore> stores) {
		m_stores = stores;
	}
	
	/**
	 * This method returns the list of activity notifiers.
	 * 
	 * @return The notifiers
	 */
	public java.util.List<ActivityNotifier> getNotifiers() {
		if (m_notifiers == null) {
			m_notifiers = new java.util.Vector<ActivityNotifier>();
		}
		return(m_notifiers);
	}
	
	/**
	 * This method sets the list of activity notifiers.
	 * 
	 * @param notifiers The notifiers
	 */
	public void setNotifiers(java.util.List<ActivityNotifier> notifiers) {
		m_notifiers = notifiers;
	}
	
	/**
	 * This method processes the supplied activity event against
	 * any predefined analysers and validators.
	 * 
	 * @param activity The activity event to be processed
	 */
	public void process(Activity activity) {
		
		// Invoke the analysers to derive any additional information
		for (ActivityAnalyser aa : getAnalysers()) {
			aa.analyse(activity);
		}
		
		// Immediately consider the activity relevant if no filters
		boolean process=(getFilters().size() == 0);
		
		// If filters defined, then check them until one indicates interest
		for (ActivityFilter af : getFilters()) {
			process = af.isRelevant(activity);
			
			if (process) {
				break;
			}
		}
		
		if (process) {
			// Validate the activity
			for (ActivityValidator av : getValidators()) {
				av.validate(activity);
			}
			
			// Store the activity
			for (ActivityStore as : getStores()) {
				as.store(activity);
			}
			
			for (ActivityNotifier an : getNotifiers()) {
				an.publish(activity);
			}
		}
	}
}
