package org.savara.bpel.osgi;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.savara.bpel.parser.BPELProtocolParser;
import org.scribble.protocol.parser.ProtocolParser;

public class Activator implements BundleActivator {

	private static final Logger LOG=Logger.getLogger(Activator.class.getName());

	private static BundleContext context;

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
        
        BPELProtocolParser pp=new BPELProtocolParser();
        
        context.registerService(ProtocolParser.class.getName(), 
				pp, props);

        if (LOG.isLoggable(Level.FINE)) {
        	LOG.fine("BPEL Protocol Parser registered");
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
