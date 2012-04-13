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

import java.util.logging.Logger;

import org.pi4soa.cdl.*;
import org.pi4soa.cdl.Interaction;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;
import org.scribble.protocol.model.Choice;
import org.pi4soa.cdl.util.*;
import org.pi4soa.common.util.NamesUtil;
import org.pi4soa.common.xml.NameSpaceUtil;
import org.pi4soa.common.xml.XMLUtils;

public class InteractionParserRule implements ParserRule {

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
		return(scribbleType == org.scribble.protocol.model.Activity.class &&
				cdlType instanceof org.pi4soa.cdl.Interaction);
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
		org.scribble.protocol.model.Block ret=new org.scribble.protocol.model.Block();
		org.pi4soa.cdl.Interaction cdl=
				(org.pi4soa.cdl.Interaction)cdlType;
		//Role fromRole=getFromRole(context, cdl);
		//Role toRole=getToRole(context, cdl);
		
		// Determine if interaction may result in
		// a timeout
		Block block=ret;
		
		/*
			TODO: Consider how to deal with 'time to complete'
			
		if (NamesUtil.isSet(cdl.getTimeToComplete())) {
			Try te=new Try();
			
			ret.getContents().add(te);
			
			block = te.getBlock();
			
			InterruptBlock interrupt=new InterruptBlock();
			
			te.getEscapeBlocks().add(interrupt);
			
			// Set expression from 'time to complete'
			// using xpath
			XPathExpression exp=new XPathExpression();
			
			exp.setQuery("cdl:hasDurationPassed('"+
					cdl.getTimeToComplete()+"')");
			
			interrupt.setExpression(exp);
			
			// Perform assignments associated with timeout
			for (int i=0; i < cdl.getRecordDetails().size(); i++) {
				org.pi4soa.cdl.RecordDetails rd=
							cdl.getRecordDetails().get(i);
				
				if (rd.getWhen() == WhenType.TIMEOUT) {
					
					// TODO: Record variable - but which role??? May need to
					// be part of the following conditions, so depends on
					// whether the record details are in the from/to timeout record details
				
					if (cdl.getTimeoutFromRoleTypeRecordDetails().contains(rd)) {
						
						if (rd.getCauseException() != null &&
								rd.getCauseException().trim().length() > 0) {
							Raise raise=new Raise();
							raise.getRoles().add(new Role(fromRole.getName()));
							
							TypeReference tref=new TypeReference();
							tref.setLocalpart(XMLUtils.getLocalname(rd.getCauseException()));
							raise.setType(tref);
							
							interrupt.getContents().add(raise);
						}
					}
					
					if (cdl.getTimeoutToRoleTypeRecordDetails().contains(rd)) {
						
						if (rd.getCauseException() != null &&
								rd.getCauseException().trim().length() > 0) {
							Raise raise=new Raise();
							raise.getRoles().add(new Role(toRole.getName()));
							
							TypeReference tref=new TypeReference();
							tref.setLocalpart(XMLUtils.getLocalname(rd.getCauseException()));
							raise.setType(tref);
							
							interrupt.getContents().add(raise);
						}
					}
				}
			}
		}
		*/
		
		java.util.Iterator<ExchangeDetails> iter=
					cdl.getExchangeDetails().iterator();
		java.util.List<Block> cbs=
					new java.util.Vector<Block>();
				
