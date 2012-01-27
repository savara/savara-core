/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.model.change;

import javax.xml.namespace.QName;

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.model.TActivityContainer;
import org.savara.bpel.model.TBoolean;
import org.savara.bpel.model.TCatch;
import org.savara.bpel.model.TElseif;
import org.savara.bpel.model.TFaultHandlers;
import org.savara.bpel.model.TIf;
import org.savara.bpel.model.TInvoke;
import org.savara.bpel.model.TOnMessage;
import org.savara.bpel.model.TPartnerLink;
import org.savara.bpel.model.TPick;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TScope;
import org.savara.bpel.model.TSequence;
import org.savara.bpel.model.TVariable;
import org.savara.bpel.util.BPELInteractionUtil;
import org.savara.bpel.util.PartnerLinkUtil;
import org.savara.bpel.util.VariableUtil;
import org.savara.protocol.model.change.ModelChangeContext;
import org.savara.protocol.model.change.ModelChangeUtils;
import org.savara.protocol.model.util.InteractionUtil;
import org.savara.protocol.util.ProtocolUtils;
import org.savara.contract.model.Contract;
import org.savara.contract.model.Interface;
import org.savara.wsdl.util.WSDLGeneratorUtil;
import org.scribble.protocol.model.*;

/**
 * This is the model change rule for the Choice.
 */
public class ChoiceModelChangeRule extends AbstractBPELModelChangeRule {

	/**
	 * This method determines whether the rule is appropriate
	 * for the supplied type of model, parent (in the context) and
	 * model object.
	 *
	 * @param context The context
	 * @param model The model
	 * @param mobj The model object causing the change
	 * @param ref The optional reference model object
	 * @return Whether the rule supports the supplied information
	 */
	@Override
	public boolean isInsertSupported(ModelChangeContext context,
				ProtocolModel model, ModelObject mobj, ModelObject ref) {
		boolean ret=false;
		
		if (mobj instanceof org.scribble.protocol.model.Choice && isBPELModel(model)) {
			ret = true;
		}
		
		return(ret);
	}

