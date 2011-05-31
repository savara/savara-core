/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package test;

import junit.framework.Assert;

import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.node.Node;
import org.apache.tuscany.sca.node.NodeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.savara.scenario.simulator.sca.ServiceInvoker;
import org.savara.scenario.simulator.sca.ServiceStore;

import simsample.test.service.TestService;

public class WSBindingTestCase {

    private static Node node;
    
    @Test
    public void testDynamicInvoke() {    	
		ServiceInvoker invoker=ServiceStore.getService("http://localhost:8080/TestServiceComponent");
	
		if (invoker == null) {
			Assert.fail("Failed to get invoker");
		}
	
		org.apache.tuscany.sca.core.invocation.impl.MessageImpl msg=new org.apache.tuscany.sca.core.invocation.impl.MessageImpl();
		
		String opName="call";
		
        Operation operation = null;
        for (InvocationChain invocationChain : invoker.getEndpoint().getInvocationChains()) {
            if (opName.equals(invocationChain.getSourceOperation().getName())) {
                operation = invocationChain.getSourceOperation();
                break;
            }
        }
        if (operation == null) {
            Assert.fail("Operation not found: " + opName);
        }

        msg.setOperation(operation);
        msg.setBody(new Object[]{"something"});
		
		Message resp=invoker.invoke(msg);
		
		if (resp == null) {
			Assert.fail("Response is null");
		} else if (resp.getBody() instanceof Exception) {
			((Exception)resp.getBody()).printStackTrace();
			Assert.fail("Response is an exception: "+resp.getBody());
		} else {
			Assert.assertEquals("hello", resp.getBody());
		}
    }

    @Test
    public void testStaticInvoke() {    	
        TestService service = node.getService(TestService.class, "TestServiceComponent/TestService");
        
        String result=service.call("boo");
        
        Assert.assertEquals("hello", result);
    }

    @BeforeClass
    public static void init() throws Exception {
    	try {
    		//synchronized(WSBindingTestCase.class) {
    		//	WSBindingTestCase.class.wait(1000);
    		//}
        node = NodeFactory.newInstance().createNode("simsample.composite").start();
        
        Node n=node;
        System.out.println("NODE="+node);
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    @AfterClass
    public static void destroy() throws Exception {
        if (node != null) {
            node.stop();
        }
    }

}
