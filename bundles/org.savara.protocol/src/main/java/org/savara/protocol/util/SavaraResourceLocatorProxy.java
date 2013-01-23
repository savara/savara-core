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

import java.net.URI;

import org.savara.common.resources.ResourceLocator;

/**
 * This class provides a proxy between the Savara and Scribble
 * resource locators.
 *
 */
public class SavaraResourceLocatorProxy implements ResourceLocator {
	
	private org.scribble.common.resource.ResourceLocator m_locator=null;

	public SavaraResourceLocatorProxy(org.scribble.common.resource.ResourceLocator locator) {
		m_locator = locator;
	}

	public URI getResourceURI(String uri) throws Exception {
		return(m_locator.getResourceURI(uri));
	}

	public String getRelativePath(String path) throws Exception {
		return (null);
	}
}
