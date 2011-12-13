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
package org.savara.bpmn2.parser.rules;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TChoreographyTask;
import org.savara.bpmn2.model.TExclusiveGateway;
import org.savara.bpmn2.model.TFlowElement;
import org.savara.bpmn2.model.TFlowNode;
import org.savara.bpmn2.model.TParallelGateway;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.model.TSequenceFlow;
import org.savara.bpmn2.model.TStartEvent;
import org.savara.common.logging.MessageFormatter;
import org.savara.protocol.model.Join;
import org.savara.protocol.model.Sync;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.CustomActivity;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.util.ActivityUtil;
import org.scribble.protocol.util.RoleUtil;

public class TChoreographyParserRule implements BPMN2ParserRule {

	private static Logger LOG=Logger.getLogger(TChoreographyParserRule.class.getName());
	
	/**
	 * This method determines whether the rule supports the
	 * supplied BPMN2 model element.
	 * 
	 * @param elem The element
	 * @return Whether the rule parses the supplied element
	 */
	public boolean isSupported(Object elem) {
		return(elem.getClass() == TChoreography.class);
	}

	/**
	 * This method parses the supplied element against the supplied
	 * context.
	 * 
	 * @param context The context
	 * @param elem The element
	 * @param container The container into which converted objects should be placed
	 */
	public void parse(BPMN2ParserContext context, Object elem, Block container) {
		TChoreography choreo=(TChoreography)elem;
		
		// Need to find the 'start event'
		TStartEvent startEvent=null;
		
		for (JAXBElement<? extends TFlowElement> jaxb : choreo.getFlowElement()) {
			if (jaxb.getValue().getClass() == TStartEvent.class) {
				if (startEvent != null) {
					context.getFeedbackHandler().error(MessageFormatter.format(
							java.util.PropertyResourceBundle.getBundle(
									"org.savara.bpmn2.Messages"), "SAVARA-BPMN2-00001"), null);
				} else {
					startEvent = (TStartEvent)jaxb.getValue();
				}
			}
		}

		if (startEvent == null) {
			context.getFeedbackHandler().error(MessageFormatter.format(
					java.util.PropertyResourceBundle.getBundle(
							"org.savara.bpmn2.Messages"), "SAVARA-BPMN2-00002"), null);
		} else {
			processNode(context, startEvent, container);
			
			cleanUpJoins(context);
			
			localiseIntroductions(context, container);
		}
	}
	
	/**
	 * This method processes the introduce statements to localise the introductions to
	 * the most appropriate blocks.
	 * 
	 * @param context The context
	 * @param container The container
	 */
	protected void localiseIntroductions(BPMN2ParserContext context, Block container) {
		
		// Work through introduced roles to localise the introduction
		java.util.Iterator<Introduces> intros=
					context.getScope().getIntroduces().values().iterator();
		
		while (intros.hasNext()) {
			Introduces rl=intros.next();
			
			for (int i=rl.getIntroducedRoles().size()-1; i >= 0; i--) {
				Role r=rl.getIntroducedRoles().get(i);
				Block b=RoleUtil.getEnclosingBlock(container.getEnclosingProtocol(), r, false);
				
				if (b == null) {
					// Report error
				} else if (b != container.getEnclosingProtocol().getBlock()){
					Introduces innerrl=null;
					
					if (b.size() > 0 && b.get(0) instanceof Introduces) {
						innerrl = (Introduces)b.get(0);
					} else {
						innerrl = new Introduces();
						innerrl.setIntroducer(rl.getIntroducer());
						b.getContents().add(0, innerrl);
					}
					
					rl.getIntroducedRoles().remove(r);
					innerrl.getIntroducedRoles().add(r);
				}
			}
			
			if (rl.getIntroducedRoles().size() > 0) {
				container.getEnclosingProtocol().getBlock().getContents().add(0, rl);
			}
		}
	}
	
