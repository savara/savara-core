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

import javax.wsdl.PortType;
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
		
		makeServiceInterfaceRemotable(wsdlPath, srcFolder);
	}
	
	protected void makeServiceInterfaceRemotable(String wsdlPath, String srcFolder) throws Exception {
		WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		
		javax.wsdl.Definition defn=reader.readWSDL(wsdlPath);
		
		if (defn != null) {
			
			// Use the namespace to obtain a Java package
			String pack=getJavaPackage(defn.getTargetNamespace());
			
			String folder=pack.replace('.', java.io.File.separatorChar);
			
			@SuppressWarnings("unchecked")
			java.util.Iterator<PortType> portTypes=defn.getPortTypes().values().iterator();
			
			while (portTypes.hasNext()) {
				PortType portType=portTypes.next();
				
				java.io.File f=new java.io.File(srcFolder+java.io.File.separatorChar+
								folder+java.io.File.separatorChar+portType.getQName().getLocalPart()+".java");
				
				if (f.exists()) {
					java.io.FileInputStream fis=new java.io.FileInputStream(f);
					
					byte[] b=new byte[fis.available()];
					fis.read(b);
					
					StringBuffer text=new StringBuffer();
					text.append(new String(b));
					
					fis.close();
					
					int index=text.indexOf("public interface");
					
					if (index != -1) {
						text.insert(index, "@org.oasisopen.sca.annotation.Remotable ");
					
						java.io.FileOutputStream fos=new java.io.FileOutputStream(f);
						
						fos.write(text.toString().getBytes());
						
						fos.close();
					} else {
						logger.severe("Service interface file '"+f.getAbsolutePath()+
								"' does not have 'public interface' to make remotable");
					}
					
				} else {
					logger.severe("Service interface file '"+f.getAbsolutePath()+"' does not exist");
				}
			}
			
		} else {
			logger.severe("Failed to retrieve WSDL definition '"+wsdlPath+"'");
		}
	}
	
	protected String getJavaPackage(String namespace) {
		String ret=null;
		
		try {
			java.net.URI uri=new java.net.URI(namespace);
			
			String host=uri.getHost();
			
			// Removing preceding www
			if (host.startsWith("www.")) {
				host = host.substring(4);
			}
			
			// Place the suffix at the beginning
			int index=host.lastIndexOf('.');
			
			if (index != -1) {
				ret = host.substring(index+1);
				
				ret += "."+host.substring(0, index);
			} else {
				ret = host;
			}
			
			ret += uri.getPath().replace('/', '.');
			
			ret = ret.toLowerCase();
			
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to get java package from namespace '"+namespace+"'", e);
		}
		
		return(ret);
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
		
		makeServiceInterfaceRemotable(wsdlPath, srcFolder);
	}
}
