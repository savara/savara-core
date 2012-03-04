/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * 31 Jul 2008 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.pi4soa.common.xml.XMLUtils;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.DataType;

public class ProtocolModelParserRule implements ParserRule {

	/**
	 * This method determines whether the rule can be applied
	 * to the supplied CDL type.
	 * 
	 * @param scribbleType The Scribble target type
	 * @param cdlType The CDL type
	 * @return Whether the rule is appropriate to convert
	 * 					the CDL type
	 */
	public boolean isSupported(Class<?> scribbleType,
						CDLType cdlType) {
		return(scribbleType == ProtocolModel.class &&
				cdlType instanceof org.pi4soa.cdl.Package);
	}
	
	/**
	 * This method converts the supplied CDL type into a
	 * Scribble model object.
	 * 
	 * @param context The converters context
	 * @param scribbleType The Scribble target type
	 * @param cdlType The CDL type to be converted
	 * @return The converted Scribble model object
	 */
	public ModelObject parse(ParserContext context,
			Class<?> scribbleType, CDLType cdlType) {
		ProtocolModel ret=new ProtocolModel();
		org.pi4soa.cdl.Package cdlpack=(org.pi4soa.cdl.Package)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				CDLTypeUtil.getURIFragment(cdlpack));
		ret.getAnnotations().add(scannotation);

		
		// Convert information types into type imports
		for (org.pi4soa.cdl.InformationType itype : cdlpack.getTypeDefinitions().getInformationTypes()) {
			setupTypeImport(itype, ret);
			/*
			// TODO: Create type import for the info type
			TypeImportList til=new TypeImportList();
			
			TypeImport ti=new TypeImport();
			ti.setName(itype.getName());
			
			DataType dt=new DataType();
			
			if (itype.getElementName() != null && itype.getElementName().trim().length() > 0) {
				String namespace=XMLUtils.getNamespace(itype.getElementName(), resolver, targetNamespace)
				dt.setDetails(itype.getElementName());
				
				AnnotationDefinitions.createAnnotation(til.getProperties(),
								AnnotationDefinitions.XSD_ELEMENT);
			} else if (itype.getTypeName() != null && itype.getTypeName().trim().length() > 0) {
				dt.setDetails(itype.getTypeName());
				
				AnnotationDefinitions.createAnnotation(til.getProperties(),
								AnnotationDefinitions.XSD_TYPE);
			}
			
			ti.setDataType(dt);
			til.setFormat(TypeSystem.XSD);
			til.getTypeImports().add(ti);
			
			// Check for schema location
			
			ret.getImports().add(til);
			*/
		}
		
		// Convert root choreography
		java.util.Iterator<org.pi4soa.cdl.Choreography> iter=
					cdlpack.getChoreographies().iterator();
		org.pi4soa.cdl.Choreography choreo=null;
	
		while (choreo == null && iter.hasNext()) {
			choreo = iter.next();
			
			if (choreo.getRoot() != Boolean.TRUE) {
				choreo = null;
			}
		}
			
		ParserRule rule=ParserRuleFactory.getConverter(Protocol.class,
					choreo);
		
		if (rule != null) {
			ret.setProtocol((Protocol)rule.parse(context,
					Protocol.class, choreo));
			
			if (ret.getProtocol() != null) {
				Annotation prtannotation=new Annotation(AnnotationDefinitions.PROTOCOL);

				prtannotation.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
								cdlpack.getTargetNamespace());
				ret.getProtocol().getAnnotations().add(prtannotation);

				// Store namespace prefix info
				java.util.Iterator<NameSpace> nss=cdlpack.getTypeDefinitions().getNameSpaces().iterator();
				
				while (nss.hasNext()) {
					NameSpace ns=nss.next();
					
					Annotation type=new Annotation(AnnotationDefinitions.TYPE);
					
					type.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY, ns.getURI());
					type.getProperties().put(AnnotationDefinitions.PREFIX_PROPERTY, ns.getPrefix());
					
					if (ns.getSchemaLocation() != null && ns.getSchemaLocation().trim().length() > 0) {
						type.getProperties().put(AnnotationDefinitions.LOCATION_PROPERTY, ns.getSchemaLocation());
					}
					
					ret.getProtocol().getAnnotations().add(type);
				}
				
