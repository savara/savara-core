package org.savara.scenario.simulator.sca.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.savara.scenario.simulation.RoleSimulator;
import org.savara.scenario.simulator.sca.SCARoleSimulator;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static final Logger logger=Logger.getLogger(Activator.class.getName());

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

        //Properties props = new Properties();
        
        SCARoleSimulator rs=new SCARoleSimulator();
        
        context.registerService(RoleSimulator.class.getName(), 
				rs, null);

        if (logger.isLoggable(Level.FINE)) {
			logger.fine("CDM Role Simulator registered");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
