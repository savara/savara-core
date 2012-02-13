/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 * Copyright 2012 Red Hat, Inc.
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
 * 13 Dec 2008 : Initial version created by gary
 * Feb 2012 : Update based on scribble v2
 */
package org.savara.protocol.model.stateless;

import org.savara.protocol.model.util.ChoiceUtil;
import org.savara.protocol.model.util.RepeatUtil;
import org.scribble.protocol.model.*;
import org.scribble.protocol.util.InteractionUtil;

/**
 * This class implements the stateless transformation
 * rule for the Block component.
 */
public class BlockStatelessTransformationRule
						extends AbstractStatelessTransformationRule {

	/**
	 * This method determines whether the stateless
	 * transformation rule is applicable to the
	 * supplied model object.
	 * 
	 * @param modelObject The model object
	 * @return Whether the model object can be transformed
	 */
	public boolean isSupported(ModelObject modelObject) {
		return(modelObject instanceof Block);
	}
	
	/**
	 * This method transforms the supplied model object into
	 * a stateless equivalent.
	 * 
	 * @param context The context
	 * @param modelObject The model object to transform
	 * @return The transformed object
	 */
	public ModelObject transform(StatelessTransformationContext context,
						ModelObject modelObject) {
		return(null);
	}
	
	/**
	 * This method transforms the supplied block activities into
	 * a stateless equivalent.
	 * 
	 * @param context The context
	 * @param src The source block
	 * @param target The target block
	 * @return Whether the block has been fully transformed
	 */
	public boolean transform(StatelessTransformationContext context,
						Block src, Block target) {
		return(processBlock(context, src, target, 0, true));
	}
	
	/**
	 * This method processes the block of activities, to determine
	 * whether some or all should be in a particular stateless
	 * path.
	 * 
	 * @param context The context
	 * @param block The target block associated with the stateless path
	 * @param src The source block
	 * @param pos The position to start in the source block
	 * @return Whether the complete src block was processed
	 */
	public static boolean processBlock(StatelessTransformationContext context,
						Block src, Block target, int pos, boolean full) {
		boolean ret=true;
		Block initialPath=context.getCurrentPath();
		
		for (int i=pos; src != null &&
					i < src.getContents().size(); i++) {
			Activity act=src.getContents().get(i);
			
			if (isWaitState(context, act)) {
			
				if (isMultiPathBehaviour(act)) {
					java.util.List<Block> mpbs=getMultiPaths(act);
					
					for (int j=0; j < mpbs.size(); j++) {
						// Save return position
						context.push(new TransformState(act, src, i));
						
						Block p=context.createNewPath();
						
						if (p != null) {
							context.transform(mpbs.get(j),
									p);
						}
						
						context.pop();
					}
					
					if (isConditional(context, act) == false) {
						src = null;
					}
					
				} else if (isSinglePathBehaviour(act)) {
					Block spb=getSinglePath(act);
					
					context.push(new TransformState(act, src, i));
		
					Block p=context.createNewPath();
					
					if (p != null) {
						context.transform(spb, p);
					}
					
					context.pop();

					if (isConditional(context, act) == false) {
						src = null;
					}

				} else {
					target = context.createNewPath();
					
					if (target != null) {
						ModelObject newact=context.transform(act);
						
						if (newact instanceof Block) {
							target.getContents().addAll(((Block)newact).getContents());
						} else if (newact instanceof Activity) {
							target.getContents().add((Activity)newact);
						}
					} else {
						src = null;
					}
				}
				
				ret = false;
			} else {
				if (isSubScope(act)) {
					context.push(new TransformState(act, src, i));
				}
				
				Block localPath=context.getCurrentPath();
				
				ModelObject newact=context.transform(act);
				
				if (newact instanceof Block) {
					target.getContents().addAll(((Block)newact).getContents());
				} else if (newact instanceof Activity) {
					target.getContents().add((Activity)newact);
				}
				
				if (localPath != context.getCurrentPath()) {
					ret = false;
				}
				
				if (isSubScope(act)) {
					context.pop();
					
					// If transformation has changed the return type, then assume
					// has wait states, otherwise continue
					if (localPath != context.getCurrentPath() ||
							!(act instanceof Repeat &&
								RepeatUtil.isDecisionMaker((Repeat)act))) {
						src = null;
					}
				}
			}
		}
		
		if (src != null && full) {
			// Came to end of block, so check if state from
			// parent construct has been stored to enable
			// further processing
			//java.util.List<TransformState> stack=
			//				context.getStack();
			boolean f_continue=true;
			
			java.util.List<TransformState> tmpstack=
					new java.util.Vector<TransformState>();
			TransformState bp=null;
			
			//for (int i=0; f_continue && i < stack.size(); i++) {
				//TransformState bp=stack.get(i);
				
			// Only process if the path has changed
			//if (initialPath != context.getCurrentPath()) {
			while (f_continue && (bp=context.pop()) != null) {
				
				tmpstack.add(bp);
				
				if (bp.getParent() instanceof Repeat) {// &&
						//RepeatUtil.isDecisionMaker((Repeat)bp.getParent())) {
					
					// Only process if the path has changed
					if (initialPath != context.getCurrentPath()) {
						context.disallowNewPaths();
						
						context.push(bp);
						
						ModelObject newact=context.transform(bp.getParent());
						
						context.pop();
						
						if (newact instanceof Block) {
							target.getContents().addAll(((Block)newact).getContents());
						} else if (newact instanceof Activity) {
							target.getContents().add((Activity)newact);
						}
						
						context.allowNewPaths();
					}
					
					f_continue = false;
				} else {
					f_continue = processBlock(context, bp.getBlock(),
									target, bp.getPosition()+1, false);
				}
			}
			//}
			
			for (int i=tmpstack.size()-1; i >= 0; i--) {
				context.push(tmpstack.get(i));
			}
		}
		
		return(ret);
	}
	
	protected static boolean isSubScope(Activity act) {
		boolean ret=false;
		
		if (act instanceof Choice ||
				act instanceof Parallel ||
				act instanceof Repeat ||
				act instanceof RecBlock ||
				act instanceof Run) {
			ret = true;
		}
		
		return(ret);
	}
	
	protected static boolean isSinglePathBehaviour(Activity act) {
		return(act instanceof Repeat);
	}
	
	protected static Block getSinglePath(Activity act) {
		Block ret=null;
		
		if (act instanceof Repeat) {
			ret = ((Repeat)act).getBlock();
		}
		
		return(ret);
	}
	
	protected static boolean isMultiPathBehaviour(Activity act) {
		return(act instanceof Choice || act instanceof Parallel);
	}
	
	protected static java.util.List<Block> getMultiPaths(Activity act) {
		java.util.List<Block> ret=null;
		
		if (act instanceof Choice) {
			ret = ((Choice)act).getPaths();
		} else if (act instanceof Parallel) {
			ret = ((Parallel)act).getPaths();
		}
		
		return(ret);
	}
	
	protected static boolean isConditional(StatelessTransformationContext context,
							Activity act) {
		boolean ret=false;
		
		if (act instanceof Repeat) {
			ret = true;
		}
		
		return(ret);
	}
	
	protected static boolean isWaitState(StatelessTransformationContext context,
							Activity act) {
		boolean ret=false;
		
		if (act instanceof Choice) {
			ret = !ChoiceUtil.isDecisionMaker((Choice)act);
			
			// If RPC based, then check if interactions in each path are responses
			if (ret && !context.isMessageBased()) {
				Choice choice=(Choice)act;
				
				ret = false;
				
				for (Block path : choice.getPaths()) {
					java.util.List<ModelObject> interactions=
									InteractionUtil.getInitialInteractions(path);
					
					for (ModelObject mobj : interactions) {
						if (!org.savara.protocol.model.util.InteractionUtil.isResponse((Interaction)mobj)) {
							ret = true;
							break;
						}
					}
					
					if (ret) {
						break;
					}
				}
			}
		} else if (act instanceof Repeat) {
			ret = !RepeatUtil.isDecisionMaker((Repeat)act);
			
		} else if (act instanceof Interaction) {
			ret = !org.savara.protocol.model.util.InteractionUtil.isSend((Interaction)act) &&
					(context.isMessageBased() || 
						org.savara.protocol.model.util.InteractionUtil.isRequest((Interaction)act));
		}
		
		return(ret);
	}
}
