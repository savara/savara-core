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
package org.savara.switchyard.java.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.savara.common.resources.DefaultResourceLocator;
import org.savara.common.resources.ResourceLocator;
import org.savara.java.generator.JavaBehaviourGenerator;
import org.savara.java.generator.util.JavaGeneratorUtil;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;

public class SwitchyardJavaGenerator extends org.savara.java.generator.JavaServiceGenerator {
	
	private static final Logger logger=Logger.getLogger(SwitchyardJavaGenerator.class.getName());
	
	public static void main(String[] args) {
		SwitchyardJavaGenerator gen=new SwitchyardJavaGenerator();
		
		if (args.length != 3) {
			System.err.println("Usage: SwitchyardJavaGenerator WSDLPath RelativeWSDLLocation DestinationFolder");
		}
		try {
			gen.createServiceInterfaceFromWSDL(args[0], args[1], args[2]);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public SwitchyardJavaGenerator() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createServiceInterfaceFromWSDL(String wsdlPath, String wsdlLocation, String srcFolder) throws Exception {
		super.createServiceInterfaceFromWSDL(wsdlPath, wsdlLocation, srcFolder);
		
		removeWebServiceAndClientAnnotations(wsdlPath, srcFolder);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createServiceImplementationFromWSDL(Role role, java.util.List<Role> refRoles,
							ProtocolModel behaviour, String wsdlPath, String wsdlLocation,
						java.util.List<String> refWsdlPaths, String srcFolder,
						ResourceLocator locator) throws Exception {
		super.createServiceImplementationFromWSDL(role, refRoles, behaviour, wsdlPath,
						wsdlLocation, refWsdlPaths, srcFolder, locator);
		
		// Process the service implementation class
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
								folder+java.io.File.separatorChar+portType.getQName().getLocalPart()+"Impl.java");
				
				if (f.exists()) {
					
					makeSwitchyardService(f, portType, srcFolder);
					
					addServiceReferencesToImplementation(f, role, refRoles, refWsdlPaths, srcFolder);
					
					addServiceBehaviour(f, role, refRoles, behaviour, srcFolder, locator);
				} else {
					logger.severe("Service file '"+f.getAbsolutePath()+"' does not exist");
				}
			}
			
			removeWebServiceAndClientAnnotations(wsdlPath, srcFolder);

		} else {
			logger.severe("Failed to retrieve WSDL definition '"+wsdlPath+"'");
		}
	}
	
	protected void makeSwitchyardService(java.io.File implFile, PortType portType, String srcFolder) throws Exception {
		
		java.io.FileInputStream fis=new java.io.FileInputStream(implFile);
		
		byte[] b=new byte[fis.available()];
		fis.read(b);
		
		StringBuffer text=new StringBuffer();
		text.append(new String(b));
		
		fis.close();
		
		int startindex=text.indexOf("import");
		int endindex=text.indexOf("public class");
		
		if (startindex != -1 && endindex != -1) {
			text.replace(startindex, endindex,
					"import java.util.logging.Logger;\r\n\r\n" +
					"@org.switchyard.component.bean.Service("+
					portType.getQName().getLocalPart()+".class)\r\n");
		
			java.io.FileOutputStream fos=new java.io.FileOutputStream(implFile);
			
			fos.write(text.toString().getBytes());
			
			fos.close();
		} else {
			logger.severe("Service file '"+implFile.getAbsolutePath()+
					"' could not change to switchyard Service annotation");
		}
	}
	
