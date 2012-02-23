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

import org.savara.bpel.model.TImport;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.model.TSequence;
import org.savara.bpel.util.ImportUtil;
import org.savara.protocol.model.change.ModelChangeContext;
import org.savara.protocol.model.change.ModelChangeUtils;
import org.savara.contract.model.Contract;
import org.savara.contract.model.Interface;
import org.scribble.protocol.model.*;

/**
 * This is the model change rule for the Conversation.
 */
public class ProtocolModelChangeRule extends AbstractBPELModelChangeRule {

	//private static final String NAME_SUFFIX = "_main";

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
	public boolean isInsertSupported(ModelChangeContext context,
				ProtocolModel model, ModelObject mobj, ModelObject ref) {
		boolean ret=false;
		
		if (mobj instanceof Protocol && isBPELModel(model)) {
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
	public boolean insert(ModelChangeContext context,
				ProtocolModel model, ModelObject mobj, ModelObject ref) {
		TProcess bpelModel=getBPELModel(model);
		Protocol conv=(Protocol)mobj;
		String roleNamespace=null;
		
		TSequence seq=new TSequence();
		
		// Check if root conversation
		if (conv.getParent() instanceof ProtocolModel) {
			
			// Set the name of the process
			bpelModel.setName(conv.getName()+"_"+
								conv.getLocatedRole().getName());
			
			// Get contract
			Contract contract=ModelChangeUtils.getContract(context, conv.getLocatedRole());
			
			if (contract != null) {
				roleNamespace = contract.getNamespace();
				
				// Set namespace
				bpelModel.setTargetNamespace(contract.getNamespace());
			}
			
			// Add import for this role
			addImport(context, bpelModel, conv, conv.getLocatedRole());

			// Add import statements for partner roles
			/*
			final java.util.List<Role> roles=new java.util.Vector<Role>();
			
			conv.getParent().visit(new DefaultVisitor() {
				
				public void accept(Introduces elem) {
					for (Role r : elem.getRoles()) {
						if (roles.contains(r) == false) {
							roles.add(r);
						}
					}
				}
			});
			*/
			java.util.List<Role> roles=conv.getRoles();
			
			for (Role r : roles) {
				addImport(context, bpelModel, conv, r);
			}
			
			// Add import for partner link types
			TImport imp=new TImport();
			imp.setLocation(bpelModel.getName()+"Artifacts.wsdl");
			imp.setNamespace(roleNamespace);
			imp.setImportType("http://schemas.xmlsoap.org/wsdl/");
			
			bpelModel.getImport().add(imp);
			
			// Add sequence to model
			bpelModel.setSequence(seq);

			// Process the activities within the conversation
			java.util.List<Activity> acts=conv.getBlock().getContents();
			
			context.pushScope();
			
			context.setParent(seq);
			
			for (int i=0; i < acts.size(); i++) {
				//if ((acts.get(i) instanceof Definition) == false) {
					context.insert(model, acts.get(i), null);
				//}
			}
			
			// Reset old parent
			context.popScope();
			
			return(true);
		} else {
			return(false);
		}
	}
	
	/**
	 * This method adds an import statement for the contract associated with the
	 * supplied role.
	 * 
	 * @param context The context
	 * @param bpelModel The model
	 * @param conv The conversation
	 * @param role The role
	 */
	protected void addImport(ModelChangeContext context, TProcess bpelModel,
								Protocol conv, Role role) {
		
		Contract contract=ModelChangeUtils.getContract(context, role);
		
		if (contract != null) {
			boolean gen=false;
			
			java.util.Iterator<Interface> iter=contract.getInterfaces().iterator();
			
			while (gen == false && iter.hasNext()) {
				Interface intf=iter.next();
				
				if (intf.getMessageExchangePatterns().size() > 0) {
					gen = true;
				}
			}
			
			if (gen) {
				String fileName=ImportUtil.getWSDLFileName(role,
								conv.getName(), "");
				
				TImport imp=new TImport();
				
				imp.setLocation(fileName);
				imp.setNamespace(contract.getNamespace());
				imp.setImportType("http://schemas.xmlsoap.org/wsdl/");
				
				bpelModel.getImport().add(imp);
			}
		}
	}
}
