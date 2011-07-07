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
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.Role;

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
		
		java.util.List<Introduces> introduces=CDMProtocolParserUtil.getRoleDeclarations(cdlpack.getChoreographies().get(0));
		
		if (introduces.size() != 2) {
			fail("Was expecting 2 introduces clauses");
		}
		
		Introduces i1=introduces.get(0);
		Introduces i2=introduces.get(1);
		
		if (i1.getIntroducer() == null || i1.getIntroducer().getName().equals("Buyer") == false) {
			fail("First introducer should be Buyer");
		}
		
		if (i1.getRoles().size() != 1) {
			fail("First introduces should have 1 role");
		} else if (i1.getRoles().get(0).getName().equals("Broker") == false) {
			fail("First introduces should be Broker");
		}
		
		if (i2.getIntroducer() == null || i2.getIntroducer().getName().equals("Broker") == false) {
			fail("Second introducer should be Broker");
		}
		
		if (i2.getRoles().size() != 1) {
			fail("Second introduces should have 1 role");
		} else if (i2.getRoles().get(0).getName().equals("CreditAgency") == false) {
			fail("Second introduces should be CreditAgency");
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
		
		java.util.List<Introduces> introduces=CDMProtocolParserUtil.getRoleDeclarations(sub);
		
		if (introduces.size() != 1) {
			fail("Was expecting 1 introduces");
		}
		
		Introduces i1=introduces.get(0);
		
		if (i1.getIntroducer().getName().equals("Broker") == false) {
			fail("Introducer should be Broker");
		}
		
		if (i1.getRoles().size() != 1) {
			fail("Number of roles should be 1");
		}
		
		if (i1.getRoles().get(0).getName().equals("SupplierQuoteEngine") == false) {
			fail("Role should be SupplierQuoteEngine");
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
		
		java.util.List<Introduces> introduces=CDMProtocolParserUtil.getRoleDeclarations(sub);
		
		if (introduces.size() != 1) {
			fail("Was expecting 1 introduces");
		}
		
		Introduces i1=introduces.get(0);
		
		if (i1.getIntroducer().getName().equals("Broker") == false) {
			fail("Introducer should be Broker");
		}
		
		if (i1.getRoles().size() != 1) {
			fail("Number of roles should be 1");
		}
		
		if (i1.getRoles().get(0).getName().equals("SupplierTxnProcessor") == false) {
			fail("Role should be SupplierTxnProcessor");
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
		
		if (roles.size() != 1) {
			fail("Was expecting 1 role");
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
