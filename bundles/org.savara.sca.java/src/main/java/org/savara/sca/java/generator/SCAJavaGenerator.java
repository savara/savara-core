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
package org.savara.sca.java.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.xml.WSDLReader;

import org.apache.cxf.tools.common.ToolContext;

public class SCAJavaGenerator {
	
	private static final Logger logger=Logger.getLogger(SCAJavaGenerator.class.getName());

	public static void main(String[] args) {
		SCAJavaGenerator gen=new SCAJavaGenerator();
		
		try {
			gen.createServiceInterfaceFromWSDL(args[0], args[1], args[2]);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public SCAJavaGenerator() {
	}
	
	public void createServiceInterfaceFromWSDL(String wsdlPath, String wsdlLocation, String srcFolder) throws Exception {
		String[] cxfargs=new String[]{
				"-d", srcFolder,
				"-wsdlLocation", wsdlLocation,
				wsdlPath
			};
		
		org.apache.cxf.tools.wsdlto.WSDLToJava wsdlToJava=new org.apache.cxf.tools.wsdlto.WSDLToJava(cxfargs);
		
		try {
			wsdlToJava.run(new ToolContext());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to generate Java interfaces", e);
			throw e;
		}
	}
	
	protected void makeServiceInterfaceRemotable(String wsdlPath, String srcFolder) throws Exception {
		WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		
		javax.wsdl.Definition defn=reader.readWSDL(wsdlPath);
		
		if (defn != null) {
			String namespace=defn.getTargetNamespace();
			
			// Use the namespace to obtain a Java package
			
		} else {
			logger.severe("Failed to retrieve WSDL definition '"+wsdlPath+"'");
		}
	}
	
	public void createServiceImplementationFromWSDL(String wsdlPath, String wsdlLocation, String srcFolder) throws Exception {
		String[] cxfargs=new String[]{
				"-impl",
				"-d", srcFolder,
				"-wsdlLocation", wsdlLocation,
				wsdlPath
			};
		
		org.apache.cxf.tools.wsdlto.WSDLToJava wsdlToJava=new org.apache.cxf.tools.wsdlto.WSDLToJava(cxfargs);
		
		try {
			wsdlToJava.run(new ToolContext());
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to generate Java interfaces", e);
			throw e;
		}
	}
}
