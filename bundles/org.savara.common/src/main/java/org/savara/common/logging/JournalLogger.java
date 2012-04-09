/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
import java.util.logging.Logger;

/**
 * This class provides a Java Logger implementation of the journal.
 *
 */
public class JournalLogger implements org.scribble.common.logging.Journal {
	
	private static final Logger LOG=Logger.getLogger(JournalLogger.class.getName());

	/**
	 * {@inheritDoc}
	 */
	public void error(String arg0, Map<String, Object> arg1) {
		LOG.severe(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String arg0, Map<String, Object> arg1) {
		LOG.info(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void warning(String arg0, Map<String, Object> arg1) {
		LOG.warning(arg0);
	}

}
