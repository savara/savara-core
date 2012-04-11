/*
 * Copyright 2005-9 Pi4 Technologies Ltd
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
 * 1 Jun 2009 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.Interaction;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;

public class FinalizerHandlerParserRule implements ParserRule {

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
		return(scribbleType == Protocol.class &&
				cdlType instanceof FinalizerHandler);
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
		Protocol ret=new Protocol();
		FinalizerHandler choreo=(FinalizerHandler)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		Object uri=CDLTypeUtil.getURIFragment(choreo);
		
		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				uri);
		ret.getAnnotations().add(scannotation);
		
		ret.getProperties().put(ModelProperties.URI, uri);
			
		context.pushScope();
		
		ret.setName(choreo.getEnclosingChoreography().getName()+"_"+choreo.getName());
		
		// Define roles
		defineRoles(context, choreo, ret);
		
		// Convert variables
		//convertVariables(context, choreo, ret.getBlock());
		
		// Define identities
		//convertIdentities(context, choreo, ret);
		
		// Process all of the activities within the
		// choreography
		convertActivities(context, choreo.getActivities(), ret.getBlock());

		context.popScope();
		
		return(ret);
	}

	
	protected void defineRoles(ParserContext context,
			final FinalizerHandler choreo, Protocol conv) {
	
		final java.util.List<ParticipantType> partTypes=new java.util.Vector<ParticipantType>();
		final java.util.List<Participant> partInstances=new java.util.Vector<Participant>();
		
		choreo.visit(new DefaultCDLVisitor() {
			
			public void interaction(Interaction interaction) {
				
				if (interaction.getEnclosingChoreography() != choreo.getEnclosingChoreography()) {
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
		java.util.Iterator<Participant> piter=partInstances.iterator();
		
		Introduces roleList=null;
		
		// TODO: Should this be protocol parameters?
		
		while (piter.hasNext()) {
			Role role=new Role();
			Participant pinst=piter.next();
			role.setName(pinst.getName());
			
			if (roleList == null) {
				roleList = new Introduces();
				conv.getBlock().getContents().add(roleList);
			}
			
			roleList.getIntroducedRoles().add(role);
			
			context.setState(role.getName(), role);
		}

		java.util.Iterator<ParticipantType> ptiter=partTypes.iterator();

		/*
		if (choreo.getRoot() != Boolean.TRUE) {
			roleList = null;
		} else {
			ptiter = choreo.getPackage().getTypeDefinitions().getParticipantTypes().iterator();
		}
		*/
	
		while (ptiter.hasNext()) {
			Role role=new Role();
			ParticipantType ptype=ptiter.next();
			role.setName(ptype.getName());
			
			if (roleList == null) {
				roleList = new Introduces();
				
				//roleList.setOpen(choreo.getEnclosingChoreography().getRoot() != Boolean.TRUE);
				
				conv.getBlock().getContents().add(roleList);
			}
			
			roleList.getIntroducedRoles().add(role);
			
			context.setState(role.getName(), role);
		}
	}
	
	/*
	protected void convertVariables(ConverterContext context,
			FinalizerHandler choreo, Block block) {
		
		java.util.List<org.pi4soa.cdl.Variable> vars=
					choreo.getEnclosingChoreography().getVariableDefinitions();
		
		for (int i=0; i < vars.size(); i++) {
			org.pi4soa.cdl.Variable var=(org.pi4soa.cdl.Variable)vars.get(i);
			
			if (var.getType() instanceof InformationType) {				
				java.util.List<RoleType> roleTypes=var.getRoleTypes();
				
				if (roleTypes.size() == 0) {
					roleTypes = choreo.getPackage().getTypeDefinitions().getRoleTypes();
				}
				
				VariableList vlist=new VariableList();
				TypeReference ref=new TypeReference();
				
				String qname=((InformationType)var.getType()).getElementName();
				
				if (NamesUtil.isSet(qname)==false) {
					qname=((InformationType)var.getType()).getTypeName();
				}
				
				ref.setNamespace(CDLTypeUtil.getNamespace(qname, var));
				ref.setLocalpart(XMLUtils.getLocalname(qname));
				
				vlist.setType(ref);
								
				org.scribble.conversation.model.Variable newVar=
					new org.scribble.conversation.model.Variable();
				
				newVar.setName(var.getName());
				
				vlist.getVariables().add(newVar);
				
				block.getContents().add(vlist);
			}
		}
	}
	*/
	
	protected void convertActivities(ParserContext context,
				java.util.List<org.pi4soa.cdl.Activity> acts, Block block) {
		
		java.util.Iterator<org.pi4soa.cdl.Activity> actiter=
						acts.iterator();
		
		while (actiter.hasNext()) {
			org.pi4soa.cdl.Activity act=actiter.next();
			
			ParserRule rule=ParserRuleFactory.getConverter(
					org.scribble.protocol.model.Activity.class, act);
			
			if (rule != null) {
				org.scribble.protocol.model.Activity activity=
					(org.scribble.protocol.model.Activity)
					rule.parse(context,
							org.scribble.protocol.model.Activity.class, act);
				
				if (activity != null) {
					
					if (activity instanceof Block) {
						block.getContents().addAll(((Block)activity).getContents());
					} else {
						block.getContents().add(activity);
					}
				}
			}
		}
	}
	
	/*
	protected void convertIdentities(ConverterContext context,
			FinalizerHandler choreo, Conversation conv) {
		
		// Define identities
		java.util.List<org.pi4soa.cdl.Variable> vars=
					choreo.getEnclosingChoreography().getVariableDefinitions();
		java.util.List<org.scribble.conversation.model.Identity> ids=
			new java.util.Vector<org.scribble.conversation.model.Identity>();

		for (int i=0; i < vars.size(); i++) {
			if (vars.get(i).getType() instanceof org.pi4soa.cdl.ChannelType) {
				org.pi4soa.cdl.ChannelType ctype=
						(org.pi4soa.cdl.ChannelType)vars.get(i).getType();
				
				java.util.Iterator<org.pi4soa.cdl.Identity> cids=
						ctype.getIdentities().iterator();
				
				while (cids.hasNext()) {
					org.pi4soa.cdl.Identity cid=cids.next();
					org.scribble.conversation.model.Identity id=
						new org.scribble.conversation.model.Identity();
					
					if (cid.getType() == KeyType.ASSOCIATION) {
						id.setIdentityType(IdentityType.Association);
					} else if (cid.getType() == KeyType.DERIVED) {
						id.setIdentityType(IdentityType.Derived);
					} else {
						id.setIdentityType(IdentityType.Primary);
					}
					
					for (int j=0; j < cid.getTokens().size(); j++) {
						id.getNames().add(cid.getTokens().get(j).getName());
					}
					
					if (ids.contains(id) == false) {
						ids.add(id);
					}
				}
			}
		}
		
		conv.getIdentities().addAll(ids);		
	}
	*/

	//private static Logger logger = Logger.getLogger("org.pi4soa.scribble.cdm.parser.rules");
}
