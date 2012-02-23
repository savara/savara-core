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
package org.savara.bpel.generator;

import org.savara.bpel.BPELDefinitions;
import org.savara.bpel.internal.model.change.BPELModelChangeContext;
import org.savara.bpel.model.TProcess;
import org.savara.bpel.util.BPELModelUtil;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.common.model.generator.ModelGenerator;
import org.savara.common.resources.ResourceLocator;
import org.scribble.protocol.model.*;

/**
 * This class represents the Protocol to BPEL implementation of the model
 * generator interface.
 */
public class ProtocolToBPELModelGenerator implements ModelGenerator {

	/**
	 * This method determines whether the generator is appropriate for
	 * the specified source and target types.
	 * 
	 * @param source The source
	 * @param targetType The target type
	 * @return Whether the specified types are supported
	 */
	public boolean isSupported(Object source, String targetType) {
		return(source instanceof ProtocolModel &&
				((ProtocolModel)source).isLocated() &&
							targetType.equals(BPELDefinitions.BPEL_TYPE));
	}

	public Object generate(Object source, FeedbackHandler handler, ResourceLocator locator) {
		BPELModelChangeContext context=
			new BPELModelChangeContext(null, new DefaultFeedbackHandler());
		ProtocolModel pm=(ProtocolModel)source;
		
		// SAVARA-175:
		// Add namespace prefix mapping to a BPEL process defined in text and
		// deserialize the process to create the initial model. This is the only
		// way to enable the namespace prefix mapping info to be associated with
		// the top level element in the exported text representation, without
		// having to resort to using internal Sun classes that change between
		// JDK versions.
		String process="<process xmlns=\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\" ";
		
		java.util.Map<String, String> prefixes=
			new java.util.HashMap<String, String>();
	
		java.util.List<Annotation> list=
				AnnotationDefinitions.getAnnotations(pm.getProtocol().getAnnotations(),
				AnnotationDefinitions.TYPE);
	
		for (Annotation annotation : list) {
			if (annotation.getProperties().containsKey(AnnotationDefinitions.NAMESPACE_PROPERTY) &&
					annotation.getProperties().containsKey(AnnotationDefinitions.PREFIX_PROPERTY)) {
				prefixes.put((String)annotation.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY),
						(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
				
				process += "xmlns:"+(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY)+
						"=\""+(String)annotation.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY)+"\" ";
			}
		}
		
		process += "/>";
		
		// Create BPEL model
		TProcess bpel=null;
		
		try {
			java.io.ByteArrayInputStream io=new java.io.ByteArrayInputStream(process.getBytes());
			
			bpel = BPELModelUtil.deserialize(io);
			
			io.close();
		} catch(Exception e) {
			handler.error("Failed to create initial BPEL process", null);
		}
		
		context.setParent(bpel);
		
		ProtocolModel bpelModel=new ProtocolModel();
		bpelModel.getProperties().put(BPELDefinitions.BPEL_MODEL_PROPERTY, bpel);
		
		context.insert(bpelModel, pm, null);
		
		return(bpel);
	}
	
}
