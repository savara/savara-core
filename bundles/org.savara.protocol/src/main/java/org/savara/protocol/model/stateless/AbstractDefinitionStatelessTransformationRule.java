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

import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.ModelObject;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;

/**
 * This class represents an abstract definition stateless
 * transformation rule. This class initialises the context
 * with appropriate top level information which can be used
 * when processing other transformations.
 */
public abstract class AbstractDefinitionStatelessTransformationRule
						extends AbstractStatelessTransformationRule {

	/**
	 * This method returns a new definition of the appropriate
	 * type.
	 * 
	 * @param context The context
	 * @return The definition
	 */
	protected abstract Protocol createDefinition(StatelessTransformationContext context);
	
	/**
	 * This method returns a new multi-path behaviour, of the
	 * appropriate type, to represent the mutually exclusive
	 * choice construct at the top level of the stateless definition.
	 * 
	 * @param context The context
	 * @return The multi-path behaviour
	 */
	protected abstract Choice createMultiPathBehaviour(StatelessTransformationContext context);
	
	/**
	 * This method transforms the supplied model object into
	 * a stateless equivalent.
	 * 
	 * @param context The context
	 * @param modelObject The model object to transform
	 * @return The transformed object
	 */
	@Override
	public ModelObject transform(StatelessTransformationContext context,
							ModelObject modelObject) {
		Protocol ret=null;
		Protocol src=(Protocol)modelObject;
		
		ret = createDefinition(context);
		
		ret.derivedFrom(src);
		
		ret.setName(src.getName());
		
		Role role=new Role();
		role.setName(src.getLocatedRole().getName());
		
		ret.setLocatedRole(role);
		
		// Create role list
		java.util.List<Role> roleList=new java.util.Vector<Role>();
		
		((DefaultStatelessTransformationContext)context).setRoleList(roleList);
		
		// Create the multi-path behaviour
		Choice mpb=createMultiPathBehaviour(context);
		
		ret.getBlock().getContents().add(mpb);
			
		((DefaultStatelessTransformationContext)context).setMultiPathBehaviour(mpb);
		
		// Create initial path and process contents of the
		// definition
		Block newPath=context.createNewPath();
		
		context.transform(src.getBlock(), newPath);
		
		// Convert role list into parameters
		for (Role r : context.getRoleList()) {
			if (!r.equals(role)) {
				ParameterDefinition pd=new ParameterDefinition();
				pd.setName(r.getName());
				ret.getParameterDefinitions().add(pd);
			}
		}

		// Check if only one choice block
		if (mpb.getPaths().size() == 1) {
			ret.getBlock().remove(mpb);
			
			ret.getBlock().getContents().addAll(mpb.getPaths().get(0).getContents());
		}
		
/* GPB: TODO: Remove duplicate paths (see if merging as in choice can be used)			
			// Check for duplicate paths in multi-path behaviour
			org.scribble.comparator.Comparator comparator=
				(org.scribble.comparator.Comparator)
				RegistryFactory.getRegistry().getExtension(
						org.scribble.comparator.Comparator.class, null);

			for (int i=mpb.getPaths().size()-1; i >= 0; i--) {
				Block path=mpb.getPaths().get(i);
				boolean f_duplicate=false;
				
				for (int j=0; f_duplicate == false &&
									j < i; j++) {
					Block path2=mpb.getPaths().get(j);
					
					DefaultModelListener l=new DefaultModelListener();
					
					BehaviourList mainBehaviourList=BehaviourList.createBehaviourList(path);
					BehaviourList refBehaviourList=BehaviourList.createBehaviourList(path2);
					
					if (comparator.compare(mainBehaviourList,
							context.getSource(), refBehaviourList,
							context.getSource(), l)) {
						f_duplicate = true;
					}
				}
				
				if (f_duplicate) {
					mpb.removePath(path);
				}
			}
		}
*/
		
		return(ret);
	}

}
