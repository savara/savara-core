/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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

import org.savara.bpel.model.TFlow;
import org.savara.bpel.model.TLink;
import org.savara.bpel.model.TLinks;
import org.savara.bpel.model.TSequence;
import org.savara.protocol.model.change.ModelChangeContext;
import org.scribble.protocol.model.*;

/**
 * This is the model change rule for the Parallel.
 */
public class ParallelModelChangeRule extends AbstractBPELModelChangeRule {

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
		
		if (mobj instanceof org.scribble.protocol.model.Parallel && isBPELModel(model)) {
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
		org.scribble.protocol.model.Parallel elem=
					 (org.scribble.protocol.model.Parallel)mobj;
		java.util.List<Block> paths=elem.getPaths();
		
		TFlow act=new TFlow();
			
		if (context.getParent() instanceof TSequence) {
			((TSequence)context.getParent()).getActivity().add(act);
		}
		
		// Check if flow links need to be declared
		java.util.List<String> linkNames=org.savara.protocol.model.util.ForkJoinUtil.getLinkNames(elem);
		
		if (linkNames.size() > 0) {
			TLinks links=new TLinks();
			act.setLinks(links);
			
			for (String linkName : linkNames) {
				TLink link=new TLink();
				link.setName(linkName);
				links.getLink().add(link);
			}
		}
			
		// Generate the individual flow paths
		for (int i=0; i < paths.size(); i++) {
			Block path=paths.get(i);
							
			TSequence seq=new TSequence();
							
			// Process the activities within the conversation
			java.util.List<Activity> acts=path.getContents();
						
			context.pushScope();
			
			context.setParent(seq);
			
			for (int j=0; j < acts.size(); j++) {
				context.insert(model, acts.get(j), null);
			}
			
			context.popScope();

			act.getActivity().add(seq);
		}
				
		return(true);
	}	
}
