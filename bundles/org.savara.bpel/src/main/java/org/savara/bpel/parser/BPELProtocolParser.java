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
import org.savara.bpel.model.TImport;
import org.savara.bpel.model.TPartnerLink;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.parser.rules.DefaultParserContext;
import org.savara.bpel.util.BPELModelUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.util.FeedbackHandlerProxy;
import org.savara.protocol.util.SavaraResourceLocatorProxy;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.*;
import org.scribble.protocol.parser.AnnotationProcessor;
import org.scribble.protocol.parser.ProtocolParser;

/**
 * This class represents the BPEL to Protocol implementation of the model
 * generator interface.
 */
public class BPELProtocolParser implements ProtocolParser {

	public boolean isSupported(Content content) {
		return(content.hasExtension(BPELDefinitions.BPEL_TYPE));
	}

	public ProtocolModel parse(ProtocolContext context, Content content, Journal journal)
									throws java.io.IOException {
		ProtocolModel ret=new ProtocolModel();
		
		// Load BPEL from the input stream
		java.io.InputStream is=content.getInputStream();
		
		TProcess process=BPELModelUtil.deserialize(is);
		
		is.close();
		
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
					protocol.setLocatedRole(new Role(role));
					break;
				}
			}
		}
		
		// Parse the imports
		parseImports(process, protocol);
		
		// Convert the process contents
		DefaultParserContext convContext=new DefaultParserContext(role, process, ret,
							new SavaraResourceLocatorProxy(context.getResourceLocator()));
		
		convContext.parse(process, protocol.getBlock().getContents(),
							new FeedbackHandlerProxy(journal));
		
		return(ret);
	}
	
	protected void parseImports(TProcess process, Protocol protocol) {
		
		for (TImport imp : process.getImport()) {
			
			Annotation annotation=new Annotation(AnnotationDefinitions.TYPE);
			
			if (imp.getLocation() != null) {
				annotation.getProperties().put(AnnotationDefinitions.LOCATION_PROPERTY,
						imp.getLocation());
			}
			
			if (imp.getNamespace() != null) {
				annotation.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
						imp.getNamespace());
			}
			
			protocol.getAnnotations().add(annotation);
		}
	}

	public void setAnnotationProcessor(AnnotationProcessor ap) {
		// Not required
	}
	
}