		while (iter.hasNext()) {
			ExchangeDetails details=iter.next();
			
			/*
			if (context.shouldIgnore(details)) {
				continue;
			}
			*/
			
			Block cb=new Block();
			
			// Convert 'before' send and receive record details
			/*
			for (int i=0; i < details.getSendRecordDetails().size(); i++) {
				RecordDetails rd=details.getSendRecordDetails().get(i);
				
				if (rd.getWhen() == WhenType.BEFORE) {
					String fromRoleName=(details.getAction() == ExchangeActionType.REQUEST?
							fromRole.getName():toRole.getName());
					*/
					// Define record expression
					/*
					Assignment assign=new Assignment();
					
					assign.getSource().setComponentURI(
							CDLTypeUtil.getURIFragment(rd));
					
					// Set role
					Role role=new Role(fromRoleName);
					
					assign.getRoles().add(role);
					
					// Set variable
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(rd.getTargetVariable().getName());
					
					assign.setVariable(var);
					
					org.scribble.expression.xpath.model.XPathExpression expr=
						new org.scribble.expression.xpath.model.XPathExpression();
					
					if (rd.getSourceExpression() != null) {
						expr.setQuery(rd.getSourceExpression());
					} else if (rd.getSourceVariable() != null) {
						expr.setQuery("cdl:getVariable('"+rd.getSourceVariable().getName()+"','','')");
					}
					
					assign.setExpression(expr);
					
					cb.getContents().add(assign);
					*/
					
			/*
					// Check for cause exception
					if (rd.getCauseException() != null &&
							rd.getCauseException().trim().length() > 0) {
						
						Raise raise=new Raise();
						raise.getRoles().add(new Role(fromRoleName));
						
						TypeReference tref=new TypeReference();
						tref.setName(XMLUtils.getLocalname(rd.getCauseException()));
						raise.setType(tref);
						
						cb.getBlock().add(raise);
					}
				}
			}
			
			for (int i=0; i < details.getReceiveRecordDetails().size(); i++) {
				RecordDetails rd=details.getReceiveRecordDetails().get(i);
				
				if (rd.getWhen() == WhenType.BEFORE) {
					String toRoleName=(details.getAction() == ExchangeActionType.REQUEST?
							toRole.getName():fromRole.getName());
			*/		
					// Define record expression
					/*
					Assignment assign=new Assignment();
					
					assign.getSource().setComponentURI(
							CDLTypeUtil.getURIFragment(rd));
					
					// Set role
					Role role=new Role(toRoleName);
					
					assign.getRoles().add(role);
					
					// Set variable
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(rd.getTargetVariable().getName());
					
					assign.setVariable(var);
					
					org.scribble.expression.xpath.model.XPathExpression expr=
						new org.scribble.expression.xpath.model.XPathExpression();
					
					if (rd.getSourceExpression() != null) {
						expr.setQuery(rd.getSourceExpression());
					} else if (rd.getSourceVariable() != null) {
						expr.setQuery("cdl:getVariable('"+rd.getSourceVariable().getName()+"','','')");
					}
					
					assign.setExpression(expr);
					
					cb.getContents().add(assign);
					*/
			/*		
					// Check for cause exception
					if (rd.getCauseException() != null &&
							rd.getCauseException().trim().length() > 0) {
						
						Raise raise=new Raise();
						raise.getRoles().add(new Role(toRoleName));
						
						TypeReference tref=new TypeReference();
						tref.setName(XMLUtils.getLocalname(rd.getCauseException()));
						raise.setType(tref);
						
						cb.getBlock().add(raise);
					}
				}
			}
			*/

			// Convert the interaction
			org.scribble.protocol.model.Interaction interaction=
					new org.scribble.protocol.model.Interaction();
			
			//interaction.getSource().setComponentURI(
			
			Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

			Object uri=CDLTypeUtil.getURIFragment(details);
			
			scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
						uri);
			interaction.getAnnotations().add(scannotation);
			
			interaction.getProperties().put(ModelProperties.URI, uri);

			
			// Check if interaction's channel has been explicitly defined
			/*
			Object ch=context.getState(cdl.getChannelVariable().getName());
			
			if (ch instanceof Channel) {
				interaction.setChannel((Channel)ch);
			}
			*/
			
			// Define message signature
			MessageSignature ms=createMessageSignature(details, interaction);
			
