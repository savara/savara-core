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
package org.savara.common.resources;

import java.net.URI;

/**
 * This class represents a default implement of the resource locator
 * interface, that can be used to load artifacts relative to a base
 * location.
 *
 */
public class DefaultResourceLocator implements ResourceLocator {

    private java.io.File _baseDir=null;

    /**
     * This constructor initializes the default resource locator
     * with the location of the base directory from which the
     * relative file paths should be derived.
     * 
     * @param baseDir The base directory
     */
    public DefaultResourceLocator(java.io.File baseDir) {
        _baseDir = baseDir;
    }
    
    /**
     * This method can be used to retrieve the URI of a resource which
     * is located at the specified URI, potentially relative to a resource
     * that is being processed.
     * 
     * @param uri The relative URI of the resource to load
     * @return The URI, or null if not found
     * @throws Exception Failed to get resource URI
     */
    public java.net.URI getResourceURI(String uri) throws Exception {
        java.net.URI ret=null;
        java.io.File file=new java.io.File(_baseDir, uri);

        if (!file.exists()) {
            ret = new URI(uri);                
        } else {
            ret = file.toURI();
        }
        
        return (ret);
    }

    /**
     * {@inheritDoc}
     */
	public String getRelativePath(String path) throws Exception {
		java.util.List<java.io.File> p1=new java.util.Vector<java.io.File>();
		java.util.List<java.io.File> p2=new java.util.Vector<java.io.File>();
		
		java.io.File f=new java.io.File(path);
		
		while (f != null) {
			p1.add(0, f);
			f = f.getParentFile();
		}
		
		f = _baseDir;
		
		while (f != null) {
			p2.add(0, f);
			f = f.getParentFile();
		}
		
		while (p1.size() > 0 && p2.size() > 0
				&& p1.get(0).equals(p2.get(0))) {
			p1.remove(0);
			p2.remove(0);
		}
		
		String ret="";
		
		for (int i=0; i < p2.size(); i++) {
			ret += ".."+java.io.File.separator;
		}
		
		for (int i=0; i < p1.size(); i++) {
			ret += p1.get(i).getName();
			
			if (p1.get(i).isDirectory()) {
				ret += java.io.File.separator;
			}
		}
		
		return (ret);
	}

}
