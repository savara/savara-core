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

import org.savara.protocol.model.change.ModelChangeContext;
import org.savara.protocol.model.change.ModelChangeUtils;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.contract.model.*;
import org.scribble.protocol.model.*;

import junit.framework.TestCase;

public class ModelChangeUtilsTest extends TestCase {

	private static final String SUB_PROTOCOL = "SubProtocol";
	private static final String MY_ROLE = "MyRole";
	private static final String MY_LOCATED_ROLE = "MyLocatedRole";
	private static final String MY_OTHER_ROLE = "MyOtherRole";
	private static final String MY_OTHER_LOCATED_ROLE = "MyOtherLocatedRole";

	public void testInitialiseContracts() {
		ModelChangeContext context=new BPELModelChangeContext(null, new DefaultFeedbackHandler());
		
		Protocol conv=new Protocol();
		Role r=new Role();
		r.setName(MY_ROLE);
		
		Contract c=new Contract();
		r.getProperties().put(Contract.class.getName(), c);
		
		conv.setLocatedRole(r);
		
		ModelChangeUtils.addContract(context, r, c);
		
		Contract c2=ModelChangeUtils.getContract(context, new Role(MY_ROLE));
		
		if (c2 == null) {
			fail("Contract not found");
		}
		
		if (c2 != c) {
			fail("Contract not the same as the one stored");
		}
	}

	public void testInitialiseContractsTwice() {
		ModelChangeContext context=new BPELModelChangeContext(null, new DefaultFeedbackHandler());
		
		Protocol conv=new Protocol();
		Role r=new Role();
		r.setName(MY_ROLE);
		
		Contract c=new Contract();
		r.getProperties().put(Contract.class.getName(), c);
		
		conv.setLocatedRole(r);
		
		ModelChangeUtils.addContract(context, r, c);
		
		Protocol conv2=new Protocol();
		Role r2=new Role();
		r2.setName(MY_OTHER_ROLE);

		Contract c2=new Contract();
		r2.getProperties().put(Contract.class.getName(), c2);
		
		conv2.setLocatedRole(r2);
		
		ModelChangeUtils.addContract(context, r2, c2);
		
		Contract c3=ModelChangeUtils.getContract(context, new Role(MY_ROLE));
		
		if (c3 == null) {
			fail("Contract not found");
		}
		
		if (c3 != c) {
			fail("Contract not the same as the one stored");
		}
		
		Contract c4=ModelChangeUtils.getContract(context, new Role(MY_OTHER_ROLE));
		
		if (c4 == null) {
			fail("Contract not found");
		}
		
		if (c4 != c2) {
			fail("Contract2 not the same as the one stored");
		}
	}
	
	public void testPushRoleContractMapping() {
		ModelChangeContext context=new BPELModelChangeContext(null, new DefaultFeedbackHandler());
		
		Protocol conv=new Protocol();
		Role r1=new Role();
		r1.setName(MY_LOCATED_ROLE);
		conv.setLocatedRole(r1);
		
		Contract c1=new Contract();
		r1.getProperties().put(Contract.class.getName(), c1);
		
		Role r2=new Role();
		r2.setName(MY_ROLE);
		
		Contract c2=new Contract();
		r2.getProperties().put(Contract.class.getName(), c2);
				
		//RoleList rl=new RoleList();
		//rl.getRoles().add(r2);
		//conv.getBlock().getContents().add(rl);
		ParameterDefinition rpd=new ParameterDefinition();
		rpd.setName(r2.getName());
		conv.getParameterDefinitions().add(rpd);
		
		ModelChangeUtils.addContract(context, r1, c1);
		ModelChangeUtils.addContract(context, r2, c2);

		Protocol subconv=new Protocol();
		subconv.setName(SUB_PROTOCOL);
		
		ParameterDefinition pd=new ParameterDefinition();
		pd.setName(MY_OTHER_ROLE);
		subconv.getParameterDefinitions().add(pd);

		Role subr1=new Role();
		subr1.setName(MY_OTHER_LOCATED_ROLE);
		subconv.setLocatedRole(subr1);
		
		Run run=new Run();
		
		ProtocolReference ref=new ProtocolReference();
		ref.setName(SUB_PROTOCOL);
		ref.setRole(subr1);
		run.setProtocolReference(ref);
		
		conv.getBlock().getContents().add(run);
		
		Parameter db=new Parameter();
		db.setName(r2.getName());
		//db.setBoundName(MY_OTHER_ROLE);
		run.getParameters().add(db);
		
		conv.getNestedProtocols().add(subconv);
		
		ModelChangeUtils.pushRoleContractMapping(context, run, null);
		
		Contract c3=ModelChangeUtils.getContract(context, new Role(MY_ROLE));
		Contract c4=ModelChangeUtils.getContract(context, new Role(MY_OTHER_ROLE));
		
		if (c3 != null) {
			fail("Contract for "+MY_ROLE+" should be null");
		}
		
		if (c4 == null) {
			fail("Contract for "+MY_OTHER_ROLE+" should NOT be null");
		}
		
		Contract c5=ModelChangeUtils.getContract(context, new Role(MY_LOCATED_ROLE));
		Contract c6=ModelChangeUtils.getContract(context, new Role(MY_OTHER_LOCATED_ROLE));
		
		if (c5 != null) {
			fail("Contract for "+MY_LOCATED_ROLE+" should be null");
		}
		
		if (c6 == null) {
			fail("Contract for "+MY_OTHER_LOCATED_ROLE+" should NOT be null");
		}
		
		if (c6 != c1) {
			fail("Located role's Contract not the same as the one stored");
		}
		
		if (c4 != c2) {
			fail("Contract not the same as the one stored");
		}
	}
	
