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

	private java.util.List<ActivityAnalyser> m_analysers=new java.util.Vector<ActivityAnalyser>();
	private java.util.List<ActivityFilter> m_filters=new java.util.Vector<ActivityFilter>();
	private java.util.List<ActivityValidator> m_validators=new java.util.Vector<ActivityValidator>();
	private java.util.List<ActivityStore> m_stores=new java.util.Vector<ActivityStore>();
	private java.util.List<ActivityNotifier> m_notifiers=new java.util.Vector<ActivityNotifier>();
	
	protected java.util.List<ActivityAnalyser> getAnalysers() {
		return(m_analysers);
	}
	
	protected java.util.List<ActivityFilter> getFilters() {
		return(m_filters);
	}
	
	protected java.util.List<ActivityValidator> getValidators() {
		return(m_validators);
	}
	
	protected java.util.List<ActivityStore> getStores() {
		return(m_stores);
	}
	
	protected java.util.List<ActivityNotifier> getNotifiers() {
		return(m_notifiers);
	}
	
	/**
	 * This method adds a new activity analyser.
	 * 
	 * @param analyser The analyser
	 */
	public void addActivityAnalyser(ActivityAnalyser analyser) {
		m_analysers.add(analyser);
	}
	
	/**
	 * This method removes an existing activity analyser.
	 * 
	 * @param analyser The analyser
	 */
	public void removeActivityAnalyser(ActivityAnalyser analyser) {
		m_analysers.remove(analyser);
	}
	
	/**
	 * This method adds a new activity filter.
	 * 
	 * @param filter The filter
	 */
	public void addActivityFilter(ActivityFilter filter) {
		m_filters.add(filter);
	}
	
	/**
	 * This method removes an existing activity filter.
	 * 
	 * @param filter The filter
	 */
	public void removeActivityFilter(ActivityFilter filter) {
		m_filters.remove(filter);
	}
	
	/**
	 * This method adds a new activity validator.
	 * 
	 * @param validator The validator
	 */
	public void addActivityValidator(ActivityValidator validator) {
		m_validators.add(validator);
	}
	
	/**
	 * This method removes an existing activity validator.
	 * 
	 * @param validator The validator
	 */
	public void removeActivityValidator(ActivityValidator validator) {
		m_validators.remove(validator);
	}
	
	/**
	 * This method adds a new activity notifier.
	 * 
	 * @param notifier The notifier
	 */
	public void addActivityNotifier(ActivityNotifier notifier) {
		m_notifiers.add(notifier);
	}
	
	/**
	 * This method removes an existing activity notifier.
	 * 
	 * @param notifier The notifier
	 */
	public void removeActivityNotifier(ActivityNotifier notifier) {
		m_notifiers.remove(notifier);
	}
	
	/**
	 * This method adds a new activity store.
	 * 
	 * @param store The store
	 */
	public void addActivityStore(ActivityStore store) {
		m_stores.add(store);
	}
	
	/**
	 * This method removes an existing activity store.
	 * 
	 * @param store The store
	 */
	public void removeActivityStore(ActivityStore store) {
		m_stores.remove(store);
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
