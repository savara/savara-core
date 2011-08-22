/*
 * Copyright 2005 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * Sep 12, 2005 : Initial version created by gary
 */

// Original version copied from pi4soa service tracker, licensed under Apache version 2

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
package org.savara.activity.validator.cdm.pi4soa;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.xml.namespace.QName;

import org.pi4soa.common.util.NamesUtil;
import org.pi4soa.common.xml.XMLUtils;
import org.pi4soa.service.Channel;
import org.pi4soa.service.Identity;
import org.pi4soa.service.Message;
import org.pi4soa.service.ServiceException;
import org.pi4soa.service.behavior.MessageClassification;
import org.pi4soa.service.behavior.MessageDefinition;
import org.pi4soa.service.behavior.Receive;
import org.pi4soa.service.behavior.Send;
import org.pi4soa.service.behavior.ServiceDescription;
import org.pi4soa.service.session.Session;
import org.pi4soa.service.tracker.ServiceTracker;
import org.pi4soa.service.tracker.TrackerEvent;
import org.savara.activity.model.Activity;
import org.savara.activity.model.Analysis;
import org.savara.activity.model.ExchangeType;
import org.savara.activity.model.InteractionActivity;
import org.savara.activity.model.ProtocolAnalysis;
import org.savara.activity.util.ActivityModelUtil;

/**
 * This class provides an implementation of the publishing service
 * tracker that sends the monitoring information using the JMS
 * API.
 *
 */
public class JMSServiceTracker implements ServiceTracker {

	private static final String SAVARA_SERVICE_TRACKER_NS = "http://www.savara.org/service/tracker";
	/**
	 * This is the default constructor for the JMS service
	 * tracker implementation.
	 *
	 */
	public JMSServiceTracker() {
	}

