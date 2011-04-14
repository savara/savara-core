package org.savara.protocol.contract.generator;

import static org.junit.Assert.*;

import org.savara.protocol.contract.generator.ContractGenerator;
import org.savara.protocol.contract.generator.ContractGeneratorFactory;
import org.savara.common.task.DefaultFeedbackHandler;
import org.savara.common.task.FeedbackHandler;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;

public class ContractGeneratorTest {

	@org.junit.Test
	public void testGenerateProtocolNull() {
		
		ContractGenerator generator=ContractGeneratorFactory.getContractGenerator();
		
		try {
			Protocol conv=null;
			
			FeedbackHandler journal=new DefaultFeedbackHandler();
			
			generator.generate(conv, journal);
			
			fail("Should have thrown IllegalArgumentException");
			
		} catch(IllegalArgumentException iae) {
			// Test worked
		}
	}

	@org.junit.Test
	public void testGenerateProtocolNotLocated() {
		
		ContractGenerator generator=ContractGeneratorFactory.getContractGenerator();
		
		try {
			Protocol conv=new Protocol();
			conv.setName("NonLocatedProtocol");
			
			FeedbackHandler journal=new DefaultFeedbackHandler();
			
			generator.generate(conv, journal);
			
			fail("Should have thrown IllegalArgumentException");
			
		} catch(IllegalArgumentException iae) {
			// Test worked
		}
	}

	@org.junit.Test
	public void testGenerateConversationAndRoleNotNull2() {
		
		ContractGenerator generator=ContractGeneratorFactory.getContractGenerator();
		
		try {
			Protocol conv=new Protocol();
			conv.setRole(new Role());
			conv.setName("LocatedProtocol");
			
			FeedbackHandler journal=new DefaultFeedbackHandler();
			
			generator.generate(conv, journal);
			
		} catch(IllegalArgumentException iae) {			
			fail("Should NOT have thrown IllegalArgumentException");
		}
	}

}