	protected void addServiceReferencesToImplementation(java.io.File implFile,
			Role role, java.util.List<Role> refRoles,
			java.util.List<String> refWsdlPaths, String srcFolder) throws Exception {
		
		// Check that the number of reference roles and wsdl paths are the same
		if (refRoles.size() != refWsdlPaths.size()) {
			throw new IllegalArgumentException("The number of reference roles and wsdl paths are not consistent");
		}

		java.io.FileInputStream fis=new java.io.FileInputStream(implFile);
		
		byte[] b=new byte[fis.available()];
		fis.read(b);
		
		StringBuffer text=new StringBuffer();
		text.append(new String(b));
		
		fis.close();
		
		int index=text.indexOf("private static final Logger");
		
		if (index != -1) {
			
			for (int i=0; i < refRoles.size(); i++) {
				javax.wsdl.Definition refDefn=getWSDLReader().readWSDL(refWsdlPaths.get(i));
				
				if (refDefn != null) {
					
					// Use the namespace to obtain a Java package
					String refPack=JavaGeneratorUtil.getJavaPackage(refDefn.getTargetNamespace());
					
					@SuppressWarnings("unchecked")
					java.util.Iterator<PortType> refPortTypes=refDefn.getPortTypes().values().iterator();
					int refPortCount=1;

					while (refPortTypes.hasNext()) {
						PortType refPortType=refPortTypes.next();
						String name=JavaBehaviourGenerator.getVariableName(refRoles.get(i).getName());
						
						if (refDefn.getPortTypes().size() > 1) {
							name += refPortCount;
						}									
					
						text.insert(index, "@javax.inject.Inject @org.switchyard.component.bean.Reference\r\n    "+
								refPack+"."+refPortType.getQName().getLocalPart()+" _"+
								name+";\r\n\r\n    ");
					}
				}
			}
		
			java.io.FileOutputStream fos=new java.io.FileOutputStream(implFile);
			
			fos.write(text.toString().getBytes());
			
			fos.close();
		} else {
			logger.severe("Service implementation file '"+implFile.getAbsolutePath()+
					"' does not have 'private static final Logger' as location to insert references");
		}
	}
	
