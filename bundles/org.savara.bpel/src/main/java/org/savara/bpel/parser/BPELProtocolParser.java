/*
 * Copyright 2005-8 Pi4 Technologies Ltd
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
 * 24 Jul 2008 : Initial version created by gary
 */
package org.savara.bpel.parser;

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.model.TPartnerLink;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.parser.rules.DefaultParserContext;
import org.savara.bpel.util.BPELModelUtil;
import org.savara.protocol.util.FeedbackHandlerProxy;
import org.savara.protocol.util.SavaraResourceLocatorProxy;
import org.scribble.common.logging.Journal;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.*;
import org.scribble.protocol.parser.AnnotationProcessor;
import org.scribble.protocol.parser.ProtocolParser;

/**
 * This class represents the BPEL to Protocol implementation of the model
 * generator interface.
 */
public class BPELProtocolParser implements ProtocolParser {

	public boolean isSupported(String sourceType) {
		return(sourceType.equals(BPELDefinitions.BPEL_TYPE));
	}

	public ProtocolModel parse(java.io.InputStream is, Journal journal, ProtocolContext context)
									throws java.io.IOException {
		ProtocolModel ret=new ProtocolModel();
		
		// Load BPEL from the input stream
		
		TProcess process=BPELModelUtil.deserialize(is);
		
		//ret.derivedFrom(this);
		//ret.getBlock().derivedFrom(this);
		
		Protocol protocol=new Protocol();
		ret.setProtocol(protocol);
		
		// Configure model name
		protocol.setName(process.getName());
				
		// Define implements reference for conversation type
		//String convType=getConversationType();
		String role=null;
		
		// Search the partner links to see if a 'myRole' has been defined
		if (process.getPartnerLinks() != null) {
			for (TPartnerLink pl : process.getPartnerLinks().getPartnerLink()) {
				if (pl.getMyRole() != null && pl.getMyRole().trim().length() > 0) {
					role = pl.getMyRole();
					protocol.setRole(new Role(role));
					break;
				}
			}
		}
		
		// Convert the process contents
		DefaultParserContext convContext=new DefaultParserContext(role, process,
							new SavaraResourceLocatorProxy(context.getResourceLocator()));
		
		convContext.parse(process, protocol.getBlock().getContents(),
							new FeedbackHandlerProxy(journal));
		
		return(ret);
	}

	public void setAnnotationProcessor(AnnotationProcessor ap) {
		// Not required
	}
	
}
