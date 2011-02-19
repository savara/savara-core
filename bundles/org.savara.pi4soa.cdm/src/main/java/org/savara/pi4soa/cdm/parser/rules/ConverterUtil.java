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

import org.pi4soa.cdl.Choreography;
import org.pi4soa.cdl.DefaultCDLVisitor;
import org.pi4soa.cdl.ExchangeDetails;
import org.pi4soa.cdl.Interaction;
import org.pi4soa.cdl.Participant;
import org.pi4soa.cdl.ParticipantType;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.pi4soa.common.xml.XMLUtils;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

/**
 * This class defines some converter utility functions.
 */
public class ConverterUtil {

	/**
	 * This method converts the supplied information type into a
	 * type reference.
	 * 
	 * @param infoType The information type
	 * @return The type reference
	 */
	public static TypeReference getTypeReference(org.pi4soa.cdl.InformationType infoType) {
		TypeReference ret=new TypeReference();
		
		// TODO: Establish 'implements' dependency between
		// XML type and an abstract type derived from the
		// XML namespace and localpart.
		
		//org.pi4soa.common.xml.XMLPrefixResolver resolver=
		//	org.pi4soa.cdl.util.CDLTypeUtil.getPrefixResolver(infoType.getPackage());
		String typeName=null;
		
		// TODO: Need to have general way to convert
		// namespace scoped type into a type reference
		
		if (org.pi4soa.common.util.NamesUtil.isSet(infoType.getTypeName())) {
			typeName = infoType.getTypeName();
			Annotation scannotation=new Annotation(AnnotationDefinitions.XSD_TYPE);
			ret.getAnnotations().add(scannotation);
		} else if (org.pi4soa.common.util.NamesUtil.isSet(infoType.getElementName())) {
			typeName = infoType.getElementName();
			Annotation scannotation=new Annotation(AnnotationDefinitions.XSD_ELEMENT);
			ret.getAnnotations().add(scannotation);
		}
		
		if (typeName != null) {
			ret.setName(infoType.getName());
			/*
			ret.setName(org.pi4soa.common.xml.XMLUtils.getLocalname(typeName));
			
			ret.getProperties().put(PropertyName.DATA_TYPE,
					(new QName(org.pi4soa.common.xml.XMLUtils.getNamespace(typeName, resolver, null),
							org.pi4soa.common.xml.XMLUtils.getLocalname(typeName))).toString());
			
			ret.getProperties().put(PropertyName.TYPE_SYSTEM, TypeSystem.XSD);
			*/
		}
		
		return(ret);
	}
	
	/**
	 * This method creates a label appropriate for the request
	 * response correlation associated with the supplied exchange.
	 * 
	 * @param details The exchange
	 * @return The label
	 */
	public static String getLabel(ExchangeDetails details) {
		String ret=null;
		
		if (details.getInteraction() != null) {
			ret = details.getInteraction().getOperation();
		}
		
		return(ret);
	}
	
	public static java.util.List<Role> getRoleParameters(final Choreography choreo) {
		java.util.List<Role> ret=new java.util.Vector<Role>();
		
		// If root choreo, then no parameters
		if (choreo.getRoot() == Boolean.TRUE) {
			return(ret);
		}
		
		final java.util.List<ParticipantType> partTypes=new java.util.Vector<ParticipantType>();
		final java.util.List<Participant> partInstances=new java.util.Vector<Participant>();
		
		choreo.visit(new DefaultCDLVisitor() {
			
			// TODO: SAVARA-161 May also need to handle 'perform' (and take care of recursion),
			// as a participant type may not be used in an intermediate sub-choreo,
			// but still would need to be passed through it to any child sub-choreos.
			// Sub-choreos should not process participant instances, so need to ignore these
			// if not the primary choreo being visited.
			
			public void interaction(Interaction interaction) {
				
				if (interaction.getEnclosingChoreography() != choreo) {
					return;
				}
				
				if (interaction.getFromParticipant() != null) {
					if (partInstances.contains(interaction.getFromParticipant()) == false) {
						partInstances.add(interaction.getFromParticipant());
					}
				} else {
					ParticipantType ptype=
						org.pi4soa.cdl.util.PackageUtil.getParticipantForRoleType(
								interaction.getFromRoleType());
					
					if (ptype != null &&
							partTypes.contains(ptype) == false) {
						partTypes.add(ptype);
					}
				}
				
				if (interaction.getToParticipant() != null) {
					if (partInstances.contains(interaction.getToParticipant()) == false) {
						partInstances.add(interaction.getToParticipant());
					}
				} else {
					ParticipantType ptype=
						org.pi4soa.cdl.util.PackageUtil.getParticipantForRoleType(
								interaction.getToRoleType());
					
					if (ptype != null &&
							partTypes.contains(ptype) == false) {
						partTypes.add(ptype);
					}
				}
			}
		});
		
		// Define roles
		java.util.Iterator<ParticipantType> ptiter=partTypes.iterator();

		while (ptiter.hasNext()) {
			ParticipantType ptype=ptiter.next();

			Role role=new Role();
			role.setName(XMLUtils.getLocalname(ptype.getName()));
			
			Annotation annotation=new Annotation(AnnotationDefinitions.NAMESPACE);
			annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
						CDLTypeUtil.getNamespace(ptype.getName(), ptype, true));
			role.getAnnotations().add(annotation);
			
			ret.add(role);
		}
		
