package org.savara.pi4soa.cdm.osgi;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.savara.pi4soa.cdm.parser.CDMProtocolParser;
import org.scribble.protocol.parser.ProtocolParser;

public class Activator implements BundleActivator {

	private static final Logger logger=Logger.getLogger(Activator.class.getName());

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
        Properties props = new Properties();
        
        CDMProtocolParser pp=new CDMProtocolParser();
        
        context.registerService(ProtocolParser.class.getName(), 
				pp, props);

        if (logger.isLoggable(Level.FINE)) {
			logger.fine("CDM Protocol Parser registered");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}