	public void testPopRoleContractMapping() {
		ModelChangeContext context=new BPELModelChangeContext(null, new DefaultFeedbackHandler());
		
		Protocol conv=new Protocol();
		Role r1=new Role();
		r1.setName(MY_LOCATED_ROLE);
		
		conv.setLocatedRole(r1);
		
		Role r2=new Role();
		r2.setName(MY_ROLE);
		
		//RoleList rl=new RoleList();
		//rl.getRoles().add(r2);
		//conv.getBlock().getContents().add(rl);
		ParameterDefinition rpd=new ParameterDefinition();
		rpd.setName(r2.getName());
		conv.getParameterDefinitions().add(rpd);
		
		Protocol subconv=new Protocol();
		subconv.setName(SUB_PROTOCOL);
		
		ParameterDefinition pd=new ParameterDefinition();
		pd.setName(MY_OTHER_ROLE);
		subconv.getParameterDefinitions().add(pd);

		Role subr1=new Role();
		subr1.setName(MY_OTHER_LOCATED_ROLE);
		subconv.setLocatedRole(subr1);
		
		Contract c1=new Contract();
		subr1.getProperties().put(Contract.class.getName(), c1);
		
		Role r3=new Role();
		r3.setName(MY_OTHER_ROLE);
		
		Contract c2=new Contract();
		r3.getProperties().put(Contract.class.getName(), c2);

		// Add contracts for subconv as if root, as normally
		// this would be done by pushing the contracts, so this
		// is just being used to setup the appropriate context
		ModelChangeUtils.addContract(context, subr1, c1);
		ModelChangeUtils.addContract(context, r3, c2);

		Run run=new Run();
		
		ProtocolReference ref=new ProtocolReference();
		ref.setName(SUB_PROTOCOL);
		ref.setRole(subr1);
		
		run.setProtocolReference(ref);
		
		conv.getBlock().getContents().add(run);
		
		Parameter db=new Parameter();
		db.setName(r2.getName());
		//db.setBoundName(MY_OTHER_ROLE);
		run.getParameters().add(db);
		
		conv.getNestedProtocols().add(subconv);
		
		ModelChangeUtils.popRoleContractMapping(context, run, null);
		
		Contract c3=ModelChangeUtils.getContract(context, new Role(MY_ROLE));
		Contract c4=ModelChangeUtils.getContract(context, new Role(MY_OTHER_ROLE));
		
		if (c3 == null) {
			fail("Contract for "+MY_ROLE+" should NOT be null");
		}
		
		if (c4 != null) {
			fail("Contract for "+MY_OTHER_ROLE+" should be null");
		}
		
		Contract c5=ModelChangeUtils.getContract(context, new Role(MY_LOCATED_ROLE));
		Contract c6=ModelChangeUtils.getContract(context, new Role(MY_OTHER_LOCATED_ROLE));
		
		if (c5 == null) {
			fail("Contract for "+MY_LOCATED_ROLE+" should NOT be null");
		}
		
		if (c6 != null) {
			fail("Contract for "+MY_OTHER_LOCATED_ROLE+" should be null");
		}
		
		if (c3 != c2) {
			fail("Contract not the same as the one stored");
		}
		
		if (c5 != c1) {
			fail("Contract not the same as the one stored");
		}
	}
}
