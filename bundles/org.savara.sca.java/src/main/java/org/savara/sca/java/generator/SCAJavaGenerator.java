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

import org.scribble.protocol.model.Role;

public class SCAJavaGenerator extends org.savara.java.generator.JavaServiceGenerator {
	
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createServiceInterfaceFromWSDL(String wsdlPath, String wsdlLocation, String srcFolder) throws Exception {
		super.createServiceInterfaceFromWSDL(wsdlPath, wsdlLocation, srcFolder);
		
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
	
	public void createServiceImplementationFromWSDL(Role role, java.util.List<Role> refRoles,
							String wsdlPath, String wsdlLocation,
						java.util.List<String> refWsdlPaths, String srcFolder) throws Exception {
		super.createServiceImplementationFromWSDL(role, refRoles, null,
						wsdlPath, wsdlLocation, refWsdlPaths, srcFolder, null);
		
		makeServiceInterfaceRemotable(wsdlPath, srcFolder);
		
		addServiceReferencesToImplementation(role, refRoles, wsdlPath, refWsdlPaths, srcFolder);
	}
	
	protected void addServiceReferencesToImplementation(Role role, java.util.List<Role> refRoles,
			String wsdlPath, java.util.List<String> refWsdlPaths, String srcFolder) throws Exception {
		WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		
		// Check that the number of reference roles and wsdl paths are the same
		if (refRoles.size() != refWsdlPaths.size()) {
			throw new IllegalArgumentException("The number of reference roles and wsdl paths are not consistent");
		}

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
								folder+java.io.File.separatorChar+portType.getQName().getLocalPart()+"Impl.java");
				
				if (f.exists()) {
					java.io.FileInputStream fis=new java.io.FileInputStream(f);
					
					byte[] b=new byte[fis.available()];
					fis.read(b);
					
					StringBuffer text=new StringBuffer();
					text.append(new String(b));
					
					fis.close();
					
					int index=text.indexOf("private static final Logger");
					
					if (index != -1) {
						
						for (int i=0; i < refRoles.size(); i++) {
							javax.wsdl.Definition refDefn=reader.readWSDL(refWsdlPaths.get(i));
							
							if (refDefn != null) {
								
								// Use the namespace to obtain a Java package
								String refPack=getJavaPackage(refDefn.getTargetNamespace());
								
								@SuppressWarnings("unchecked")
								java.util.Iterator<PortType> refPortTypes=refDefn.getPortTypes().values().iterator();
								int refPortCount=1;

								while (refPortTypes.hasNext()) {
									PortType refPortType=refPortTypes.next();
									String name=Character.toLowerCase(refRoles.get(i).getName().charAt(0))+
											refRoles.get(i).getName().substring(1);
									
									if (refDefn.getPortTypes().size() > 1) {
										name += refPortCount;
									}									
								
									text.insert(index, "@org.oasisopen.sca.annotation.Reference\r\n    "+
											refPack+"."+refPortType.getQName().getLocalPart()+" "+
											name+";\r\n\r\n    ");
								}
							}
						}
					
						java.io.FileOutputStream fos=new java.io.FileOutputStream(f);
						
						fos.write(text.toString().getBytes());
						
						fos.close();
					} else {
						logger.severe("Service implementation file '"+f.getAbsolutePath()+
								"' does not have 'private static final Logger' as location to insert references");
					}
					
				} else {
					logger.severe("Service implementation file '"+f.getAbsolutePath()+"' does not exist");
				}
			}
			
		} else {
			logger.severe("Failed to retrieve WSDL definition '"+wsdlPath+"'");
		}
	}
	
	public void createServiceComposite(Role role, java.util.List<Role> refRoles,
							String wsdlPath, java.util.List<String> refWsdlPaths,
								String resourceFolder) throws Exception {
		WSDLReader reader=javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		
		javax.wsdl.Definition defn=reader.readWSDL(wsdlPath);
		
		if (defn != null) {
			// Use the namespace to obtain a Java package
			String pack=getJavaPackage(defn.getTargetNamespace());
			
			StringBuffer composite=new StringBuffer();
			
			composite.append("<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"\r\n");
			composite.append("\t\txmlns:tuscany=\"http://tuscany.apache.org/xmlns/sca/1.1\"\r\n");
			composite.append("\t\ttargetNamespace=\"");
			composite.append(defn.getTargetNamespace());
			composite.append("\"\r\n\t\tname=\"");
			composite.append(role.getName());
			composite.append("\" >\r\n");
			
			@SuppressWarnings("unchecked")
			java.util.Iterator<PortType> portTypes=defn.getPortTypes().values().iterator();
			
			while (portTypes.hasNext()) {
				PortType portType=portTypes.next();
				
				composite.append("\t<component name=\""+portType.getQName().getLocalPart()+"Component\">\r\n");
				composite.append("\t\t<implementation.java class=\""+pack+"."+
									portType.getQName().getLocalPart()+"Impl\" />\r\n");
				composite.append("\t\t<service name=\""+portType.getQName().getLocalPart()+"\">\r\n");
				composite.append("\t\t\t<interface.java interface=\""+pack+"."+
									portType.getQName().getLocalPart()+"\" />\r\n");
				composite.append("\t\t\t<binding.ws uri=\"http://localhost:8080/"+
									portType.getQName().getLocalPart()+"Component\" />\r\n");
				composite.append("\t\t</service>\r\n");
				
				for (int i=0; i < refWsdlPaths.size(); i++){
					String refWsdlPath=refWsdlPaths.get(i);
					javax.wsdl.Definition refDefn=reader.readWSDL(refWsdlPath);
					
					if (refDefn != null) {
						
						@SuppressWarnings("unchecked")
						java.util.Iterator<PortType> refPortTypes=refDefn.getPortTypes().values().iterator();
						int refPortCount=1;
						
						while (refPortTypes.hasNext()) {
							PortType refPortType=refPortTypes.next();
							String name=Character.toLowerCase(refRoles.get(i).getName().charAt(0))+
									refRoles.get(i).getName().substring(1);
							
							if (refDefn.getPortTypes().size() > 1) {
								name += refPortCount;
							}
							
							composite.append("\t\t<reference name=\""+name+"\">\r\n");
							composite.append("\t\t\t<binding.ws uri=\"http://localhost:8080/"+
											refPortType.getQName().getLocalPart()+"Component\" />\r\n");
							composite.append("\t\t</reference>\r\n");
							
							refPortCount++;
						}
					}
				}
				
				composite.append("\t</component>\r\n");
			}

			composite.append("</composite>\r\n");
			
			java.io.FileOutputStream fos=new java.io.FileOutputStream(resourceFolder+java.io.File.separatorChar+
									role.getName()+".composite");
			
			fos.write(composite.toString().getBytes());
			
			fos.close();
		}		
	}
}