			/*
			MessageSignature ms=new MessageSignature();
			ms.setOperation(cdl.getOperation());
			
			if (NamesUtil.isSet(details.getFaultName())) {
				ms.getProperties().put(FAULT_NAME,
						details.getFaultName());
			}
			
			// Define interaction message type
			if (details.getType() instanceof InformationType) {
				InformationType itype=(InformationType)details.getType();
				
				TypeReference ref=ConverterUtil.getTypeReference(itype);
				
				ms.getTypeReferences().add(ref);
				
				if (NamesUtil.isSet(itype.getTypeName())) {
					String ns=CDLTypeUtil.getNamespace(itype.getTypeName(),
									cdl);
					String lp=XMLUtils.getLocalname(itype.getTypeName());
					
					if (lp != null) {
						ms.getProperties().put(MESSAGE_TYPE_LOCALPART,
										lp);
					}
					if (ns != null) {
						ms.getProperties().put(MESSAGE_TYPE_NAMESPACE,
										ns);
					}
				} else if (NamesUtil.isSet(itype.getElementName())) {
					String ns=CDLTypeUtil.getNamespace(itype.getElementName(),
							cdl);
					String lp=XMLUtils.getLocalname(itype.getElementName());
			
					if (lp != null) {
						ms.getProperties().put(MESSAGE_TYPE_LOCALPART,
										lp);
					}
					if (ns != null) {
						ms.getProperties().put(MESSAGE_TYPE_NAMESPACE,
										ns);
					}
				}
			}
			*/
			
			interaction.setMessageSignature(ms);
						
			cb.add(interaction);