	/**
	 * Check whether some of the parallel constructs, added to support the
	 * fork/join, can be removed to leave a simplified choice/parallel.
	 * 
	 * @param context The context
	 */
	protected void cleanUpJoins(BPMN2ParserContext context) {
		// Check the join blocks to see whether the choices can be simplified
		java.util.Iterator<Block> joinBlocks=
					context.getScope().getJoinBlocks().values().iterator();
		
		// Remove join blocks that converge on other joins
		while (joinBlocks.hasNext()) {
			Block joinBlock=joinBlocks.next();
			
			// Check if parallel with only one other block
			if (joinBlock.getParent() instanceof Parallel &&
					((Parallel)joinBlock.getParent()).getPaths().size() == 2) {
				
				if (joinBlock.getContents().size() == 2 &&
						joinBlock.getContents().get(0) instanceof Join &&
						joinBlock.getContents().get(1) instanceof Sync) {
					// Check that join and sync are associated with same role
					Join join=(Join)joinBlock.getContents().get(0);
					Sync sync=(Sync)joinBlock.getContents().get(1);
					
					if ((join.getRoles().size() == 0 && sync.getRoles().size() == 0) ||
						join.getRoles().containsAll(sync.getRoles())) {
						
						Parallel par=(Parallel)joinBlock.getParent();
						Block parParent=(Block)par.getParent();
						int parIndex=parParent.indexOf(par);
						
						// Remove join path, so only remaining block is the
						// normal content
						par.getPaths().remove(joinBlock);
						
						// Extract contents of other path
						parParent.remove(par);
						parParent.getContents().addAll(parIndex,
									par.getPaths().get(0).getContents());
						
						// Substitute labels in join with sync label in connected join
						Join otherJoin=(Join)context.getScope().getJoin(sync.getLabel());
						
						if (otherJoin != null) {
							otherJoin.getLabels().remove(sync.getLabel());
							otherJoin.getLabels().addAll(join.getLabels());
						}
						
						// Remove join block
						joinBlocks.remove();
					}
				}
			}
		}
		
		// Remove join blocks that have no subsequent activities
		joinBlocks = context.getScope().getJoinBlocks().values().iterator();
		
		while (joinBlocks.hasNext()) {
			Block joinBlock=joinBlocks.next();
			
			// Check if parallel with only one other block
			if (joinBlock.getParent() instanceof Parallel &&
					((Parallel)joinBlock.getParent()).getPaths().size() == 2) {
				
				if (joinBlock.getContents().size() == 1 &&
						joinBlock.getContents().get(0) instanceof Join) {
					
					Parallel par=(Parallel)joinBlock.getParent();
					Block parParent=(Block)par.getParent();
					int parIndex=parParent.indexOf(par);
					
					// Remove join path, so only remaining block is the
					// normal content
					par.getPaths().remove(joinBlock);
					
					// Extract contents of other path
					parParent.remove(par);
					parParent.getContents().addAll(parIndex,
								par.getPaths().get(0).getContents());

					// Need to find and remove sync's for join
					Join join=(Join)joinBlock.getContents().get(0);
					
					for (String label : join.getLabels()) {
						Sync sync=context.getScope().getSync(label);
						
						((Block)sync.getParent()).remove(sync);
					}
				}
			}
		}
	}
	