	/**
	 * This method is invoked to initialize the JMS connection, based
	 * on the established properties.
	 * 
	 */
	public void initialize() {
		
		logger.info("Initializing Savara JMS Service Tracker");
		
		javax.naming.Context ctx=null;

		try {
			if (NamesUtil.isSet(m_jndiInitialContextFactory)) {
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
				try {
					javax.jms.ConnectionFactory factory =
						(javax.jms.ConnectionFactory)
							ctx.lookup(m_jmsConnectionFactory);
		
					m_connection = factory.createConnection();
					
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("Connection: "+m_connection);
					}
					
				} catch(java.lang.RuntimeException re) {
					if (m_jmsConnectionFactoryAlternate != null) {
						QueueConnectionFactory qcf = (QueueConnectionFactory)
							ctx.lookup(m_jmsConnectionFactoryAlternate);
						m_connection = qcf.createQueueConnection();

						if (logger.isLoggable(Level.FINER)) {
							logger.finer("Alternate Connection: "+m_connection);
						}
						
					} else {
						throw re;
					}
				}
	
				javax.jms.Destination dest =
					(javax.jms.Destination)ctx.lookup(m_jmsDestination);
	
				m_session = m_connection.createSession(false,
						javax.jms.Session.AUTO_ACKNOWLEDGE);
				
				m_producer = m_session.createProducer(dest);
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine(Thread.currentThread()+
							": Created JMS ServiceTracker connection");
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
	 * This method sets the alternate JMS connection factory name, to
	 * be looked up within JNDI.
	 * 
	 * @param cf The connection factory
	 */
	public void setJMSConnectionFactoryAlternate(String cf) {
		m_jmsConnectionFactoryAlternate = cf;
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
	protected synchronized void publish(String serviceName, Session session,
						String mesg) {
		
		// Establish JMS connection and topic
		if (m_initialized == false &&
				m_jmsConnectionFactory != null &&
				m_jmsDestination != null) {
			initialize();
			
			m_initialized = true;
		}
		
		if (m_producer != null) {
			
			try {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine(mesg);
				}
				
				TextMessage tm=m_session.createTextMessage(mesg);
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(Thread.currentThread()+
						": Sending JMS ServiceTracker record="+tm);
				}

				m_producer.send(tm);
			} catch(Exception e) {
				logger.severe("Failed to publish message: "+e);
			}
			
		} else {
			logger.warning("Could not publish message: "+mesg);
		}
	}
	
	/**
	 * This method is used to publish the service tracker message.
	 * 
	 * @param serviceName The service name
	 * @param session The session
	 * @param mesg The message
	 */
	protected synchronized void publish(Activity activity) {
		
		// Establish JMS connection and topic
		if (m_initialized == false &&
				m_jmsConnectionFactory != null &&
				m_jmsDestination != null) {
			initialize();
			
			m_initialized = true;
		}
		
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
	 * This method closes the service tracker.
	 *
	 * @exception ServiceException Failed to close
	 */
	public void close() throws ServiceException {
		
		try {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(Thread.currentThread()+
					": Closing JMS ServiceTracker connection");
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
						
			m_initialized = false;

			throw new ServiceException("Failed to close JMS connection", e);
		}
		
		m_initialized = false;
	}
	
	/**
	 * This method sets whether the message payload should be
	 * recorded as part of the service tracker send and receive
	 * events.
	 * 
	 * @param val Whether to record the message payload
	 */
	public void setRecordMessagePayload(Boolean val) {
		if (val != null) {
			m_recordMessagePayload = val.booleanValue();
		} else {
			m_recordMessagePayload = false;
		}
	}
	
	/**
	 * This method indicates that a new service instance
	 * has started.
	 * 
	 * @param service The service
	 * @param session The session
	 */
	public void serviceStarted(ServiceDescription service,
				Session session) {
	}

	/**
	 * This method indicates that a service instance
	 * has finished.
	 * 
	 * @param service The service
	 * @param session The session
	 */
	public void serviceFinished(ServiceDescription service,
				Session session) {
	}

	/**
	 * This method indicates that a new sub session
	 * has started within an existing service instance.
	 * 
	 * @param parent The parent session
	 * @param session The session
	 */
	public void subSessionStarted(Session parent, Session session) {
	}
	
	/**
	 * This method indicates that an existing
	 * sub session has finished.
	 * 
	 * @param parent The parent session
	 * @param session The session
	 */
	public void subSessionFinished(Session parent, Session session) {
	}
	
	/**
	 * This method registers the fact that a message has been
	 * sent from a stateful service.
	 * 
	 * @param activity The behavioral activity
	 * @param session The session
	 * @param channel The channel
	 * @param mesg The message that has been handled
	 */
	public void sentMessage(Send activity, Session session,
					Channel channel, Message mesg) {
		
		Activity ia=createInteractionActivity(activity, session, mesg);
		
		((InteractionActivity)ia.getType()).setOutbound(true);		
		
		publish(ia);
	}

	protected Activity createInteractionActivity(org.pi4soa.service.behavior.MessageActivity activity,
							Session session, Message mesg) {
		Activity ret=new Activity();
		InteractionActivity ia=new InteractionActivity();
		ret.setType(ia);

		ia.setOperationName(activity.getOperationName());
		ia.setFaultName(activity.getFaultName());
		ia.setDestinationType(mesg.getServiceType());
		
		ia.setExchangeType(activity.getMessageDefinition().getClassification() == 
						MessageClassification.REQUEST ?
				ExchangeType.REQUEST : ExchangeType.RESPONSE);
		
		org.savara.activity.model.Message mp=new org.savara.activity.model.Message();
		mp.setType(mesg.getType());
		mp.setAny(getMessagePayload(mesg));
		
		ia.getMessage().add(mp);

		ProtocolAnalysis pa=new ProtocolAnalysis();
		pa.setExpected(Boolean.TRUE);
		pa.setProtocol(activity.getServiceDescription().getFullyQualifiedName());
		
		pa.setRole(XMLUtils.getLocalname(activity.getServiceDescription().getName()));
		
		Analysis anal=new Analysis();
		anal.setAny(pa);
		anal.setType(new QName("http://www.savara.org/activity", "ProtocolAnalysis"));
		
		ret.getAnalysis().add(anal);
		
		// Add context information from the identities info
		java.util.Iterator<Identity> ids=null;
		
		if (session != null) {
			ids = session.getPrimaryIdentities().iterator();
		}
		
		// If session has no identity information, but a
		// message is supplied, then try getting identity
		// from it.
		if ((ids == null || ids.hasNext() == false) &&
				mesg != null && mesg.getMessageIdentities() != null) {
			ids = mesg.getMessageIdentities().iterator();
		}
		
		if (ids != null && ids.hasNext()) {
			
			while (ids.hasNext()) {
				Identity cur=ids.next();
				
				org.savara.activity.model.Context context=new org.savara.activity.model.Context();
				
				String name=null;
				for (String token : cur.getTokens()) {
					if (name == null) {
						name = token;
					} else {
						name += ":"+token;
					}
				}
				
				context.setName(name);
				
				String value=null;
				
				for (Object val : cur.getValues()) {
					if (value == null) {
						value = val.toString();
					} else {
						value += ":"+val.toString();
					}
				}
				
				context.setValue(value);
				
				ret.getContext().add(context);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method registers the fact that a message has been
	 * sent from a stateless service.
	 * 
	 * @param defn The message definition
	 * @param mesg The message that has been handled
	 */
	public void sentMessage(MessageDefinition defn, Message mesg) {
	}
	
	/**
	 * This method registers the fact that a message has been
	 * received from a stateful service.
	 * 
	 * @param activity The behavioral activity
	 * @param session The session
	 * @param channel The channel
	 * @param mesg The message that has been handled
	 */
	public void receivedMessage(Receive activity, Session session,
					Channel channel, Message mesg) {
		Activity act=createInteractionActivity(activity, session, mesg);

		((InteractionActivity)act.getType()).setOutbound(false);
		
		publish(act);
	}
	
	/**
	 * This method registers the fact that a message has been
	 * received from a stateless service.
	 * 
	 * @param defn The message definition
	 * @param mesg The message that has been handled
	 */
	public void receivedMessage(MessageDefinition defn, Message mesg) {
	}

	/**
	 * This method registers that a message was not expected.
	 * 
	 * @param sdesc The service description, if known
	 * @param session The session, or null if a stateless service,
	 * 				or cannot be associated with a session
	 * @param mesg The message that was not expected
	 * @param reason The optional reason why the message was
	 * 					considered to be unexpected
	 */
	public void unexpectedMessage(ServiceDescription sdesc,
			Session session, Message mesg, String reason) {
		String messageText=getMessageText(mesg, null);
		
		// Process the reason field to ensure it has no
		// character that would cause the XML a problem
		reason = processAttributeContents(reason);
		
		String xml="<sd:"+TrackerEvent.UNEXPECTED_MESSAGE+
				" "+SERVICE_INSTANCE_ID_ATTR+"=\""+
				getServiceInstanceId(session)+"\" "+
				SESSION_ID_ATTR+"=\""+
					getSessionId(session)+"\" "+TIMESTAMP_ATTR+"=\""+
					System.currentTimeMillis()+"\" ><sd:"+DETAILS_NODE+">"+
						reason+"</sd:"+DETAILS_NODE+">"+messageText+
						"</sd:"+TrackerEvent.UNEXPECTED_MESSAGE+">";

		String serviceName=null;
		
		if (sdesc != null) {
			serviceName = sdesc.getFullyQualifiedName();
		} else {
			serviceName = getServiceName(session);
		}
		
		String serviceVersion=null;
		
		if (sdesc != null) {
			serviceVersion = sdesc.getVersion();
		}
		
		record(serviceName, serviceVersion, session, mesg, xml, ERROR, null);
	}
	
	/**
	 * This method registers that an exception was not handled.
	 * 
	 * @param session The session, or null if a stateless service
	 * @param excType The exception type
	 */
	public void unhandledException(Session session, String excType) {
		
		String xml="<sd:"+TrackerEvent.UNHANDLED_EXCEPTION+
					" "+SERVICE_INSTANCE_ID_ATTR+"=\""+
					getServiceInstanceId(session)+"\" "+
					SESSION_ID_ATTR+"=\""+
					getSessionId(session)+"\" "+TIMESTAMP_ATTR+"=\""+
					System.currentTimeMillis()+"\" ><sd:"+DETAILS_NODE+">"+
					excType+"</sd:"+DETAILS_NODE+"></sd:"+
					TrackerEvent.UNHANDLED_EXCEPTION+">";

		record(getServiceName(session), session, xml, ERROR, null);
	}
	
	/**
	 * This method reports information regarding the processing
	 * of a service session. The details can either be specified
	 * as a textual string (unstructured data), 
	 * or as a structured XML fragment.<p>
	 * 
	 * @param session The session, or null if a stateless service
	 * @param details The details
	 */
	public void information(Session session, String details) {
		String xml="<sd:"+TrackerEvent.INFORMATION+
				" "+SERVICE_INSTANCE_ID_ATTR+"=\""+
				getServiceInstanceId(session)+"\" "+
				SESSION_ID_ATTR+"=\""+getSessionId(session)+
					"\" "+TIMESTAMP_ATTR+"=\""+
					System.currentTimeMillis()+"\" ><sd:"+DETAILS_NODE+">"+
					XMLUtils.encodeXMLString(details)+
					"</sd:"+DETAILS_NODE+"></sd:"+
					TrackerEvent.INFORMATION+">";

		record(getServiceName(session), session, xml, INFO, null);
	}
	
	/**
	 * This method reports information regarding the processing
	 * of a service session. The details can either be specified
	 * as a textual string (unstructured data), 
	 * or as a structured XML fragment.<p>
	 * 
	 * @param session The session, or null if a stateless service
	 * @param details The details
	 * @param exc The optional exception
	 */
	public void warning(Session session, String details,
							Throwable exc) {
		String excText="";
		if (exc != null) {
			try {
				java.io.StringWriter s=new java.io.StringWriter();
				java.io.PrintWriter p=new java.io.PrintWriter(s);
				
				exc.printStackTrace(p);
				
				p.close();
				s.close();
				
				excText = "<sd:"+EXCEPTION_NODE+">"+s.toString()+"</sd:"+EXCEPTION_NODE+">";
			} catch(Exception e) {
				logger.severe("Failed to record exception stack trace: "+e);
				
				excText = "<sd:"+EXCEPTION_NODE+">"+exc.getLocalizedMessage()+"</sd:"+EXCEPTION_NODE+">";
			}
		}
								
		String xml="<sd:"+TrackerEvent.WARNING+
				" "+SERVICE_INSTANCE_ID_ATTR+"=\""+
				getServiceInstanceId(session)+"\" "+
				SESSION_ID_ATTR+"=\""+getSessionId(session)+
							"\" "+TIMESTAMP_ATTR+"=\""+
					System.currentTimeMillis()+"\" ><sd:"+DETAILS_NODE+">"+
							XMLUtils.encodeXMLString(details)+
							"</sd:"+DETAILS_NODE+">"+excText+"</sd:"+
							TrackerEvent.WARNING+">";

		record(getServiceName(session), session, xml, WARNING, exc);
	}
	
	/**
	 * This method reports information regarding the processing
	 * of a service session. The details can either be specified
	 * as a textual string (unstructured data), 
	 * or as a structured XML fragment.<p>
	 * 
	 * @param session The session, or null if a stateless service
	 * @param details The details
	 * @param exc The optional exception
	 */
	public void error(Session session, String details,
							Throwable exc) {
		String excText="";
		if (exc != null) {
			try {
				java.io.StringWriter s=new java.io.StringWriter();
				java.io.PrintWriter p=new java.io.PrintWriter(s);
				
				exc.printStackTrace(p);
				
				p.close();
				s.close();
				
				excText = "<sd:"+EXCEPTION_NODE+">"+s.toString()+"</sd:"+EXCEPTION_NODE+">";
			} catch(Exception e) {
				logger.severe("Failed to record exception stack trace: "+e);
				
				excText = "<sd:"+EXCEPTION_NODE+">"+exc.getLocalizedMessage()+"</sd:"+EXCEPTION_NODE+">";
			}
		}
						
		
		String xml="<sd:"+TrackerEvent.ERROR+
				" "+SERVICE_INSTANCE_ID_ATTR+"=\""+
				getServiceInstanceId(session)+"\" "+
				SESSION_ID_ATTR+"=\""+getSessionId(session)+
					"\" "+TIMESTAMP_ATTR+"=\""+
					System.currentTimeMillis()+"\" ><sd:"+DETAILS_NODE+">"+
					XMLUtils.encodeXMLString(details)+
					"</sd:"+DETAILS_NODE+">"+excText+"</sd:"+
					TrackerEvent.ERROR+">";

		record(getServiceName(session), session, xml, ERROR, exc);
	}
	
	/**
	 * Check the format of the specified session id.
	 * 
	 * @param session The initial session
	 * @return The reformatted session id
	 */
	protected String getSessionId(Session session) {
		String ret=null;
		
		if (session != null) {
			ret = session.getId().getSessionId();
		}
		
		if (ret == null) {
			ret = "";
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether a long expanded form
	 * of id should be used for the service instance id.
	 * 
	 * @return Whether the expanded form of id is used
	 */
	protected boolean isIdExpanded() {
		return(true);
	}
	
	/**
	 * This method returns the service instance id
	 * associated with the specified session.
	 * 
	 * @param session The session
	 * @return The service instance id
	 */
	protected String getServiceInstanceId(Session session) {
		String ret=null;
		
		if (session != null) {
			if (isIdExpanded()) {
				ret = session.getId().getServiceDescriptionName()+
						"/"+session.getId().getServiceInstanceId();				
			} else {
				ret = session.getId().getServiceInstanceId();
			}
		}
		
		if (ret == null) {
			ret = "";
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the service name associated with the
	 * supplied session.
	 * 
	 * @param session The session
	 * @return The service name
	 */
	protected String getServiceName(Session session) {
		String ret="";
		
		if (session != null && session.getId() != null) {
			ret = session.getId().getServiceDescriptionName();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the service name associated with the
	 * supplied message definition.
	 * 
	 * @param defn The message definition
	 * @return The service name
	 */
	protected String getServiceName(MessageDefinition defn) {
		String ret="";

		if (defn != null && defn.getServiceDescription() != null) {
			ret = defn.getServiceDescription().getFullyQualifiedName();
		}
		
		return(ret);		
	}
	
	/**
	 * This method constructs the session related information
	 * to be recorded.
	 * 
	 * @param session The session
	 * @return The session text
	 */
	protected String getSessionIdentityText(Session session) {
		return(getSessionIdentityText(session, null));
	}
	
	/**
	 * This method constructs the session related information
	 * to be recorded.
	 * 
	 * @param session The session
	 * @param mesg The optional message
	 * @return The session text
	 */
	public String getSessionIdentityText(Session session, Message mesg) {
		StringBuffer identities=new StringBuffer();
		
		if (session != null && session.getSessionIdentity() != null) {
			identities.append("<sd:"+SESSION_IDENTITY_NODE+">"+
					"<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
					session.getSessionIdentity().getName()+"\" >"+
					XMLUtils.encodeXMLString(session.getSessionIdentity().getFullId())+
					"</sd:"+IDENTITY_NODE+">"+
					"</sd:"+SESSION_IDENTITY_NODE+">");
		}
		
		java.util.Iterator<Identity> ids=null;
		
		if (session != null) {
			ids = session.getPrimaryIdentities().iterator();
		}
		
		// If session has no identity information, but a
		// message is supplied, then try getting identity
		// from it.
		if ((ids == null || ids.hasNext() == false) &&
				mesg != null && mesg.getMessageIdentities() != null) {
			ids = mesg.getMessageIdentities().iterator();
		}
		
		if (ids != null && ids.hasNext()) {
			identities.append("<sd:"+PRIMARY_IDENTITIES_NODE+">");
			
			while (ids.hasNext()) {
				Identity cur=ids.next();
				
				identities.append("<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
						cur.getName()+"\" >");
				identities.append(XMLUtils.encodeXMLString(cur.getFullId()));
				identities.append("</sd:"+IDENTITY_NODE+">");
			}
			
			identities.append("</sd:"+PRIMARY_IDENTITIES_NODE+">");
		}
		
		return(identities.toString());
	}
	
	/**
	 * This method returns the XML text associated with the
	 * message.
	 * 
	 * @param mesg The message
	 * @param defn The message definition from the description
	 * @return The message text
	 */
	public String getMessageText(Message mesg, MessageDefinition defn) {
		String type=null;
		
		if (defn == null) {
			type = (mesg.isRequest()?REQUEST_RPC_MESSAGE_TYPE:RESPONSE_RPC_MESSAGE_TYPE);

			if (mesg.isRPCStyle() == false) {
				type = MESSAGE_STYLE_MESSAGE_TYPE;
			}
		} else {
			type = (defn.getClassification()==MessageClassification.REQUEST?
					REQUEST_RPC_MESSAGE_TYPE:RESPONSE_RPC_MESSAGE_TYPE);
		}
		
		if (NamesUtil.isSet(mesg.getFaultName())) {
			type = FAULT_RPC_MESSAGE_TYPE;
		}
		
		return(getMessageTextForType(mesg, type));
	}
	
	/**
	 * This method returns the XML text associated with the
	 * message.
	 * 
	 * @param mesg The message
	 * @param type The message classification (request/response/message/fault)
	 * @return The message text
	 */
	protected String getMessageTextForType(Message mesg, String type) {
		String ret=null;
		String fault="";
		String messageType="";
		String identities="";
		String valueText=getMessagePayload(mesg);
		String endpoint="";
		
		if (NamesUtil.isSet(mesg.getFaultName())) {
			fault = FAULT_ATTR+"=\""+mesg.getFaultName()+"\" ";
		}
		
		if (NamesUtil.isSet(mesg.getType())) {
			messageType = MESSAGE_TYPE_ATTR+"=\""+mesg.getType()+"\" ";
		}
		
		if (mesg.getSessionIdentity() != null) {
			identities = "<sd:"+SESSION_IDENTITY_NODE+">"+
					"<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
					mesg.getSessionIdentity().getName()+"\" >"+
					mesg.getSessionIdentity().getFullId()+
					"</sd:"+IDENTITY_NODE+">"+
					"</sd:"+SESSION_IDENTITY_NODE+">";
			
			if (mesg.getChannelIdentity() != null) {
				identities += "<sd:"+CHANNEL_IDENTITY_NODE+">"+
						"<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
						mesg.getChannelIdentity().getName()+"\" >"+
						mesg.getChannelIdentity().getFullId()+
						"</sd:"+IDENTITY_NODE+">"+
						"</sd:"+CHANNEL_IDENTITY_NODE+">";
			}
		} else if (mesg.getMessageIdentities() != null &&
				mesg.getMessageIdentities().size() > 0) {

			identities += "<sd:"+PRIMARY_IDENTITIES_NODE+">";
			
			for (int i=0; i < mesg.getMessageIdentities().size(); i++) {
				Identity id=(Identity)mesg.getMessageIdentities().get(i);

				identities += "<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
						id.getName()+"\" >";
				identities += id.getFullId();
				identities += "</sd:"+IDENTITY_NODE+">";
			}
			
			identities += "</sd:"+PRIMARY_IDENTITIES_NODE+">";
		}
		
		if (mesg.getServiceEndpoint() != null) {
			endpoint = mesg.getServiceEndpoint().toText(null);
		}
		
		String serviceType=mesg.getServiceType();
		if (serviceType == null) {
			serviceType = "";
		}
		
		ret = "<sd:"+MESSAGE_NODE+" "+OPERATION_ATTR+"=\""+
				(mesg.getOperationName()==null?"":mesg.getOperationName())+
				"\" "+TYPE_ATTR+"=\""+type+"\" "+fault+
				messageType+SERVICE_TYPE_ATTR+"=\""+
				serviceType+"\" ><sd:"+ENDPOINT_NODE+">"+
				endpoint+"</sd:"+ENDPOINT_NODE+">"+identities+
				valueText+"</sd:"+MESSAGE_NODE+">";
		
		return(ret);
	}
	
	/**
	 * This method constructs the message payload value for the
	 * send and receive tracker events. The information is only
	 * provided if the service tracker is configured to supply
	 * it, as the performance impact of transporting large
	 * message payloads in tracker messages would not be appropriate
	 * for production environments - however it may be useful
	 * in development and test environments. If the supplied
	 * message is a multi-part message containing a single part
	 * then the single part will be extracted and used. If a
	 * multi-part message with multiple parts, then the map
	 * textual representation will be provided. If the value is
	 * a DOM representation, then it will be converted to a
	 * string. All other situations will rely on the 'toString'
	 * representation of the value to be appropriate.
	 * 
	 * @param mesg The message
	 * @return The payload
	 */
	@SuppressWarnings("rawtypes")
	protected String getMessagePayload(Message mesg) {
		String ret="";
		
		if (m_recordMessagePayload) {
			Object value=mesg.getValue();
			if (mesg.isMultiPart() && ((java.util.Map)value).size() == 1) {
				value = ((java.util.Map)value).values().iterator().next();
			}
			
			if (value instanceof org.w3c.dom.Node) {
				try {
					value = XMLUtils.getText((org.w3c.dom.Node)value);
				} catch(Exception e) {
					logger.severe("Failed to convert message DOM value" +
							"to string: "+e);
					value = "";
				}
			}
			
			if (value != null) {
				//ret = "<sd:"+VALUE_NODE+">"+value.toString()+
				//			"</sd:"+VALUE_NODE+">";
				ret = value.toString();
			}
		}

		return(ret);
	}
	
	/**
	 * This method returns the XML text associated with the
	 * channel.
	 * 
	 * @param channel The channel
	 * @return The channel text
	 */
	protected String getChannelText(Channel channel) {
		
		if (channel == null) {
			return("");
		}
		
		StringBuffer identities=new StringBuffer();
		
		java.util.Set<Identity> ids=channel.getPrimaryIdentities();
		
		if (ids != null && ids.size() > 0) {
			identities.append("<sd:"+PRIMARY_IDENTITIES_NODE+">");
			
			java.util.Iterator<Identity> iter=ids.iterator();
			while (iter.hasNext()) {
				Identity id=iter.next();
				
				identities.append("<sd:"+IDENTITY_NODE+" "+NAME_ATTR+"=\""+
						id.getName()+"\" >");
				identities.append(XMLUtils.encodeXMLString(id.getFullId()));
				identities.append("</sd:"+IDENTITY_NODE+">");
			}
			
			identities.append("</sd:"+PRIMARY_IDENTITIES_NODE+">");
		}
		
		return("<sd:"+CHANNEL_NODE+" "+NAME_ATTR+"=\""+
				channel.getName()+"\" "+TYPE_ATTR+"=\""+
				channel.getType()+"\" >"+
				identities.toString()+"</sd:"+CHANNEL_NODE+">");
	}
	
	/**
	 * This method processes the supplied value to deal with any
	 * unwanted characters that would be inappropriate for an
	 * XML attribute value.
	 * 
	 * @param value The value
	 * @return The processed value
	 */
	protected String processAttributeContents(String value) {
		String ret=value;
		
		ret = ret.replaceAll("\"", "");
		
		return(ret);
	}
	
	/**
	 * This method records the tracker information. This method
	 * also takes the optional 'message', used to derive session
	 * identity, if the session does not currently have any
	 * identity. This situation should only occur if the first
	 * message to a service is an unexpected message. 
	 * 
	 * @param serviceName The service name
	 * @param serviceVersion The optional service version
	 * @param session The session
	 * @param mesg The message
	 * @param xml The information to be recorded
	 * @param type The type of information (INFO, WARNING, ERROR)
	 * @param exc The optional exception
	 */
	protected void record(String serviceName, String serviceVersion,
			Session session, Message mesg, String xml, String type, Throwable exc) {
		
		String message=build(serviceName, serviceVersion,
						session, mesg, xml);
			
		publish(serviceName, session, message);
	}
	
	/**
	 * This method records the tracker information.
	 * 
	 * @param serviceName The service name
	 * @param session The session
	 * @param xml The information to be recorded
	 * @param type The type of information (INFO, WARNING, ERROR)
	 * @param exc The optional exception
	 */
	protected void record(String serviceName, Session session,
				String xml, String type, Throwable exc) {
		record(serviceName, null, session, null, xml, type, exc);
	}
	
	/**
	 * This method constructs the service tracker message to be
	 * published.
	 * 
	 * @param serviceName The service name
	 * @param serviceVersion The optional service version
	 * @param session The session
	 * @param mesg The optional message
	 * @param xml The XML for the current tracker event
	 * @return The message
	 */
	protected String build(String serviceName, String serviceVersion,
			Session session, Message mesg, String xml) {
		StringBuffer ret=new StringBuffer();
		String versionText="";
		String nameText="";
		
		if (serviceName != null) {
			nameText = NAME_ATTR+"=\""+serviceName+"\" ";
		}
		
		if (serviceVersion != null) {
			versionText=VERSION_ATTR+"=\""+serviceVersion+"\" ";
			
		} else if (session != null && session.getId() != null &&
				session.getId().getServiceDescriptionVersion() != null) {
			versionText=VERSION_ATTR+"=\""+
					session.getId().getServiceDescriptionVersion()+
						"\" ";
		}
		
		ret.append("<sd:record xmlns:sd=\""+
				SAVARA_SERVICE_TRACKER_NS+"\" "+nameText+versionText+">");
		
		ret.append(getSessionIdentityText(session, mesg));
		
		ret.append("<sd:events>");
		
		ret.append(xml);
		
		ret.append("</sd:events>");
		ret.append("</sd:record>");
		
		return(ret.toString());
	}

	/**
	 * This method determines whether message payload will be
	 * recorded.
	 * 
	 * @param bool Whether the message payload is recorded
	 */
	public void setRecordMessagePayload(String bool) {
		logger.info("Record message payload: "+bool);
		
		if (bool != null && bool.equalsIgnoreCase("true")) {
			m_recordMessagePayload = true;
		} else {
			m_recordMessagePayload = false;
		}
	}
	
    private static Logger logger = Logger.getLogger(JMSServiceTracker.class.getName());	

    private boolean m_recordMessagePayload=false;
    
	public static final String RECORD_MESSAGE_PAYLOAD = "pi4soa.tracker.messagePayload";

	public static final String INFO="info";
	public static final String WARNING="warning";
	public static final String ERROR="error";	
	
	public static final String MESSAGE_NODE="message";
	public static final String DETAILS_NODE="details";
	public static final String OPERATION_ATTR="operation";
	public static final String FAULT_ATTR="fault";
	public static final String NAME_ATTR="name";
	public static final String TIMESTAMP_ATTR="timestamp";
	public static final String TYPE_ATTR="type";
	public static final String MESSAGE_TYPE_ATTR="messageType";
	public static final String SERVICE_TYPE_ATTR="serviceType";
	public static final String EXCEPTION_NODE="exception";
	public static final String SERVICE_INSTANCE_ID_ATTR="serviceInstanceId";
	public static final String SESSION_ID_ATTR="sessionId";
	public static final String PARENT_SESSION_ID_ATTR="parentSessionId";
	
	public static final String VERSION_ATTR = "version";
	public static final String FAULT_RPC_MESSAGE_TYPE = "fault";
	public static final String RESPONSE_RPC_MESSAGE_TYPE = "response";
	public static final String REQUEST_RPC_MESSAGE_TYPE = "request";
	public static final String MESSAGE_STYLE_MESSAGE_TYPE = "message";
	public static final String SESSION_IDENTITY_NODE = "sessionIdentity";
	public static final String CHANNEL_IDENTITY_NODE = "channelIdentity";
	public static final String IDENTITY_NODE = "identity";
	public static final String PRIMARY_IDENTITIES_NODE = "primaryIdentities";
	public static final String ENDPOINT_NODE = "endpoint";
	public static final String CHANNEL_NODE = "channel";
	public static final String VALUE_NODE = "value";

	protected static final String TRACKER_JNDI_INITIAL_CONTEXT_FACTORY_PROPERTY = "pi4soa.tracker.jndi.initialContextFactory";
	protected static final String TRACKER_JNDI_PROVIDER_URL_PROPERTY = "pi4soa.tracker.jndi.providerURL";
	protected static final String TRACKER_JNDI_FACTORY_URL_PACKAGES_PROPERTY = "pi4soa.tracker.jndi.factoryURLPackages";
	protected static final String TRACKER_JMS_TOPIC_CONNECTION_FACTORY_PROPERTY = "pi4soa.tracker.jms.topicConnectionFactory";
	protected static final String TRACKER_JMS_TOPIC_PROPERTY = "pi4soa.tracker.jms.topic";
	protected static final String TRACKER_JMS_CONNECTION_FACTORY_PROPERTY = "pi4soa.tracker.jms.connectionFactory";
	protected static final String TRACKER_JMS_CONNECTION_FACTORY_ALTERNATE_PROPERTY = "pi4soa.tracker.jms.connectionFactoryAlternate";
	protected static final String TRACKER_JMS_DESTINATION_PROPERTY = "pi4soa.tracker.jms.destination";
	protected static final String TRACKER_JMS_CLIENT_ID_PROPERTY = "pi4soa.tracker.jms.clientId";
	protected static final String TRACKER_JMS_DURABLE_SUBSCRIPTION_PROPERTY = "pi4soa.tracker.jms.durableSubscription";

	private boolean m_initialized=false;
	private String m_jndiInitialContextFactory=null;
	private String m_jndiProviderURL=null;
	private String m_jndiFactoryURLPackages=null;
	private String m_jmsConnectionFactory=null;
	private String m_jmsConnectionFactoryAlternate=null;
	private String m_jmsDestination=null;
	
	private javax.jms.Connection m_connection=null;
	private javax.jms.Session m_session=null;
	private javax.jms.MessageProducer m_producer=null;
}
