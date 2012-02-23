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

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TSequence;
import org.savara.protocol.model.change.*;
import org.scribble.protocol.model.*;

/**
 * This is the abstract ESB model change rule.
 */
public abstract class AbstractBPELModelChangeRule extends AbstractModelChangeRule {

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
				ProtocolModel model, ModelObject mobj, ModelObject ref) {
		boolean ret=false;
		
		if (context.getParent() instanceof TSequence && isBPELModel(model)) {
			ret = true;
		}
		
		return(ret);
	}
	
	protected boolean isBPELModel(ProtocolModel model) {
		return(model.getProperties().get(BPELDefinitions.BPEL_MODEL_PROPERTY) instanceof TProcess);
	}
	
	protected TProcess getBPELModel(ProtocolModel model) {
		return((TProcess)model.getProperties().get(BPELDefinitions.BPEL_MODEL_PROPERTY));
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
		
		return(ret);
	}

	/**
	 * This method removes the supplied model object from the
	 * supplied model. The model object representation must
	 * contain the necessary model specific to remove the 
	 * object from the underlying model representation.
	 * 
	 * @param context The context
	 * @param model The model being changed
	 * @param mobj The model object to be deleted
	 * @return Whether the change has been applied
	 */
	@Override
	public boolean delete(ModelChangeContext context,
				ProtocolModel model, ModelObject mobj) {
		boolean ret=false;

		return(ret);
	}
}