			// Set interface name
			if (cdl.getToRoleType() != null) {
				// TODO: Deal with interfaces that have multiple behaviours	
				Annotation annotation=new Annotation(AnnotationDefinitions.INTERFACE);
				
				String qname=getInterfaceName(cdl);
				
				annotation.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
						NameSpaceUtil.getNamespace(qname));
				annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
						NameSpaceUtil.getLocalPart(qname));
				
				interaction.getAnnotations().add(annotation);
			}
			
			interaction.setFromRole(new Role(getFromRole(context, details)));
			interaction.getToRoles().add(new Role(getToRole(context, details)));

			if (details.getAction() == ExchangeActionType.REQUEST) {
				
				if (details.getSendVariable() != null) {
					//org.scribble.conversation.model.Variable var=
					//		new org.scribble.conversation.model.Variable();
					//var.setName(details.getSendVariable().getName());
					interaction.getProperties().put("SendVariable",
									details.getSendVariable().getName());
				}
				
				if (details.getReceiveVariable() != null) {
					/*
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(details.getReceiveVariable().getName());
					interaction.setToVariable(var);
					*/
					interaction.getProperties().put("ReceiveVariable",
							details.getReceiveVariable().getName());
				}
				
				// Check if request has response/fault exchanges
				java.util.List<ExchangeDetails> resps=
						InteractionUtil.getResponseExchangeDetails(details);
				
				if (resps != null && resps.size() > 0) {
					Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
					annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY,
								CDMProtocolParserUtil.getLabel(details));
					interaction.getAnnotations().add(annotation);
				}
				
				// Check if fault thrown
				/* TODO: Determine what to do about cause exceptions on interactions
				 * These could be used to define catch blocks?
				 *
				if (details.getSendCauseException() != null &&
						details.getSendCauseException().trim().length() > 0) {
					Raise raise=new Raise();
					raise.getRoles().add(new Role(fromRole.getName()));
					
					TypeReference tref=new TypeReference();
					tref.setName(XMLUtils.getLocalname(details.getSendCauseException()));
					raise.setType(tref);
					
					cb.getBlock().add(raise);
				}
				
				if (details.getReceiveCauseException() != null &&
						details.getReceiveCauseException().trim().length() > 0) {
					Raise raise=new Raise();
					raise.getRoles().add(new Role(toRole.getName()));
					
					TypeReference tref=new TypeReference();
					tref.setName(XMLUtils.getLocalname(details.getReceiveCauseException()));
					raise.setType(tref);
					
					cb.getBlock().add(raise);
				}
				*/
			} else {
				//interaction.setFromRole(toRole);
				//interaction.getToRoles().add(fromRole);
				
				if (details.getReceiveVariable() != null) {
					/*
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(details.getReceiveVariable().getName());
					interaction.setFromVariable(var);
					*/
					interaction.getProperties().put("SendVariable", details.getReceiveVariable().getName());
				}
				
				if (details.getSendVariable() != null) {
					/*
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(details.getSendVariable().getName());
					interaction.setToVariable(var);
					*/
					interaction.getProperties().put("ReceiveVariable", details.getReceiveVariable().getName());
				}
				
				if (details.getAction() == ExchangeActionType.RESPOND) {
					Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
					annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY,
								CDMProtocolParserUtil.getLabel(details));
					interaction.getAnnotations().add(annotation);
				}

				// Check if fault thrown
				/* TODO: Determine what to do about cause exceptions on interactions
				 * These could be used to define catch blocks?
				 *
				if (details.getSendCauseException() != null &&
						details.getSendCauseException().trim().length() > 0) {
					Raise raise=new Raise();
					raise.getRoles().add(new Role(toRole.getName()));
					
					TypeReference tref=new TypeReference();
					tref.setName(XMLUtils.getLocalname(details.getSendCauseException()));
					raise.setType(tref);
					
					cb.getBlock().add(raise);
				}
				
				if (details.getReceiveCauseException() != null &&
						details.getReceiveCauseException().trim().length() > 0) {
					Raise raise=new Raise();
					raise.getRoles().add(new Role(fromRole.getName()));
					
					TypeReference tref=new TypeReference();
					tref.setName(XMLUtils.getLocalname(details.getReceiveCauseException()));
					raise.setType(tref);
					
					cb.getBlock().add(raise);
				}
				*/
			}
			
			// Convert 'after' send and receive record details
			/*
			for (int i=0; i < details.getSendRecordDetails().size(); i++) {
				RecordDetails rd=details.getSendRecordDetails().get(i);
				
				if (rd.getWhen() == WhenType.AFTER) {
					String fromRoleName=(details.getAction() == ExchangeActionType.REQUEST?
							fromRole.getName():toRole.getName());
			*/		
					/*
					// Define record expression
					Assignment assign=new Assignment();
					
					assign.getSource().setComponentURI(
							CDLTypeUtil.getURIFragment(rd));
					
					// Set role
					Role role=new Role(fromRoleName);
					
					assign.getRoles().add(role);
					
					// Set variable
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(rd.getTargetVariable().getName());
					
					assign.setVariable(var);
					
					org.scribble.expression.xpath.model.XPathExpression expr=
						new org.scribble.expression.xpath.model.XPathExpression();
					
					if (rd.getSourceExpression() != null) {
						expr.setQuery(rd.getSourceExpression());
					} else if (rd.getSourceVariable() != null) {
						expr.setQuery("cdl:getVariable('"+rd.getSourceVariable().getName()+"','','')");
					}
					
					assign.setExpression(expr);
					
					cb.getContents().add(assign);
					*/
			/*		
					// Check for cause exception
					if (rd.getCauseException() != null &&
							rd.getCauseException().trim().length() > 0) {
						
						Raise raise=new Raise();
						raise.getRoles().add(new Role(fromRoleName));
						
						TypeReference tref=new TypeReference();
						tref.setName(XMLUtils.getLocalname(rd.getCauseException()));
						raise.setType(tref);
						
						cb.getBlock().add(raise);
					}
				}
			}
			
			for (int i=0; i < details.getReceiveRecordDetails().size(); i++) {
				RecordDetails rd=details.getReceiveRecordDetails().get(i);
				
				if (rd.getWhen() == WhenType.AFTER) {
					String toRoleName=(details.getAction() == ExchangeActionType.REQUEST?
							toRole.getName():fromRole.getName());
					
					/*
					// Define record expression
					Assignment assign=new Assignment();
					
					assign.getSource().setComponentURI(
							CDLTypeUtil.getURIFragment(rd));
					
					// Set role
					Role role=new Role(toRoleName);
					
					assign.getRoles().add(role);
					
					// Set variable
					org.scribble.conversation.model.Variable var=
							new org.scribble.conversation.model.Variable();
					var.setName(rd.getTargetVariable().getName());
					
					assign.setVariable(var);
					
					// If target variable is a channel, then make explicit
					if (rd.getTargetVariable().getType() instanceof ChannelType) {
						Object state=context.getState(rd.getTargetVariable().getName());
						
						if (state == null) {
							Channel newch=new Channel();
							newch.setName(rd.getTargetVariable().getName());
							
							context.setState(newch.getName(), newch);
							
							//
						}
					}
					
					// Convert source expression
					org.scribble.expression.xpath.model.XPathExpression expr=
						new org.scribble.expression.xpath.model.XPathExpression();
					
					if (rd.getSourceExpression() != null) {
						expr.setQuery(rd.getSourceExpression());
					} else if (rd.getSourceVariable() != null) {
						expr.setQuery("cdl:getVariable('"+rd.getSourceVariable().getName()+"','','')");
					}
					
					assign.setExpression(expr);
					
					cb.getContents().add(assign);
					*/
		/*			
					// Check for cause exception
					if (rd.getCauseException() != null &&
							rd.getCauseException().trim().length() > 0) {
						
						Raise raise=new Raise();
						raise.getRoles().add(new Role(toRoleName));
						
						TypeReference tref=new TypeReference();
						tref.setName(XMLUtils.getLocalname(rd.getCauseException()));
						raise.setType(tref);
						
						cb.getBlock().add(raise);
					}
				}
			}
			*/

			cbs.add(cb);
		}
		
		if (cbs.size() > 2) {
			block.getContents().addAll(cbs.remove(0).getContents());
			
			Choice choice=new Choice();
			
			//choice.getRoles().add(new Role(toRole.getName()));
			
			for (int i=0; i < cbs.size(); i++) {
				Block cb=cbs.get(i);
				
				choice.getPaths().add(cb);
			}
			
			block.getContents().add(choice);			
		} else {
			for (int i=0; i < cbs.size(); i++) {
				Block cb=cbs.get(i);
				block.getContents().addAll(cb.getContents());
			}
		}

		return(ret);
	}
	
	public static Role getFromRole(ParserContext context, org.pi4soa.cdl.ExchangeDetails ed) {
		Role fromRole=null;
		org.pi4soa.cdl.Interaction cdl=ed.getInteraction();
		
		if (ed.getAction() == ExchangeActionType.REQUEST) {
			if (cdl.getFromParticipant() != null) {
				fromRole = (Role)
					context.getState(XMLUtils.getLocalname(cdl.getFromParticipant().getName()));
				
				if (fromRole == null) {
					logger.severe("Failed to get 'from' role '"+XMLUtils.getLocalname(cdl.getFromParticipant().getName())+"'");
				}
			} else if (cdl.getFromRoleType() != null) {			
				ParticipantType ptype=
					PackageUtil.getParticipantForRoleType(cdl.getFromRoleType());
				
				fromRole = (Role)
					context.getState(XMLUtils.getLocalname(ptype.getName()));
				
				if (fromRole == null) {
					logger.severe("Failed to get 'from' role '"+XMLUtils.getLocalname(ptype.getName())+"'");
				}
			}
		} else {
			if (cdl.getToParticipant() != null) {
				fromRole = (Role)
					context.getState(XMLUtils.getLocalname(cdl.getToParticipant().getName()));
	
				if (fromRole == null) {
					logger.severe("Failed to get 'to' role '"+XMLUtils.getLocalname(cdl.getFromParticipant().getName())+"'");
				}
			} else if (cdl.getToRoleType() != null) {
				ParticipantType ptype=
					PackageUtil.getParticipantForRoleType(cdl.getToRoleType());
				
				fromRole = (Role)
					context.getState(XMLUtils.getLocalname(ptype.getName()));
				
				if (fromRole == null) {
					logger.severe("Failed to get 'to' role '"+XMLUtils.getLocalname(ptype.getName())+"'");
				}
			}
		}

		return(fromRole);
	}
	
	public static Role getToRole(ParserContext context, org.pi4soa.cdl.ExchangeDetails ed) {
		Role toRole=null;
		org.pi4soa.cdl.Interaction cdl=ed.getInteraction();
		
		if (ed.getAction() == ExchangeActionType.REQUEST) {
			if (cdl.getToParticipant() != null) {
				toRole = (Role)
					context.getState(XMLUtils.getLocalname(cdl.getToParticipant().getName()));
	
				if (toRole == null) {
					logger.severe("Failed to get 'to' role '"+XMLUtils.getLocalname(cdl.getFromParticipant().getName())+"'");
				}
			} else if (cdl.getToRoleType() != null) {
				ParticipantType ptype=
					PackageUtil.getParticipantForRoleType(cdl.getToRoleType());
				
				toRole = (Role)
					context.getState(XMLUtils.getLocalname(ptype.getName()));
				
				if (toRole == null) {
					logger.severe("Failed to get 'to' role '"+XMLUtils.getLocalname(ptype.getName())+"'");
				}
			}
		} else {
			if (cdl.getFromParticipant() != null) {
				toRole = (Role)
					context.getState(XMLUtils.getLocalname(cdl.getFromParticipant().getName()));
				
				if (toRole == null) {
					logger.severe("Failed to get 'from' role '"+XMLUtils.getLocalname(cdl.getFromParticipant().getName())+"'");
				}
			} else if (cdl.getFromRoleType() != null) {			
				ParticipantType ptype=
					PackageUtil.getParticipantForRoleType(cdl.getFromRoleType());
				
				toRole = (Role)
					context.getState(XMLUtils.getLocalname(ptype.getName()));
				
				if (toRole == null) {
					logger.severe("Failed to get 'from' role '"+XMLUtils.getLocalname(ptype.getName())+"'");
				}
			}
		}

		return(toRole);
	}
	
	/**
	 * This method creates a message signature.
	 * 
	 * @param details The CDL exchange details
	 * @param parent The parent model object, upon which any annotations would be recorded
	 * @return The message signature
	 */
	public static MessageSignature createMessageSignature(ExchangeDetails details, ModelObject parent) {
		org.pi4soa.cdl.Interaction cdl=details.getInteraction();
		
		// Define message signature
		MessageSignature ms=new MessageSignature();
		ms.setOperation(cdl.getOperation());
		
		if (NamesUtil.isSet(details.getFaultName())) {
			Annotation annotation=new Annotation(AnnotationDefinitions.FAULT);
			annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY, details.getFaultName());
			parent.getAnnotations().add(annotation);
			
			// Append fault name to operation to distinguish it from a normal response
			ms.setOperation(cdl.getOperation()+"-"+details.getFaultName());
		}

		// Define interaction message type
		if (details.getType() instanceof InformationType) {
			InformationType itype=(InformationType)details.getType();
			
			TypeReference ref=CDMProtocolParserUtil.getTypeReference(itype);
			
			ms.getTypeReferences().add(ref);
			
			// TODO: Determine if required (SAVARA-158)
			/*
			ms.getProperties().put(PropertyName.TYPE_SYSTEM, TypeSystem.XSD);
			
			if (NamesUtil.isSet(itype.getTypeName())) {
				String ns=CDLTypeUtil.getNamespace(itype.getTypeName(),
								cdl);
				String lp=XMLUtils.getLocalname(itype.getTypeName());
				
				QName qname=new QName(ns, lp);
				ms.getProperties().put(PropertyName.DATA_TYPE, qname.toString());
				
				ms.getProperties().put(PropertyName.XSD_TYPE, "true");
				
				/*
				if (lp != null) {
					ms.getProperties().put(MESSAGE_TYPE_LOCALPART,
									lp);
				}
				if (ns != null) {
					ms.getProperties().put(MESSAGE_TYPE_NAMESPACE,
									ns);
				}
				*/
			/*
			} else if (NamesUtil.isSet(itype.getElementName())) {
				String ns=CDLTypeUtil.getNamespace(itype.getElementName(),
						cdl);
				String lp=XMLUtils.getLocalname(itype.getElementName());
		
				QName qname=new QName(ns, lp);
				ms.getProperties().put(PropertyName.DATA_TYPE, qname.toString());
								
				ms.getProperties().put(PropertyName.XSD_ELEMENT, "true");
				
				/*
				if (lp != null) {
					ms.getProperties().put(MESSAGE_TYPE_LOCALPART,
									lp);
				}
				if (ns != null) {
					ms.getProperties().put(MESSAGE_TYPE_NAMESPACE,
									ns);
				}
				*/
			//}
		}
		
		return(ms);
	}
	
	/**
	 * This method returns the interface name associated with the
	 * supplied role type.
	 * 
	 * @param interaction The interaction
	 * @return The interface name
	 */
	protected static String getInterfaceName(org.pi4soa.cdl.Interaction interaction) {
		String ret=null;
		
		if (interaction != null) {
			String intfName=getInterface(interaction);
			
			String ns=CDLTypeUtil.getNamespace(intfName, interaction, false);
			
			if (ns == null) {
				ParticipantType ptype=null;
				
				// Check if interaction is associated with a 'to' participant/type
				// from which a service namespace could be derived
				if (interaction.getToRoleType() != null &&
						(ptype = PackageUtil.getParticipantForRoleType(
								interaction.getToRoleType())) != null) {
					ns=CDLTypeUtil.getNamespace(ptype.getName(), interaction, false);
					
					logger.fine("Deriving namespace from interactions 'to' participant type="+ns);
				}
				
				// If no other namespace is available, then use target namespace
				if (ns == null) {
					ns = interaction.getPackage().getTargetNamespace();
					logger.fine("Using CDL package targetNamespace="+ns);
				}
			}
			
			String lp=XMLUtils.getLocalname(intfName);
			
			ret = NameSpaceUtil.getFullyQualifiedName(ns, lp);
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the interaction's interface.
	 * 
	 * @param interaction The interaction
	 * @return The interface
	 */
	protected static String getInterface(Interaction interaction) {
		String ret=null;
			
		if (interaction != null &&
				interaction.getChannelVariable() != null &&
				interaction.getChannelVariable().getType() instanceof
						ChannelType) {
			ChannelType ctype=(ChannelType)
					interaction.getChannelVariable().getType();
			
			if (ctype.getBehavior() != null) {
				ret = ctype.getBehavior().getInterface();
				
				if (NamesUtil.isSet(ret)==false) {
					ret = ctype.getBehavior().getName();
				}
			} else if (ctype.getRoleType() != null) {
				
				// Attempt to find default behavior
				org.pi4soa.cdl.Behavior behavior=
					ctype.getRoleType().getBehavior(null);
				
				if (behavior != null) {
					ret = behavior.getInterface();
					
					if (NamesUtil.isSet(ret)==false) {
						ret = behavior.getName();
					}
				}
			}
		}
		
		return(ret);
	}

	private static Logger logger = Logger.getLogger("org.pi4soa.scribble.cdm.parser.rules");
}
