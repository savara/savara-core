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
package org.savara.java.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.resources.ResourceLocator;
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
import org.scribble.protocol.model.TypeImportList;
import org.scribble.protocol.model.TypeReference;
import org.scribble.protocol.util.InteractionUtil;
import org.scribble.protocol.util.TypesUtil;

/**
 * This class provides capabilities to generate stateless behaviour
 * as Java code.
 *
 */
public class JavaBehaviourGenerator {
	
	private static final Logger logger=Logger.getLogger(JavaBehaviourGenerator.class.getName());
	
	private ProtocolModel _behaviour=null;
	private ResourceLocator _locator=null;
	private java.util.List<String> _importedTypes=new java.util.Vector<String>();
	
	/**
	 * This is the constructor for the Java benaviour generator.
	 * 
	 * @param behaviour The stateless endpoint behaviour
	 * @param locator The optional resource locator
	 */
	public JavaBehaviourGenerator(ProtocolModel behaviour,
						ResourceLocator locator) {
		_behaviour = behaviour;
		_locator = locator;
	}
	
	/**
	 * This method derives the Java type to use, and if appropriate
	 * includes the type in the import list, and then returns
	 * just the local type.
	 * 
	 * @param type The fully qualified Java type
	 * @return The Java type, local if imported, or fully qualified
	 */
	protected String getImportedType(String type) {
		String ret=type;
				
		// Check if in import list
		if (ret.indexOf('.') != -1 && !_importedTypes.contains(ret)) {
			_importedTypes.add(ret);
		}

		return(getLocalJavaType(ret));
	}
	
	/**
	 * This method derives the Java type to use, and if appropriate
	 * includes the type in the import list, and then returns
	 * just the local type.
	 * 
	 * @param interaction The interaction
	 * @param locator The optional resource locator
	 * @return The Java type, local if imported, or fully qualified
	 */
	protected String getImportedType(Interaction interaction, ResourceLocator locator) {
		String ret=getJavaType(interaction, locator);
				
		// Check if in import list
		if (ret.indexOf('.') != -1 && !_importedTypes.contains(ret)) {
			_importedTypes.add(ret);
		}

		return(getLocalJavaType(ret));
	}
	
	public static String getJavaPackage(String namespace) {
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
	
	protected static String getJavaType(Interaction interaction, ResourceLocator locator) {
		String ret=null;
		TypeReference tref=interaction.getMessageSignature().getTypeReferences().get(0);
		TypeImport ti=TypesUtil.getTypeImport(tref);
		
		if (ti != null && ti.getDataType() != null) {
			QName type=QName.valueOf(ti.getDataType().getDetails());
			
			ret = getJavaPackage(type.getNamespaceURI());
			
			if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse(interaction)) {
				ret = org.savara.protocol.model.util.InteractionUtil.getFaultName(interaction)+"Fault";
				
				if (!org.savara.protocol.model.util.InteractionUtil.isSend(interaction)) {
					Annotation ann=AnnotationDefinitions.getAnnotationWithProperty(
							interaction.getEnclosingProtocol().getAnnotations(),
							AnnotationDefinitions.INTERFACE, AnnotationDefinitions.ROLE_PROPERTY,
							interaction.getFromRole().getName());
					
					if (ann != null) {
						String ns=(String)ann.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY);
						
						ret = getJavaPackage(ns)+"."+ret;
					}
				}
			} else {
				ret += "."+type.getLocalPart();
				
				// Check if XSD element - and if so find the XSD type
				if (locator != null && ti.getParent() instanceof TypeImportList &&
						((TypeImportList)ti.getParent()).getLocation() != null) {
					String location=((TypeImportList)ti.getParent()).getLocation();
					try {
						java.net.URI uri=locator.getResourceURI(location);
						
						DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
						fact.setNamespaceAware(true);
						
						DocumentBuilder builder=fact.newDocumentBuilder();
						
						java.io.InputStream is=uri.toURL().openStream();
						
						org.w3c.dom.Document doc=builder.parse(is);
						
						is.close();
						
						org.w3c.dom.NodeList elemList=
								doc.getDocumentElement().getElementsByTagNameNS(
										"http://www.w3.org/2001/XMLSchema", "element");
						
						for (int i=0; i < elemList.getLength(); i++) {
							org.w3c.dom.Element elem=(org.w3c.dom.Element)elemList.item(i);
							
							String name=elem.getAttribute("name");
							String elemType=elem.getAttribute("type");
							
							if (name.equals(type.getLocalPart())) {
								String prefix=org.savara.common.util.XMLUtils.getPrefix(elemType);
								String ns=null;
								
								if (prefix != null && prefix.trim().length() > 0) {
									ns = elem.lookupNamespaceURI(prefix);
								} else {
									ns = elem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
								}
								
								ret = getJavaPackage(ns);
								ret += "."+org.savara.common.util.XMLUtils.getLocalname(elemType);
							}
						}

					} catch(Exception e) {
						logger.log(Level.SEVERE, "Failed to obtain XSD schema '"+location+"'", e);
					}
				}
			}
		}

