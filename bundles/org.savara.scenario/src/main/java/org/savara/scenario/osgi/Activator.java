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
package org.savara.scenario.osgi;

import java.util.Properties;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.savara.scenario.simulation.DefaultScenarioSimulator;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulation.RoleSimulatorFactory;
import org.savara.scenario.simulation.ScenarioSimulator;

public class Activator implements BundleActivator {

	private org.osgi.util.tracker.ServiceTracker m_roleSimulatorTracker=null;

	private static final Logger logger=Logger.getLogger(Activator.class.getName());
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		
		// Register the scenario simulator implementation
        Properties props = new Properties();

        ScenarioSimulator ssim=new DefaultScenarioSimulator();
        
        context.registerService(ScenarioSimulator.class.getName(), 
        					ssim, props);
        
        logger.info("Registered Scenario Simulator");
		
		// Obtain references to role simulators
        /*
		ServiceReference sref=context.getServiceReference(RoleSimulator.class.getName());
		
		if (sref != null) {
			RoleSimulator rs=(RoleSimulator)context.getService(sref);
			RoleSimulatorFactory.register(rs);
		} else {
		*/
	        m_roleSimulatorTracker = new ServiceTracker(context,
	        		RoleSimulator.class.getName(), null) {
	        	
				public Object addingService(ServiceReference ref) {
					Object ret=super.addingService(ref);
					
					logger.fine("Role simulator being registered: "+ret);
					
					RoleSimulatorFactory.register((RoleSimulator)ret);
					
					return(ret);
				}
	        };
	        
	        m_roleSimulatorTracker.open();
		//}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}
