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
package org.savara.scenario.simulation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.scenario.model.Event;
import org.savara.scenario.model.MessageEvent;
import org.savara.scenario.model.Role;
import org.savara.scenario.model.Scenario;
import org.savara.scenario.simulation.model.RoleDetails;
import org.savara.scenario.simulation.model.Simulation;
import org.savara.scenario.simulation.model.SimulatorDetails;
import org.savara.scenario.util.ScenarioModelUtil;
import org.savara.scenario.util.SimulationModelUtil;

/**
 * This class provides a main for running a scenario simulator.
 *
 */
public class ScenarioSimulatorMain {
	
	private static final Logger logger=Logger.getLogger(ScenarioSimulatorMain.class.getName());

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Usage: ScenarioSimulatorMain simulation");
			System.exit(1);
		}
		
		Simulation simulation=null;
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream(args[0]);
			
			if (is == null) {
				java.io.File f=new java.io.File(args[0]);
				if (f.exists()) {
					is = new java.io.FileInputStream(f);
				}
			}
			
			if (is != null) {
				simulation = SimulationModelUtil.deserialize(is);
				
				System.err.println("SIMULATION="+simulation);
				
				is.close();
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to load scenario simulation '"+args[0]+"'", e);
		}
		
		if (simulation != null) {
			ScenarioSimulatorMain simulator=new ScenarioSimulatorMain();
			
			System.exit(simulator.simulate(simulation));
		} else {
			System.exit(1);
		}
	}
	
	/**
	 * This method runs the simulator against the supplied file.
	 * 
	 * @param scenarioFile The scenario file
	 * @param props The properties
	 * @return The return code, 0 normal, other values represent errors
	 */
	public int simulate(Simulation simulation) {
		return(simulate(simulation, null));
	}
	
	/**
	 * This method runs the simulator against the supplied file.
	 * 
	 * @param simulation The simulation
	 * @param handler The optional simulation handler
	 * @return The return code, 0 normal, other values represent errors
	 */
	public int simulate(Simulation simulation, SimulationHandler handler) {
		int ret=-1;
		Scenario scenario=loadScenario(simulation);
		
		if (scenario != null) {
			
			java.util.Map<Role, RoleSimulator> roleSimulators=loadRoleSimulators(scenario, simulation);
			
			java.util.Map<Role, SimulationContext> contexts=loadSimulationContexts(scenario,
								simulation, roleSimulators);
		
			ScenarioSimulator simulator=getSimulator();
			
			if (simulator != null) {
				if (handler == null) {
					handler = new ConsoleSimulationHandler();
				} else {
					handler = new ProxySimulationHandler(handler);
				}
				
				try {
					simulator.simulate(scenario, roleSimulators, contexts, handler);
					
					if (((SimulationHandlerBase)handler).isFailed() == false) {
						ret = 0;
					}
				} catch(Exception e) {
					logger.log(Level.SEVERE, "Failed to simulate", e);
				}
			}
		}
	
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Simulation completed with code: "+ret);
		}
		
		return(ret);
	}
	
	protected ScenarioSimulator getSimulator() {
		return(ScenarioSimulatorFactory.getScenarioSimulator());
	}
	
	protected java.util.Map<Role, RoleSimulator> loadRoleSimulators(Scenario scenario, Simulation simulation) {
		java.util.Map<Role, RoleSimulator> ret=new java.util.HashMap<Role, RoleSimulator>();
		
		for (Role role : scenario.getRole()) {
			
			for (RoleDetails details : simulation.getRoles()) {
				if (details.getScenarioRole().equals(role.getName())) {
					
					for (SimulatorDetails simdetails : simulation.getSimulators()) {
						if (simdetails.getName().equals(details.getSimulator())) {
							try {
								// Instantiate simulator for role
								Class<?> cls=Class.forName(simdetails.getClassName());
								
								Object obj=cls.newInstance();
								
								if (obj instanceof RoleSimulator) {
									ret.put(role, (RoleSimulator)obj);							
									break;
								}
								
							} catch(Exception e) {
								logger.log(Level.SEVERE, "Failed to instantiate role simulator '"+
												details.getSimulator()+"'", e);
							}
						}
					}
				}
			}
		}
		
		return(ret);
	}

	protected java.util.Map<Role, SimulationContext> loadSimulationContexts(Scenario scenario,
						Simulation simulation, java.util.Map<Role, RoleSimulator> roleSimulators) {
		java.util.Map<Role, SimulationContext> ret=new java.util.HashMap<Role, SimulationContext>();
		
		for (Role role : scenario.getRole()) {
			
			for (RoleDetails details : simulation.getRoles()) {
				if (details.getScenarioRole().equals(role.getName())) {
					RoleSimulator rsim=roleSimulators.get(role);
					
					try {
						java.io.InputStream is=ClassLoader.getSystemResourceAsStream(details.getModel());
						
						if (is == null) {
							java.io.File f=new java.io.File(details.getModel());
							if (f.exists()) {
								is = new java.io.FileInputStream(f);
							}
						}
						
						if (is != null) {
							
							// Get context
							SimulationContext context=loadContext(simulation,
												role, details, rsim);
								
							if (context != null) {
								logger.fine("Adding context for role '"+role.getName()+"'");
								ret.put(role, context);
							} else {
								logger.severe("Failed to load context for role '"+role.getName()+"'");
							}
							
							is.close();
						} else {
							logger.severe("Failed to find model '"+details.getModel()+"'");
						}
					} catch(Exception e) {
						logger.log(Level.SEVERE, 
								"Failed to load simulation model '"+details.getModel()+"'", e);
					}
					
				}
			}
		}
		
		return(ret);
	}
	
	protected Scenario loadScenario(Simulation simulation) {
		Scenario scenario=null;
		
		try {
			java.io.InputStream is=ClassLoader.getSystemResourceAsStream(simulation.getScenario());
			
			if (is == null) {
				java.io.File f=new java.io.File(simulation.getScenario());
				if (f.exists()) {
					is = new java.io.FileInputStream(f);
				}
			}
			
			if (is != null) {
				scenario = ScenarioModelUtil.deserialize(is);
				
				is.close();
			} else {
				logger.severe("Failed to find scenario '"+simulation.getScenario()+"'");
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to load scenario '"+simulation.getScenario()+"'", e);
		}
		
		return(scenario);
	}
	
	protected SimulationContext loadContext(Simulation simulation, Role role,
						RoleDetails details, RoleSimulator rsim) {
		DefaultSimulationContext ret=null;
		
		try {
			java.net.URL url=ClassLoader.getSystemResource(simulation.getScenario());
			
			java.io.File f=null;
			
			if (url != null) {
				f = new java.io.File(url.getFile());
			} else {
				f = new java.io.File(simulation.getScenario());
				
				if (f.exists() == false) {
					f = null;
				}
			}
			
			if (f != null) {
				ret = new DefaultSimulationContext(f);	

				java.io.InputStream is=null;
				java.io.File modelFile=null;
				
				url = ClassLoader.getSystemResource(details.getModel());
				
				if (url != null) {
					is=url.openStream();
					modelFile = new java.io.File(url.getFile());
				} else {
					modelFile = new java.io.File(details.getModel());
					if (modelFile.exists()) {
						is = new java.io.FileInputStream(modelFile);
					} else {
						modelFile = null;
					}
				}
				
				if (modelFile != null && is != null) {
					Object model=rsim.getModel(new SimulationModel(modelFile.getName(), is));
					
					// Check if model should be projected to a particular role
					if (rsim.getModelRoles(model).size() == 0) {
						ret.setModel(model);
					} else {
						Role localRole=role;
						
						if (details.getModelRole() != null) {
							localRole = new Role();
							localRole.setName(details.getModelRole());
						}
						
						model = rsim.getModelForRole(model, localRole);
						ret.setModel(model);
					}
					
					is.close();
				} else {
					ret = null;
				}
			} else {
				logger.severe("Failed to locate scenario '"+simulation.getScenario()+"'");
			}
			
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to load simulation context '"+details.getModel()+"'", e);
		}
		
		return(ret);
	}
	
	public class SimulationHandlerBase implements SimulationHandler {
		
		private boolean m_failed=false;
		
		public void start(Event event) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("START: "+printable(event));
			}
		}

		public void end(Event event) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("END: "+printable(event));
			}
		}

		public void noSimulator(Event event) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("NO SIMULATOR: "+printable(event));
			}
		}

		public void processed(Event event) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("PROCESSED: "+printable(event));
			}
		}

		public void unexpected(Event event) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("UNEXPECTED: "+printable(event));
			}
			setFailed(true);
		}

		public void error(String mesg, Event event, Throwable e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "ERROR: "+event.getId()+" \""+mesg+"\"", e);
			}
			setFailed(true);
		}
		
		public boolean isFailed() {
			return(m_failed);
		}
		
		protected void setFailed(boolean b) {
			m_failed = b;
		}
		
		protected String printable(Event event) {
			String ret=null;
			
			if (event instanceof MessageEvent) {
				MessageEvent me=(MessageEvent)event;
				ret = event.getClass().getSimpleName()+"["+event.getId()+"] "+me.getOperationName()+"(";
				
				for (int i=0; i < me.getParameter().size(); i++) {
					if (i != 0) {
						ret += ",";
					}
					ret += me.getParameter().get(i).getValue();
				}
				
				ret += ")";
			}
			
			if (event.isErrorExpected()) {
				ret += " (Error Expected)";
			}
			
			return(ret);
		}
	}
	
	public class ConsoleSimulationHandler extends SimulationHandlerBase {

		public void start(Event event) {
			super.start(event);
			System.err.println(">>> START [ID="+event.getId()+"]");
		}

		public void end(Event event) {
			super.end(event);
			System.err.println(">>> END [ID="+event.getId()+"]");
		}

		public void noSimulator(Event event) {
			super.noSimulator(event);
			System.err.println(">>> NO_SIMULATOR [ID="+event.getId()+"]");
		}

		public void processed(Event event) {
			super.processed(event);
			success(event, printable(event));
		}

		public void unexpected(Event event) {
			super.unexpected(event);
			failure(event, printable(event));
		}

		public void error(String mesg, Event event, Throwable e) {
			super.error(mesg, event, e);
			failure(event, printable(event)+"["+mesg+"]");
		}
		
		protected void success(Event event, String mesg) {
			System.err.println(">>> SUCCESS [ID="+event.getId()+"] "+mesg);
		}
		
		protected void failure(Event event, String mesg) {
			System.err.println(">>> FAIL [ID="+event.getId()+"] "+mesg);
		}
	}
	
	public class ProxySimulationHandler extends SimulationHandlerBase {

		private SimulationHandler m_handler=null;
		
		public ProxySimulationHandler(SimulationHandler handler) {
			m_handler = handler;
		}
		
		public void start(Event event) {
			super.start(event);
			m_handler.start(event);
		}

		public void end(Event event) {
			super.end(event);
			m_handler.end(event);
		}

		public void noSimulator(Event event) {
			super.noSimulator(event);
			m_handler.noSimulator(event);
		}

		public void processed(Event event) {
			super.processed(event);
			m_handler.processed(event);
		}

		public void unexpected(Event event) {
			super.unexpected(event);
			m_handler.unexpected(event);
		}

		public void error(String mesg, Event event, Throwable e) {
			super.error(mesg, event, e);
			m_handler.error(mesg, event, e);
		}
		
	}
}