	public void createServiceComposite(Role role, java.util.List<Role> refRoles,
				String wsdlPath, java.util.List<String> refWsdlPaths,
				String resourceFolder, String srcFolder) throws Exception {
		javax.wsdl.Definition defn=getWSDLReader().readWSDL(wsdlPath);
		
		if (defn != null) {
			
			java.io.File wsdlFile=new java.io.File(wsdlPath);
			
			if (!wsdlFile.exists()) {
				java.net.URL url=ClassLoader.getSystemResource(wsdlPath);
				
				wsdlFile = new java.io.File(url.getFile());
				
				if (!wsdlFile.exists()) {
					logger.severe("Failed to find WSDL file '"+wsdlPath+"'");
				}
			}
			
			ResourceLocator locator=new DefaultResourceLocator(wsdlFile.getParentFile());
			
			StringBuffer composite=new StringBuffer();
			StringBuffer transformers=new StringBuffer();
			StringBuffer component=new StringBuffer();
			
			String targetNamespace=defn.getTargetNamespace();
			String name=role.getName();
			
			composite.append("<switchyard xmlns=\"urn:switchyard-config:switchyard:1.0\"\r\n");
			
			// TODO: May need to add other namespaces here
						
			composite.append("\t\ttargetNamespace=\""+targetNamespace+"\"\r\n");
			composite.append("\t\tname=\""+name+"\">\r\n");

			composite.append("\t<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\"\r\n");
			composite.append("\t\t\ttargetNamespace=\"");
			composite.append(targetNamespace);
			composite.append("\"\r\n\t\t\tname=\"");
			composite.append(name);
			composite.append("\" >\r\n");

			@SuppressWarnings("unchecked")
			java.util.Iterator<PortType> portTypes=defn.getPortTypes().values().iterator();
			
			while (portTypes.hasNext()) {
				PortType portType=portTypes.next();
				
				// Define transformers
				addPortTypeTransformers(portType, true, transformers,
								defn, locator, srcFolder);

				String wsdlName=wsdlPath;
				int ind=wsdlName.lastIndexOf('/');
				
				String wsdlLocation=wsdlName;
				
				if (ind != -1) {
					wsdlLocation = wsdlName.substring(ind+1);
					wsdlName = wsdlName.substring(ind+1)
							+"#wsdl.porttype("+portType.getQName().getLocalPart()+")";
				}
				
				composite.append("\t\t<service name=\""+portType.getQName().getLocalPart()+
						"\" promote=\""+portType.getQName().getLocalPart()+"Component/"
						+portType.getQName().getLocalPart()+"\">\r\n");
				
				composite.append("\t\t\t<interface.wsdl interface=\"wsdl/"
									+wsdlName+"\" />\r\n");
				composite.append("\t\t\t<binding.soap xmlns=\"urn:switchyard-component-soap:config:1.0\">\r\n");
				composite.append("\t\t\t\t<wsdl>wsdl/"+wsdlLocation+"</wsdl>\r\n");
				composite.append("\t\t\t\t<socketAddr>:18001</socketAddr>\r\n");
				composite.append("\t\t\t</binding.soap>\r\n");
								
				composite.append("\t\t</service>\r\n");

				
				String pack=JavaGeneratorUtil.getJavaPackage(defn.getTargetNamespace());

				component.append("\t\t<component name=\""+portType.getQName().getLocalPart()+"Component\">\r\n");
				
				component.append("\t\t\t<implementation.bean xmlns=\"urn:switchyard-component-bean:config:1.0\" " +
						"class=\""+pack+"."+portType.getQName().getLocalPart()+"Impl\"/>\r\n");
				
				component.append("\t\t\t<service name=\""+portType.getQName().getLocalPart()+"\" >\r\n");
				component.append("\t\t\t\t<interface.java interface=\""+pack+"."
									+portType.getQName().getLocalPart()+"\"/>\r\n");
				component.append("\t\t\t</service>\r\n");
				
				for (int i=0; i < refWsdlPaths.size(); i++){
					String refWsdlPath=refWsdlPaths.get(i);
					javax.wsdl.Definition refDefn=getWSDLReader().readWSDL(refWsdlPath);
					
					java.io.File refWsdlFile=new java.io.File(refWsdlPath);
					
					if (!refWsdlFile.exists()) {
						java.net.URL url=ClassLoader.getSystemResource(refWsdlPath);
						
						refWsdlFile = new java.io.File(url.getFile());
						
						if (!refWsdlFile.exists()) {
							logger.severe("Failed to find ref WSDL file '"+refWsdlPath+"'");
						}
					}
					
					ResourceLocator refLocator=new DefaultResourceLocator(refWsdlFile.getParentFile());
					
					if (refDefn != null) {
						
						@SuppressWarnings("unchecked")
						java.util.Iterator<PortType> refPortTypes=refDefn.getPortTypes().values().iterator();
						
						while (refPortTypes.hasNext()) {
							PortType refPortType=refPortTypes.next();
							
							// Define transformers
							addPortTypeTransformers(refPortType, false,
									transformers, refDefn, refLocator,
									srcFolder);

							String refWsdlName=refWsdlPath;
							
							wsdlLocation=refWsdlName;
							
							ind = refWsdlName.lastIndexOf('/');
							if (ind != -1) {
								wsdlLocation = refWsdlName.substring(ind+1);
								refWsdlName = refWsdlName.substring(ind+1)
										+"#wsdl.porttype("+refPortType.getQName().getLocalPart()+")";
							}
							
							composite.append("\t\t<reference name=\""+refPortType.getQName().getLocalPart()+
									"\" promote=\""+portType.getQName().getLocalPart()+"Component/"
									+refPortType.getQName().getLocalPart()+"\" multiplicity=\"1..1\" >\r\n");

							composite.append("\t\t\t<interface.wsdl interface=\"wsdl/"
												+refWsdlName+"\" />\r\n");
							composite.append("\t\t\t<binding.soap xmlns=\"urn:switchyard-component-soap:config:1.0\">\r\n");
							composite.append("\t\t\t\t<wsdl>wsdl/"+wsdlLocation+"</wsdl>\r\n");
							composite.append("\t\t\t\t<socketAddr>:18001</socketAddr>\r\n");
							composite.append("\t\t\t</binding.soap>\r\n");
							composite.append("\t\t</reference>\r\n");
							
							String refPack=JavaGeneratorUtil.getJavaPackage(refDefn.getTargetNamespace());

							component.append("\t\t\t<reference name=\""+refPortType.getQName().getLocalPart()+"\" >\r\n");
							component.append("\t\t\t\t<interface.java interface=\""+refPack+"."
												+refPortType.getQName().getLocalPart()+"\"/>\r\n");
							component.append("\t\t\t</reference>\r\n");
						}						
					}
				}
				
				component.append("\t\t</component>\r\n");
				
				composite.append(component.toString());
			}

			composite.append("\t</composite>\r\n");
			
			if (transformers.length() > 0) {
				composite.append("\t<transforms xmlns:xform=\"urn:switchyard-config:transform:1.0\">\r\n");
				composite.append(transformers.toString());
				composite.append("\t</transforms>\r\n");				
			}
			
			composite.append("</switchyard>\r\n");
			
			java.io.FileOutputStream fos=new java.io.FileOutputStream(resourceFolder+
					java.io.File.separatorChar+"switchyard.xml");
			
			fos.write(composite.toString().getBytes());
			
			fos.close();
		}		
	}
	
