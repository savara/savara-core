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
package org.savara.protocol.util;

import java.util.Map;

public class FeedbackHandlerProxy implements org.savara.common.logging.FeedbackHandler {

	private org.scribble.common.logging.Journal m_journal=null;
	
	public FeedbackHandlerProxy(org.scribble.common.logging.Journal journal) {
		m_journal = journal;
	}

	public void error(String arg0, Map<String, Object> arg1) {
		m_journal.error(arg0, arg1);
	}

	public void info(String arg0, Map<String, Object> arg1) {
		m_journal.info(arg0, arg1);
	}

	public void warning(String arg0, Map<String, Object> arg1) {
		m_journal.warning(arg0, arg1);
	}
}
