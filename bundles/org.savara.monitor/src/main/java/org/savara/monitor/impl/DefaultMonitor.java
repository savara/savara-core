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
package org.savara.monitor.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.monitor.ConversationId;
import org.savara.monitor.ConversationResolver;
import org.savara.monitor.MonitorResult;
import org.savara.monitor.SessionStore;
import org.savara.monitor.Message;
import org.savara.monitor.Monitor;
import org.savara.protocol.ProtocolCriteria.Direction;
import org.savara.protocol.ProtocolId;
import org.savara.protocol.repository.ProtocolRepository;
import org.scribble.common.logging.CachedJournal;
import org.scribble.protocol.export.monitor.MonitorProtocolExporter;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.monitor.DefaultProtocolMonitor;
import org.scribble.protocol.monitor.DefaultSession;
import org.scribble.protocol.monitor.MonitorContext;
import org.scribble.protocol.monitor.ProtocolMonitor;
import org.scribble.protocol.monitor.Result;
import org.scribble.protocol.monitor.Session;
import org.scribble.protocol.monitor.model.Description;
import org.scribble.protocol.monitor.util.MonitorModelUtil;

/**
 * This class provides a default implementation of the
 * Montor.
 *
 */
public class DefaultMonitor implements Monitor {

	private ProtocolRepository m_protocolRepository=null;
	private SessionStore m_sessionStore=null;
	private ConversationResolver m_conversationResolver=null;
	private ProtocolMonitor m_monitor=new DefaultProtocolMonitor();
	private DescriptionCache m_descriptionCache=new DescriptionCache();
	private MonitorProtocolExporter m_exporter=new MonitorProtocolExporter();
	private	MonitorContext _context=new MonitorContextImpl();
	
	private static final Logger logger=Logger.getLogger(DefaultMonitor.class.getName());
	
	/**
	 * This method sets the monitor context.
	 * 
	 * @param context The monitor context
	 */
	public void setMonitorContext(MonitorContext context) {
		_context = context;
	}
	
	/**
	 * This method returns the monitor context.
	 * 
	 * @return The monitor context
	 */
	public MonitorContext getMonitorContext() {
		return (_context);
	}
	
	/**
	 * This method sets the protocol monitor.
	 * 
	 * @param pm The protocol monitor
	 */
	public void setProtocolMonitor(ProtocolMonitor pm) {
		m_monitor = pm;
	}
	
	/**
	 * This method sets the protocol repository to use when
	 * monitoring.
	 * 
	 * @param rep The protocol repository
	 */
	public void setProtocolRepository(ProtocolRepository rep) {
		m_protocolRepository = rep;
	}
	
	/**
	 * This method sets the session store to use when
	 * monitoring.
	 * 
	 * @param store The session store
	 */
	public void setSessionStore(SessionStore store) {
		m_sessionStore = store;
	}
	
	/**
	 * This method sets the conversation resolver to use when
	 * processing messages against the monitor.
	 * 
	 * @param resolver The conversation resolver
	 */
	public void setConversationResolver(ConversationResolver resolver) {
		m_conversationResolver = resolver;
	}
	
