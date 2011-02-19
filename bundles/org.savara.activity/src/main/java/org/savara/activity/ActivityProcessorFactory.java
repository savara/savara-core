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

/**
 * This class provides a factory for the ActivityProcessor
 * component.
 *
 */
public class ActivityProcessorFactory {

	private static ActivityProcessor m_instance=null;
	
	/**
	 * This method returns the activity processor.
	 * 
	 * @return The activity processor
	 */
	public static ActivityProcessor getActivityProcessor() {
		return(m_instance);
	}
	
	/**
	 * This method sets the activity processor associated with the
	 * factory.
	 * 
	 * @param processor The activity processor
	 */
	public static void setActivityProcessor(ActivityProcessor processor) {
		m_instance = processor;
	}
}
