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
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.protocol.model.*;
import org.scribble.protocol.model.Choice;

public class ChoiceParserRule implements ParserRule {

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
				cdlType instanceof org.pi4soa.cdl.Choice);
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
		org.scribble.protocol.model.Activity ret=null;
		org.pi4soa.cdl.Choice cdl=(org.pi4soa.cdl.Choice)cdlType;
		java.util.List<Block> blocks=new java.util.Vector<Block>();
		//Role fromRole=null;
		//java.util.List<Role> toRoles=new java.util.Vector<Role>();
		
		// Check if all paths are associated with the same from and to role
		//boolean f_sameRoles=isSameRoles(context, cdl);
		
		// Process all of the activities within the
		// choreography
		java.util.Iterator<org.pi4soa.cdl.Activity> actiter=
					cdl.getActivities().iterator();
		while (actiter.hasNext()) {
			org.pi4soa.cdl.Activity act=actiter.next();
			
			ParserRule rule=ParserRuleFactory.getConverter(
					org.scribble.protocol.model.Activity.class, act);
		
			if (rule != null) {
				
				context.pushState();
				
				/*
				// Find exchange details for this path
				InteractionLocator locator=new InteractionLocator(act);
				
				act.visit(locator);
				
				// If interaction found, then make sure it is ignored when
				// processing the when path contents
				if (f_sameRoles && locator.getInteraction() != null &&
						locator.getInteraction().getExchangeDetails().size() > 0) {
					context.ignore(locator.getInteraction().getExchangeDetails().get(0));
				}
				*/
				
				org.scribble.protocol.model.Activity activity=
					(org.scribble.protocol.model.Activity)
					rule.parse(context,
							org.scribble.protocol.model.Activity.class, act);
				
				if (activity != null) {
					/* TODO: Consider whether collapsing choices is appropriate?
					 * possibly if same from/to roles.
					 */
					/*
					if (activity instanceof Choice) {
						
						if (f_sameRoles) {
							blocks.addAll(((Choice)activity).getWhens());
						} else {
							When block=new When();
							
							// Check if single path in choice
							Choice c=(Choice)activity;
							
							if (c.getWhens().size() == 1) {
								// Make when message signature back into an interaction
								org.scribble.protocol.model.Interaction i=
										new org.scribble.protocol.model.Interaction();
								i.derivedFrom(c.getWhens().get(0));
								
								i.setMessageSignature(c.getWhens().get(0).getMessageSignature());
								i.setFromRole(c.getFromRole());
								i.getToRoles().add(c.getToRole());
								
								block.getBlock().add(i);
								
								// Add contained activities
								block.getBlock().getContents().addAll(
											c.getWhens().get(0).getBlock().getContents());
							} else {
								block.getBlock().add(activity);
							}
							
							blocks.add(block);
						} 
						
						// TODO: If fromRole not set, use choice one? If fromRole set,
						// then may need to compare? Same for two roles?
						if (fromRole == null && f_sameRoles) {
							fromRole = ((Choice)activity).getFromRole();
							
							if (toRoles.contains(((Choice)activity).getToRole()) == false) {
								toRoles.add(((Choice)activity).getToRole());
							}
						}
					} else {

						When block=new When();
						
						if (f_sameRoles && locator.getInteraction() != null &&
								locator.getInteraction().getExchangeDetails().size() > 0) {
							
							// Get the first exchange details
							ExchangeDetails ed=locator.getInteraction().getExchangeDetails().get(0);
							
							if (ed != null) {
							
								MessageSignature ms=InteractionParserRule.createMessageSignature(ed,
															block);
							
								// TODO: Need to update/verify from/to roles on containing choice
							
								block.setMessageSignature(ms);
								
								fromRole = InteractionParserRule.getFromRole(context,
														ed);
								
								Role toRole = InteractionParserRule.getToRole(context,
														ed);
								if (toRoles.contains(toRole) == false) {
									toRoles.add(toRole);
								}
								
								if (ed.getAction() == ExchangeActionType.RESPOND) {
									Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
									annotation.getProperties().put(AnnotationDefinitions.REPLY_TO_PROPERTY,
												CDMProtocolParserUtil.getLabel(ed));
									block.getAnnotations().add(annotation);
								} else {
									// Check if request has response/fault exchanges
									java.util.List<ExchangeDetails> resps=
											InteractionUtil.getResponseExchangeDetails(ed);
									
									if (resps != null && resps.size() > 0) {
										Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
										annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY,
													CDMProtocolParserUtil.getLabel(ed));
										block.getAnnotations().add(annotation);
									}
								}
								
								Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

								scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
											CDLTypeUtil.getURIFragment(ed));
								
								block.getAnnotations().add(scannotation);
								
								// Set interface name
								if (locator.getInteraction().getToRoleType() != null) {
									// TODO: Deal with interfaces that have multiple behaviours	
									Annotation annotation=new Annotation(AnnotationDefinitions.INTERFACE);
									annotation.getProperties().put(AnnotationDefinitions.NAME_PROPERTY,
												InteractionParserRule.getInterfaceName(locator.getInteraction()));
									block.getAnnotations().add(annotation);
								}
							}
						}
						*/
					
					if (activity instanceof Choice && act instanceof Conditional) {
						// Extract block from choice, as nested choice is not required
						// for conditional path
						java.util.List<org.scribble.common.model.Annotation> annotations=
												activity.getAnnotations();
						
						activity = ((Choice)activity).getPaths().get(0);
						
						activity.getAnnotations().addAll(annotations);
					}
					
					Block block=null;
					
					if (activity instanceof Block) {
						block = (Block)activity;
					} else {
						block = new Block();
						block.add(activity);							
					}
					
					blocks.add(block);
					//}
				}
				
				context.popState();
			}
		}

		/*
		for (int i=0; i < blocks.size(); i++) {
			When b=blocks.get(i);
						
			if (b.getMessageSignature() == null) {
				// Create label
				MessageSignature ms=new MessageSignature();
				ms.setOperation("label"+i);
				b.setMessageSignature(ms);
			}
		}
		*/
		
		ret = new org.scribble.protocol.model.Choice();
		
		((org.scribble.protocol.model.Choice)ret).
				getPaths().addAll(blocks);
		
		if (ret != null) {
			Annotation scannotation=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);
			
			scannotation.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
					CDLTypeUtil.getURIFragment(cdl));
			ret.getAnnotations().add(scannotation);
		}
		
		return(ret);
	}
	
	/*
	protected boolean isSameRoles(ParserContext context, org.pi4soa.cdl.Choice cdl) {
		boolean ret=true;
		Role fromRole=null;
		Role toRole=null;
		
		for (int i=0; ret == true && i < cdl.getActivities().size(); i++) {
			org.pi4soa.cdl.Activity act=cdl.getActivities().get(i);
			Role from=null;
			Role to=null;
			
			ParserRule rule=ParserRuleFactory.getConverter(
					org.scribble.protocol.model.Activity.class, act);
		
			if (rule != null) {
				
				context.pushState();
				
				// Find exchange details for this path
				InteractionLocator locator=new InteractionLocator(act);
				
				act.visit(locator);
				
				org.scribble.protocol.model.Activity activity=
					(org.scribble.protocol.model.Activity)
					rule.parse(context,
							org.scribble.protocol.model.Activity.class, act);
				
				if (activity instanceof Choice) {
					from = ((Choice)activity).getFromRole();
					to = ((Choice)activity).getToRole();
				} else if (activity != null) {

					if (locator.getInteraction() != null &&
							locator.getInteraction().getExchangeDetails().size() > 0) {
							
						// Get the first exchange details
						ExchangeDetails ed=locator.getInteraction().getExchangeDetails().get(0);
						
						if (ed != null) {
							
							from = InteractionParserRule.getFromRole(context,
														ed);
								
							to = InteractionParserRule.getToRole(context,
														ed);
						}
					}
				}
				
				context.popState();
			}
			
			if (i == 0) {
				fromRole = from;
				toRole = to;
			} else {				
				if (from != null) {
					if (fromRole == null) {
						ret = false;
					} else {
						ret = fromRole.equals(from);
					}
				} else {
					ret = (fromRole == null);
				}
				
				if (to != null) {
					if (toRole == null) {
						ret = false;
					} else {
						ret = toRole.equals(to);
					}
				} else {
					ret = (toRole == null);
				}
			}
		}
		
		return(ret);
	}
	
	
	public static class InteractionLocator implements CDLVisitor {

		private Interaction m_interaction=null;
		private boolean f_clearInteraction=false;
		private org.pi4soa.cdl.Activity m_parent=null;
		
		public InteractionLocator(org.pi4soa.cdl.Activity parent) {
			m_parent = parent;
		}
		
		public Interaction getInteraction() {
			return(f_clearInteraction ? null : m_interaction);
		}
		
		public void assign(Assign assign) {
			// TODO Auto-generated method stub
			
		}

		public void choiceStart(org.pi4soa.cdl.Choice choice) {
			if (m_interaction == null && choice != m_parent) {
				f_clearInteraction = true;
			}
		}

		public void choiceEnd(org.pi4soa.cdl.Choice choice) {
			// TODO Auto-generated method stub
			
		}

		public void choreographyStart(Choreography choreography) {
			// TODO Auto-generated method stub
			
		}

		public void choreographyEnd(Choreography choreography) {
			// TODO Auto-generated method stub
			
		}

		public void conditionalStart(Conditional conditional) {
			if (m_interaction == null && conditional != m_parent) {
				f_clearInteraction = true;
			}
		}

		public void conditionalEnd(Conditional conditional) {
			// TODO Auto-generated method stub
			
		}

		public void exceptionHandlerStart(ExceptionHandler handler) {
			// TODO Auto-generated method stub
			
		}

		public void exceptionHandlerEnd(ExceptionHandler handler) {
			// TODO Auto-generated method stub
			
		}

		public void exceptionWorkUnitStart(ExceptionWorkUnit workunit) {
			// TODO Auto-generated method stub
			
		}

		public void exceptionWorkUnitEnd(ExceptionWorkUnit workunit) {
			// TODO Auto-generated method stub
			
		}

		public void finalizerStart(FinalizerHandler finalizer) {
			// TODO Auto-generated method stub
			
		}

		public void finalizerEnd(FinalizerHandler finalizer) {
			// TODO Auto-generated method stub
			
		}

		public void finalize(Finalize finalize) {
			// TODO Auto-generated method stub
			
		}

		public void interaction(Interaction interaction) {
			if (m_interaction == null) {
				m_interaction = interaction;
			}
		}

		public void noAction(NoAction noAction) {
			// TODO Auto-generated method stub
			
		}

		public void packageStart(Package pack) {
			// TODO Auto-generated method stub
			
		}

		public void packageEnd(Package pack) {
			// TODO Auto-generated method stub
			
		}

		public void parallelStart(Parallel parallel) {
			// TODO Auto-generated method stub
			
		}

		public void parallelEnd(Parallel parallel) {
			// TODO Auto-generated method stub
			
		}

		public void perform(Perform perform) {
			perform.getChoreography().visit(this);	
		}

		public void sequenceStart(Sequence sequence) {
			// TODO Auto-generated method stub
			
		}

		public void sequenceEnd(Sequence sequence) {
			// TODO Auto-generated method stub
			
		}

		public void silentAction(SilentAction silentAction) {
			// TODO Auto-generated method stub
			
		}

		public void whenStart(org.pi4soa.cdl.When when) {
			// TODO Auto-generated method stub
			
		}

		public void whenEnd(org.pi4soa.cdl.When when) {
			// TODO Auto-generated method stub
			
		}

		public void whileStart(While whileElem) {
			// TODO Auto-generated method stub
			
		}

		public void whileEnd(While whileElem) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	*/
}
