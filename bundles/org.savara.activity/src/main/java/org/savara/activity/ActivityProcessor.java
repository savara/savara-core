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
 * This interface represents the activity processor.
 */
public interface ActivityProcessor {

	/**
	 * This method returns the activity analyser.
	 * 
	 * @return The analyser
	 */
	public ActivityAnalyser getAnalyser();
	
	/**
	 * This method sets the activity analyser.
	 * 
	 * @param analyser The analyser
	 */
	public void setAnalyser(ActivityAnalyser analyser);
	
	/**
	 * This method returns the activity filter.
	 * 
	 * @return The filter
	 */
	public ActivityFilter getFilter();
	
	/**
	 * This method sets the activity filter.
	 * 
	 * @param filter The filter
	 */
	public void setFilter(ActivityFilter filter);
	
	/**
	 * This method returns the activity store.
	 * 
	 * @return The store
	 */
	public ActivityStore getStore();
	
	/**
	 * This method sets the activity store.
	 * 
	 * @param store The store
	 */
	public void setStore(ActivityStore store);
	
	/**
	 * This method returns the activity notifier.
	 * 
	 * @return The notifier
	 */
	public ActivityNotifier getNotifier();
	
	/**
	 * This method sets the activity notifier.
	 * 
	 * @param notifier The notifier
	 */
	public void setNotifier(ActivityNotifier notifier);
	
	/**
	 * This method processes the supplied activity event against
	 * any predefined analysers, and optional persists and/or
	 * distributes the event.
	 * 
	 * @param activity The activity event to be processed
	 */
	public void process(Activity activity);
	
}
