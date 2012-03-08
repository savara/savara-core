/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.monitor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.monitor.DefautMonitorContext;
import org.savara.monitor.Message;
import org.scribble.protocol.monitor.Result;
import org.scribble.protocol.monitor.model.Annotation;
import org.scribble.protocol.monitor.model.MessageNode;

public class DefaultMonitorContextTest {

	@Test
	public void testValidateNoFaultValid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Message mesg=new Message();
		mesg.setOperator("buy");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (result != Result.VALID) {
			fail("Should be valid");
		}
	}

	@Test
	public void testValidateNoFaultInvalid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Message mesg=new Message();
		mesg.setOperator("buyX");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (result == null) {
			fail("Result not returned");
		}
		
		if (result.isValid()) {
			fail("Should be invalid");
		}
	}
	
	@Test
	public void testValidateFaultValid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Annotation ann=new Annotation();
		mesgNode.getAnnotation().add(ann);
		ann.setId("Id");
		ann.setValue(" Fault(name=BuyFailed) ");
		
		Message mesg=new Message();
		mesg.setOperator("buy");
		mesg.setFault("BuyFailed");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (result != Result.VALID) {
			fail("Should be valid");
		}
	}
	
	@Test
	public void testValidateNodeFaultInvalid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Annotation ann=new Annotation();
		mesgNode.getAnnotation().add(ann);
		ann.setId("Id");
		ann.setValue(" Fault(name=BuyFailed) ");
		
		Message mesg=new Message();
		mesg.setOperator("buy");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (result.isValid()) {
			fail("Should be invalid");
		}
	}
	
	@Test
	public void testValidateMessageFaultInvalid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Message mesg=new Message();
		mesg.setOperator("buy");
		mesg.setFault("BuyFailed");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (result.isValid()) {
			fail("Should be invalid");
		}
	}
	
	@Test
	public void testValidateMessageFaultEmptyValid() {
		DefautMonitorContext context=new DefautMonitorContext();
		
		MessageNode mesgNode=new MessageNode();
		mesgNode.setOperator("buy");
		
		Message mesg=new Message();
		mesg.setOperator("buy");
		mesg.setFault("");
		
		Result result=context.validate(null, mesgNode, mesg);
		
		if (!result.isValid()) {
			fail("Should be valid");
		}
	}
}
