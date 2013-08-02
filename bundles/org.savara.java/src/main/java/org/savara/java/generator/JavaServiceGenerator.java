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

import javax.wsdl.PortType;
import javax.wsdl.xml.WSDLReader;

import org.apache.cxf.tools.common.ToolContext;
import org.savara.common.resources.ResourceLocator;
import org.savara.java.generator.util.JavaGeneratorUtil;
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
	 * This method returns the path to the XJC binding file.
	 * 
	 * @return The file path
	 * @throws Exception Failed to return the file path
	 */
	protected String getXJCBindingFilePath() throws Exception {
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream("config/bindings.xjc");
		String filePath=null;
		String errmsg=null;
		
		if (is == null) {
			is = JavaServiceGenerator.class.getResourceAsStream("/config/bindings.xjc");
		}
		
		if (is == null) {
			errmsg="Failed to find XJC binding config file";
		} else {
			try {
				byte[] b=new byte[is.available()];
				is.read(b);
				
				is.close();
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Bindings config="+new String(b));
				}
				
				java.io.File tmp=java.io.File.createTempFile("savara", "xjc");
				tmp.deleteOnExit();
				
				java.io.FileOutputStream fos=new java.io.FileOutputStream(tmp);
				
				fos.write(b);
				
				fos.flush();
				fos.close();
				
				filePath = tmp.getAbsolutePath();
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to load XJC binding file", e);
				errmsg = "Failed to load XJC binding file: "+e;
			}
		}

		if (errmsg != null) {
			throw new Exception("Unable to generate service interface. "+errmsg);
		}

		return (filePath);
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
		String filePath=getXJCBindingFilePath();
		
		String[] cxfargs=new String[]{
				"-d", srcFolder,
				"-wsdlLocation", wsdlLocation,
				"-b", filePath,
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
	 * This method removes the JWS (java web service) annotations from a service interface
	 * and the service client class.
	 * 
	 * @param wsdlPath The WSDL path
	 * @param srcFolder The source folder where the interface has been generated
	 * @throws Exception Failed to remove the annotations
	 */
	protected void removeWebServiceAndClientAnnotations(String wsdlPath, String srcFolder) throws Exception {
		
		// Process the service interface to remove web service annotations
		javax.wsdl.Definition defn=getWSDLReader().readWSDL(wsdlPath);
		
		if (defn != null) {
			
			// Use the namespace to obtain a Java package
			String pack=JavaGeneratorUtil.getJavaPackage(defn.getTargetNamespace());
			
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
					
					removeLines(text, "import javax.jws.");
					removeLines(text, "        @Web");
					removeLines(text, "    @Web");
					removeLines(text, "@Web");
					removeLines(text, "    @Oneway");
					removeLines(text, "@SOAPBinding");

					// Write file contents back
					java.io.FileOutputStream fos=new java.io.FileOutputStream(f);
					
					fos.write(text.toString().getBytes());
					
					fos.close();
					
				} else {
					logger.severe("Service file '"+f.getAbsolutePath()+"' does not exist");
				}
				
				f = new java.io.File(srcFolder+java.io.File.separatorChar+
						folder+java.io.File.separatorChar+portType.getQName().getLocalPart()+"Service.java");
		
				if (f.exists()) {
					if (!f.delete()) {
						logger.warning("Failed to delete service client: "+f);
					}
				}
			}
			
		} else {
			logger.severe("Failed to retrieve WSDL definition '"+wsdlPath+"'");
		}
	}
	
	/**
	 * This method removes a line that begins with the supplied value.
	 * 
	 * @param text The text for the interface
	 * @param startsWith The value to search for
	 */
	protected void removeLines(StringBuffer text, String startsWith) {
		int startindex=-1;
		int endindex=-1;
		
		do {
			startindex=text.indexOf(startsWith);
			endindex=text.indexOf("\n", startindex);
			
			if (startindex != -1 && endindex != -1) {
				text.replace(startindex, endindex+1, "");
			}
		} while (startindex != -1);
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
		String filePath=getXJCBindingFilePath();
		
		String[] cxfargs=new String[]{
				"-impl",
				"-d", srcFolder,
				"-wsdlLocation", wsdlLocation,
				"-b", filePath,
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
