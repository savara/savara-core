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
import javax.xml.namespace.QName;

import org.apache.cxf.tools.common.ToolContext;
import org.savara.protocol.model.util.ChoiceUtil;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeImport;
import org.scribble.protocol.model.TypeReference;
import org.scribble.protocol.util.InteractionUtil;
import org.scribble.protocol.util.TypesUtil;

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
	
	protected String getJavaType(Interaction interaction) {
		String ret=null;
		TypeReference tref=interaction.getMessageSignature().getTypeReferences().get(0);
		TypeImport ti=TypesUtil.getTypeImport(tref);
		
		if (ti != null && ti.getDataType() != null) {
			QName type=QName.valueOf(ti.getDataType().getDetails());
			
			ret = getJavaPackage(type.getNamespaceURI());
			
			if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse(interaction)) {
				ret += "."+org.savara.protocol.model.util.InteractionUtil.getFaultName(interaction)+"Fault";
			} else {
				ret += "."+type.getLocalPart()+"Type";
			}
		}

		return(ret);
	}
	
	protected String getVariableName(String name) {
		return(Character.toLowerCase(name.charAt(0))+name.substring(1));
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
						String name=getVariableName(refRoles.get(i).getName());
						
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
			String varName="_"+Character.toLowerCase(r.getName().charAt(0))+r.getName().substring(1);
			roleMapping.put(r, varName);
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
					
					String opbody=getOperationBody(reg[6], reg[5], behaviour, roleMapping);
					
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
		
		java.io.FileOutputStream fos=new java.io.FileOutputStream(implFile);
		
		fos.write(str.getBytes());
		
		fos.close();
	}
	
	protected String getOperationBody(String opName, String returnType, ProtocolModel behaviour,
					java.util.Map<Role,String> roleMapping) {
		String ret=null;
	
		Block block=getStatelessBehaviourForOperation(opName, behaviour);
		
		if (block != null) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Behaviour for operation="+opName+" is: "+block);
			}
			
			StringBuffer code=new StringBuffer();
			int indent=2;
			java.util.Map<String,String> typeVarMap=new java.util.HashMap<String,String>();
			
			if (!returnType.equals("void")) {
				newline(code, indent);
				code.append(returnType+" ret;");
				
				typeVarMap.put(returnType, "ret");
			}
			
			processBlock(block, code, indent, roleMapping, typeVarMap);
			
			if (!returnType.equals("void")) {
				newline(code, indent);
				code.append("return (ret);");
			}
			
			ret = code.toString();
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
	
	protected void processBlock(Block block, StringBuffer code, int indent,
						java.util.Map<Role,String> roleMap, java.util.Map<String,String> typeVarMap) {
		
		for (int i=0; i < block.getContents().size(); i++) {
			Activity act=block.getContents().get(i);
			
			if (act instanceof Interaction) {
				Interaction interaction=(Interaction)act;
				
				if (org.savara.protocol.model.util.InteractionUtil.isRequest(interaction) &&
						org.savara.protocol.model.util.InteractionUtil.isSend(interaction)) {
					
					if (i+1 < block.getContents().size()) {
						Activity next=block.getContents().get(i+1);
						
						// Check if followed by an interaction representing a response to this
						// request
						if (next instanceof Interaction &&
								org.savara.protocol.model.util.InteractionUtil.isResponseForRequest(
											(Interaction)next, interaction)) {
							processInvoke(interaction, (Interaction)next,
									code, indent, roleMap, typeVarMap);
							i++; // Skip response
							
						// Check if followed by a choice representing normal and fault responses
						} else if (next instanceof Choice &&
								org.savara.protocol.model.util.InteractionUtil.isResponseAndFaultHandler(
											(Choice)next, interaction)) {							
							processInvoke(interaction, (Choice)next,
									code, indent, roleMap, typeVarMap);
							i++; // Skip normal/response fault handler
						}
					}
				} else if (org.savara.protocol.model.util.InteractionUtil.isResponse(interaction) &&
						org.savara.protocol.model.util.InteractionUtil.isSend(interaction)) {
					processResponse(interaction, code, indent, roleMap, typeVarMap);
				}
			} else if (act instanceof Choice && ChoiceUtil.isDecisionMaker((Choice)act)) {
				
				for (int j=0; j < ((Choice)act).getPaths().size(); j++) {
					Block path=((Choice)act).getPaths().get(j);
					
					// Save type/var map
					java.util.Map<String,String> savedTypeVarMap=new java.util.HashMap<String, String>(typeVarMap);
					
					newline(code, indent);
					
					if (j == 0) {
						code.append("if (false) { // TODO: Set expression");
					} else if (j != ((Choice)act).getPaths().size()-1) {
						code.append("} else if (false) { // TODO: Set expression");
					} else {
						code.append("} else {");
					}
					
					processBlock(path, code, indent+1, roleMap, typeVarMap);

					// Restore type/var map
					typeVarMap = savedTypeVarMap;
				}
				
				newline(code, indent);
				code.append("}");				
			}
		}
	}
	
	protected void newline(StringBuffer code, int indent) {
		code.append("\r\n");
		for (int i=0; i < indent; i++) {
			code.append("    ");
		}
	}
	
	protected void processResponse(Interaction resp,
			StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
			java.util.Map<String,String> typeVarMap) {
		
		// Check if fault response
		if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse(resp)) {
			String faultType=getJavaType(resp);
			
			newline(code, indent);
			code.append("throw new "+faultType+"();");
		} else {
			String type=getJavaType(resp);
			String name=typeVarMap.get(type);
			
			newline(code, indent);
			code.append("// TODO: Add code here to return response");
			newline(code, indent);
			code.append("// "+name+" = ....;");
		}
	}

	protected void processInvoke(Interaction req, Interaction resp,
			StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
			java.util.Map<String,String> typeVarMap) {

	}

	protected void processInvoke(Interaction req, Choice respAndFaults,
					StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
					java.util.Map<String,String> typeVarMap) {
		// Sort choice paths into normal and fault responses
		Block normalPath=null;
		Interaction normalInteraction=null;
		java.util.List<Block> faultPaths=new java.util.Vector<Block>();
		java.util.List<Interaction> faultInteractions=new java.util.Vector<Interaction>();
		
		for (Block b : respAndFaults.getPaths()) {
			
			java.util.List<ModelObject> ints=InteractionUtil.getInitialInteractions(b);
			
			if (ints.size() != 1) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Response fault handler path should only have single initial interaction: "+b);
				}
			} else {
				if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse((Interaction)ints.get(0))) {
					faultPaths.add(b);
					faultInteractions.add((Interaction)ints.get(0));
				} else if (normalPath == null) {
					normalPath = b;
					normalInteraction = (Interaction)ints.get(0);
				} else {
					logger.severe("More than one normal path exists in the choice: "+respAndFaults);
				}
			}
		}
		
		newline(code, indent);
		
		code.append("try {");
		
		indent++;
		
		// Save type/var map
		java.util.Map<String,String> savedTypeVarMap=new java.util.HashMap<String, String>(typeVarMap);
		
		// Identify request Java type, and determine if a new variable is required
		String reqType=getJavaType(req);
		String reqVarName=typeVarMap.get(reqType);
		
		if (reqVarName == null) {
			newline(code, indent);
			
			// Declare new request variable
			reqVarName = getVariableName(req.getMessageSignature().getOperation())+"Req";
			
			typeVarMap.put(reqType, reqVarName);
			
			code.append("// TODO: Add code here to initialize request");
			newline(code, indent);
			code.append(reqType+" "+reqVarName+";\r\n");
		}

		newline(code, indent);
		
		// Get normal response and create var if does not exist
		if (normalPath != null && normalInteraction != null &&
					normalInteraction.getMessageSignature().getTypeReferences().size() == 1) {
			String typeStr=getJavaType(normalInteraction);
			String varName=typeVarMap.get(typeStr);
			
			if (varName == null) {
				varName = getVariableName(req.getMessageSignature().getOperation())+"Result";
				
				typeVarMap.put(typeStr, varName);
				
				code.append(typeStr+" ");
			}
			
			code.append(varName+" = ");
		}
		
		// Get variable for role
		String roleVar=roleMap.get(req.getToRoles().get(0));
		
		code.append(roleVar+"."+req.getMessageSignature().getOperation()+"("+reqVarName+");");
		
		// Process remainder of normal block
		if (normalPath != null) {
			processBlock(normalPath, code, indent, roleMap, typeVarMap);
		}
		
		indent--;
		
		// Restore type/var map
		typeVarMap = savedTypeVarMap;
		
		// For each of the fault blocks - create a catch block for fault
		for (int i=0; i < faultPaths.size(); i++) {
			Block faultPath=faultPaths.get(i);
			Interaction faultInteraction=faultInteractions.get(i);
			
			// Save type/var map
			savedTypeVarMap=new java.util.HashMap<String, String>(typeVarMap);
			
			String faultType=getJavaType(faultInteraction);
			String faultName=getVariableName(org.savara.protocol.model.util.InteractionUtil.getFaultName(faultInteraction));
			
			newline(code, indent);
			
			code.append("} catch ("+faultType+" "+faultName+") {");
			
			typeVarMap.put(faultType, faultName);
			
			// Process fault block
			processBlock(faultPath, code, indent+1, roleMap, typeVarMap);
			
			// Restore type/var map
			typeVarMap = savedTypeVarMap;
		}
		
		newline(code, indent);
		
		code.append("}");
		
	}
}
