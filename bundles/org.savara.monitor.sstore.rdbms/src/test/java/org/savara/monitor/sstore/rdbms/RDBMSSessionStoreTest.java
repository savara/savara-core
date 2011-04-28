/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.monitor.sstore.rdbms;

import java.io.Serializable;
import java.sql.DriverManager;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.savara.monitor.ConversationInstanceId;
import org.savara.protocol.ProtocolId;

/**
 * 
 * 
 * @author Jeff Yu
 *
 */
public class RDBMSSessionStoreTest extends Assert{
	
	private static RDBMSSessionStore store;
	
	private static ProtocolId pid;
	
	private static DummySession session;
	
	@BeforeClass
	public static void setUp() throws Exception{
		Class.forName("org.h2.Driver");
		DriverManager.getConnection("jdbc:h2:target/db/h2", "sa", "");
				
		store = new RDBMSSessionStore();		
		store.setConfiguration(null);
		
		pid = new ProtocolId("name", "role");
		
		session = new DummySession();
		session.setDescription("DummySession");
	}
	
	
	@AfterClass
	public static void afterClass() throws Exception {
		store.close();
	}
	
	@Test
	public void testCreateNewSession() throws Exception {		
		ConversationInstanceId cid = new ConversationInstanceId("1");
		Serializable o = store.create(pid, cid, session);
		assertEquals("DummySession", ((DummySession)o).getDescription());
	}
	
	@Test
	public void testCreateDuplicatedSession() throws Exception {
		ConversationInstanceId cid = new ConversationInstanceId("11");
		store.create(pid, cid, session);
		DummySession session2 = new DummySession();
		
		try {
			store.create(pid, cid, session2);
			fail ("here should not be executed");
		} catch (IllegalArgumentException e) {
			// expected exception
		}
	}
	
	@Test
	public void testFindSession() throws Exception {
		ConversationInstanceId cid = new ConversationInstanceId("2");
		store.create(pid, cid, session);
		Serializable o = store.find(pid, cid);
		assertEquals("DummySession", ((DummySession)o).getDescription() );
	}
	
	@Test
	public void testUpdateSession() throws Exception {
		ConversationInstanceId cid = new ConversationInstanceId("3");
		DummySession ds = new DummySession();
		ds.setDescription("first one");
		store.create(pid, cid, ds);
		ds.setDescription("Updated Session");
		store.update(pid, cid, ds);
		Serializable o = store.find(pid, cid);
		assertEquals("Updated Session", ((DummySession)o).getDescription() );
	}
	
	@Test
	public void testDeleteSession() throws Exception {
		ConversationInstanceId cid = new ConversationInstanceId("4");
		store.create(pid, cid, session);
		store.remove(pid, cid);
		Serializable o = store.find(pid, cid);
		assertNull(o);
	}

}
