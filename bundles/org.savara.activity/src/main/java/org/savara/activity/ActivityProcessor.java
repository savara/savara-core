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
	 * This method adds a new activity analyser.
	 * 
	 * @param analyser The analyser
	 */
	public void addActivityAnalyser(ActivityAnalyser analyser);
	
	/**
	 * This method removes an existing activity analyser.
	 * 
	 * @param analyser The analyser
	 */
	public void removeActivityAnalyser(ActivityAnalyser analyser);
	
	/**
	 * This method adds a new activity filter.
	 * 
	 * @param filter The filter
	 */
	public void addActivityFilter(ActivityFilter filter);
	
	/**
	 * This method removes an existing activity filter.
	 * 
	 * @param filter The filter
	 */
	public void removeActivityFilter(ActivityFilter filter);
	
	/**
	 * This method adds a new activity validator.
	 * 
	 * @param validator The validator
	 */
	public void addActivityValidator(ActivityValidator validator);
	
	/**
	 * This method removes an existing activity validator.
	 * 
	 * @param validator The validator
	 */
	public void removeActivityValidator(ActivityValidator validator);
	
	/**
	 * This method adds a new activity store.
	 * 
	 * @param store The store
	 */
	public void addActivityStore(ActivityStore store);
	
	/**
	 * This method removes an existing activity store.
	 * 
	 * @param store The store
	 */
	public void removeActivityStore(ActivityStore store);
	
	/**
	 * This method adds a new activity notifier.
	 * 
	 * @param notifier The notifier
	 */
	public void addActivityNotifier(ActivityNotifier notifier);
	
	/**
	 * This method removes an existing activity notifier.
	 * 
	 * @param notifier The notifier
	 */
	public void removeActivityNotifier(ActivityNotifier notifier);
	
	/**
	 * This method processes the supplied activity event against
	 * any predefined analysers and validators.
	 * 
	 * @param activity The activity event to be processed
	 */
	public void process(Activity activity);
	
}
