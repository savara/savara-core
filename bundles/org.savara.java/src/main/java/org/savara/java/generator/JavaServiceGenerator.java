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
package org.savara.java.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.xml.WSDLReader;

import org.apache.cxf.tools.common.ToolContext;
import org.savara.common.resources.ResourceLocator;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;

public class JavaServiceGenerator {
	
	private static final Logger logger=Logger.getLogger(JavaServiceGenerator.class.getName());
	
	private WSDLReader _reader=null;

	public static void main(String[] args) {
		JavaServiceGenerator gen=new JavaServiceGenerator();
		
		if (args.length != 3) {
			System.err.println("Usage: SwitchyardJavaGenerator WSDLPath RelativeWSDLLocation DestinationFolder");
		}
		try {
			gen.createServiceInterfaceFromWSDL(args[0], args[1], args[2]);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public JavaServiceGenerator() {
		try {
			_reader = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to get WSDL reader", e);
		}
	}
	
	/**
	 * This method returns a WSDL reader.
	 * 
	 * @return The WSDL reader
	 */
	protected WSDLReader getWSDLReader() {
		return (_reader);
	}
	
	/**
	 * This method creates a Java service interface from a WSDL file.
	 * 
	 * @param wsdlPath The WSDL file path
	 * @param wsdlLocation The location of the WSDL
	 * @param srcFolder The folder to generate the Java source
	 * @throws Exception Failed to generate the service interfaces
	 */
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
	
	/**
	 * This method generates the Java service implementations from the supplied WSDL.
	 * 
	 * @param role The role associated with the service being generated
	 * @param refRoles The list of roles referenced by the service implementation
	 * @param behaviour The endpoint behaviour of the service implementation
	 * @param wsdlPath The WSDL file path
	 * @param wsdlLocation The wsdl location
	 * @param refWsdlPaths The WSDL files referenced by the service
	 * @param srcFolder The location of the folder to generate the source
	 * @param locator The resource locator
	 * @throws Exception Failed to generate implementation
	 */
	public void createServiceImplementationFromWSDL(Role role, java.util.List<Role> refRoles,
							ProtocolModel behaviour, String wsdlPath, String wsdlLocation,
						java.util.List<String> refWsdlPaths, String srcFolder,
						ResourceLocator locator) throws Exception {
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