	protected void addPortTypeTransformers(PortType portType, boolean provider, StringBuffer transformers,
					javax.wsdl.Definition wsdl, ResourceLocator locator,
					String srcFolder) {
		String attr1=(provider ? "from" : "to");
		String attr2=(provider ? "to" : "from");
		
		for (Object opobj : portType.getOperations()) {
			if (opobj instanceof javax.wsdl.Operation) {
				javax.wsdl.Operation op=(javax.wsdl.Operation)opobj;
				
				String qname="";
				String javaType="";
				String javaPackage="";
				
				javax.wsdl.Part p=(javax.wsdl.Part)op.getInput().getMessage().
								getParts().values().iterator().next();
				qname = p.getElementName().toString();
				
				javaType = getJavaType(wsdl, p.getElementName(), locator);
				
				if (javaType != null) {
					int ind=javaType.lastIndexOf('.');
					if (ind != -1) {
						javaPackage = javaType.substring(0, ind);
					}
				}
								
				transformers.append("\t\t<xform:transform.jaxb\r\n");
				transformers.append("\t\t\t"+attr1+"=\""+qname+"\"\r\n");
				transformers.append("\t\t\t"+attr2+"=\"java:"+javaType+"\"\r\n");
				transformers.append("\t\t\tcontextPath=\""+javaPackage+"\"/>\r\n");
				
				if (op.getOutput() != null) {
					p = (javax.wsdl.Part)op.getOutput().getMessage().
									getParts().values().iterator().next();
					qname = p.getElementName().toString();
					
					javaType = getJavaType(wsdl, p.getElementName(), locator);
					
					if (javaType != null) {
						int ind=javaType.lastIndexOf('.');
						if (ind != -1) {
							javaPackage = javaType.substring(0, ind);
						}
					}
									
					transformers.append("\t\t<xform:transform.jaxb\r\n");
					transformers.append("\t\t\t"+attr2+"=\""+qname+"\"\r\n");
					transformers.append("\t\t\t"+attr1+"=\"java:"+javaType+"\"\r\n");
					transformers.append("\t\t\tcontextPath=\""+javaPackage+"\"/>\r\n");
				}
				
				for (Object faultObj : op.getFaults().values()) {
					if (faultObj instanceof javax.wsdl.Fault) {
						javax.wsdl.Fault fault=(javax.wsdl.Fault)faultObj;
						
						p = (javax.wsdl.Part)fault.getMessage().
								getParts().values().iterator().next();
						qname = p.getElementName().toString();
						
						String javaFaultType = getJavaType(wsdl, p.getElementName(), locator);
						
						String pack=JavaGeneratorUtil.getJavaPackage(fault.getMessage().getQName().getNamespaceURI());
						
						javaType = pack+"."+fault.getMessage().getQName().getLocalPart();

						String transformerClassName=getFaultTransformerClassName(javaType, provider);
						
						String faultTransClass=pack+"."+transformerClassName;
						
						transformers.append("\t\t<xform:transform.java class=\""+faultTransClass+"\"\r\n");
						transformers.append("\t\t\t"+attr2+"=\""+qname+"\"\r\n");
						transformers.append("\t\t\t"+attr1+"=\"java:"+javaType+"\"/>\r\n");
						
						// Create transformer class
						String folder=pack.replace('.', java.io.File.separatorChar);
						
						String javaFile=folder+java.io.File.separator+transformerClassName;
						
						java.io.File jFile=new java.io.File(srcFolder+java.io.File.separator+javaFile+".java");
						
						jFile.getParentFile().mkdirs();
						
						try {
							java.io.FileOutputStream fos=new java.io.FileOutputStream(jFile);

							generateFaultTransformer(qname, javaType, javaFaultType, provider, fos);
							
							fos.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Failed to create fault transformer", e);
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method returns the Java fault transformer class name.
	 * 
	 * @param javaFaultName The fault class name
	 * @param provider Whether the fault is generated for the provider
	 * @return The fault transformer class name
	 */
	protected String getFaultTransformerClassName(String javaFaultName, boolean provider) {
		int ind=javaFaultName.lastIndexOf('.');
		String clsName=javaFaultName.substring(ind+1);
		
		String ret = clsName + (provider ? "Provider" : "Consumer") + "Transformer";
		
		return (ret);
	}
	
	/**
	 * This method generates the contents of a fault transformer.
	 * 
	 * @param qname The XML type's fully qualified name
	 * @param javaFaultName The Java class for the fault name
	 * @param javaFaultType The Java class for the fault type
	 * @param os The output stream
	 * @throws Exception Failed to generate fault transformer
	 */
	protected void generateFaultTransformer(String qname, String javaFaultName,
						String javaFaultType, boolean provider,
						java.io.OutputStream os) throws Exception {
		int ind=javaFaultName.lastIndexOf('.');
		String pack=javaFaultName.substring(0, ind);
		String clsName=getFaultTransformerClassName(javaFaultName, provider);
				
		ind = javaFaultType.lastIndexOf('.');
		String faultTypePackage=javaFaultType.substring(0, ind);
		
		// Get template
		String template=(provider ? "JavaTypeToQName" : "QNameToJavaType");
		
		java.io.InputStream is=SwitchyardJavaGenerator.class.getResourceAsStream("/templates/"
							+template+"Transformer.java");

		if (is == null) {
			logger.severe("Failed to load template '"+template+"'");
			return;
		}
		
		byte[] b=new byte[is.available()];
		is.read(b);
		
		is.close();
		
		String str=new String(b);
		
		str = str.replaceAll("%PACKAGE%", pack);
		str = str.replaceAll("%CLASSNAME%", clsName);
		str = str.replaceAll("%FAULTCLASS%", javaFaultName);
		str = str.replaceAll("%FAULTTYPE%", javaFaultType);
		str = str.replaceAll("%FAULTTYPEPACKAGE%", faultTypePackage);
		str = str.replaceAll("%FAULTQNAME%", qname);
		str = str.replaceAll("%FAULTMESSAGE%", clsName);
		
		os.write(str.getBytes());
	}
	
	/**
	 * This method scans the supplied wsdl to locate the element definition and return
	 * the Java type associated with its type.
	 * 
	 * @param wsdl The WSDL
	 * @param element The element
	 * @param locator The locator
	 * @return The Java type, or null if not found
	 */
	protected String getJavaType(javax.wsdl.Definition wsdl, QName element, ResourceLocator locator) {
		// Find schema location
		java.util.List<?> imps=wsdl.getImports(element.getNamespaceURI());
		
		for (Object obj : imps) {
			if (obj instanceof javax.wsdl.Import) {
				String javaType=JavaGeneratorUtil.getElementJavaType(element,
						((javax.wsdl.Import)obj).getLocationURI(), locator);
				
				if (javaType != null) {
					return (javaType);
				}
			}
		}

		return (null);
	}
	
	protected void addServiceBehaviour(java.io.File implFile, Role role, java.util.List<Role> refRoles,
			ProtocolModel behaviour, String srcFolder, ResourceLocator locator) throws Exception {
		
		java.io.FileInputStream fis=new java.io.FileInputStream(implFile);
		byte[] b=new byte[fis.available()];
		fis.read(b);
		fis.close();
		
		// Create variable/role mapping
		java.util.Map<Role, String> roleMapping=new java.util.HashMap<Role, String>();
		for (Role r : refRoles) {
			String varName="_"+Character.toLowerCase(r.getName().charAt(0))+r.getName().substring(1);
			roleMapping.put(r, varName);
		}
		
		// Process the Java class to replace the operation bodies
		String str=new String(b);
		
		int index=0;
		
		JavaBehaviourGenerator bg=new JavaBehaviourGenerator(behaviour, locator);
		
		// NOTE: Tried using Eclipse JDT, but pulled in many dependencies and
		// was an over complex approach to just replace a method body, so decided
		// to do it the low tech way :)
		do {
			index = str.indexOf("    public ", index);
			
			if (index != -1) {
				int startIndex = str.indexOf("{", index);
				int endIndex = str.indexOf("\n    }", startIndex);
				
				if (startIndex != -1 && endIndex != -1) {
					// Find out operation name
					String[] reg=str.substring(index, startIndex).split("[ \\(\\)]");
					
					String opbody=bg.getOperationBody(reg[6], reg[5],
							reg[7], reg[8], roleMapping);
					
					if (opbody != null) {
						str = str.substring(0, startIndex+1)+
									opbody+str.substring(endIndex);
						index = startIndex+1+opbody.length();
					} else {
						index = endIndex;
					}
				} else {
					index = -1;
				}
			}
		
		} while (index != -1);
		
		// Insert the import statements
		String importStatements=bg.getImportStatements();
		
		if (importStatements != null) {
			int ind=str.indexOf("import");
			
			if (ind != -1) {
				str = str.substring(0, ind)+importStatements+"\r\n"+str.substring(ind);
			}
		}
		
		java.io.FileOutputStream fos=new java.io.FileOutputStream(implFile);
		
		fos.write(str.getBytes());
		
		fos.close();
	}
	
}