	protected void processNode(BPMN2ParserContext context, TFlowNode elem, Block container) {
		
		if (elem.getIncoming().size() > 1) {
			
			// Check if join block has already been registered
			Block b=context.getScope().getJoinBlocks().get(elem);
			
			if (b == null) {
				container = createJoin(context, elem, container);
			} else {
				
				// Check if in scope
				ModelObject parent=container.getParent();
				while (parent != null && parent != b && (parent instanceof Protocol) == false) {
					parent = parent.getParent();
				}
				
				if (parent != b) {
					// Find common enclosing block
					java.util.List<Block> list=new java.util.Vector<Block>();
					list.add(container);
					list.add(b);
					
					Block common=ActivityUtil.getEnclosingBlock(list);
					
					if (common != null) {
						
						if (!isInScopeOfSingleParallel(common, list)) {
							// Find parallel
							parent = common.getParent();
							
							
							while (parent != null && (parent instanceof Parallel) == false
									&& (parent instanceof Protocol) == false) {
								parent = parent.getParent();
							}
							
							if (parent instanceof Parallel) {
								Parallel oldParallel=(Parallel)b.getParent();
	
								oldParallel.getPaths().remove(b);
								
								((Parallel)parent).getPaths().add(b);
								
								// Check if old parallel should be removed/simplified
								if (oldParallel.getPaths().size() == 0) {
									((Block)oldParallel.getParent()).remove(oldParallel);
								} else if (oldParallel.getPaths().size() == 1) {
									int pos=((Block)oldParallel.getParent()).indexOf(oldParallel);
									
									if (pos == -1) {
										LOG.severe("Could not find position of parallel");
									} else {
										((Block)oldParallel.getParent()).getContents().addAll(pos,
												oldParallel.getPaths().get(0).getContents());
									}
									((Block)oldParallel.getParent()).remove(oldParallel);
								}
							} else {
								LOG.severe("Unable to find a containing parallel construct");
							}
						}
					} else {
						LOG.severe("Failed to find common block");
					}
				}
				
				// Don't want to process path further
				return;
			}
		}
		
		BPMN2ParserRule rule=ParserRuleFactory.getParserRule(elem);
		
		if (rule != null) {
			rule.parse(context, elem, container);
		}
		
		// Check outbound connections to see whether sequence or gateway
		if (elem.getOutgoing().size() == 1) {
			
			// Get link
			TSequenceFlow seq=(TSequenceFlow)
					context.getScope().getBPMN2Element(elem.getOutgoing().get(0).getLocalPart());
			
			if (seq != null) {
				Object target=seq.getTargetRef();
				
				if (target instanceof TFlowNode) {
					
					if (((TFlowNode)target).getIncoming().size() > 1) {
						
						// Add sync
						Sync sync=new Sync();
						sync.setLabel(getJoinName(elem.getOutgoing().get(0).getLocalPart()));
						
						// Get role
						getRoles(context, (TFlowNode)target, sync);
						
						container.add(sync);
						
						context.getScope().registerSync(sync);
					}

					processNode(context, (TFlowNode)target, container);
				}
			}
		} else if (elem.getOutgoing().size() > 1) {
			
			if (elem instanceof TExclusiveGateway) {
				
				// Create outer parallel
				Parallel parallel=new Parallel();
				
				container.add(parallel);
				
				// Add to review list
				context.getScope().getParallelReviewList().add(parallel);
				
				Block mainBlock=new Block();
				parallel.getPaths().add(mainBlock);
				
				Choice choice=new Choice();
				
				mainBlock.add(choice);
				
				for (QName seqFlowQName : elem.getOutgoing()) {
					TSequenceFlow seq=(TSequenceFlow)
							context.getScope().getBPMN2Element(seqFlowQName.getLocalPart());
					
					Block b=new Block();
					choice.getPaths().add(b);
					
					if (seq.getTargetRef() instanceof TFlowNode) {
						if (((TFlowNode)seq.getTargetRef()).getIncoming().size() > 1) {
							
							// Add sync
							Sync sync=new Sync();
							sync.setLabel(getJoinName(seqFlowQName.getLocalPart()));
							
							// Get role
							getRoles(context, (TFlowNode)seq.getTargetRef(), sync);
							
							b.add(sync);
							
							context.getScope().registerSync(sync);
						}
						
						processNode(context, (TFlowNode)seq.getTargetRef(), b);
					}
				}
			} else {
					
				// Create outer parallel
				Parallel parallel=new Parallel();
				
				container.add(parallel);
				
				for (QName seqFlowQName : elem.getOutgoing()) {
					TSequenceFlow seq=(TSequenceFlow)
							context.getScope().getBPMN2Element(seqFlowQName.getLocalPart());
					
					Block b=new Block();
					parallel.getPaths().add(b);
					
					if (seq.getTargetRef() instanceof TFlowNode) {

						if (((TFlowNode)seq.getTargetRef()).getIncoming().size() > 1) {
							
							// Add sync
							Sync sync=new Sync();
							sync.setLabel(getJoinName(seqFlowQName.getLocalPart()));
							
							// Get role
							getRoles(context, (TFlowNode)seq.getTargetRef(), sync);
							
							b.add(sync);
							
							context.getScope().registerSync(sync);
						}
						
						processNode(context, (TFlowNode)seq.getTargetRef(), b);
					}
				}
			}
		}
	}
	
