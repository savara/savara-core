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

import org.savara.pi4soa.cdm.parser.rules.ParserContext;
import org.savara.pi4soa.cdm.parser.rules.ParserRule;
import org.savara.pi4soa.cdm.parser.rules.ParserRuleFactory;
import org.savara.pi4soa.cdm.parser.rules.DefaultParserContext;
import org.savara.protocol.util.FeedbackHandlerProxy;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.parser.AnnotationProcessor;
import org.scribble.protocol.parser.ProtocolParser;

/**
 * This class provides the model parser for the CDM notation.
 * 
 */
public class CDMProtocolParser implements ProtocolParser {

	private static Logger logger = Logger.getLogger(CDMProtocolParser.class.getName());

	/**
	 * The default constructor.
	 */
	public CDMProtocolParser() {
	}
	
	public boolean isSupported(Content content) {
		return(content.hasExtension("cdm"));
	}
	
	public ProtocolModel parse(ProtocolContext context, Content content, Journal journal)
							throws java.io.IOException {
		ProtocolModel ret=null;
		
		if (content != null) {
			try {						
				java.io.InputStream is=content.getInputStream();
				
				org.pi4soa.cdl.Package cdlpack=
						org.pi4soa.cdl.CDLManager.load(is);
		
				is.close();
				
				ParserRule rule=ParserRuleFactory.getConverter(ProtocolModel.class,
									cdlpack);
			
				if (rule != null) {
					ParserContext cctxt=
						new DefaultParserContext(new FeedbackHandlerProxy(journal));
					
					ret = (ProtocolModel)rule.parse(cctxt,
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
}
