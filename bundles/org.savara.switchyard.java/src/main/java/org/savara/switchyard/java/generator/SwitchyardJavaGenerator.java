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
import javax.wsdl.xml.WSDLReader;

import org.apache.cxf.tools.common.ToolContext;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.util.InteractionUtil;

public class SwitchyardJavaGenerator {
	
	private static final Logger logger=Logger.getLogger(SwitchyardJavaGenerator.class.getName());
	
	private WSDLReader _reader=null;

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
		try {
			_reader = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to get WSDL reader", e);
		}
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
		
		//makeSwitchyardService(wsdlPath, srcFolder);
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
							ProtocolModel behaviour, String wsdlPath, String wsdlLocation,
						java.util.List<String> refWsdlPaths, String srcFolder) throws Exception {
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
		
		// Process the service implementation class
		javax.wsdl.Definition defn=_reader.readWSDL(wsdlPath);
		
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
					
					makeSwitchyardService(f, portType, srcFolder);
					
					addServiceReferencesToImplementation(f, role, refRoles, refWsdlPaths, srcFolder);
					
					addServiceBehaviour(f, role, refRoles, behaviour, srcFolder);
				} else {
					logger.severe("Service file '"+f.getAbsolutePath()+"' does not exist");
				}
			}
			
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
				javax.wsdl.Definition refDefn=_reader.readWSDL(refWsdlPaths.get(i));
				
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
					
						text.insert(index, "@javax.inject.Inject @org.switchyard.component.bean.Reference\r\n    "+
								refPack+"."+refPortType.getQName().getLocalPart()+" "+
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
								String resourceFolder) throws Exception {
		javax.wsdl.Definition defn=_reader.readWSDL(wsdlPath);
		
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
					javax.wsdl.Definition refDefn=_reader.readWSDL(refWsdlPath);
					
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
	
	protected void addServiceBehaviour(java.io.File implFile, Role role, java.util.List<Role> refRoles,
			ProtocolModel behaviour, String srcFolder) throws Exception {
		
		java.io.FileInputStream fis=new java.io.FileInputStream(implFile);
		byte[] b=new byte[fis.available()];
		fis.read(b);
		fis.close();
		
		// Create variable/role mapping
		java.util.Map<Role, String> roleMapping=new java.util.HashMap<Role, String>();
		for (Role r : refRoles) {
			roleMapping.put(r, r.getName());
		}
		
		// Process the Java class to replace the operation bodies
		String str=new String(b);
		
		int index=0;
		
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
					String[] reg=str.substring(index, startIndex).split("[ \\(]");
					
					String opbody=getOperationBody(reg[6], behaviour, roleMapping);
					
					if (opbody != null) {
						str = str.substring(0, startIndex+1)+"\r\n"+
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
		
		java.io.FileOutputStream fos=new java.io.FileOutputStream(implFile);
		
		fos.write(str.getBytes());
		
		fos.close();
	}
	
	protected String getOperationBody(String opName, ProtocolModel behaviour,
					java.util.Map<Role,String> roleMapping) {
		String ret=null;
	
		Block block=getStatelessBehaviourForOperation(opName, behaviour);
		
		if (block != null) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Behaviour for operation="+opName+" is: "+block);
			}
			
			CodeGenerator cg=new CodeGenerator(roleMapping);
			
			block.visit(cg);
			
			ret = cg.getCode();
		}
		
		return(ret);
	}
	
	protected Block getStatelessBehaviourForOperation(String opName, ProtocolModel behaviour) {
		Block ret=null;
		
		if (behaviour.getProtocol().getBlock().size() == 1 &&
				behaviour.getProtocol().getBlock().get(0) instanceof Choice) {
			for (Block b : ((Choice)behaviour.getProtocol().getBlock().get(0)).getPaths()) {
				
				// Get first interaction, and check whether it is for
				// the supplied operation name
				java.util.List<ModelObject> interactions=InteractionUtil.getInitialInteractions(b);
				
				if (interactions != null) {
					if (interactions.size() == 1) {
						MessageSignature msig=InteractionUtil.getMessageSignature(interactions.get(0));
						
						if (msig.getOperation() != null && msig.getOperation().equals(opName)) {
							ret = b;
							
							break;
						}
					} else {
						logger.severe("Stateless behaviour shouldn't have more " +
								"than one initial interaction for path: "+b);
					}
				}
			}
		} else {
			// Assume only a single path
			// Get first interaction, and check whether it is for
			// the supplied operation name
			java.util.List<ModelObject> interactions=
					InteractionUtil.getInitialInteractions(behaviour.getProtocol().getBlock());
			
			if (interactions != null) {
				if (interactions.size() == 1) {
					MessageSignature msig=InteractionUtil.getMessageSignature(interactions.get(0));
					
					if (msig.getOperation() != null && msig.getOperation().equals(opName)) {
						ret = behaviour.getProtocol().getBlock();
					}
				} else {
					logger.severe("Stateless behaviour shouldn't have more " +
							"than one initial interaction for path: "+behaviour.getProtocol().getBlock());
				}
			}
		}
	
		return(ret);
	}
	
	protected class CodeGenerator extends org.scribble.protocol.model.DefaultVisitor {
		
		private int _indent=0;
		private StringBuffer _code=new StringBuffer();
		private java.util.Map<Role,String> _roleMapping=null;
		
		public CodeGenerator(java.util.Map<Role,String> roleMapping) {
			_roleMapping = roleMapping;
		}
		
		public String getCode() {
			return(_code.toString());
		}
		
		protected void newline() {
			_code.append("\r\n");
			for (int i=0; i < _indent; i++) {
				_code.append("\t");
			}
		}
		
		public boolean start(Block elem) {
			_code.append(" {\r\n");
			_indent++;
			
			return(true);
		}
		
		public void end(Block elem) {
			newline();
			_code.append("}");
			_indent--;
		}
	}
}
