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
package org.savara.monitor.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.monitor.ConversationId;
import org.savara.monitor.internal.InMemorySessionStore;
import org.savara.protocol.ProtocolId;
import org.scribble.protocol.monitor.DefaultSession;

public class InMemorySessionStoreTest {

	@Test
	public void testCreateSessionNoConversationInstanceId() {
		InMemorySessionStore store=new InMemorySessionStore();
		
		try {
			store.create(new ProtocolId("name","role"), null, null);
			
			fail("Should have thrown IllegalArgumentException");
		} catch(IllegalArgumentException iae) {
			// Expected
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testCreateSessionNoProtocolId() {
		InMemorySessionStore store=new InMemorySessionStore();
		
		try {
			store.create(null, new ConversationId("id"), null);
			
			fail("Should have thrown IllegalArgumentException");
		} catch(IllegalArgumentException iae) {
			// Expected
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testCreateSessionDuplicateInvalidId() {
		InMemorySessionStore store=new InMemorySessionStore();
		DefaultSession session = new DefaultSession();
		try {
			ProtocolId pid=new ProtocolId("p","r");
			ConversationId id=new ConversationId("1");
			
			try {
				store.create(pid, id, session);
			} catch(Exception inner) {
				fail("Should not have failed");
			}
			
			store.create(pid, id, session);
			
			fail("Should have thrown IllegalArgumentException");
		} catch(IllegalArgumentException iae) {
			// Expected
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testCreateSession() {
		InMemorySessionStore store=new InMemorySessionStore();
		DefaultSession s = new DefaultSession();
		try {
			ProtocolId pid=new ProtocolId("p","r");
			ConversationId id=new ConversationId("1");

			if (store.create(pid, id, s) == null) {
				fail("No context created");
			}

		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testRemoveSession() {
		InMemorySessionStore store=new InMemorySessionStore();
		DefaultSession s = new DefaultSession();
		try {
			ProtocolId pid=new ProtocolId("p","r");
			ConversationId id=new ConversationId("1");

			if (store.create(pid, id, s) == null) {
				fail("No context created");
			}
			
			java.io.Serializable c1=store.find(pid, id);
			
			if (c1 == null) {
				fail("Should not be null");
			}
			
			store.remove(pid, id);
			
			if (store.find(pid, id) != null) {
				fail("Should not find the session");
			}
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testFindSession() {
		InMemorySessionStore store=new InMemorySessionStore();
		DefaultSession s = new DefaultSession();
		
		try {
			ProtocolId pid=new ProtocolId("p","r");
			ConversationId id1=new ConversationId("1");

			java.io.Serializable c1=null;
			
			if ((c1=store.create(pid, id1, s)) == null) {
				fail("No context created for id 1");
			}
			
			ConversationId id2=new ConversationId("2");

			java.io.Serializable c2=null;
			
			if ((c2=store.create(pid, id2, s)) == null) {
				fail("No context created for id 2");
			}
			
			java.io.Serializable result=store.find(pid, id1);

			if (result == null) {
				fail("Result is null");
			}
			
			if (result != c1) {
				fail("Conversation 1 was not returned");
			}
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}

	@Test
	public void testUpdateSession() {
		InMemorySessionStore store=new InMemorySessionStore();
		DefaultSession s = new DefaultSession();
		
		try {
			ProtocolId pid=new ProtocolId("p","r");
			ConversationId id1=new ConversationId("1");

			java.io.Serializable c1=null;
			
			if ((c1=store.create(pid, id1, s)) == null) {
				fail("No session created for id 1");
			}
			
			ConversationId id2=new ConversationId("2");

			java.io.Serializable c2=null;
			
			if ((c2=store.create(pid, id2, s)) == null) {
				fail("No session created for id 2");
			}
			
			// Update context for id1 with context for id2
			store.update(pid, id1, c2);
			
			java.io.Serializable result=store.find(pid, id1);

			if (result == null) {
				fail("Result is null");
			}
			
			if (result != c2) {
				fail("Session 2 was not returned");
			}
		} catch(Exception e) {
			fail("Not expecting: "+e);
		}
	}
}