	protected void getRoles(BPMN2ParserContext context, TFlowNode node, CustomActivity activity) {
		//Role ret=null;
		
		if (node instanceof TChoreographyTask) {
			TChoreographyTask task=(TChoreographyTask)node;
			
			for (QName qname : task.getParticipantRef()) {
				TParticipant p=(TParticipant)
						context.getScope().getBPMN2Element(qname.getLocalPart());
				
				if (p != null) {
					// TODO: May need to determine whether role should be added - if
					// not in scope of the join - could check context, but problem is
					// a later use may have it in the context, but still be
					// not relevant to the join
					
					Role r=new Role(p.getName());
					
					if (!activity.getRoles().contains(r)) {
						activity.getRoles().add(r);
					}
				} else {
					LOG.severe("Could not find participant for id '"+qname.getLocalPart()+"'");
				}
			}
		} else if (node.getOutgoing().size() > 0) {
			
			for (QName qname : node.getOutgoing()) {
				TSequenceFlow seqFlow=(TSequenceFlow)
						context.getScope().getBPMN2Element(qname.getLocalPart());
				TFlowNode otherNode=(TFlowNode)seqFlow.getTargetRef();
				
				if (otherNode != null) {
					getRoles(context, otherNode, activity);
					
					/*
					if (ret == null) {
						ret = r;
					} else if (ret.equals(r) == false) {
						context.getFeedbackHandler().error("Inconsistent initiating roles after gateway '"+
								ret+"' and '"+r+"'", null);
					}
					*/
				} else {
					LOG.severe("Unable to find node for '"+qname.getLocalPart()+"'");
				}
			}
			
			/*
			if (count > 0 && count < node.getOutgoing().size()) {
				context.getFeedbackHandler().error("Path does not identify an initiating participant", null);
			}
			*/
		}
		
		//return(ret);
	}
	
	/**
	 * This method determines whether the supplied list of blocks are inscope of
	 * a single parallel construct that is contained within the supplied block.
	 * 
	 * @param common The block to check
	 * @param list The list of blocks that must be in scope of a single parallel
	 * @return Whether the list of blocks are in the scope of a single parallel
	 */
	protected boolean isInScopeOfSingleParallel(Block common, java.util.List<Block> list) {
		// Check if parallel contained in block
		// and also has the listed blocks contained
		// directly or indirectly
		boolean inscope=false;
		
		for (Activity act : common.getContents()) {
			if (act instanceof Parallel) {
				// Check if all blocks are indirectly contained in
				// this parallel
				java.util.List<Block> blks=new java.util.Vector<Block>(list);
				
				for (Block path : ((Parallel)act).getPaths()) {
					
					for (int i=blks.size()-1; i >= 0; i--) {
						Block blk=blks.get(i);
						java.util.List<Block> sublist=
								new java.util.Vector<Block>();
						sublist.add(blk);
						sublist.add(path);
						
						Block enclosing=ActivityUtil.getEnclosingBlock(sublist);
						
						if (enclosing == path) {
							blks.remove(blk);
						}
					}
				}
				
				if (blks.size() == 0) {
					inscope = true;
					break;
				}
			}
		}

		return(inscope);
	}
	
	/**
	 * This method constructs the join path, and returns the new container associated
	 * with it.
	 * 
	 * @param context The context
	 * @param elem The flow node
	 * @param container The existing container
	 * @return The new container
	 */
	protected Block createJoin(BPMN2ParserContext context, TFlowNode elem, Block container) {
		// Add join
		Join join=new Join();
		for (QName qname : elem.getIncoming()) {
			join.getLabels().add(getJoinName(qname.getLocalPart()));
			
			context.getScope().registerJoin(join);
		}
		
		getRoles(context, elem, join);
		
		join.setXOR(elem instanceof TExclusiveGateway);
		
		// Find parent parallel construct
		ModelObject parent=container.getParent();
		while (parent != null && (parent instanceof Parallel) == false
						&& (parent instanceof Protocol) == false) {
			parent = parent.getParent();
		}

		// Need to create new container for subsequent processing
		container = new Block();
		
		if (parent instanceof Parallel) {
			((Parallel)parent).getPaths().add(container);
		} else if (parent instanceof Protocol) {
			// TODO: What if no parallel???
			LOG.severe("No enclosing parallel construct in protocol");
		} else {
			LOG.severe("No enclosing parallel construct");
		}
		
		container.add(join);
		
		// Register block
		context.getScope().getJoinBlocks().put(elem, container);
		
		return(container);
	}
	
	protected String getJoinName(String label) {
		String ret=label;
		
		if (ret != null && ret.startsWith("SequenceFlow_")) {
			ret = ret.replaceAll("SequenceFlow_", "L");
		}
		
		return(ret);
	}
}