				// Move any namespace annotations from sub-protocols to the top level protocol
				final Protocol top=ret.getProtocol();
				
				ret.getProtocol().visit(new DefaultVisitor() {
					public boolean start(Protocol elem) {
						if (elem != top) {
							AnnotationDefinitions.copyAnnotations(elem.getAnnotations(),
									top.getAnnotations(), AnnotationDefinitions.INTERFACE);
							
						}
						return(true);
					}
				});
				
				// Initialize any choices
				ret.getProtocol().visit(new DefaultVisitor() {
					
					public boolean start(Choice elem) {
						Role fromRole=null;

						for (Block b : elem.getPaths()) {
							// Identify 'from' role
							if (fromRole == null) {
								java.util.List<ModelObject> list=org.scribble.protocol.util.InteractionUtil.getInitialInteractions(b);
							
								for (ModelObject mo : list) {
									if (mo instanceof org.scribble.protocol.model.Interaction) {
										fromRole = ((org.scribble.protocol.model.Interaction)mo).getFromRole();
										
										if (fromRole != null) {
											break;
										}
									}
								}
							}
							if (fromRole != null) {
								break;
							}
						}
						
						if (fromRole != null) {
							elem.setRole(new Role(fromRole));
						}

						return(true);
					}
				});
			}
		} else {
			logger.severe("Failed to find conversation conversion rule");
		}
		
		return(ret);
	}
	
	protected void setupTypeImport(org.pi4soa.cdl.InformationType infoType, ProtocolModel model) {
		org.pi4soa.common.xml.XMLPrefixResolver resolver=
			org.pi4soa.cdl.util.CDLTypeUtil.getPrefixResolver(infoType.getPackage());
		String typeName=null;
		Annotation xsdAnnotation=null;
		
		if (org.pi4soa.common.util.NamesUtil.isSet(infoType.getTypeName())) {
			typeName = infoType.getTypeName();
			xsdAnnotation = new Annotation(AnnotationDefinitions.XSD_TYPE);
			
		} else if (org.pi4soa.common.util.NamesUtil.isSet(infoType.getElementName())) {
			typeName = infoType.getElementName();
			xsdAnnotation = new Annotation(AnnotationDefinitions.XSD_ELEMENT);
		}
		
		if (typeName != null) {
			String prefix=XMLUtils.getPrefix(typeName);
			String location=null;
			
			// Find location
			if (prefix != null) {
				for (NameSpace ns : infoType.getPackage().getTypeDefinitions().getNameSpaces()) {
					if (ns.getPrefix().equals(prefix)) {
						location = ns.getURI();
						break;
					}
				}
			}
			
			// Check if type import exists for the location
			TypeImportList til=null;
			
			for (ImportList il : model.getImports()) {
				if (il instanceof TypeImportList) {
					TypeImportList tilist=(TypeImportList)il;
					
					if ((tilist.getLocation() == null && location == null) ||
						(tilist.getLocation() != null && location != null &&
								tilist.getLocation().equals(location)) &&
								AnnotationDefinitions.getAnnotation(tilist.getAnnotations(),
										xsdAnnotation.getName()) != null) {						
						til = tilist;
						break;
					}
				}
			}
			
			if (til == null) {
				til = new TypeImportList();
				til.setFormat(TypeSystem.XSD);
				til.setLocation(location);
				
				til.getAnnotations().add(xsdAnnotation);
				
				model.getImports().add(til);
			}
			
			TypeImport ti=til.getTypeImport(infoType.getName());
			
			String dataType=(new QName(org.pi4soa.common.xml.XMLUtils.getNamespace(typeName, resolver, null),
					org.pi4soa.common.xml.XMLUtils.getLocalname(typeName))).toString();
			
			if (ti == null) {
				ti = new TypeImport();
				ti.setName(infoType.getName());
				ti.setDataType(new DataType(dataType));
				
				til.getTypeImports().add(ti);
			} else {
				// TODO: Verify same data type
			}
		}
	}

	private static Logger logger = Logger.getLogger(ProtocolModelParserRule.class.getName());
}
