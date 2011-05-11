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
}
