/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.common.logging;

import java.util.Map;

public class DefaultFeedbackHandler implements FeedbackHandler {
	
	private java.util.List<IssueDetails> m_issues=new java.util.Vector<IssueDetails>();
	private boolean f_errors=false;
	private boolean f_warnings=false;

	public void error(String issue, Map<String, Object> props) {
		m_issues.add(new IssueDetails(IssueType.Error, issue, props));
		f_errors = true;
	}

	public void info(String issue, Map<String, Object> props) {
		m_issues.add(new IssueDetails(IssueType.Info, issue, props));
	}

	public void warning(String issue, Map<String, Object> props) {
		m_issues.add(new IssueDetails(IssueType.Warning, issue, props));
		f_warnings = true;
	}
	
	/**
	 * This method returns the list of issues that have been reported to this
	 * journal.
	 * 
	 * @return The list of issues
	 */
	public java.util.List<IssueDetails> getIssues() {
		return(m_issues);
	}
	
	/**
	 * This method determines whether any errors have been reported.
	 * 
	 * @return Whether errors have been reported
	 */
	public boolean hasErrors() {
		return(f_errors);
	}
	
	/**
	 * This method determines whether any warnings have been reported.
	 * 
	 * @return Whether warnings have been reported
	 */
	public boolean hasWarnings() {
		return(f_warnings);
	}
	
	/**
	 * This method applies any cached issues to the supplied logger.
	 * 
	 * @param logger The logger
	 */
	public void apply(FeedbackHandler logger) {
		for (IssueDetails id : m_issues) {
			if (id.getIssueType() == IssueType.Error) {
				logger.error(id.getMessage(), id.getProperties());
			} else if (id.getIssueType() == IssueType.Info) {
				logger.info(id.getMessage(), id.getProperties());
			} else if (id.getIssueType() == IssueType.Warning) {
				logger.warning(id.getMessage(), id.getProperties());
			}
		}
	}

	public enum IssueType {
		Error,
		Info,
		Warning
	}

	public class IssueDetails {
		public IssueDetails(IssueType type, String mesg,
						Map<String, Object> props) {
			m_type = type;
			m_message = mesg;
			m_properties = props;
		}
		
		public IssueType getIssueType() {
			return(m_type);
		}
		
		public String getMessage() {
			return(m_message);
		}
		
		public Map<String,Object> getProperties() {
			return(m_properties);
		}
		
		private IssueType m_type=IssueType.Info;
		private String m_message=null;
		private Map<String,Object> m_properties=null;
	}
}