		java.util.Iterator<Participant> piter=partInstances.iterator();
		
		while (piter.hasNext()) {
			Participant pinst=piter.next();
			
			// Only include 'free' participant instances, as these are bound by
			// a calling protocol.
			if (pinst.getFree() == Boolean.TRUE) {
				Role role=new Role();
				role.setName(XMLUtils.getLocalname(pinst.getName()));
				
				Annotation annotation=new Annotation(AnnotationDefinitions.NAMESPACE);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
							CDLTypeUtil.getNamespace(pinst.getName(), pinst, true));
				role.getAnnotations().add(annotation);
				
				ret.add(role);
			}
		}

		return(ret);
	}

	public static java.util.List<Role> getRoleDeclarations(final Choreography choreo) {
		java.util.List<Role> ret=new java.util.Vector<Role>();
		
		final java.util.List<ParticipantType> partTypes=new java.util.Vector<ParticipantType>();
		final java.util.List<Participant> partInstances=new java.util.Vector<Participant>();
		
		choreo.visit(new DefaultCDLVisitor() {
			
			public void interaction(Interaction interaction) {
				
				if (interaction.getEnclosingChoreography() != choreo) {
					return;
				}
				
				if (interaction.getFromParticipant() != null) {
					if (partInstances.contains(interaction.getFromParticipant()) == false) {
						partInstances.add(interaction.getFromParticipant());
					}
				} else {
					ParticipantType ptype=
						org.pi4soa.cdl.util.PackageUtil.getParticipantForRoleType(
								interaction.getFromRoleType());
					
					if (ptype != null &&
							partTypes.contains(ptype) == false) {
						partTypes.add(ptype);
					}
				}
				
				if (interaction.getToParticipant() != null) {
					if (partInstances.contains(interaction.getToParticipant()) == false) {
						partInstances.add(interaction.getToParticipant());
					}
				} else {
					ParticipantType ptype=
						org.pi4soa.cdl.util.PackageUtil.getParticipantForRoleType(
								interaction.getToRoleType());
					
					if (ptype != null &&
							partTypes.contains(ptype) == false) {
						partTypes.add(ptype);
					}
				}
			}
		});
		
		// Define roles
		
		if (choreo.getRoot() == Boolean.TRUE) {
			// Use complete list for now - when SAVARA-161 being fixed, then could use
			// the participant type list returned from scanning the full choreo
			java.util.Iterator<ParticipantType> ptiter=// partTypes.iterator();
					choreo.getPackage().getTypeDefinitions().getParticipantTypes().iterator();
	
			while (ptiter.hasNext()) {
				ParticipantType ptype=ptiter.next();

				Role role=new Role();
				role.setName(XMLUtils.getLocalname(ptype.getName()));
				
				Annotation annotation=new Annotation(AnnotationDefinitions.NAMESPACE);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
							CDLTypeUtil.getNamespace(ptype.getName(), ptype, true));
				role.getAnnotations().add(annotation);
				
				ret.add(role);
			}
		}
		
		java.util.Iterator<Participant> piter=partInstances.iterator();
		
		while (piter.hasNext()) {
			Participant pinst=piter.next();

			// Check that not a 'free' participant instance - i.e. is not bound
			// from outer choreo
			if (pinst.getFree() == Boolean.FALSE) {
				Role role=new Role();
				role.setName(XMLUtils.getLocalname(pinst.getName()));
				
				Annotation annotation=new Annotation(AnnotationDefinitions.NAMESPACE);
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
							CDLTypeUtil.getNamespace(pinst.getName(), pinst, true));
				role.getAnnotations().add(annotation);
				
				ret.add(role);
			}			
		}
	
		return(ret);
	}
}
