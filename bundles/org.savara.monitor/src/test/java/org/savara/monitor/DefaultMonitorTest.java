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
package org.savara.monitor;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Test;
import org.savara.monitor.ConversationId;
import org.savara.monitor.DefaultMonitor;
import org.savara.monitor.InMemorySessionStore;
import org.savara.monitor.Message;
import org.savara.protocol.ProtocolId;
import org.savara.protocol.repository.InMemoryProtocolRepository;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.monitor.DefaultSession;
import org.scribble.protocol.monitor.ProtocolMonitorFactory;

public class DefaultMonitorTest {

	@Test
	public void testNoProtocolRepository() {
		DefaultMonitor mon=new DefaultMonitor();
		
		Message mesg=new Message();
		
		ProtocolId pid=new ProtocolId("name", "role");
		
		ConversationId cid=new ConversationId("cid");
		
		mon.setProtocolMonitor(ProtocolMonitorFactory.createProtocolMonitor());
		mon.setSessionStore(new InMemorySessionStore());
		
		try {
			mon.process(pid, cid, mesg);
			fail("Should have generated illegal state exception");
		} catch(IllegalStateException ise) {
			// Expected
		} catch(Exception e) {
			fail("Unexpected: "+e);
		}
	}

	@Test
	public void testNoSessionStore() {
		DefaultMonitor mon=new DefaultMonitor();
		
		Message mesg=new Message();
		
		ProtocolId pid=new ProtocolId("name", "role");
		
		ConversationId cid=new ConversationId("cid");
		
		mon.setProtocolMonitor(ProtocolMonitorFactory.createProtocolMonitor());
		
		InMemoryProtocolRepository rep=new InMemoryProtocolRepository();
		mon.setProtocolRepository(rep);
		
		try {
			mon.process(pid, cid, mesg);
			fail("Should have generated illegal state exception");
		} catch(IllegalStateException ise) {
			// Expected
		} catch(Exception e) {
			fail("Unexpected: "+e);
		}
	}

	@Test
	public void testProcessMessage() {
		DefaultMonitor mon=new DefaultMonitor();
		
		Message mesg=new Message();
		
		ProtocolId pid=new ProtocolId("name", "role");
		
		ConversationId cid=new ConversationId("cid");
		
		mon.setProtocolMonitor(ProtocolMonitorFactory.createProtocolMonitor());
		
		InMemoryProtocolRepository rep=new InMemoryProtocolRepository();
		rep.addProtocol(pid, new ProtocolModel());
		mon.setProtocolRepository(rep);

		InMemorySessionStore store=new InMemorySessionStore();		
		mon.setSessionStore(store);
		
		try {
			mon.process(pid, cid, mesg);
		} catch(Exception e) {
			fail("Unexpected: "+e);
		}
	}

	@Test
	public void testProcessMessageWithExistingSession() {
		DefaultMonitor mon=new DefaultMonitor();
		
		Message mesg=new Message();
		
		ProtocolId pid=new ProtocolId("name", "role");
		
		ConversationId cid=new ConversationId("cid");
		
		mon.setProtocolMonitor(ProtocolMonitorFactory.createProtocolMonitor());
		
		InMemoryProtocolRepository rep=new InMemoryProtocolRepository();
		rep.addProtocol(pid, new ProtocolModel());
		mon.setProtocolRepository(rep);

		InMemorySessionStore store=new InMemorySessionStore();
		
		DefaultSession s1=new DefaultSession();
		
		try {
			store.create(pid, cid, s1);
			store.update(pid, cid, s1);
		} catch(Exception e) {
			fail("Failed: "+e);
		}
		
		mon.setSessionStore(store);
		
		try {
			mon.process(pid, cid, mesg);
		} catch(Exception e) {
			fail("Unexpected: "+e);
		}
	}

	@Test
	public void testSingleMessageSession() {
		DefaultMonitor mon=new DefaultMonitor();
		
		Message mesg=new Message();
		
		ProtocolId pid=new ProtocolId("name", "role");
		
		ConversationId cid=new ConversationId("cid");
		
		mon.setProtocolMonitor(ProtocolMonitorFactory.createProtocolMonitor());
		
		InMemoryProtocolRepository rep=new InMemoryProtocolRepository();
		rep.addProtocol(pid, new ProtocolModel());
		mon.setProtocolRepository(rep);
		
		TestInMemorySessionStore store=new TestInMemorySessionStore();
				
		mon.setSessionStore(store);
		
		try {
			mon.process(pid, cid, mesg);
		} catch(Exception e) {
			fail("Unexpected: "+e);
		}
		
		if (store.f_created == true) {
			fail("Session should not be created after single message");
		}
	}
	
	public class TestInMemorySessionStore extends InMemorySessionStore {
		
		public boolean f_created=false;
		public boolean f_removed=false;
		
		@Override
		public java.io.Serializable create(ProtocolId pid, ConversationId cid, Serializable session) {
		
			DefaultSession s1=new DefaultSession() {
				public boolean isFinished() {
					return(true);
				}
			};
			
			addSession(pid, cid, s1);
			
			f_created = true;
			
			return(s1);
		}

		public void remove(ProtocolId pid, ConversationId cid) {
			f_removed = true;
		}
	};
			
}

