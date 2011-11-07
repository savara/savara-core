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
package org.savara.activity.osgi;

import java.util.Properties;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.savara.activity.ActivityAnalyser;
import org.savara.activity.ActivityFilter;
import org.savara.activity.ActivityNotifier;
import org.savara.activity.ActivityProcessor;
import org.savara.activity.ActivityProcessorFactory;
import org.savara.activity.ActivityStore;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private org.osgi.util.tracker.ServiceTracker m_activityFilterTracker=null;
	private org.osgi.util.tracker.ServiceTracker m_activityAnalyserTracker=null;
	private org.osgi.util.tracker.ServiceTracker m_activityStoreTracker=null;
	private org.osgi.util.tracker.ServiceTracker m_activityNotifierTracker=null;

	private static final Logger _log=Logger.getLogger(Activator.class.getName());

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

        // Register activity processor
        final ActivityProcessor avm=ActivityProcessorFactory.getActivityProcessor();
        
        context.registerService(ActivityProcessor.class.getName(), 
        					avm, props);
        
        _log.fine("Registered Activity Validation Manager");
        
        // Create activity filter tracker
        m_activityFilterTracker = new ServiceTracker(context,
							ActivityFilter.class.getName(), null) {

			public Object addingService(ServiceReference ref) {
				Object ret=super.addingService(ref);
				
				_log.info("Activity filter has been added: "+ret);
				
				// TODO: If already set, then may need to replace with list
				// implementation and add subsequent implementations to it
				avm.setFilter((ActivityFilter)ret);
				
				return(ret);
			}
		};

		m_activityFilterTracker.open();
		
        // Create activity analyser tracker
        m_activityAnalyserTracker = new ServiceTracker(context,
							ActivityAnalyser.class.getName(), null) {

			public Object addingService(ServiceReference ref) {
				Object ret=super.addingService(ref);
				
				_log.info("Activity analyser has been added: "+ret);
				
				// TODO: If already set, then may need to replace with list
				// implementation and add subsequent implementations to it
				avm.setAnalyser((ActivityAnalyser)ret);
				
				return(ret);
			}
		};

		m_activityAnalyserTracker.open();
		
        // Create activity store tracker
        m_activityStoreTracker = new ServiceTracker(context,
							ActivityStore.class.getName(), null) {

			public Object addingService(ServiceReference ref) {
				Object ret=super.addingService(ref);
				
				_log.info("Activity store has been added: "+ret);
				
				// TODO: If already set, then may need to replace with list
				// implementation and add subsequent implementations to it
				avm.setStore((ActivityStore)ret);
				
				return(ret);
			}
		};

		m_activityStoreTracker.open();
		
        // Create activity notifier tracker
        m_activityNotifierTracker = new ServiceTracker(context,
							ActivityNotifier.class.getName(), null) {

			public Object addingService(ServiceReference ref) {
				Object ret=super.addingService(ref);
				
				_log.info("Activity notifier has been added: "+ret);
				
				// TODO: If already set, then may need to replace with list
				// implementation and add subsequent implementations to it
				avm.setNotifier((ActivityNotifier)ret);
				
				return(ret);
			}
		};

		m_activityNotifierTracker.open();
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
