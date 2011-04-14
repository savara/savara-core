/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.savara.protocol.contract.generator.impl;

import org.savara.protocol.contract.generator.impl.ContractIntrospector;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.task.DefaultFeedbackHandler;
import org.savara.common.task.FeedbackHandler;
import org.savara.contract.model.Contract;
import org.savara.contract.model.Interface;
import org.savara.contract.model.MessageExchangePattern;
import org.savara.contract.model.OneWayRequestMEP;
import org.savara.contract.model.RequestResponseMEP;
import org.savara.contract.model.Type;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.ProtocolReference;
import org.scribble.protocol.model.Run;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeReference;

import static org.junit.Assert.*;

public class ContractIntrospectorTest {

	private static final String MY_ROLE = "myRole";
	//private static final String TYPE_NS = "typeNS";
	private static final String TYPE_LP = "typeLP";
	private static final String OP_NAME = "opName";

	@org.junit.Test
	public void testNoSubProtocolWithoutRun() {
		Role r=new Role();
		r.setName("role");
		
		Protocol top=new Protocol();
		top.setName("top");
		top.setRole(r);
		
		Protocol mid=new Protocol();
		mid.setName("mid");
		mid.setRole(r);
		
		Protocol sub=new Protocol();
		sub.setName("sub");
		sub.setRole(r);
		
		ProtocolReference ref=new ProtocolReference();
		ref.setName("sub");
		ref.setRole(r);
		
		Run run=new Run();
		run.setProtocolReference(ref);
		
		top.getBlock().getContents().add(mid);
		
		mid.getBlock().getContents().add(run);
		mid.getBlock().getContents().add(sub);
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		ContractIntrospector introspector=new ContractIntrospector(top, null, r, handler);
		
		introspector.process();
		
		if (introspector.getProcessedProtocols().size() != 0) {
			fail("Expecting 0 processed protocol, but got: "+introspector.getProcessedProtocols().size());
		}
	}
	
	@org.junit.Test
	public void testSubProtocolWithRun() {
		Role r=new Role();
		r.setName("role");
		
		Protocol top=new Protocol();
		top.setName("top");
		top.setRole(r);
		
		Protocol mid=new Protocol();
		mid.setName("mid");
		mid.setRole(r);
		
		Protocol sub=new Protocol();
		sub.setName("sub");
		sub.setRole(r);
		
		ProtocolReference ref1=new ProtocolReference();
		ref1.setName("mid");
		ref1.setRole(r);
		
		Run run1=new Run();
		run1.setProtocolReference(ref1);
		
		top.getBlock().getContents().add(run1);
		top.getBlock().getContents().add(mid);
		
		ProtocolReference ref2=new ProtocolReference();
		ref2.setName("sub");
		ref2.setRole(r);
		
		Run run2=new Run();
		run2.setProtocolReference(ref2);
		
		mid.getBlock().getContents().add(run2);
		mid.getBlock().getContents().add(sub);
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		ContractIntrospector introspector=new ContractIntrospector(top, null, r, handler);
		
		introspector.process();
		
		if (introspector.getProcessedProtocols().size() != 2) {
			fail("Expecting 2 processed protocol, but got: "+introspector.getProcessedProtocols().size());
		}
		
		if (introspector.getProcessedProtocols().contains(mid) == false) {
			fail("Should contain mid");
		}
		
		if (introspector.getProcessedProtocols().contains(sub) == false) {
			fail("Should contain sub");
		}
	}
	
