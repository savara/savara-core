/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 * Copyright 2012 Red Hat, Inc.
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
 * 13 Dec 2008 : Initial version created by gary
 * Feb 2012 : Update based on scribble v2
 */
package org.savara.protocol.model.stateless;

import org.scribble.protocol.model.*;

/**
 * This class provides the protocol model specific
 * stateless transformation rule.
 */
public class ProtocolModelStatelessTransformationRule
					extends AbstractStatelessTransformationRule {

	/**
	 * This method determines whether the stateless
	 * transformation rule is applicable to the
	 * supplied model object.
	 * 
	 * @param modelObject The model object
	 * @return Whether the model object can be transformed
	 */
	public boolean isSupported(ModelObject modelObject) {
		return(modelObject instanceof ProtocolModel);
	}
	
	/**
	 * This method transforms the supplied model object into
	 * a stateless equivalent.
	 * 
	 * @param context The context
	 * @param modelObject The model object to transform
	 * @return The transformed object
	 */
	public ModelObject transform(StatelessTransformationContext context,
						ModelObject modelObject) {
		ProtocolModel src=(ProtocolModel)modelObject;
		ProtocolModel ret=new ProtocolModel();
		
		ret.derivedFrom(src);
		
		// Transform (copy) the import statements
		for (ImportList il : src.getImports()) {
			if (il instanceof TypeImportList) {
				ret.getImports().add(new TypeImportList((TypeImportList)il));
			} else if (il instanceof ProtocolImportList) {
				ret.getImports().add(new ProtocolImportList((ProtocolImportList)il));
			}
		}
		
		ret.setProtocol((Protocol)context.transform(src.getProtocol()));
		
		return(ret);
	}
}
