/*
 * Copyright 2007-9 Pi4 Technologies Ltd
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
 * 6 May 2009 : Initial version created by gary
 */
package org.savara.pi4soa.cdm.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.pi4soa.cdm.parser.rules.ConverterContext;
import org.savara.pi4soa.cdm.parser.rules.ConverterRule;
import org.savara.pi4soa.cdm.parser.rules.ConverterRuleFactory;
import org.savara.pi4soa.cdm.parser.rules.DefaultConverterContext;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.parser.AnnotationProcessor;
import org.scribble.protocol.parser.ProtocolParser;

/**
 * This class provides the model parser for the CDM notation.
 * 
 */
public class CDMProtocolParser implements ProtocolParser {

	/**
	 * The default constructor.
	 */
	public CDMProtocolParser() {
	}
	
	public boolean isSupported(String sourceType) {
		return("cdm".equals(sourceType));
	}
	
	public ProtocolModel parse(java.io.InputStream is, Journal journal, ProtocolContext context)
							throws java.io.IOException {
		ProtocolModel ret=null;
		
		if (is != null) {
			try {						
				org.pi4soa.cdl.Package cdlpack=
						org.pi4soa.cdl.CDLManager.load(is);
		
				ConverterRule rule=ConverterRuleFactory.getConverter(ProtocolModel.class,
									cdlpack);
			
				if (rule != null) {
					ConverterContext cctxt=
						new DefaultConverterContext(journal, context);
					
					ret = (ProtocolModel)rule.convert(cctxt,
							ProtocolModel.class, cdlpack);
				}
			
			} catch(Exception e) {
				logger.log(Level.SEVERE,
						"Failed to load model", e);
			}
		
			if (logger.isLoggable(java.util.logging.Level.FINEST)) {
				
				org.scribble.protocol.export.text.TextProtocolExporter exporter=
							new org.scribble.protocol.export.text.TextProtocolExporter();
				
				try {
					java.io.ByteArrayOutputStream os=
						new java.io.ByteArrayOutputStream();
					
					exporter.export(ret, journal, os);
					
					String str=new String(os.toByteArray());
					
					System.out.println("EXPORTED CDM TEXT:");
					System.out.println(str);
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		return(ret);
	}

	public void setAnnotationProcessor(AnnotationProcessor ap) {
		// Not required
	}

	private static Logger logger = Logger.getLogger("org.pi4soa.scribble.cdm.parser");
}
