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

import java.util.logging.Logger;

import org.scribble.protocol.model.*;

/**
 * This class provides the conversation specific
 * stateless transformation rule for the While construct.
 */
public class RepeatStatelessTransformationRule 
					extends AbstractStatelessTransformationRule {

	private static final Logger LOG=Logger.getLogger(RepeatStatelessTransformationRule.class.getName());
	
	/**
	 * This method determines whether the stateless
	 * transformation rule is applicable to the
	 * supplied model object.
	 * 
	 * @param modelObject The model object
	 * @return Whether the model object can be transformed
	 */
	public boolean isSupported(ModelObject modelObject) {
		return(modelObject instanceof Repeat);
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
		Activity ret=null;
		Repeat src=(Repeat)modelObject;
		
		// NOTE: Need to determine if should be 'repeat', if all
		// contents are not wait states, and an 'if' if
		// only translating part of the while loop contents.
		// If using the 'if', then need to also place activities
		// following the while into an 'else' path.
		
		Block cb=new Block();
		
		if (context.transform(src.getBlock(), cb)) {
			ret = new Repeat();
			
			for (int i=0; i < src.getRoles().size(); i++) {
				Role r=(Role)context.transform(src.getRoles().get(i));
				
				if (r != null) {
					((Repeat)ret).getRoles().add(r);
				}
			}
		
			((Repeat)ret).setBlock(cb);
			
			// TODO: Need to be able to cater for situation
			// where while contents are fully processed
			// and therefore the subsequent activities
			// need to be further processed. Currently
			// the assumption is that the subsequent
			// activities will be processed as part of
			// the block contents. Might need to return
			// a block, and have a means of transferring
			// into the block that would normally add
			// the While.
		} else {
			ret = new Choice();
			
			if (src.getRoles().size() > 1) {
				LOG.severe("Too many roles to transform for repeat");
			}
			
			for (int i=0; i < src.getRoles().size(); i++) {
				Role r=(Role)context.transform(src.getRoles().get(i));
				
				if (r != null) {
					((Choice)ret).setRole(r);
				}
			}
		
			((Choice)ret).getPaths().add(cb);
			
			Block elseBlock=new Block();
			
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
			//	TransformState bp=stack.get(i);
				
			while (f_continue && (bp=context.pop()) != null) {
				
				tmpstack.add(bp);

				// TODO: Possibly check for 'parent' object
				// that is repetition and also whether it
				// is completely defined within the same
				// block - can this be done based on checking
				// the stack?
				
//* GPB: TO INVESTIGATE				
				if (bp.getParent() != src &&
						BlockStatelessTransformationRule.isSinglePathBehaviour(bp.getParent()) &&
						BlockStatelessTransformationRule.isWaitState(context, bp.getParent()) == false &&
						bp.getParent() instanceof Repeat) {
					f_continue = false;

				} else {
					f_continue = BlockStatelessTransformationRule.processBlock(context, bp.getBlock(),
								elseBlock, bp.getPosition()+1, false);
				}
//*/				
			}
			
			for (int i=tmpstack.size()-1; i >= 0; i--) {
				context.push(tmpstack.get(i));
			}
			
			((Choice)ret).getPaths().add(elseBlock);

		}
	
		ret.derivedFrom(src);
		
		return(ret);
	}
}