		return(ret);
	}
	
	protected static String getLocalJavaType(String qualifiedType) {
		String ret=qualifiedType;
		
		int ind=qualifiedType.lastIndexOf('.');
		if (ind != -1) {
			ret = qualifiedType.substring(ind+1);
		}
		
		return(ret);
	}
	
	public static String getVariableName(String name) {
		return(Character.toLowerCase(name.charAt(0))+name.substring(1));
	}

	public String getOperationBody(String opName, String returnType,
					String parameterType, String parameterName,
					java.util.Map<Role,String> roleMapping) {
		String ret=null;
	
		Block block=getStatelessBehaviourForOperation(opName);
		
		if (block != null) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Behaviour for operation="+opName+" is: "+block);
			}
			
			StringBuffer code=new StringBuffer();
			int indent=2;
			java.util.Map<String,String> typeVarMap=new java.util.HashMap<String,String>();
			
			typeVarMap.put(getImportedType(parameterType), parameterName);
			
			if (!returnType.equals("void")) {
				newline(code, indent);
				
				String localType=getImportedType(returnType);
				
				code.append(localType+" ret=null;\r\n");
				
				typeVarMap.put(localType, "ret");
			}
			
			processBlock(block, code, indent, roleMapping, typeVarMap, _locator);
			
			if (!returnType.equals("void")) {
				newline(code, indent);
				code.append("return (ret);");
			}
			
			ret = code.toString();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the import statements derived from
	 * processing the operation bodies.
	 * 
	 * @return The import statements
	 */
	public String getImportStatements() {
		StringBuffer code=new StringBuffer();
		
		for (String type : _importedTypes) {
			code.append("import "+type+";\r\n");
		}
		
		return(code.toString());
	}
	
	protected Block getStatelessBehaviourForOperation(String opName) {
		Block ret=null;
		
		if (_behaviour.getProtocol().getBlock().size() == 1 &&
				_behaviour.getProtocol().getBlock().get(0) instanceof Choice) {
			for (Block b : ((Choice)_behaviour.getProtocol().getBlock().get(0)).getPaths()) {
				
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
					InteractionUtil.getInitialInteractions(_behaviour.getProtocol().getBlock());
			
			if (interactions != null) {
				if (interactions.size() == 1) {
					MessageSignature msig=InteractionUtil.getMessageSignature(interactions.get(0));
					
					if (msig.getOperation() != null && msig.getOperation().equals(opName)) {
						ret = _behaviour.getProtocol().getBlock();
					}
				} else {
					logger.severe("Stateless behaviour shouldn't have more " +
							"than one initial interaction for path: "+_behaviour.getProtocol().getBlock());
				}
			}
		}
	
		return(ret);
	}
	
	protected void processBlock(Block block, StringBuffer code, int indent,
						java.util.Map<Role,String> roleMap, java.util.Map<String,String> typeVarMap,
						ResourceLocator locator) {
		
		for (int i=0; i < block.getContents().size(); i++) {
			Activity act=block.getContents().get(i);
			
			if (act instanceof Interaction) {
				Interaction interaction=(Interaction)act;
				
				if (org.savara.protocol.model.util.InteractionUtil.isRequest(interaction)) {

					if (org.savara.protocol.model.util.InteractionUtil.isSend(interaction)) {
					
						if (i+1 < block.getContents().size()) {
							Activity next=block.getContents().get(i+1);
							
							// Check if followed by an interaction representing a response to this
							// request
							boolean processed=false;
							
							if (next instanceof Interaction &&
									org.savara.protocol.model.util.InteractionUtil.isResponseForRequest(
												(Interaction)next, interaction)) {
								processInvoke(interaction, (Interaction)next,
										code, indent, roleMap, typeVarMap, locator);
								i++; // Skip response
								
								processed = true;
								
							// Check if followed by a choice representing normal and fault responses
							} else if (next instanceof Choice &&
									org.savara.protocol.model.util.InteractionUtil.isResponseAndFaultHandler(
												(Choice)next, interaction)) {							
								processInvoke(interaction, (Choice)next,
										code, indent, roleMap, typeVarMap, locator);
								i++; // Skip normal/response fault handler

								processed = true;
							}
							
							if (!processed) {
								// Handle one-way requests
								processInvoke(interaction, (Interaction)null,
										code, indent, roleMap, typeVarMap, locator);
							}
						}
					} else {
						processReceive(interaction,
								code, indent, roleMap, typeVarMap, locator);
					}
				} else if (org.savara.protocol.model.util.InteractionUtil.isResponse(interaction) &&
						org.savara.protocol.model.util.InteractionUtil.isSend(interaction)) {
					processResponse(interaction, code, indent, roleMap, typeVarMap, locator);
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
					
					processBlock(path, code, indent+1, roleMap, typeVarMap, locator);

					// Restore type/var map
					typeVarMap = savedTypeVarMap;
				}
				
				newline(code, indent);
				code.append("}\r\n");				
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
			java.util.Map<String,String> typeVarMap,
			ResourceLocator locator) {
		
		// Check if fault response
		if (org.savara.protocol.model.util.InteractionUtil.isFaultResponse(resp)) {
			String faultType=getImportedType(resp, locator);
			
			newline(code, indent);
			code.append("throw new "+faultType+"();");
		} else {
			String type=getImportedType(resp, locator);
			String name=typeVarMap.get(type);
			
			newline(code, indent);
			code.append("// TODO: Add code here to return response");
			newline(code, indent);
			code.append("// "+name+" = ....;");
		}
	}

	protected void processReceive(Interaction req,
			StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
			java.util.Map<String,String> typeVarMap,
			ResourceLocator locator) {
		
		String type=getImportedType(req, locator);
		String name=typeVarMap.get(type);
		
		newline(code, indent);
		code.append("// TODO: Add code here to handle request (in variable '"+name+"')\r\n");
	}

	protected void processInvoke(Interaction req, Interaction resp,
			StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
			java.util.Map<String,String> typeVarMap,
			ResourceLocator locator) {

		// Get variable for role
		String roleVar=roleMap.get(req.getToRoles().get(0));
		
		// Identify request Java type, and determine if a new variable is required
		String reqType=getImportedType(req, locator);
		String reqVarName=typeVarMap.get(reqType);
		
		if (reqVarName == null) {
			newline(code, indent);
			
			// Declare new request variable
			reqVarName = getVariableName(req.getMessageSignature().getOperation())+"Req";
			
			typeVarMap.put(reqType, reqVarName);
			
			code.append("// TODO: Add code here to initialize request");
			newline(code, indent);
			code.append(reqType+" "+reqVarName+"=null;\r\n");
		}

		newline(code, indent);
		
		if (resp != null) {
			String typeStr=getImportedType(resp, locator);
			String varName=typeVarMap.get(typeStr);
			
			if (varName == null) {
				varName = getVariableName(req.getMessageSignature().getOperation())+"Result";
				
				typeVarMap.put(typeStr, varName);
				
				code.append(typeStr+" ");
			}
		
			code.append(varName+" = ");
		}

		code.append(roleVar+"."+req.getMessageSignature().getOperation()+"("+reqVarName+");\r\n");
	}

	protected void processInvoke(Interaction req, Choice respAndFaults,
					StringBuffer code, int indent, java.util.Map<Role,String> roleMap,
					java.util.Map<String,String> typeVarMap,
					ResourceLocator locator) {
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
		String reqType=getImportedType(req, locator);
		String reqVarName=typeVarMap.get(reqType);
		
		if (reqVarName == null) {
			newline(code, indent);
			
			// Declare new request variable
			reqVarName = getVariableName(req.getMessageSignature().getOperation())+"Req";
			
			typeVarMap.put(reqType, reqVarName);
			
			code.append("// TODO: Add code here to initialize request");
			newline(code, indent);
			code.append(reqType+" "+reqVarName+"=null;\r\n");
		}

		newline(code, indent);
		
		// Get normal response and create var if does not exist
		if (normalPath != null && normalInteraction != null &&
					normalInteraction.getMessageSignature().getTypeReferences().size() == 1) {
			String typeStr=getImportedType(normalInteraction, locator);
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
		
		code.append(roleVar+"."+req.getMessageSignature().getOperation()+"("+reqVarName+");\r\n");
		
		// Process remainder of normal block
		if (normalPath != null) {
			processBlock(normalPath, code, indent, roleMap, typeVarMap, locator);
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
			
			String faultType=getImportedType(faultInteraction, locator);
			String faultName=getVariableName(org.savara.protocol.model.util.InteractionUtil.getFaultName(faultInteraction));
			
			newline(code, indent);
			
			code.append("} catch ("+faultType+" "+faultName+") {");
			
			typeVarMap.put(faultType, faultName);
			
			// Process fault block
			processBlock(faultPath, code, indent+1, roleMap, typeVarMap, locator);
			
			// Restore type/var map
			typeVarMap = savedTypeVarMap;
		}
		
		newline(code, indent);
		
		code.append("}\r\n");
		
	}
}
