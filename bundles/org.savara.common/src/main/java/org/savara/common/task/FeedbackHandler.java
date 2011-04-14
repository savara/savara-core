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
package org.savara.common.task;

/**
 * This interface represents the component used by other scribble modules
 * to record issues.
 *
 */
public interface FeedbackHandler {

	/**
	 * The start line in the document at which the issue has been detected.
	 */
	public static final String START_LINE = "start.line";
	
	/**
	 * The start column in the document at which the issue has been detected.
	 */
	public static final String START_COLUMN = "start.column";
	
	/**
	 * The end line in the document at which the issue has been detected.
	 */
	public static final String END_LINE = "end.line";
	
	/**
	 * The end column in the document at which the issue has been detected.
	 */
	public static final String END_COLUMN = "end.column";
	
	/**
	 * The start position in the document at which the issue has been detected.
	 */
	public static final String START_POSITION = "position.start";
	
	/**
	 * The end position in the document at which the issue has been detected.
	 */
	public static final String END_POSITION = "position.end";
	
	/**
	 * The optional resource URL associated with the issue.
	 */
	public static final String RESOURCE_URL = "resource.url";
	
	/**
	 * This method records an error issue.
	 * 
	 * @param issue The issue text
	 * @param props The optional properties associated with the issue
	 */
	public void error(String issue, java.util.Map<String,Object> props);
	
	/**
	 * This method records a warning issue.
	 * 
	 * @param issue The issue text
	 * @param props The optional properties associated with the issue
	 */
	public void warning(String issue, java.util.Map<String,Object> props);
	
	/**
	 * This method records an information issue.
	 * 
	 * @param issue The issue text
	 * @param props The optional properties associated with the issue
	 */
	public void info(String issue, java.util.Map<String,Object> props);

}