	@org.junit.Test
	public void testCreateSingleInterface() {
		Protocol protocol=new Protocol();
		Role role=new Role();
		role.setName(MY_ROLE);
		protocol.setRole(role);
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		ContractIntrospector introspector=new ContractIntrospector(protocol, null, role, handler);
		
		if (introspector.getContract().getInterfaces().size() != 0) {
			fail("Should be 0 interfaces: "+introspector.getContract().getInterfaces().size());
		}
		
		Interface intf=introspector.getInterface(null, null);
		
		if (intf == null) {
			fail("Interface not created");
		}
		
		if (introspector.getContract().getInterfaces().size() != 1) {
			fail("Should be 1 interface: "+introspector.getContract().getInterfaces().size());
		}
		
		Interface intf2=introspector.getInterface(null, null);
		
		if (intf2 != intf) {
			fail("Interfaces are different");
		}
		
		if (introspector.getContract().getInterfaces().size() != 1) {
			fail("Should still only be 1 interface: "+introspector.getContract().getInterfaces().size());
		}
	}
	
	@org.junit.Test
	public void testVisitInteractionOneWayRequestRPC() {
		Protocol protocol=new Protocol();
		Role role=new Role();
		role.setName(MY_ROLE);
		protocol.setRole(role);
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		ContractIntrospector introspector=new ContractIntrospector(protocol, null, role, handler);
		
		Interaction interaction=new Interaction();
		
		MessageSignature msig=new MessageSignature();
		msig.setOperation(OP_NAME);
		
		TypeReference tref=new TypeReference();
		tref.setName(TYPE_LP);
		//tref.setNamespace(TYPE_NS);
		msig.getTypeReferences().add(tref);
		
		interaction.setMessageSignature(msig);
		interaction.setFromRole(new Role());
		
		protocol.getBlock().add(interaction);
		
		introspector.accept(interaction);
		
		Contract contract=introspector.getContract();
		
		Interface intf=contract.getInterface(role.getName());
		
		if (intf == null) {
			fail("Interface '"+role.getName()+"' not found");
		}
		
		MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(OP_NAME);
		
		if (mep == null) {
			fail("Operation '"+OP_NAME+"' not found");
		}
		
		if ((mep instanceof OneWayRequestMEP) == false) {
			fail("Not a oneway request");
		}
		
		if (mep.getTypes().size() != 1) {
			fail("One type expected, but got: "+mep.getTypes().size());
		}
		
		Type t=mep.getTypes().get(0);
		
		if (t.getName().equals(TYPE_LP) == false) {
			fail("Type name not correct: "+t.getName());
		}
	}
	
	
	@org.junit.Test
	public void testVisitInteractionRequestResponseRPCOnlyRequest() {
		Protocol protocol=new Protocol();
		Role role=new Role();
		role.setName(MY_ROLE);
		protocol.setRole(role);
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		ContractIntrospector introspector=new ContractIntrospector(protocol, null, role, handler);
		
		Interaction interaction=new Interaction();
		
		MessageSignature msig=new MessageSignature();
		msig.setOperation(OP_NAME);
		
		TypeReference tref=new TypeReference();
		tref.setName(TYPE_LP);
		//tref.setNamespace(TYPE_NS);
		msig.getTypeReferences().add(tref);
		
		interaction.setMessageSignature(msig);
		interaction.setFromRole(new Role());
		
		Annotation annotation=new Annotation(AnnotationDefinitions.CORRELATION);
		annotation.getProperties().put(AnnotationDefinitions.REQUEST_PROPERTY, "label");
		interaction.getAnnotations().add(annotation);
		
		protocol.getBlock().add(interaction);
		
		introspector.accept(interaction);
		
		Contract contract=introspector.getContract();
		
		Interface intf=contract.getInterface(role.getName());
		
		if (intf == null) {
			fail("Interface '"+role.getName()+"' not found");
		}
		
		MessageExchangePattern mep=intf.getMessageExchangePatternForOperation(OP_NAME);
		
		if (mep == null) {
			fail("Operation '"+OP_NAME+"' not found");
		}
		
		if ((mep instanceof RequestResponseMEP) == false) {
			fail("Not a request/response MEP");
		}
		
		if (mep.getTypes().size() != 1) {
			fail("One type expected, but got: "+mep.getTypes().size());
		}
		
		Type t=mep.getTypes().get(0);
		
		if (t.getName().equals(TYPE_LP) == false) {
			fail("Type name not correct: "+t.getName());
		}
	}
}
