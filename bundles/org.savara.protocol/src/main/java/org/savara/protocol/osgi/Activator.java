package org.savara.protocol.osgi;

import java.util.Properties;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.savara.protocol.export.text.JoinTextProtocolExporterRule;
import org.savara.protocol.export.text.SyncTextProtocolExporterRule;
import org.savara.protocol.projection.JoinProjectorRule;
import org.savara.protocol.projection.SyncProjectorRule;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.protocol.export.text.TextProtocolExporterRule;
import org.scribble.protocol.parser.ProtocolParserManager;
import org.scribble.protocol.projection.ProtocolProjector;
import org.scribble.protocol.projection.impl.ProjectorRule;

public class Activator implements BundleActivator {

	private static Logger logger = Logger.getLogger(Activator.class.getName());

	private static BundleContext context;

	private org.osgi.util.tracker.ServiceTracker m_protocolProjectorTracker=null;

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

        // Register extension rules
        JoinProjectorRule jpr=new JoinProjectorRule();        
        context.registerService(ProjectorRule.class.getName(), 
                            jpr, props);
        
        SyncProjectorRule spr=new SyncProjectorRule();        
        context.registerService(ProjectorRule.class.getName(), 
                            spr, props);
        
        JoinTextProtocolExporterRule jtper=new JoinTextProtocolExporterRule();        
        context.registerService(TextProtocolExporterRule.class.getName(), 
        					jtper, props);
        
        SyncTextProtocolExporterRule stper=new SyncTextProtocolExporterRule();        
        context.registerService(TextProtocolExporterRule.class.getName(), 
        					stper, props);
        
		// Make sure any bundles, associated with scribble, are started (excluding
		// the designer itself)
		Bundle[] bundles=context.getBundles();

		for (int i=0; i < bundles.length; i++) {
			Bundle bundle=bundles[i];
			
			if (bundle != null) {
				//Object val=bundle.getHeaders().get(SERVICE_COMPONENT);
				if (bundle.getSymbolicName().startsWith("org.scribble.") &&
						bundle.getSymbolicName().endsWith("designer") == false) {
				
					//if (bundle.getState() == Bundle.RESOLVED) {
						logger.fine("Pre-empt bundle start: "+bundle);
						bundle.start();
					//}
				}
			}
		}

		// Initialize the protocol parser manager
		ServiceReference sref=context.getServiceReference(ProtocolParserManager.class.getName());
		
		ProtocolParserManager ppm=(ProtocolParserManager)context.getService(sref);
		
		ProtocolServices.setParserManager(ppm);
		
		// Initialize the protocol projector
		sref=context.getServiceReference(ProtocolProjector.class.getName());
		
		if (sref != null) {
			ProtocolProjector pp=(ProtocolProjector)context.getService(sref);
			ProtocolServices.setProtocolProjector(pp);
		} else {
			// Create service tracker
	        m_protocolProjectorTracker = new ServiceTracker(context,
	        		ProtocolProjector.class.getName(), null) {
	        	
				public Object addingService(ServiceReference ref) {
					Object ret=super.addingService(ref);
					
					logger.fine("Projector being set: "+ret);
					
					ProtocolServices.setProtocolProjector((ProtocolProjector)ret);
					
					return(ret);
				}
	        };
	        
	        m_protocolProjectorTracker.open();
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
