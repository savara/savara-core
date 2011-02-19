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
package org.savara.activity.notifier.jms;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.TextMessage;
import javax.naming.Context;

import org.savara.activity.ActivityNotifier;
import org.savara.activity.model.Activity;
import org.savara.activity.util.ActivityModelUtil;
import org.savara.common.config.Configuration;

/**
 * This class provides the JMS implementation of the ActivityNotifier
 * interface.
 *
 */
public class JMSActivityNotifier implements ActivityNotifier {

	public static final String JMS_DESTINATION = "savara.activity.notifier.jms.destination";
	public static final String JMS_CONNECTION_FACTORY = "savara.activity.notifier.jms.connection.factory";

	private static Logger logger = Logger.getLogger(JMSActivityNotifier.class.getName());	

	private String m_jndiInitialContextFactory=null;
	private String m_jndiProviderURL=null;
	private String m_jndiFactoryURLPackages=null;
	private String m_jmsConnectionFactory=null;
	private String m_jmsDestination=null;
	
	private javax.jms.Connection m_connection=null;
	private javax.jms.Session m_session=null;
	private javax.jms.MessageProducer m_producer=null;

	public void setConfiguration(Configuration config) {
		
		logger.info("Initializing SAVARA JMS Activity Notifier");
		
		// Set the properties from the supplied configuration
		if (m_jndiInitialContextFactory == null) {
			m_jndiInitialContextFactory = config.getProperty(Context.INITIAL_CONTEXT_FACTORY);
		}
		
		if (m_jndiProviderURL == null) {
			m_jndiProviderURL = config.getProperty(Context.PROVIDER_URL);
		}
		
		if (m_jndiFactoryURLPackages == null) {
			m_jndiFactoryURLPackages = config.getProperty(Context.URL_PKG_PREFIXES);
		}
		
		if (m_jmsConnectionFactory == null) {
			m_jmsConnectionFactory = config.getProperty(JMS_CONNECTION_FACTORY);
		}
		
		if (m_jmsDestination == null) {
			m_jmsDestination = config.getProperty(JMS_DESTINATION);
		}
		
		javax.naming.Context ctx=null;

		try {
			if (m_jndiInitialContextFactory != null) {
				java.util.Properties jndiProps = new java.util.Properties();
				jndiProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
								m_jndiInitialContextFactory);
				jndiProps.setProperty(Context.PROVIDER_URL,
								m_jndiProviderURL);
				
				if (m_jndiFactoryURLPackages != null) {
					jndiProps.setProperty(Context.URL_PKG_PREFIXES,
								m_jndiFactoryURLPackages);
				}
				
				ctx = new javax.naming.InitialContext(jndiProps);
			} else {
				if (logger.isLoggable(java.util.logging.Level.FINER)) {
					logger.finer("Creating default initial context");
				}
				ctx = new javax.naming.InitialContext();
			}
		} catch(Exception e) {
			logger.severe("Failed to create JNDI initial context: "+e);
		}
		
		// Get connection factory and destination topic
		if (ctx != null) {
			
			try {
				javax.jms.ConnectionFactory factory =
					(javax.jms.ConnectionFactory)
						ctx.lookup(m_jmsConnectionFactory);
	
				m_connection = factory.createConnection();
				
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Connection: "+m_connection);
				}
	
				javax.jms.Destination dest =
					(javax.jms.Destination)ctx.lookup(m_jmsDestination);
	
				m_session = m_connection.createSession(false,
						javax.jms.Session.AUTO_ACKNOWLEDGE);
				
				m_producer = m_session.createProducer(dest);
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine(Thread.currentThread()+
							": Created JMS Activity Notifier connection");
				}
				
			} catch(Exception e) {
				logger.severe("Failed to create JMS connection: "+e);
			}
		}
	}
	
	/**
	 * This method sets the JNDI initial context factory class name.
	 * 
	 * @param factory The factory class name
	 */
	public void setJNDIInitialContextFactory(String factory) {
		m_jndiInitialContextFactory = factory;
	}
	
	/**
	 * This method sets the provider URL that is used when publishing
	 * the tracker events.
	 * 
	 * @param url The provider url
	 */
	public void setJNDIProviderURL(String url) {
		m_jndiProviderURL = url;
	}
	
	/**
	 * This method sets the JNDI factory URL packages.
	 * 
	 * @param pkgs The packages
	 */
	public void setJNDIFactoryURLPackages(String pkgs) {
		m_jndiFactoryURLPackages = pkgs;
	}
	
	/**
	 * This method sets the JMS connection factory name, to
	 * be looked up within JNDI.
	 * 
	 * @param cf The connection factory
	 */
	public void setJMSConnectionFactory(String cf) {
		m_jmsConnectionFactory = cf;
	}
	
	/**
	 * This method sets the JMS destination name, to
	 * be looked up within JNDI.
	 * 
	 * @param dest The destination
	 */
	public void setJMSDestination(String dest) {
		m_jmsDestination = dest;
	}
	
	/**
	 * This method is used to publish the service tracker message.
	 * 
	 * @param serviceName The service name
	 * @param session The session
	 * @param mesg The message
	 */
	public void publish(Activity activity) {
		
		if (m_producer != null) {
			
			try {
				ByteArrayOutputStream os=new ByteArrayOutputStream();
				
				ActivityModelUtil.serialize(activity, os);
				
				String act=new String(os.toByteArray());

				if (logger.isLoggable(Level.FINE)) {
					logger.fine(act);
				}
				
				TextMessage tm=m_session.createTextMessage(act);
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(Thread.currentThread()+
						": Sending JMS ServiceTracker record="+tm);
				}

				m_producer.send(tm);
			} catch(Exception e) {
				logger.severe("Failed to publish message: "+e);
			}
			
		} else {
			logger.warning("Could not publish message: "+activity);
		}
	}
	
	/**
	 * This method closes the activity notifier.
	 *
	 */
	public void close() {
		
		try {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(Thread.currentThread()+
					": Closing JMS Activity Notifier connection");
			}
			
			if (m_session != null) {
				m_session.close();
				m_session = null;
			}

			if (m_connection != null) {
				m_connection.close();
				m_connection = null;
			}
			
		} catch(Exception e) {
			logger.severe("Failed to close JMS connection: "+e);
		}
	}
	
}