	/**
	 * This method adds a new model object, within a
	 * parent model object, with the details supplied in
	 * another model object. The supplied model object
	 * will usually be from a different model representation
	 * (e.g. due to a merge), so the details will be
	 * copied and placed in the representation associated
	 * with the supplied model and parent model object.<p>
	 * <p>
	 * If a reference model object is supplied, then the
	 * insertion will occur relative to it. If the reference
	 * object is a block, then it means that the insertion
	 * should occur at the end of the block. Otherwise the
	 * new model object should be inserted before the
	 * reference object, within the containing block.<p>
	 * <p>
	 * If the reference object is not supplied, then the
	 * new model object should be inserted at the end of
	 * the behaviour associated with the parent in the model
	 * change context.
	 * 
	 * @param context The context
	 * @param model The model being changed
	 * @param mobj The model object details to be inserted
	 * @param ref The optional reference model object
	 * @return Whether the change has been applied
	 */
	@Override
	public boolean insert(ModelChangeContext context,
				ProtocolModel model, ModelObject mobj, ModelObject ref) {
		TProcess bpelModel=getBPELModel(model);
		org.scribble.protocol.model.Choice elem=
					 (org.scribble.protocol.model.Choice)mobj;
		java.util.List<Block> paths=elem.getPaths();
		
		/*
		
		// SAVARA-215 - ignore choice if only labels and no path contents
		boolean f_emptyPaths=true;
		for (When when : paths) {
			if (when.getMessageSignature().getTypeReferences().size() > 0 ||
					when.getBlock().size() > 0) {
				f_emptyPaths = false;
				break;
			}
		}
		if (f_emptyPaths) {
			return(true);
		}
		*/
		
		Role role=null;
		//String roleNamespace=null;
	
		if (elem.getEnclosingProtocol() != null) {
			role = elem.getEnclosingProtocol().getLocatedRole();
		}

		//Contract contract=ModelChangeUtils.getContract(context,
		//		elem.enclosingProtocol().getRole());
		
		//if (contract != null) {
		//	roleNamespace = contract.getNamespace();
		//}
	
		// Check if the 'If' construct is to handle responses
		// to a preceding request being sent
		if (InteractionPatterns.isResponseAndFaultHandler(elem)) {
			
			if (context.getParent() instanceof TSequence) {
				// Find fault handler
				TSequence seq=(TSequence)context.getParent();
				TScope scope=(TScope)context.getProperties().get(BPELDefinitions.BPEL_FAULT_SCOPE_PROPERTY);
				
				if (scope == null) {
					TSequence newseq = new TSequence();
					
					// Move invoke
					java.util.List<Object> acts=
						((TSequence)context.getParent()).getActivity();

					if (acts.size() > 0 &&
							acts.get(acts.size()-1) instanceof TInvoke) {
						TInvoke invoke=(TInvoke)acts.get(acts.size()-1);
						acts.remove(invoke);
						newseq.getActivity().add(invoke);
					}

					// Create scope and fault handler
					scope = new TScope();
					scope.setFaultHandlers(new TFaultHandlers());

					context.getProperties().put(BPELDefinitions.BPEL_FAULT_SCOPE_PROPERTY, scope);
					context.getProperties().put(BPELDefinitions.BPEL_FAULT_SCOPE_PARENT_PROPERTY, seq);
					
					seq.getActivity().add(scope);
					
					seq = newseq;
					
					scope.setSequence(seq);
					
					context.setParent(seq);
				}
				
				TFaultHandlers fh=scope.getFaultHandlers();
				
				for (int i=0; i < paths.size(); i++) {
					Block path=paths.get(i);
					
					Interaction interaction=InteractionPatterns.getFirstInteraction(path);
					
					//if (path.getBlock().getContents().size() > 0) {
					//	Activity act=path.getBlock().getContents().get(0);
						TSequence subseq=null;
						
						if (InteractionUtil.isFaultResponse(interaction)) {
							String faultName=InteractionUtil.getFaultName(interaction);
							
							Contract fromContract = ModelChangeUtils.getContract(context,
										(interaction.getFromRole()==null?role:interaction.getFromRole()));
							
							// Define fault message type
							QName qname= WSDLGeneratorUtil.getFaultMessageType(fromContract.getNamespace(),
												faultName,
												ProtocolUtils.getNamespacePrefix(elem.getModel(), fromContract.getNamespace()));
							
							String faultVarName=qname.getLocalPart()+"Var";
							//String mesgType=InteractionPatterns.getMessageTypeLocalPart((Interaction)act);
							//String namespace=InteractionPatterns.getMessageTypeNameSpace((Interaction)act);
					
							TCatch c=new TCatch();
							c.setFaultVariable(faultVarName);
							
							// Find namespace prefix
							/* TODO: Sort out namespace prefix issue
							if (qname != null) {
								String prefix=bpelModel.addNamespace(qname.getNamespaceURI());
								
								if (prefix != null) {
									mesgType = prefix+":"+qname.getLocalPart();
									faultName = prefix+":"+faultName;
								}
							}
							*/
							
							c.setFaultName(new QName(qname.getNamespaceURI(), faultName,
									ProtocolUtils.getNamespacePrefix(elem.getModel(), qname.getNamespaceURI())));
							c.setFaultMessageType(qname);
							
							// Add catch to fault handler
							fh.getCatch().add(c);
							
							subseq = new TSequence();
							c.setSequence(subseq);
						} else {
							subseq = seq;

							TInvoke invoke=BPELInteractionUtil.getInvoke(
										interaction.getMessageSignature().getOperation(),
										context.getProperties());
							
							if (invoke != null) {
								Contract fromContract = ModelChangeUtils.getContract(context,
										interaction.getFromRole());
							
								QName qname=null;
								if (InteractionUtil.isRequest(interaction)) {
									qname = WSDLGeneratorUtil.getRequestMessageType(fromContract.getNamespace(),
											interaction.getMessageSignature().getOperation(),
											ProtocolUtils.getNamespacePrefix(elem.getModel(), fromContract.getNamespace()));
								} else if (InteractionUtil.isFaultResponse(interaction)) {
									qname = WSDLGeneratorUtil.getFaultMessageType(fromContract.getNamespace(),
											InteractionUtil.getFaultName(interaction),
											ProtocolUtils.getNamespacePrefix(elem.getModel(), fromContract.getNamespace()));
								} else {
									qname = WSDLGeneratorUtil.getResponseMessageType(fromContract.getNamespace(),
											interaction.getMessageSignature().getOperation(),
														ProtocolUtils.getNamespacePrefix(elem.getModel(), fromContract.getNamespace()));
								}

								String varName=qname.getLocalPart()+"Var"; //InteractionPatterns.getVariableName((Interaction)act);
								
								invoke.setOutputVariable(varName);

								// Create variable
								if (varName != null) {
									createVariable(context, varName, interaction, bpelModel);
								}
							}
						}
						
						context.pushScope();
						
						context.setParent(subseq);
						
						for (int j=0; j < path.getContents().size(); j++) {
							context.insert(model, path.getContents().get(j), null);
						}
						
						context.popScope();
					//}
				}
			} else {
				// TODO: Error handling
			}
		} else if (InteractionPatterns.isSwitch(elem)) {
			TPick act=new TPick();
			
			if (context.getParent() instanceof TSequence) {
				((TSequence)context.getParent()).getActivity().add(act);
			}
			
			for (int i=0; i < paths.size(); i++) {
				Block path=paths.get(i);
				
				TSequence seq=new TSequence();

				Interaction interaction=InteractionPatterns.getFirstInteraction(path);
				
				if (interaction != null) {
					
					// Process the activities within the conversation
					java.util.List<Activity> acts=path.getContents();
								
					context.pushScope();
					
					context.setParent(seq);
					
					for (int j=0; j < acts.size(); j++) {
						context.insert(model, acts.get(j), null);
					}
					
					context.popScope();
					
					//Interaction recv=InteractionPatterns.getPickPathInteraction(path);
					
					TOnMessage onm=new TOnMessage();
					onm.setSequence(seq);
					
					TPartnerLink pl=new TPartnerLink();
					String portType=null;
					String namespace=null;
					
					String prevPLName=role.getName()+"To"+interaction.getFromRole().getName();
					
					//String mainPrefix=null;
					
					/* TODO: namespace issue
					if (contract != null) {
						mainPrefix = bpelModel.addNamespace(contract.getNamespace());
					}
					*/
					
					// Check if partner link already exists in
					// other direction
					TPartnerLink prev=
						PartnerLinkUtil.getPartnerLink(bpelModel, prevPLName);
					
					Contract contract=null;
	
					if (interaction != null && InteractionUtil.isRequest(interaction) && prev == null) {
						
						pl.setMyRole(role.getName());
						pl.setName(interaction.getFromRole().getName()+"To"+role.getName());
						
						String plt=interaction.getFromRole().getName()+"To"+role.getName()+"Service"+"LT";
											
						contract = ModelChangeUtils.getContract(context, role);
						
						if (contract != null) {
							pl.setPartnerLinkType(new QName(contract.getNamespace(), plt,
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));
							
							if (contract.getInterfaces().size() > 0) {
								Interface intf = contract.getInterfaces().iterator().next();
								
								portType = intf.getName();
								namespace = intf.getNamespace();
								
								/* TODO: namespace issue
								String prefix = bpelModel.addNamespace(intf.getNamespace());
								
								if (prefix != null) {
									portType = prefix+":"+portType;
								}
								*/
							}
						}
					} else {
						pl.setMyRole(role.getName());
						pl.setPartnerRole(elem.getRole().getName());
						pl.setName(role.getName()+"To"+elem.getRole().getName());
						
						String plt=role.getName()+"To"+elem.getRole().getName()+"Requester"+"LT";
						
	
						//portType = role.getName()+
						//		recv.getFromRole().getName()+"CallbackPT";				
	
						contract = ModelChangeUtils.getContract(context, elem.getRole());
						
						if (contract != null) {
							pl.setPartnerLinkType(new QName(contract.getNamespace(), plt,
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));
	
							if (contract.getInterfaces().size() > 0) {
								Interface intf = contract.getInterfaces().iterator().next();
								
								portType = intf.getName();
								namespace = intf.getNamespace();
								
								/* TODO: Namespace issue
								String prefix = bpelModel.addNamespace(intf.getNamespace());
								
								if (prefix != null) {
									portType = prefix+":"+portType;
								}
								*/
							}
						}
					}
				
					QName qname=null;
					if (InteractionUtil.isRequest(interaction)) {
						qname = WSDLGeneratorUtil.getRequestMessageType(contract.getNamespace(),
								interaction.getMessageSignature().getOperation(),
								ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
					} else if (InteractionUtil.isFaultResponse(interaction)) {
						qname = WSDLGeneratorUtil.getFaultMessageType(contract.getNamespace(),
								InteractionUtil.getFaultName(interaction),
								ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
					} else {
						qname = WSDLGeneratorUtil.getResponseMessageType(contract.getNamespace(),
								interaction.getMessageSignature().getOperation(),
								ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
					}
	
					String varName=qname.getLocalPart()+"Var"; //InteractionPatterns.getVariableName(recv);
					
					if (varName != null) {
						onm.setVariable(varName);
					}
					
					// Create partner link
					TPartnerLink other=
						PartnerLinkUtil.getPartnerLink(bpelModel, pl.getName());
	
					if (other == null) {
						bpelModel.getPartnerLinks().getPartnerLink().add(pl);
					} else {
						if (other.getPartnerRole() == null &&
								pl.getPartnerRole() != null) {
							other.setPartnerRole(pl.getPartnerRole());
						}
						if (other.getMyRole() == null &&
								pl.getMyRole() != null) {
							other.setMyRole(pl.getMyRole());
						}
					}
					
					// Create variable
					if (varName != null) {
						createVariable(context, varName, interaction, bpelModel);
					}
				
					// Check if create instance
					if (org.scribble.protocol.util.InteractionUtil.isInitialInteraction(elem.getModel(), path)) {
						act.setCreateInstance(TBoolean.YES);
					}
					
					// Set details on interaction
					onm.setPartnerLink(pl.getName());
					onm.setPortType(new QName(namespace, portType,
							ProtocolUtils.getNamespacePrefix(elem.getModel(), namespace)));
	
					MessageSignature ms=interaction.getMessageSignature();
					if (ms.getOperation() != null) {
						onm.setOperation(ms.getOperation());
					}
					
					act.getOnMessage().add(onm);
				}
			}
			
		} else {
			TIf act=new TIf();
			
			if (context.getParent() instanceof TSequence) {
				((TSequence)context.getParent()).getActivity().add(act);
			}
			
			for (int i=0; i < paths.size(); i++) {
				Block path=paths.get(i);
								
				TSequence seq=new TSequence();
				
				/*
				Interaction interaction=InteractionPatterns.getFirstInteraction(path);
				
				if (interaction != null && interaction.getMessageSignature().getTypeReferences().size() > 0) {
					Contract contract=null;
					
					if ((InteractionUtil.isRequest(interaction) && !InteractionUtil.isSend(interaction)) ||
							(InteractionUtil.isResponse(interaction) && InteractionUtil.isSend(interaction))) {
						contract = ModelChangeUtils.getContract(context, role);
					} else if (InteractionUtil.isRequest(interaction)) {
						contract = ModelChangeUtils.getContract(context, interaction.getToRoles().get(0));
					} else {
						contract = ModelChangeUtils.getContract(context, interaction.getFromRole());
					}
					
					// Handle when message signature
					if (InteractionUtil.isResponse(interaction)) {
						QName qname=null;
						if (InteractionUtil.isRequest(interaction)) {
							qname = WSDLGeneratorUtil.getRequestMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						} else if (InteractionUtil.isFaultResponse(interaction)) {
							qname = WSDLGeneratorUtil.getFaultMessageType(contract.getNamespace(),
									InteractionUtil.getFaultName(interaction),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						} else {
							qname = WSDLGeneratorUtil.getResponseMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						}
						
						String varName = qname.getLocalPart()+"Var";
	
						TVariable var=VariableUtil.getVariable(bpelModel, varName);
	
						if (var == null) {
							var = new TVariable();
							var.setName(varName);
							
							var.setMessageType(qname);
							
							bpelModel.getVariables().getVariable().add(var);
						}
	
						TReply reply = new TReply();
						seq.getActivity().add(reply);
						
						// TODO: What about if multiple 'to' roles
						TPartnerLink pl=new TPartnerLink();
	
						pl.setMyRole(role.getName()+"Service");
						pl.setName(interaction.getToRoles().get(0).getName()+"To"+role.getName());
						
						String plt=interaction.getToRoles().get(0).getName()+"To"+role.getName()+"Service"+"LT";
						
						pl.setPartnerLinkType(new QName(contract.getNamespace(), plt,
								ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));
		
						//portType = role.getName()+"PT";
						
						if (InteractionUtil.isFaultResponse(interaction)) {
							String faultName=InteractionUtil.getFaultName(interaction);
							
							// TODO: Not sure what to do about namespace here?
							reply.setFaultName(new QName(contract.getNamespace(), faultName,
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));
						}
						
						if (varName != null) {
							reply.setVariable(varName);
						}
	
						// Create partner link
						TPartnerLink other=
								PartnerLinkUtil.getPartnerLink(bpelModel, pl.getName());
						if (other == null) {
							bpelModel.getPartnerLinks().getPartnerLink().add(pl);
						} else {
							if (other.getPartnerRole() == null &&
									pl.getPartnerRole() != null) {
								other.setPartnerRole(pl.getPartnerRole());
							}
							if (other.getMyRole() == null &&
									pl.getMyRole() != null) {
								other.setMyRole(pl.getMyRole());
							}
						}
	
						String portType=null;
						
						if (contract.getInterfaces().size() > 0) {
							portType = contract.getInterfaces().iterator().next().getName();
						}
	
						reply.setPartnerLink(pl.getName());			
						reply.setPortType(new QName(contract.getNamespace(),portType,
								ProtocolUtils.getNamespacePrefix(elem.getModel(),contract.getNamespace())));	
						if (interaction.getMessageSignature() != null) {
							reply.setOperation(interaction.getMessageSignature().getOperation());
						}
	
						reply.setName(InteractionUtil.getName(interaction));
					} else {
						QName qname=null;
						if (InteractionUtil.isRequest(interaction)) {
							qname = WSDLGeneratorUtil.getRequestMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						} else if (InteractionUtil.isFaultResponse(interaction)) {
							qname = WSDLGeneratorUtil.getFaultMessageType(contract.getNamespace(),
									InteractionUtil.getFaultName(interaction),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						} else {
							qname = WSDLGeneratorUtil.getResponseMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace()));
						}
						
						String varName = qname.getLocalPart()+"Var";
	
						TVariable var=VariableUtil.getVariable(bpelModel, varName);
	
						if (var == null) {
							var = new TVariable();
							var.setName(varName);
							
							var.setMessageType(qname);
							
							bpelModel.getVariables().getVariable().add(var);
						}
	
						TInvoke invoke=new TInvoke();
						seq.getActivity().add(invoke);
											
						if (varName != null) {
							invoke.setInputVariable(varName);
						}
						
						// TODO: What about if multiple 'to' roles
						TPartnerLink pl=new TPartnerLink();
	
						if (role != null && interaction.getToRoles().size() > 0) {
							pl.setPartnerRole(interaction.getToRoles().get(0).getName()+"Requester");
							pl.setName(role.getName()+"To"+interaction.getToRoles().get(0).getName());
							
							String plt=role.getName()+"To"+interaction.getToRoles().get(0).getName()+"LT";
							
							pl.setPartnerLinkType(new QName(contract.getNamespace(), plt,
									ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));
	
							// Create partner link
							TPartnerLink other=
									PartnerLinkUtil.getPartnerLink(bpelModel, pl.getName());
							if (other == null) {
								bpelModel.getPartnerLinks().getPartnerLink().add(pl);
							} else {
								if (other.getPartnerRole() == null &&
										pl.getPartnerRole() != null) {
									other.setPartnerRole(pl.getPartnerRole());
								}
								if (other.getMyRole() == null &&
										pl.getMyRole() != null) {
									other.setMyRole(pl.getMyRole());
								}
							}
						}
						
						String portType=null;
						
						if (contract.getInterfaces().size() > 0) {
							portType = contract.getInterfaces().iterator().next().getName();
						}
	
						invoke.setPartnerLink(pl.getName());			
						invoke.setPortType(new QName(contract.getNamespace(),portType,
								ProtocolUtils.getNamespacePrefix(elem.getModel(), contract.getNamespace())));	
						if (interaction.getMessageSignature() != null) {
							invoke.setOperation(interaction.getMessageSignature().getOperation());
						}
	
						invoke.setName(InteractionUtil.getName(interaction));
					}
				}
				*/
				
				// Process the activities within the conversation
				java.util.List<Activity> acts=path.getContents();
							
				context.pushScope();
				
				context.setParent(seq);
				
				for (int j=0; j < acts.size(); j++) {
					context.insert(model, acts.get(j), null);
				}
				
				context.popScope();
	
				if (i == 0) {
					act.setSequence(seq);
				} else if (i == elem.getPaths().size()-1) {
					TActivityContainer construct=new TActivityContainer();
					construct.setSequence(seq);
					
					act.setElse(construct);
				} else {
					TElseif construct=new TElseif();
					
					construct.setSequence(seq);
					
					act.getElseif().add(construct);
				}
			}
		}
				
		return(true);
	}
	
	/**
	 * This method creates a variable, if one does not already exist for the
	 * supplied name, with the message type associated with the supplied
	 * interaction.
	 * 
	 * @param varName The variable name
	 * @param interaction The interaction associated with the variable
	 * @param bpelModel The BPEL model
	 */
	protected void createVariable(ModelChangeContext context, String varName,
						Interaction interaction, TProcess bpelModel) {
		TVariable var=VariableUtil.getVariable(bpelModel, varName);

		if (var == null) {
			Role role=null;
			
			if (interaction.getEnclosingProtocol() != null) {
				role = interaction.getEnclosingProtocol().getLocatedRole();
			}

			var = new TVariable();
			var.setName(varName);
			
			Role roleType=null;
			Contract contract=null;
			
			if (InteractionUtil.isRequest(interaction)) {
				// TODO: How to deal with multiple to roles
				if (interaction.getToRoles().size() > 0) {
					roleType = interaction.getToRoles().get(0);
				}
				if (roleType == null) {
					roleType = role;
				}
			} else {
				roleType = interaction.getFromRole();
				if (roleType == null) {
					roleType = role;
				}
			}
			
			if (roleType != null) {
				contract = ModelChangeUtils.getContract(context, roleType);
			}

			QName qname=null;
			if (InteractionUtil.isRequest(interaction)) {
				qname = WSDLGeneratorUtil.getRequestMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(interaction.getModel(), contract.getNamespace()));
			} else {
				qname = WSDLGeneratorUtil.getResponseMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(interaction.getModel(), contract.getNamespace()));
			}

			/*
			String mesgType=qname.getLocalPart();
			
			// Find namespace prefix
			if (qname.getNamespaceURI() != null) {
				String pfix=bpelModel.getBPELProcess().addNamespace(qname.getNamespaceURI());
				
				if (pfix != null) {
					mesgType = pfix+":"+mesgType;
				}
			}
			*/
			
			var.setMessageType(qname);
			
			bpelModel.getVariables().getVariable().add(var);
		}
	}
}
