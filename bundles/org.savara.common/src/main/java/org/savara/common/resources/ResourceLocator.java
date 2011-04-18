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
package org.savara.common.resources;

/**
 * This interface represents a resource locator that can be used to
 * load artifacts relative to another resource being processed.
 *
 */
public interface ResourceLocator {

	/**
	 * This method can be used to retrieve the URI of a resource which
	 * is located at the specified URI, potentially relative to a resource
	 * that is being processed.
	 * 
	 * @param uri The relative URI of the resource to load
	 * @return The URI, or null if not found
	 * @throws Exception Failed to obtain URI
	 */
	public java.net.URI getResourceURI(String uri) throws Exception;

}
