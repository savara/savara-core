/*
 * Copyright 2009 www.scribble.org
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
 */
package org.savara.protocol.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.scribble.protocol.export.ProtocolExportManager;
import org.scribble.protocol.parser.DefaultProtocolParserManager;
import org.scribble.protocol.parser.ProtocolParser;
import org.scribble.protocol.parser.ProtocolParserManager;
import org.scribble.protocol.projection.ProtocolProjector;
import org.scribble.protocol.validation.ProtocolValidationManager;
//import org.scribble.protocol.monitor.ProtocolMonitor;

/**
 * This class provides a manager for accessing services used
 * by the designer.
 *
 */
public class ProtocolServices {
	
	private static final Logger LOG=Logger.getLogger(ProtocolServices.class.getName());
	
	private static ProtocolValidationManager m_validationManager=null;
	private static ProtocolParserManager m_parserManager=null;
	private static ProtocolProjector m_protocolProjector=null;
	//private static ProtocolMonitor m_protocolMonitor=null;
	private static ProtocolExportManager m_protocolExportManager=null;
	
	private static String[] PARSER_CLASS_NAMES = {
		"org.scribble.protocol.parser.antlr.ANTLRProtocolParser",
		"org.savara.bpel.parser.BPELProtocolParser",
		"org.savara.bpmn2.parser.choreo.BPMN2ChoreographyProtocolParser",
		"org.savara.pi4soa.cdm.parser.CDMProtocolParser"
	};

	public static ProtocolValidationManager getValidationManager() {
		return(m_validationManager);
	}
	
	public static void setValidationManager(ProtocolValidationManager vm) {
		m_validationManager = vm;
	}
	
	public static ProtocolParserManager getParserManager() {
		if (m_parserManager == null) {
			m_parserManager = new DefaultProtocolParserManager();
			
			for (String className : PARSER_CLASS_NAMES) {
				// Instantiate default parser
				try {
					Class<?> cls=Class.forName(className);
					
					Object obj=cls.newInstance();
					
					if (obj instanceof ProtocolParser) {
						ProtocolParser pp=(ProtocolParser)obj;
						pp.setAnnotationProcessor(new org.savara.protocol.parser.AnnotationProcessor());
						
						m_parserManager.getParsers().add(pp);
					}
				} catch(ClassNotFoundException cfne) {
					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine("Protocol parser '"+className+"' was not found");
					}
				} catch(Exception e) {
					LOG.log(Level.SEVERE, "Failed to load protocol parser '"+className+"': "+e, e);
				}
			}
		}
		
		return(m_parserManager);
	}
	
	public static void setParserManager(ProtocolParserManager pm) {
		m_parserManager = pm;
	}
	
	/*
	public static ProtocolMonitor getProtocolMonitor() {
		return(m_protocolMonitor);
	}
	
	public static void setProtocolMonitor(ProtocolMonitor parser) {
		m_protocolMonitor = parser;
	}
	*/
	
	public static ProtocolProjector getProtocolProjector() {
		if (m_protocolProjector == null) {
			// Instantiate default projector
			try {
				Class<?> cls=Class.forName("org.scribble.protocol.projection.impl.ProtocolProjectorImpl");
				
				Object obj=cls.newInstance();
				
				if (obj instanceof ProtocolProjector) {
					m_protocolProjector = (ProtocolProjector)obj;
					
					((org.scribble.protocol.projection.impl.ProtocolProjectorImpl)m_protocolProjector).
							getCustomRules().add(new org.savara.protocol.projection.JoinProjectorRule());
					((org.scribble.protocol.projection.impl.ProtocolProjectorImpl)m_protocolProjector).
							getCustomRules().add(new org.savara.protocol.projection.SyncProjectorRule());
				}
			} catch(Exception e) {
				// Ignore
			}
		}
		
		return(m_protocolProjector);
	}
	
	public static void setProtocolProjector(ProtocolProjector projector) {
		m_protocolProjector = projector;
	}
	
	public static ProtocolExportManager getProtocolExportManager() {
		return(m_protocolExportManager);
	}
	
	public static void setProtocolExportManager(ProtocolExportManager pem) {
		m_protocolExportManager = pem;
	}	
}