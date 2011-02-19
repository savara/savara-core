package org.savara.protocol.contract.generator;

import static org.junit.Assert.*;

import org.savara.protocol.contract.generator.ContractGenerator;
import org.savara.protocol.contract.generator.ContractGeneratorFactory;
import org.scribble.common.logging.CachedJournal;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.Role;

public class ContractGeneratorTest {

	@org.junit.Test
	public void testGenerateProtocolNull() {
		
		ContractGenerator generator=ContractGeneratorFactory.getContractGenerator();
		
		try {
			Protocol conv=null;
			
			Journal journal=new CachedJournal();
			
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
			
			Journal journal=new CachedJournal();
			
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
			
			Journal journal=new CachedJournal();
			
			generator.generate(conv, journal);
			
		} catch(IllegalArgumentException iae) {			
			fail("Should NOT have thrown IllegalArgumentException");
		}
	}

}
