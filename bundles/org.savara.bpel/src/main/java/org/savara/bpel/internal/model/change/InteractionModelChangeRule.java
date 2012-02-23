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
package org.savara.bpel.internal.model.change;

import javax.xml.namespace.QName;

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TBoolean;
import org.savara.bpel.model.TFaultHandlers;
import org.savara.bpel.model.TInvoke;
import org.savara.bpel.model.TPartnerLink;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TReceive;
import org.savara.bpel.model.TReply;
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
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.contract.model.Contract;
import org.savara.contract.model.Interface;
import org.savara.wsdl.util.WSDLGeneratorUtil;
import org.scribble.protocol.model.*;

/**
 * This is the model change rule for the Conversation Interaction.
 */
public class InteractionModelChangeRule extends AbstractBPELModelChangeRule {

	/**
	 * This method determines whether the rule is appropriate
	 * for the supplied type of model, parent (in the context) and inserted
	 * model object.
	 *
	 * @param context The context
	 * @param model The model
	 * @param mobj The model object being inserted
	 * @param ref The optional reference model object
	 * @return Whether the rule supports the supplied information
	 */
	@Override
	public boolean isInsertSupported(ModelChangeContext context,
					ProtocolModel model, ModelObject mobj,
					ModelObject ref) {
		boolean ret=false;
		
		if (mobj instanceof Interaction &&
				super.isInsertSupported(context, model, mobj, ref)) {
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
		Interaction interaction=(Interaction)mobj;
		boolean ret=false;
		TActivity act=null;
		TPartnerLink pl=new TPartnerLink();
		Role role=null;
		String portType=null;	
		String varName=null; //InteractionPatterns.getVariableName(interaction);
		Interface intf=null;
		String roleNamespace=null;
			
		if (interaction.getEnclosingProtocol() != null) {
			role = interaction.getEnclosingProtocol().getLocatedRole();
		}
		
		// Identify port type role
		//String prefix=null;
		
		//String mainPrefix=null;
		
		//Contract contract=ModelChangeUtils.getContract(context,
		//				interaction.enclosingProtocol().getRole());
		
		Contract contract=null;
		
		if ((InteractionUtil.isRequest(interaction) && !InteractionUtil.isSend(interaction)) ||
				(InteractionUtil.isResponse(interaction) && InteractionUtil.isSend(interaction))) {
			contract = ModelChangeUtils.getContract(context, role);
		} else if (InteractionUtil.isRequest(interaction)) {
			contract = ModelChangeUtils.getContract(context, interaction.getToRoles().get(0));
		} else {
			contract = ModelChangeUtils.getContract(context, interaction.getFromRole());
		}
		
		/* TODO: Namespace issue
		if (contract != null) {
			mainPrefix = bpelModel.getBPELProcess().addNamespace(contract.getNamespace());
		}
		*/
		
		Contract roleContract=ModelChangeUtils.getContract(context, role);
		if (roleContract != null) {
			roleNamespace = roleContract.getNamespace();
		}
		
		Role roleType=null;
		
		if (InteractionUtil.isRequest(interaction)) {
			// TODO: What about if multiple 'to' roles
			if (interaction.getToRoles().size() >= 1) {
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
			//contract = ModelChangeUtils.getContract(context, roleType);

			if (contract != null) {
				Annotation annotation=null;
				if ((annotation=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(),
									AnnotationDefinitions.INTERFACE)) != null) {
					String intfName=(String)annotation.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
					String intfNamespace=(String)annotation.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY);
					
					intf = contract.getInterface(intfNamespace, intfName);
					
					if (intf == null) {
						javax.xml.namespace.QName qname=javax.xml.namespace.QName.valueOf(intfName);
						
						if (intfNamespace == null) {
							// Check if name field used to encode qname
							intf = contract.getInterface(qname.getNamespaceURI(), qname.getLocalPart());
						}
						
						if (intf == null) {
							// Try localpart
							intf = contract.getInterface(null, qname.getLocalPart());
						}
					}
				} else if (contract.getInterfaces().size() > 0) {
					intf = contract.getInterfaces().iterator().next();
				}
			}
		}
		
		if (intf != null) {
			portType = intf.getName();
			
			/* TODO: Namespace issue
			prefix = bpelModel.getBPELProcess().addNamespace(intf.getNamespace());
			
			if (prefix != null) {
				portType = prefix+":"+portType;
			}
			*/
		}

		// Create variable
		//if (varName != null) {
			QName qname=null;
			if (InteractionUtil.isRequest(interaction)) {
				qname = WSDLGeneratorUtil.getRequestMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(interaction.getModel(), contract.getNamespace()));
			} else if (InteractionUtil.isFaultResponse(interaction)) {
				qname = WSDLGeneratorUtil.getFaultMessageType(contract.getNamespace(),
						InteractionUtil.getFaultName(interaction),
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), contract.getNamespace()));
			} else {
				qname = WSDLGeneratorUtil.getResponseMessageType(contract.getNamespace(),
									interaction.getMessageSignature().getOperation(),
									ProtocolUtils.getNamespacePrefix(interaction.getModel(), contract.getNamespace()));
			}
			
			varName = qname.getLocalPart()+"Var";

		//}

		// Check if send or receive
		if (InteractionUtil.isSend(interaction)) {
			
			// Make sure variable exists
			getVariable(bpelModel, varName, qname);
			
			// TODO: Record variables against relevant interaction
			// based activity - probably only mechanism for
			// establishing message type

			if (InteractionUtil.isRequest(interaction)) {
				
				if (InteractionPatterns.isFaultHandlerRequired(interaction)) {
					
					TScope scope=new TScope();
					
					if (context.getParent() instanceof TSequence) {
						((TSequence)context.getParent()).getActivity().add(scope);
					}
										
					context.getProperties().put(BPELDefinitions.BPEL_FAULT_SCOPE_PROPERTY, scope);
					context.getProperties().put(BPELDefinitions.BPEL_FAULT_SCOPE_PARENT_PROPERTY,
												context.getParent());
					
					TFaultHandlers fhs=new TFaultHandlers();
					
					TSequence seq=new TSequence();
					
					scope.setSequence(seq);
					scope.setFaultHandlers(fhs);
					
					context.setParent(seq);
				}
				
				act = new TInvoke();
				
				// TODO: What about if multiple 'to' roles
				pl.setPartnerRole(interaction.getToRoles().get(0).getName());
				pl.setName(role.getName()+"To"+interaction.getToRoles().get(0).getName());
				
				String plt=role.getName()+"To"+interaction.getToRoles().get(0).getName()+"LT";
				
				pl.setPartnerLinkType(new QName(roleNamespace, plt,
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), roleNamespace)));

				//portType = interaction.getToRole().getName()+"PT";
				
				if (varName != null) {
					((TInvoke)act).setInputVariable(varName);
				}
				
			} else {
				act = new TReply();
		
				// TODO: What about if multiple 'to' roles
				pl.setMyRole(role.getName());
				pl.setName(interaction.getToRoles().get(0).getName()+"To"+role.getName());
				
				String plt=interaction.getToRoles().get(0)+"To"+role.getName()+"Service"+"LT";
				
				pl.setPartnerLinkType(new QName(roleNamespace, plt,
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), roleNamespace)));

				//portType = role.getName()+"PT";
				
				if (InteractionUtil.isFaultResponse(interaction)) {
					String faultName=InteractionUtil.getFaultName(interaction);
					
					// Find namespace prefix
					//if (intf != null) {
					//	String prefix=bpelModel.getBPELProcess().addNamespace(intf.getNamespace());
						
					/*
						if (prefix != null) {
							faultName = prefix+":"+faultName;
						}
						*/
					//}
					
					// TODO: Not sure what to do about namespace here?
					((TReply)act).setFaultName(new QName(intf.getNamespace(), faultName,
							ProtocolUtils.getNamespacePrefix(interaction.getModel(), intf.getNamespace())));
				}
				
				if (varName != null) {
					((TReply)act).setVariable(varName);
				}
			}
		} else if (InteractionPatterns.isResponseInFaultHandler(interaction) == false) {
			
			// Make sure variable exists
			getVariable(bpelModel, varName, qname);
			
			if (InteractionPatterns.isSyncNormalResponse(interaction)) {
				
				//System.out.println("ADD RESP VAR TO PRECEDING");
				if (context.getParent() instanceof TSequence) {
					java.util.List<Object> acts=
							((TSequence)context.getParent()).getActivity();
					
					if (acts.size() > 0 &&
							acts.get(acts.size()-1) instanceof TInvoke) {
						TInvoke invoke=(TInvoke)acts.get(acts.size()-1);
						
						invoke.setOutputVariable(varName);
					}
				}
				
			} else if (InteractionPatterns.isInteractionPickPathTrigger(interaction) == false) {
				act = new TReceive();
				
				String intfName=null;
				Annotation intfAnn=AnnotationDefinitions.getAnnotation(interaction.getAnnotations(), AnnotationDefinitions.INTERFACE);
				
				if (intfAnn != null) {
					intfName = (String)intfAnn.getProperties().get(AnnotationDefinitions.NAME_PROPERTY);
				}
				
				if (intfName != null && intfName.trim().length() > 0) {
					intfName = javax.xml.namespace.QName.valueOf(intfName).getLocalPart();
				} else {
					intfName = role.getName();
				}
	
				if (InteractionUtil.isRequest(interaction)) {
		
					pl.setMyRole(role.getName());
					pl.setName(interaction.getFromRole().getName()+"To"+role.getName());
					
					String plt=interaction.getFromRole().getName()+"To"+role.getName()+"Service"+"LT";

					pl.setPartnerLinkType(new QName(roleNamespace, plt,
							ProtocolUtils.getNamespacePrefix(interaction.getModel(), roleNamespace)));
	
					//portType = role.getName()+"PT";
				} else {
					pl.setMyRole(role.getName());
					pl.setPartnerRole(interaction.getFromRole().getName());
					pl.setName(role.getName()+"To"+interaction.getFromRole().getName());
					
					String plt=role.getName()+"To"+interaction.getFromRole().getName()+"Requester"+"LT";
					
					pl.setPartnerLinkType(new QName(roleNamespace, plt,
							ProtocolUtils.getNamespacePrefix(interaction.getModel(), roleNamespace)));
	
					//portType = role.getName()+
					//		interaction.getFromRole().getName()+"CallbackPT";				
				}

				if (varName != null) {
					((TReceive)act).setVariable(varName);
				}
				
				// Check if create instance
				if (org.scribble.protocol.util.InteractionUtil.isInitialInteraction(interaction.getModel(), interaction)) {
					((TReceive)act).setCreateInstance(TBoolean.YES);
				}
			}
		} else if (!InteractionUtil.isFaultResponse(interaction)) {
			
			// Make sure variable exists
			getVariable(bpelModel, varName, qname);
			
			// Retrieve the fault scope and parent
			TScope scope=(TScope)context.getProperties().get(BPELDefinitions.BPEL_FAULT_SCOPE_PROPERTY);
			TSequence parent=(TSequence)context.getProperties().get(BPELDefinitions.BPEL_FAULT_SCOPE_PARENT_PROPERTY);
			
			int pos=parent.getActivity().indexOf(scope);
			
			// Move scope to current location, and place the second+ activities in the scope
			// at the location where the scope was originally - having the effort of
			// relocating the scope/fault handlers/invoke to the location of the normal
			// response
			parent.getActivity().remove(scope);
			((TSequence)context.getParent()).getActivity().add(scope);
			
			TSequence scopedSeq=scope.getSequence();
			
			while (scopedSeq.getActivity().size() > 1) {
				TActivity scopedAct=(TActivity)scopedSeq.getActivity().get(1);
				scopedSeq.getActivity().remove(1);
				
				parent.getActivity().add(pos++, scopedAct);
			}
			
			context.setParent(scopedSeq);
		}
		
		// TODO: Possibly if the channel is set, then
		// might be a callback channel, so could use
		// channel name to reflect callback port

		// TODO: If both myRole and partnerRole are specified
		// then one is a callback porttype
		
		if (act != null) {
			
			// Set the name
			act.setName(InteractionUtil.getName(interaction));
			
			if (context.getParent() instanceof TSequence) {
				((TSequence)context.getParent()).getActivity().add(act);
				
				ret = true;
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
			
			// Set details on interaction
			if (act instanceof TInvoke) {
				((TInvoke)act).setPartnerLink(pl.getName());			
				((TInvoke)act).setPortType(new QName(intf.getNamespace(),portType,
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), intf.getNamespace())));	
				if (interaction.getMessageSignature() != null) {
					((TInvoke)act).setOperation(interaction.getMessageSignature().getOperation());
					
					BPELInteractionUtil.registerInvoke((TInvoke)act, context.getProperties());
				}
			} else if (act instanceof TReceive) {
				((TReceive)act).setPartnerLink(pl.getName());			
				((TReceive)act).setPortType(new QName(intf.getNamespace(),portType,
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), intf.getNamespace())));	
				if (interaction.getMessageSignature() != null) {
					((TReceive)act).setOperation(interaction.getMessageSignature().getOperation());
				}
			} else if (act instanceof TReply) {
				((TReply)act).setPartnerLink(pl.getName());			
				((TReply)act).setPortType(new QName(intf.getNamespace(),portType,
						ProtocolUtils.getNamespacePrefix(interaction.getModel(), intf.getNamespace())));	
				if (interaction.getMessageSignature() != null) {
					((TReply)act).setOperation(interaction.getMessageSignature().getOperation());
				}
			}
		}
		
		return(ret);
	}

	/**
	 * This method determines whether the rule is appropriate
	 * for the supplied type of model, parent (in the context)
	 * and modified model object.
	 *
	 * @param context The context
	 * @param model The model
	 * @param fromObj The source model object
	 * @param toObj The model object to be updated
	 * @return Whether the rule supports the supplied information
	 */
	@Override
	public boolean isUpdateSupported(ModelChangeContext context,
					ProtocolModel model, ModelObject fromObj, ModelObject toObj) {
		boolean ret=false;
		
		if (fromObj instanceof Interaction &&
				toObj instanceof Interaction && isBPELModel(model)) {
			ret = true;
		}
		
		return(ret);
	}
	
	/**
	 * This method modifies an existing model object, within a
	 * parent model object, with the details supplied in
	 * another model object.
	 * 
	 * @param context The context
	 * @param model The model being changed
	 * @param fromObj The source model object
	 * @param toObj The model object to be updated
	 * @return Whether the change has been applied
	 */
	@Override
	public boolean update(ModelChangeContext context,
				ProtocolModel model, ModelObject fromObj, ModelObject toObj) {
		boolean ret=false;
		
		return(ret);
	}
		
	/**
	 * This method determines whether the rule is appropriate
	 * for the supplied type of model, parent (in the context)
	 * and deleted model object.
	 * 
	 * @param context The context
	 * @param model The model
	 * @param mobj The model object to be removed
	 * @return Whether the rule supports the supplied information
	 */
	@Override
	public boolean isDeleteSupported(ModelChangeContext context,
						ProtocolModel model, ModelObject mobj) {
		boolean ret=false;
		
		if (mobj instanceof Interaction &&
				super.isDeleteSupported(context, model, mobj)) {
			ret = true;
		}
		
		return(ret);
	}
	
	protected TVariable getVariable(TProcess bpelModel, String varName, QName qname) {
		TVariable var=VariableUtil.getVariable(bpelModel, varName);

		if (var == null) {
			var = new TVariable();
			var.setName(varName);
			
			//String mesgType=qname.getLocalPart();
			
			// Find namespace prefix
			/* TODO: Namespace issue
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
	
		return(var);
	}
}