	/**
	 * This method is used to indicate that a message has been
	 * sent or received, and should be monitored against the
	 * specified protocol id and optional conversation instance
	 * id.
	 * 
	 * If the protocol id is not specified, then the first
	 * relevant protocol will be used. If none are found, then
	 * a null result will be returned.
	 * 
	 * If the conversation instance id is not explicitly
	 * specified, then the protocol monitor will be responsible
	 * for deriving the appropriate value.
	 * 
	 * @param pid The optional protocol id
	 * @param cid The optional conversation instance id
	 * @param mesg The message
	 * @return The monitor result, or null if a suitable protocol was not found
	 * @throws ProtocolUnknownException Unknown protocol name or role
	 * @throws IOException Failed to create or retrieve session
	 */
	public MonitorResult process(ProtocolId pid, ConversationId cid, Message mesg) {
		MonitorResult ret=null;
		
		if (m_protocolRepository == null) {
			throw new IllegalStateException("Protocol repository has not been configured");
		} else if (m_sessionStore == null) {
			throw new IllegalStateException("Session store has not been configured");
		}
		
		if (pid == null) {
			// Find the protocol ids relevant for this message
			java.util.List<ProtocolId> pids=m_protocolRepository.getProtocols(mesg);
			
			if (pids != null) {
				for (ProtocolId pi : pids) {
					Description desc=getProtocolDescription(pi);
					ConversationId lcid=cid;
					
					// Check if conversation instance id should be derived
					if (lcid == null && m_conversationResolver != null) {
						lcid = m_conversationResolver.getConversationId(desc, mesg);
					}
					
					Result result=processProtocol(pi, lcid, desc, mesg);
					
					if (result != null && result != Result.NOT_HANDLED) {
						ret = new MonitorResult(pi, lcid, result.isValid(),
								result.getReason(), result.getProperties());	
						break;
					}
				}
			} else if (logger.isLoggable(Level.FINEST)) {
				logger.finest("No protocols found for message '"+mesg+"'");
			}
		} else {
			Description desc=getProtocolDescription(pid);
			
			// Check if conversation instance id should be derived
			if (cid == null && m_conversationResolver != null) {
				cid = m_conversationResolver.getConversationId(desc, mesg);
			}
			
			Result result=processProtocol(pid, cid, desc, mesg);
			
			if (result != null && result != Result.NOT_HANDLED) {
				ret = new MonitorResult(pid, cid, result.isValid(),
						result.getReason(), result.getProperties());	
			}
		}
		
		return(ret);
	}
	
	protected Result processProtocol(ProtocolId pid, ConversationId cid, Description desc, Message mesg) {
		Result ret=null;
		
		// If conversation id not available, then must fail monitoring
		// TODO: HOW SHOULD TECHNICAL FAILURE BE REFLECTED IN ACTIVITY?
		if (cid == null) {
			logger.severe("Unable to find conversation instance id for protocol '"+pid+
					"' and message '"+mesg+"'");
			return(Result.INVALID);
		}
		
		java.io.Serializable session=m_sessionStore.find(pid, cid);
		
		boolean f_created=false;
		
		if (session == null) {

			// Try to create new session
			session = (DefaultSession)m_monitor.createSession(_context, desc, DefaultSession.class);
			
			m_sessionStore.create(pid, cid, session);

			f_created = true;
		}
		
		if (session instanceof Session) {
			// Won't specify role, as part of protocol description not
			// generally in the runtime environment - possible future
			// enhancement
			if (mesg.getDirection() == Direction.Outbound) {
				ret = m_monitor.messageSent(_context, desc, (Session)session, mesg);
			} else {
				ret = m_monitor.messageReceived(_context, desc, (Session)session, mesg);
			}
			
			// If session just created but result not handled, or session finished
			// then remove
			if ((f_created && ret == Result.NOT_HANDLED) ||
						((Session)session).isFinished()) {
				m_sessionStore.remove(pid, cid);
			} else {
				m_sessionStore.update(pid, cid, session);
			}
			
		} else {
			logger.severe("Inappropriate session type returned");
			ret = Result.NOT_HANDLED;
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the protocol's monitorable description.
	 * 
	 * @param pid The protocol id
	 * @return The description
	 */
	protected Description getProtocolDescription(ProtocolId pid) {
		Description ret=getDescriptionCache().getDescription(pid);
		
		if (ret == null) {
			try {
				ProtocolModel pm=m_protocolRepository.getProtocol(pid);
			
				// Convert protocol model to monitoring description
				CachedJournal journal=new CachedJournal();
				java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
				
				m_exporter.export(pm, journal, os);
				
				os.close();
				
				if (journal.hasErrors()) {
					logger.severe("Errors detected when exporting protocol '"+
									pid+"' to monitorable description");
				} else {
					java.io.InputStream is=new java.io.ByteArrayInputStream(os.toByteArray());
					
					ret = MonitorModelUtil.deserialize(is);
					
					is.close();
				}
			} catch(Exception e) {
				logger.log(Level.SEVERE,
						"Failed to obtain monitorable description for protocol '"+
								pid+"'", e);
			}
			
			if (ret != null) {
				getDescriptionCache().setDescription(pid, ret);
			}
		}
		
		return(ret);
	}
	
	protected DescriptionCache getDescriptionCache() {
		return(m_descriptionCache);
	}
	
	protected void setDescriptionCache(DescriptionCache dc) {
		m_descriptionCache = dc;
	}
}
