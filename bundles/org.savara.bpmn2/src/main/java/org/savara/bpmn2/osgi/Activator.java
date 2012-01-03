package org.savara.bpmn2.osgi;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.savara.bpmn2.parser.choreo.BPMN2ChoreographyProtocolParser;
import org.scribble.protocol.parser.ProtocolParser;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private static final Logger LOG=Logger.getLogger(Activator.class.getName());

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

        Properties props = new Properties();
        
        BPMN2ChoreographyProtocolParser pp=new BPMN2ChoreographyProtocolParser();
        
        context.registerService(ProtocolParser.class.getName(), 
				pp, props);

        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine("BPMN2 Protocol Parser registered");
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
