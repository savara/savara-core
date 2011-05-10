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
package org.savara.pi4soa.cdm.parser.rules;

import static org.junit.Assert.*;

import org.pi4soa.cdl.CDLManager;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Parallel;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.When;

public class CDMProtocolParserUtilTest {

	@org.junit.Test
	public void testGetRoleDeclarationsTopChoreo() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleDeclarations(cdlpack.getChoreographies().get(0));
		
		if (roles.size() != 4) {
			fail("Was expecting 4 roles");
		}
		
		if (roles.contains(new Role("Broker")) == false) {
			fail("Broker not found");
		}
		
		if (roles.contains(new Role("Buyer")) == false) {
			fail("Buyer not found");
		}
		
		if (roles.contains(new Role("CreditAgency")) == false) {
			fail("CreditAgency not found");
		}
		
		// Maybe only until the search is refined to used participant types
		if (roles.contains(new Role("Supplier")) == false) {
			fail("Supplier not found");
		}
	}

	@org.junit.Test
	public void testGetRoleDeclarationsSubChoreo1() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		org.pi4soa.cdl.Choreography top=cdlpack.getChoreographies().get(0);
		
		org.pi4soa.cdl.Choreography sub=top.getEnclosedChoreography("RequestForQuote");
		
		if (sub == null) {
			fail("Failed to get 'RequestForQuote' sub-choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleDeclarations(sub);
		
		if (roles.size() != 1) {
			fail("Was expecting 1 roles");
		}
		
		if (roles.contains(new Role("SupplierQuoteEngine")) == false) {
			fail("SupplierQuoteEngine not found");
		}
	}

	@org.junit.Test
	public void testGetRoleDeclarationsSubChoreo2() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		org.pi4soa.cdl.Choreography top=cdlpack.getChoreographies().get(0);
		
		org.pi4soa.cdl.Choreography sub=top.getEnclosedChoreography("CompleteTransaction");
		
		if (sub == null) {
			fail("Failed to get 'CompleteTransaction' sub-choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleDeclarations(sub);
		
		if (roles.size() != 1) {
			fail("Was expecting 1 roles");
		}
		
		if (roles.contains(new Role("SupplierTxnProcessor")) == false) {
			fail("SupplierTxnProcessor not found");
		}
	}

	@org.junit.Test
	public void testGetRoleParametersTopChoreo() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(cdlpack.getChoreographies().get(0));
		
		if (roles.size() != 0) {
			fail("Was expecting 0 roles");
		}
	}

	@org.junit.Test
	public void testGetRoleParametersSubChoreo1() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		org.pi4soa.cdl.Choreography top=cdlpack.getChoreographies().get(0);
		
		org.pi4soa.cdl.Choreography sub=top.getEnclosedChoreography("RequestForQuote");
		
		if (sub == null) {
			fail("Failed to get 'RequestForQuote' sub-choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(sub);
		
		if (roles.size() != 1) {
			fail("Was expecting 1 roles");
		}
		
		if (roles.contains(new Role("Broker")) == false) {
			fail("Broker not found");
		}
	}

	@org.junit.Test
	public void testGetRoleParametersSubChoreo2() {
		org.pi4soa.cdl.Package cdlpack=null;
		
		try {
			cdlpack = CDLManager.load("testmodels/cdm/ESBBroker.cdm");			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load choreography: "+e);
		}
		
		if (cdlpack.getChoreographies().size() != 1) {
			fail("Expecting only one top level choreo");
		}
		
		org.pi4soa.cdl.Choreography top=cdlpack.getChoreographies().get(0);
		
		org.pi4soa.cdl.Choreography sub=top.getEnclosedChoreography("CompleteTransaction");
		
		if (sub == null) {
			fail("Failed to get 'CompleteTransaction' sub-choreo");
		}
		
		java.util.List<Role> roles=CDMProtocolParserUtil.getRoleParameters(sub);
		
		if (roles.size() != 3) {
			fail("Was expecting 3 roles");
		}
		
		if (roles.contains(new Role("Broker")) == false) {
			fail("Broker not found");
		}
		
		if (roles.contains(new Role("Buyer")) == false) {
			fail("Buyer not found");
		}
		
		if (roles.contains(new Role("CreditAgency")) == false) {
			fail("CreditAgency not found");
		}
	}
	
	@org.junit.Test
	public void testGetEnclosingBlockSingleActivities() {
		Protocol p=new Protocol();
		
		Role r0=new Role();
		r0.setName("r0");
		
		Interaction i0=new Interaction();
		i0.setFromRole(r0);
		p.getBlock().add(i0);
		
		Role r1=new Role();
		r1.setName("r1");
		
		Role r2=new Role();
		r2.setName("r2");
		
		Parallel par=new Parallel();
		p.getBlock().add(par);
		
		Block parb1=new Block();
		par.getBlocks().add(parb1);
		
		Interaction i1=new Interaction();
		i1.setFromRole(r1);
		
		parb1.add(i1);
		
		Block parb2=new Block();
		par.getBlocks().add(parb2);
		
		Choice choice1=new Choice();
		parb2.add(choice1);
		
		When w1=new When();
		choice1.getWhens().add(w1);
		
		Block wb1=new Block();
		w1.setBlock(wb1);
		
		Interaction i2=new Interaction();
		i2.setFromRole(r2);
		
		wb1.add(i2);
		
		// Check results
		Block b0=CDMProtocolParserUtil.getEnclosingBlock(p, r0);
		
		if (b0 == null) {
			fail("b0 is null");
		}
		
		if (b0 != p.getBlock()) {
			fail("b0 not protocol block");
		}
		
		Block b1=CDMProtocolParserUtil.getEnclosingBlock(p, r1);
		
		if (b1 == null) {
			fail("b1 is null");
		}
		
		if (b1 != parb1) {
			fail("b1 not parallel block");
		}
		
		Block b2=CDMProtocolParserUtil.getEnclosingBlock(p, r2);
		
		if (b2 == null) {
			fail("b2 is null");
		}
		
		if (b2 != wb1) {
			fail("b2 not when block");
		}
	}
	
	@org.junit.Test
	public void testGetEnclosingBlockMultiActivities() {
		Protocol p=new Protocol();
		
		Role r0=new Role();
		r0.setName("r0");
		
		Interaction i0=new Interaction();
		i0.setFromRole(r0);
		p.getBlock().add(i0);
		
		Role r1=new Role();
		r1.setName("r1");
		
		Role r2=new Role();
		r2.setName("r2");
		
		Parallel par=new Parallel();
		p.getBlock().add(par);
		
		Block parb1=new Block();
		par.getBlocks().add(parb1);
		
		Interaction i11=new Interaction();
		i11.setFromRole(r1);
		
		parb1.add(i11);
		
		Block parb2=new Block();
		par.getBlocks().add(parb2);
		
		Interaction i12=new Interaction();
		i12.setFromRole(r1);
		
		parb2.add(i12);
		
		Choice choice1=new Choice();
		parb2.add(choice1);
		
		When w1=new When();
		choice1.getWhens().add(w1);
		
		Block wb1=new Block();
		w1.setBlock(wb1);
		
		Interaction i21=new Interaction();
		i21.setFromRole(r2);
		
		wb1.add(i21);
		
		When w2=new When();
		choice1.getWhens().add(w2);
		
		Block wb2=new Block();
		w2.setBlock(wb2);
		
		Interaction i22=new Interaction();
		i22.setFromRole(r2);
		
		wb2.add(i22);
		
		// Check results
		Block b0=CDMProtocolParserUtil.getEnclosingBlock(p, r0);
		
		if (b0 == null) {
			fail("b0 is null");
		}
		
		if (b0 != p.getBlock()) {
			fail("b0 not protocol block");
		}
		
		Block b1=CDMProtocolParserUtil.getEnclosingBlock(p, r1);
		
		if (b1 == null) {
			fail("b1 is null");
		}
		
		if (b1 != p.getBlock()) {
			fail("b1 not parallel parent block (i.e. protocol block)");
		}
		
		Block b2=CDMProtocolParserUtil.getEnclosingBlock(p, r2);
		
		if (b2 == null) {
			fail("b2 is null");
		}
		
		if (b2 != parb2) {
			fail("b2 not parallel block containing choice");
		}
	}
}
