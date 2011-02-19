/*
 * Copyright 2009-10 www.scribble.org
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
 */
package org.savara.scenario.util;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.savara.scenario.model.Scenario;

public class ScenarioModelUtil {

	public static Scenario deserialize(java.io.InputStream is) throws IOException {
		Scenario ret=null;
		
		try {
			JAXBContext context = JAXBContext.newInstance("org.savara.scenario.model",
					ScenarioModelUtil.class.getClassLoader());
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			//note: setting schema to null will turn validator off
			//unmarshaller.setSchema(null);
			Object xmlObject = unmarshaller.unmarshal(is);
			
			if (xmlObject instanceof JAXBElement) {
				ret = (Scenario)((JAXBElement<?>)xmlObject).getValue();
			}
			
		} catch(Exception e) {
			throw new IOException("Failed to deserialize scenario", e);
		}
		
		return(ret);
	}
	
	public static void serialize(Scenario scenario, java.io.OutputStream os) throws IOException {
		
		try {
			org.savara.scenario.model.ObjectFactory factory=
						new org.savara.scenario.model.ObjectFactory();
			
			JAXBContext context = JAXBContext.newInstance(Scenario.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			marshaller.marshal(factory.createScenario(scenario), os);
		} catch(Exception e) {
			throw new IOException("Failed to serialize scenario", e);
		}
	}
}
