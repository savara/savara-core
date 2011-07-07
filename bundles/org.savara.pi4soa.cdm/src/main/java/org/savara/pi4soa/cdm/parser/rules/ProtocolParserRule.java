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
 * 6 Jun 2008 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser.rules;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.util.CDLTypeUtil;
import org.pi4soa.common.util.NamesUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;
import org.scribble.protocol.util.RoleUtil;

public class ProtocolParserRule implements ParserRule {

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
				cdlType instanceof Choreography);
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
		Choreography choreo=(Choreography)cdlType;
		
		Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

		scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
				CDLTypeUtil.getURIFragment(choreo));
		ret.getAnnotations().add(scannotation);
			
		context.pushScope();
		
		//LocatedName modelName=new LocatedName();
		//modelName.setName(choreo.getName());
		
		ret.setName(choreo.getName());
		
		// Define roles
		//defineRoles(context, choreo, ret.getBlock());
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(choreo);
		
		for (Role r : roles) {
			ParameterDefinition pd=new ParameterDefinition();
			pd.setName(r.getName());
			ret.getParameterDefinitions().add(pd);
			
			context.setState(r.getName(), r);
		}

		java.util.List<Role> declared=CDMProtocolParserUtil.getRoleDeclarations(choreo);
		
		if (declared.size() > 0) {
			Introduces rl=new Introduces();
			
			// TODO: Need to discover introducing role (or roles if need to be
			// different for each introduced role)
			// For now use any role in the list of parameters
			if (roles.size() > 0) {
				rl.setIntroducer(roles.get(0));
			} else {
				Role r=declared.get(0);
				
				ParameterDefinition pd=new ParameterDefinition();
				pd.setName(r.getName());
				ret.getParameterDefinitions().add(pd);
				
				context.setState(r.getName(), r);
				
				// Associate Namespace annotation with protocol
				Annotation annotation=AnnotationDefinitions.getAnnotation(r.getAnnotations(),
							AnnotationDefinitions.NAMESPACE);
				
				if (annotation != null) {
					Annotation pa=new Annotation(AnnotationDefinitions.NAMESPACE);
					pa.getProperties().putAll(annotation.getProperties());
					pa.getProperties().put(AnnotationDefinitions.ROLE_PROPERTY, r.getName());
					ret.getAnnotations().add(pa);
				}

				rl.setIntroducer(r);
				declared.remove(0);
			}
			
			for (Role r : declared) {
				rl.getRoles().add(r);
				
				context.setState(r.getName(), r);
				
				// Associate Namespace annotation with protocol
				Annotation annotation=AnnotationDefinitions.getAnnotation(r.getAnnotations(),
							AnnotationDefinitions.NAMESPACE);
				
				if (annotation != null) {
					Annotation pa=new Annotation(AnnotationDefinitions.NAMESPACE);
					pa.getProperties().putAll(annotation.getProperties());
					pa.getProperties().put(AnnotationDefinitions.ROLE_PROPERTY, r.getName());
					ret.getAnnotations().add(pa);
				}
			}
			
			ret.getBlock().add(rl);
		}
		
		// Check if root, then need to project other sibling choreos
		if (choreo.getRoot() == Boolean.TRUE) {
			
			java.util.Iterator<Choreography> citer=
				choreo.getPackage().getChoreographies().iterator();
	
			while (citer.hasNext()) {
				Choreography subchoreo=citer.next();
				
				if (subchoreo != choreo) {
					Protocol subconv=(Protocol)
							parse(context, Protocol.class, subchoreo);
					
					ret.getBlock().getContents().add(subconv);
					
					context.addProtocol(subconv);
				}
				
				for (int i=0; i < subchoreo.getFinalizers().size(); i++) {
					FinalizerHandler finalizer=subchoreo.getFinalizers().get(i);
					
					ParserRule rule=ParserRuleFactory.getConverter(
							Protocol.class, finalizer);
					
					if (rule != null) {
						
						Protocol subconv=(Protocol)
							rule.parse(context, Protocol.class, finalizer);
					
						ret.getBlock().getContents().add(subconv);
					
						context.addProtocol(subconv);
					}
				}
			}
		}
		
		// Process any sub-choreographies	
		java.util.Iterator<Choreography> citer=
					choreo.getEnclosedChoreographies().iterator();
		
		while (citer.hasNext()) {
			Choreography subchoreo=citer.next();

			Protocol subconv=(Protocol)
					parse(context, Protocol.class, subchoreo);
			
			ret.getBlock().getContents().add(subconv);
			
			context.addProtocol(subconv);
			
			for (int i=0; i < subchoreo.getFinalizers().size(); i++) {
				FinalizerHandler finalizer=subchoreo.getFinalizers().get(i);
				
				ParserRule rule=ParserRuleFactory.getConverter(
						Protocol.class, finalizer);
				
				if (rule != null) {
					
					subconv = (Protocol)
						rule.parse(context, Protocol.class, finalizer);
				
					ret.getBlock().getContents().add(subconv);
				
					context.addProtocol(subconv);
				}
			}
		}
		
		// Convert variables
		//convertVariables(context, choreo, ret.getBlock());
		
		// Define identities
		//convertIdentities(context, choreo, ret);
		
		// Check if exception handlers have been defined
		// and/or completion condition
		if (NamesUtil.isSet(choreo.getCompletionCondition()) ||
			(choreo.getExceptionHandler() != null &&
					choreo.getExceptionHandler().getExceptionWorkUnits().size() > 0)) {
			
			Do te=new Do();
			ret.getBlock().getContents().add(te);
			
			// Process all of the activities within the
			// choreography
			convertActivities(context, choreo.getActivities(),
						te.getBlock());
			
			for (int i=0; choreo.getExceptionHandler() != null &&
						i < choreo.getExceptionHandler().
						getExceptionWorkUnits().size(); i++) {
				ExceptionWorkUnit ewu=choreo.getExceptionHandler().
					getExceptionWorkUnits().get(i);
				
				Interrupt interruptPath=new Interrupt();

				/*
				 * TODO: Consider how to deal with catch types
				 *
				if (NamesUtil.isSet(ewu.getExceptionType())) {
					TypeReference ref=new TypeReference();
					ref.setLocalpart(XMLUtils.getLocalname(ewu.getExceptionType()));
					ref.setNamespace(CDLTypeUtil.getNamespace(
							ewu.getExceptionType(), choreo));
					
					catchPath.setType(ref);					
				}
				*/
				
				te.getInterrupts().add(interruptPath);
				
				convertActivities(context, ewu.getActivities(),
								interruptPath.getBlock());
			}
			
			/*
			 * TODO: Deal with completion condition
			 *
			if (NamesUtil.isSet(choreo.getCompletionCondition())) {
				InterruptBlock interrupt=new InterruptBlock();
				
				te.getEscapeBlocks().add(interrupt);
				
				// Set expression from completion condition
				// using xpath
				XPathExpression exp=new XPathExpression();
				
				exp.setQuery(choreo.getCompletionCondition());
				
				interrupt.setExpression(exp);

				// No activities
			}
			*/
		} else {
		
			// Process all of the activities within the
			// choreography
			convertActivities(context, choreo.getActivities(), ret.getBlock());
		}
		
		// Transfer sub-conversations to end of block
		if (ret.getBlock().getContents().size() > 0) {
			org.scribble.protocol.model.Activity lastAct=
				ret.getBlock().getContents().get(ret.getBlock().getContents().size()-1);
			int pos=0;
			while (ret.getBlock().get(pos) != lastAct) {
				if (ret.getBlock().get(pos) instanceof Protocol) {
					Protocol c=(Protocol)ret.getBlock().get(pos);
	
					ret.getBlock().getContents().remove(pos);
					ret.getBlock().getContents().add(c);
					
					context.removeProtocol(c);
				} else {
					pos++;
				}
			}
		}
		
		// SAVARA-214 - check if declared roles should be moved to inner blocks
		if (ret.getBlock().get(0) instanceof Introduces) {
			Introduces rl=(Introduces)ret.getBlock().get(0);
			
			for (int i=rl.getRoles().size()-1; i >= 0; i--) {
				Role r=rl.getRoles().get(i);
				Block b=RoleUtil.getEnclosingBlock(ret, r);
				
				if (b == null) {
					// Report error
				} else if (b != ret.getBlock()){
					Introduces innerrl=null;
					
					if (b.size() > 0 && b.get(0) instanceof Introduces) {
						innerrl = (Introduces)b.get(0);
					} else {
						innerrl = new Introduces();
						innerrl.setIntroducer(rl.getIntroducer());
						b.getContents().add(0, innerrl);
					}
					
					rl.getRoles().remove(r);
					innerrl.getRoles().add(r);
				}
			}
			
			if (rl.getRoles().size() == 0) {
				ret.getBlock().remove(rl);
			}
		}
		
		context.popScope();
		
		return(ret);
	}

	/*
	protected static void defineRoles(ConverterContext context,
			final Choreography choreo, Block block) {
	
		//org.pi4soa.cdl.interfaces.InterfaceDeriver intfDeriver=
		//			org.pi4soa.cdl.interfaces.InterfaceFactory.getInterfaceDeriver();
		
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
		java.util.Iterator<Participant> piter=partInstances.iterator();
		
		RoleList roleList=null;
		
		while (piter.hasNext()) {
			Role role=new Role();
			Participant pinst=piter.next();
			role.setName(XMLUtils.getLocalname(pinst.getName()));
			role.getProperties().put(PropertyName.NAMESPACE,
						CDLTypeUtil.getNamespace(pinst.getName(), pinst, true));
			
			if (roleList == null) {
				roleList = new RoleList();
				block.getContents().add(roleList);
			}
			
			roleList.getRoles().add(role);
			
			context.setState(role.getName(), role);
		}

		java.util.Iterator<ParticipantType> ptiter=partTypes.iterator();

		if (choreo.getRoot() != Boolean.TRUE) {
			roleList = null;
		} else {
			ptiter = choreo.getPackage().getTypeDefinitions().getParticipantTypes().iterator();
		}
	
		while (ptiter.hasNext()) {
			Role role=new Role();
			ParticipantType ptype=ptiter.next();
			role.setName(XMLUtils.getLocalname(ptype.getName()));
			role.getProperties().put(PropertyName.NAMESPACE,
						CDLTypeUtil.getNamespace(ptype.getName(), ptype, true));
			
			if (roleList == null) {
				roleList = new RoleList();
				
				//roleList.setOpen(choreo.getRoot() != Boolean.TRUE);
				
				block.getContents().add(roleList);
			}
			
			roleList.getRoles().add(role);
			
			context.setState(role.getName(), role);
		}
	}
	*/
	
	/*
	protected static void setupInterfaces(org.pi4soa.cdl.RoleType roleType, Contract contract,
					org.pi4soa.cdl.interfaces.InterfaceDeriver intfDeriver) {
		
		org.pi4soa.cdl.interfaces.RoleTypeDefinition rtd=
						intfDeriver.getRoleTypeDefinition(roleType);
				
		InterfaceVisitorImpl iv=new InterfaceVisitorImpl(contract);
		
		rtd.visit(iv);
	}
	*/
	
	/*
	protected void convertVariables(ConverterContext context,
			Choreography choreo, Block block) {
		
		java.util.List<org.pi4soa.cdl.Variable> vars=
					choreo.getVariableDefinitions();
		
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
				
				/* TODO: See if variables should be located?
				for (int j=0; j < roleTypes.size(); j++) {
					org.scribble.conversation.model.Variable newVar=
						new org.scribble.conversation.model.Variable();
					
					newVar.setName(var.getName());
				}
				*/
				
	/*
				org.scribble.conversation.model.Variable newVar=
					new org.scribble.conversation.model.Variable();
				
				newVar.setName(var.getName());
				
				vlist.getVariables().add(newVar);
				
				block.getContents().add(vlist);
				
			} else if (var.getType() instanceof ChannelType) {	
				
				// TODO: Only convert if channel is explicitly defined
				

			}
		}
	}
	*/
	
	protected static void convertActivities(ParserContext context,
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
					Choreography choreo, Conversation conv) {
		
		// Define identities
		java.util.List<org.pi4soa.cdl.Variable> vars=
					choreo.getVariableDefinitions();
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
	
	/*
	 * Not sure if required when using TAP to relate resources, rather than
	 * info in each artifact.
	 *
	private void registerRole(ConverterContext context,
			Choreography choreo,
			Protocol conv, RoleList roleList,
				Role role, CDLType cdlType) {
		
		if (cdlType.getSemanticAnnotations().size() > 0) {
			java.util.Iterator<SemanticAnnotation> iter=
					cdlType.getSemanticAnnotations().iterator();
			
			while (iter.hasNext()) {
				SemanticAnnotation annotation=iter.next();
				
				if (annotation.getName() != null &&
						annotation.getName().equals(CONVERSATION_TYPE)) {
					String convType=annotation.getAnnotation();
					
					if (convType != null) {
						int index=convType.indexOf('@');
					
						if (index != -1) {
							ConformanceReference ref=
								new ConformanceReference(ConversationNotation.NOTATION_CODE);
						
							ref.setLocatedRole(convType.substring(index+1));
							
							String mainpart=convType.substring(0, index);
							
							index = mainpart.lastIndexOf(".");
							
							String namespace="";
							String localpart=mainpart;
							
							if (index != -1) {
								namespace=mainpart.substring(0, index);								
								localpart=mainpart.substring(index+1);
							}
							
							String[] elems=localpart.split("\\$");
							if (elems.length > 1) {
								localpart = elems[0];
								
								for (int i=1; i < elems.length; i++) {
									ref.getSubDefinitionPath().addPathElement(elems[i]);
								}
							}
							
							ref.setNamespace(namespace);
							ref.setLocalpart(localpart);
							
							ref.setFullyQualified(true);
							
							conv.getConformsTo().add(ref);
							
							ModelReference sref=context.getSource();
							
							// If not the root choreography, then
							// identify the sub definition path
							// in the source reference
							if (choreo.getRoot() != Boolean.TRUE) {
								sref = new ModelReference(sref);
								Choreography sub=choreo;
								
								while (sub != null && sub.getRoot() != Boolean.TRUE) {
									sref.getSubDefinitionPath().addPathElement(0, sub.getName());
									
									sub = sub.getParent().getEnclosingChoreography();
								}
							}
							
							// Establish 'conforms to' dependency
							// between source (which could be a
							// sub definition) and the identified
							// conversation type
							
							// GPB: TO REMOVE dm.recordDependency(sref, ref,
							//		DependencyType.ConformsTo);

						} else {
							logger.warning("Conversation type '"+
									convType+"' does not " +
									"contain '@' located role separator");
						}
					}
					
				}
			}
		}
	}
		*/

	//private static Logger logger = Logger.getLogger(ProtocolConverterRuleImpl.class.getPackage().getName());
	
	/*
	public static class InterfaceVisitorImpl implements InterfaceVisitor,
							java.io.Serializable {
		
		private static final long serialVersionUID = 4068744630125720449L;
		
		private org.savara.contract.model.Contract m_contract=null;
		private org.savara.contract.model.Interface m_currentInterface=null;
		private org.savara.contract.model.MessageExchangePattern m_currentMEP=null;

		public InterfaceVisitorImpl(Contract contract) {
			m_contract = contract;
		}
		
		public Contract getContract() {
			return(m_contract);
		}
		
		public void interfaceEnd(InterfaceDefinition defn) {
			m_currentInterface = null;
		}

		public void interfaceStart(InterfaceDefinition defn) {
			m_currentInterface = new org.savara.contract.model.Interface();
			
			m_currentInterface.setNamespace(defn.getNamespace());

			if (defn.getInterfaceName() != null &&
					defn.getInterfaceName().trim().length() > 0) {
				m_currentInterface.setName(defn.getInterfaceName());
			} else {
				m_currentInterface.setName(defn.getBehaviorName());
			}
			
			m_contract.getInterfaces().add(m_currentInterface);
		}

		public void message(MessageDefinition defn) {
			
			if (defn.getClassification() == MessageDefinition.INPUT) {
				m_currentMEP.getTypes().add(createType(defn));
				
			} else if (m_currentMEP instanceof RequestResponseMEP) {
				RequestResponseMEP mep=(RequestResponseMEP)m_currentMEP;
				
				if (defn.getClassification() == MessageDefinition.OUTPUT) {
					mep.getResponseTypes().add(createType(defn));
					
				} else if (defn.getClassification() == MessageDefinition.OUTFAULT) {
					FaultDetails fault=new FaultDetails();
					
					fault.setName(defn.getFaultName());
					//fault.setNamespace(defn.getFaultNamespace());
					
					fault.getTypes().add(createType(defn));
					
					mep.getFaultDetails().add(fault);
				}
			}
		}
		
		protected Type createType(MessageDefinition defn) {
			Type ret=new Type();
	
			TypeDefinition td=new TypeDefinition();
			
			td.setTypeSystem(TypeSystem.XSD);
			
			if (defn.getElement() != null && defn.getElement().trim().length() > 0) {
				td.setName(defn.getElement());
				
				QName qname=new QName(defn.getElementNamespace(), defn.getElement());
				td.setDataType(qname.toString());
				
				td.getProperties().put(PropertyName.XSD_ELEMENT, "true");

			} else if (defn.getType() != null && defn.getType().trim().length() > 0) {
				td.setName(defn.getType());
								
				QName qname=new QName(defn.getTypeNamespace(), defn.getType());
				td.setDataType(qname.toString());
				
				td.getProperties().put(PropertyName.XSD_TYPE, "true");
			}
			
			ret.setName(td.getName());

			if (getContract().getTypeDefinition(td.getName()) == null) {
				getContract().getTypeDefinitions().add(td);
			}

			return(ret);
		}

		public void operationEnd(OperationDefinition defn) {
			m_currentMEP = null;
		}

		public void operationStart(OperationDefinition defn) {
			if (defn.getMessages().size() == 1 &&
					((MessageDefinition)defn.getMessages().get(0)).getClassification()
							== MessageDefinition.INPUT) {
				m_currentMEP = new OneWayRequestMEP();
			} else {
				m_currentMEP = new RequestResponseMEP();
			}
			
			m_currentMEP.setOperation(defn.getOperationName());
			
			m_currentInterface.getMessageExchangePatterns().add(m_currentMEP);
		}

		public void roleTypeEnd(RoleTypeDefinition defn) {
			// TODO Auto-generated method stub
			
		}

		public void roleTypeStart(RoleTypeDefinition defn) {
			// TODO Auto-generated method stub
			
		}	
	}
	*/
}
