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
package org.savara.scenario.simulator.scribble;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.protocol.util.ProtocolServices;
import org.savara.scenario.model.Event;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.ReceiveEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.SendEvent;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.SimulationContext;
import org.savara.scenario.simulation.SimulationHandler;
import org.savara.scenario.simulation.SimulationModel;
import org.scribble.common.logging.CachedJournal;
import org.scribble.common.resource.ByteArrayContent;
import org.scribble.common.resource.Content;
import org.scribble.protocol.DefaultProtocolContext;
import org.scribble.protocol.export.monitor.MonitorProtocolExporter;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.monitor.DefaultSession;
import org.scribble.protocol.monitor.MonitorContext;
import org.scribble.protocol.monitor.ProtocolMonitor;
import org.scribble.protocol.monitor.ProtocolMonitorFactory;
import org.scribble.protocol.monitor.Result;
import org.scribble.protocol.monitor.Session;
import org.scribble.protocol.monitor.model.Description;
import org.scribble.protocol.monitor.util.MonitorModelUtil;

/**
 * This class provides the scribble implementation of the Role Simulator interface.
 *
 */
public class ScribbleRoleSimulator implements RoleSimulator {

	private static Logger LOG=Logger.getLogger(ScribbleRoleSimulator.class.getName());
	
	private static final String PROTOCOL_SIMULATOR = "Protocol simulator";

	private ProtocolMonitor _monitor=ProtocolMonitorFactory.createProtocolMonitor();
	private MonitorProtocolExporter _exporter=new MonitorProtocolExporter();
	private MonitorContext _context=new MonitorContextImpl();
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return PROTOCOL_SIMULATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(SimulationContext context) throws Exception {
		context.getProperties().put(Session.class.getName(), _monitor.createSession(_context,
					(Description)context.getModel(), DefaultSession.class));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSupported(SimulationModel model) {
		Content content=new ByteArrayContent(model.getName(), null);
		
		return (ProtocolServices.getParserManager().isParserAvailable(content));
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModel(SimulationModel model) {
		Object ret=null;
		
		try {
			java.io.InputStream is=model.getContents();

			byte[] b=new byte[is.available()];
			is.read(b);
			
			is.close();
			
			Content content=new ByteArrayContent(model.getName(), b);
			
			CachedJournal journal=new CachedJournal();
			
			ret = ProtocolServices.getParserManager().parse(null, content, journal);
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Failed to get model", e);
		}
		
		return (ret);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Role> getModelRoles(Object model) {
		if (model instanceof ProtocolModel) {
			java.util.List<Role> ret=new java.util.Vector<Role>();
			
			if (((ProtocolModel)model).isLocated()) {
				Role role=new Role();
				role.setName(((ProtocolModel)model).getProtocol().getLocatedRole().getName());
				ret.add(role);
			} else {
				for (org.scribble.protocol.model.Role r : ((ProtocolModel)model).getRoles()) {
					Role role=new Role();
					role.setName(r.getName());
					ret.add(role);
				}
			}
			
			return (ret);
		}
		
		return (Collections.emptyList());
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModelForRole(Object model, Role role) {
		Object ret=null;
		
		DefaultProtocolContext context=
				new DefaultProtocolContext(ProtocolServices.getParserManager(),
						null);

		CachedJournal journal=new CachedJournal();

		ProtocolModel local=null;
		
		if (((ProtocolModel)model).isLocated()) {
			if (((ProtocolModel)model).getProtocol().getLocatedRole().getName().equals(role.getName())) {
				local = (ProtocolModel)model;
			}
		} else {
			local = ProtocolServices.getProtocolProjector().project(context,
					(ProtocolModel)model, new org.scribble.protocol.model.Role(role.getName()),
								journal);
		}

		if (local != null) {
			try {
				// Convert protocol model to monitoring description
				java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
				
				_exporter.export(local, journal, os);
				
				os.close();
				
				if (journal.hasErrors()) {
					LOG.severe("Errors detected when exporting protocol '"+
									local+"' to monitorable description");
				} else {
					java.io.InputStream is=new java.io.ByteArrayInputStream(os.toByteArray());
					
					ret = MonitorModelUtil.deserialize(is);
					
					is.close();
				}
			} catch(Exception e) {
				LOG.log(Level.SEVERE, "Failed to export monitor description for located protocol model", e);
			}
		}
		
		return (ret);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onEvent(SimulationContext context, Event event,
							SimulationHandler handler) {
		
		if (event instanceof MessageEvent) {
			MessageEvent me=(MessageEvent)event;
			
			if (me.getParameter().size() == 1) {
				String type=me.getParameter().get(0).getType();
				String path=me.getParameter().get(0).getValue();
				
				java.io.Serializable value=null;
				
				if (path != null) {
					try {
						java.io.InputStream is=context.getResource(path);
						
						byte[] b=new byte[is.available()];
						
						is.read(b);
						
						is.close();
						
						// Assume value is text
						value = new String(b);
						
					} catch(Exception e) {
						handler.error("Failed to obtain message value", event, e);
					}
				}
				
				try {
					Message mesg=new Message();
					
					mesg.setOperator(((MessageEvent) event).getOperationName());
					mesg.setFault(((MessageEvent) event).getFaultName());
					mesg.getTypes().add(type);
					mesg.getValues().add((String)value);
					
					Result result=null;
					
					if (event instanceof SendEvent) {
						result = _monitor.messageSent(_context, (Description)context.getModel(),
									(Session)context.getProperties().get(Session.class.getName()), mesg);
					} else if (event instanceof ReceiveEvent) {
						result = _monitor.messageReceived(_context, (Description)context.getModel(),
								(Session)context.getProperties().get(Session.class.getName()), mesg);
					}
					
					if (result.isValid()) {
						handler.processed(event);
					} else {
						handler.unexpected(event);
					}
				} catch(Exception e) {
					handler.error("Failed to simulate message", event, e);
				}
			} else {
				handler.error("Cannot simulate event as does not have single parameter",
								event, null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void close(SimulationContext context) throws Exception {
		
	}
}
