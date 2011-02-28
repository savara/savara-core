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

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.model.TScope;
import org.savara.bpel.model.TSequence;
import org.savara.protocol.model.change.ModelChangeContext;
import org.savara.protocol.model.change.ModelChangeUtils;
import org.scribble.protocol.model.*;
import org.scribble.protocol.util.RunUtil;

/**
 * This is the model change rule for the Run.
 */
public class RunModelChangeRule extends AbstractBPELModelChangeRule {

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
		
		if (mobj instanceof Run && isBPELModel(model)) {
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
		Run elem=(Run)mobj;

		Protocol defn=RunUtil.getInnerProtocol(elem.enclosingProtocol(),
				elem.getProtocolReference());

		if (defn != null) {
			
			// Push details related to sub-choreo
			ModelChangeUtils.pushRoleContractMapping(context, elem, context.getJournal());
			
			//ModelChangeUtils.addContracts(context, elem.getProtocol(), false);
			
			//context.insert(model, elem.getProtocol(), ref);
			
			// Create a scope
			TScope scope=new TScope();
			TSequence seq=new TSequence();

			((TSequence)context.getParent()).getActivity().add(scope);
			
			// NOTE: Currently needs to be added after adding scope
			// to parent sequence, as otherwise the DOM element
			// associated with the 'seq' sequence becomes
			// disconnected from the actual document - due to
			// the fact that added elements are copied (in
			// turn due to an xml parser exception).
			scope.setSequence(seq);
			
			context.getProperties().put(BPELDefinitions.BPEL_SCOPE_PROPERTY, scope);

			// Process the activities within the conversation
			java.util.List<Activity> acts=defn.getBlock().getContents();
			
			Object parent=context.getParent();
			
			context.setParent(seq);
			
			for (int i=0; i < acts.size(); i++) {
				context.insert(model, acts.get(i), null);
			}
			
			// Reset old parent
			context.setParent(parent);

			// Pop details related to sub-choreo
			//ModelChangeUtils.removeContracts(context, elem.getProtocol(), false);
			
			ModelChangeUtils.popRoleContractMapping(context, elem, context.getJournal());
		}
		
		return(true);
	}
	
}
