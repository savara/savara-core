/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpmn2.model.util;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBElement;

import org.savara.bpmn2.model.ObjectFactory;
import org.savara.bpmn2.model.TActivity;
import org.savara.bpmn2.model.TComplexGateway;
import org.savara.bpmn2.model.TDataAssociation;
import org.savara.bpmn2.model.TDataObjectReference;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TExclusiveGateway;
import org.savara.bpmn2.model.TInclusiveGateway;
import org.savara.bpmn2.model.TInputOutputBinding;
import org.savara.bpmn2.model.TSequenceFlow;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * This class provides utility functions for dealing with the JSON
 * representation of a BPMN2 model.
 *
 */
public class BPMN2ModelJSONUtil {

	private static final ObjectMapper MAPPER=new ObjectMapper();
	
	private static final ObjectFactory FACTORY=new ObjectFactory();
	
	static {
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		//MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		MAPPER.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		
	    MAPPER.addMixInAnnotations(TActivity.class, TActivityMixIn.class);
	    MAPPER.addMixInAnnotations(TComplexGateway.class, TComplexGatewayMixIn.class);
	    MAPPER.addMixInAnnotations(TDataAssociation.class, TDataAssociationMixIn.class);
	    MAPPER.addMixInAnnotations(TDataObjectReference.class, TDataObjectReferenceMixIn.class);
	    MAPPER.addMixInAnnotations(TExclusiveGateway.class, TExclusiveGatewayMixIn.class);
	    MAPPER.addMixInAnnotations(TInclusiveGateway.class, TInclusiveGatewayMixIn.class);
	    MAPPER.addMixInAnnotations(TInputOutputBinding.class, TInputOutputBindingMixIn.class);
	    MAPPER.addMixInAnnotations(TSequenceFlow.class, TSequenceFlowMixIn.class);

	    SimpleModule testModule = new SimpleModule("BPMN2Module", new Version(1, 0, 0, null, null, null))
	    		.addDeserializer(JAXBElement.class, new JAXBElementDeserializer());
	    MAPPER.registerModule(testModule);
	    
	}
	
	/**
	 * This method deserializes the JSON representation of the BPMN2 model.
	 * 
	 * @param is The input stream
	 * @return The BPMN2 definition
	 * @throws IOException Failed to deserialize
	 */
	public static TDefinitions deserialize(java.io.InputStream is) throws IOException {
		return (MAPPER.readValue(is, TDefinitions.class));
	}
	
	/**
	 * This method serializes the BPMN2 model into a JSON representation.
	 * 
	 * @param defns The BPMN2 model
	 * @param os The output stream
	 * @throws IOException Failed to serialize
	 */
	public static void serialize(TDefinitions defns, java.io.OutputStream os) throws IOException {
		MAPPER.writeValue(os, defns);
	}

	/**
	 * A deserializer specifically implemented to handle the JAXBElement wrapper
	 * around some model objects.
	 *
	 */
	public static class JAXBElementDeserializer extends JsonDeserializer<JAXBElement<?>> {

		@Override
		public JAXBElement<?> deserialize(JsonParser jp,
				DeserializationContext context) throws IOException,
				JsonProcessingException {
			JAXBElement<?> ret=null;
			
			JsonNode tree=MAPPER.readTree(jp);
			
			JsonNode typeNode=tree.get("declaredType");
			
			JsonNode valueNode=tree.get("value");
			
			String declaredType=typeNode.asText();
			
			try {
				Class<?> cls=Class.forName(declaredType);
				
				Object elem=MAPPER.convertValue(valueNode, cls);
				
				// Create JAXBElement wrapped version of the element
				String methodName="create";
				
				if (cls.getSimpleName().charAt(0) == 'T') {
					methodName += cls.getSimpleName().substring(1);
				} else {
					methodName += cls.getSimpleName();
				}
				
				Method method=ObjectFactory.class.getMethod(methodName, cls);
				
				if (method != null) {
					ret = (JAXBElement<?>)method.invoke(FACTORY, elem);
				}
			} catch(Exception e) {
				throw new IOException("Failed to deserialize object of type '"+declaredType+"'", e);
			}
			
			return (ret);
		}
		
	}
	
	abstract class TActivityMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object _default;
	}
	
	abstract class TComplexGatewayMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object _default;
	}
	
	abstract class TDataAssociationMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object targetRef;
	}
	
	abstract class TDataObjectReferenceMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object dataObjectRef;
	}
	
	abstract class TExclusiveGatewayMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object _default;
	}
	
	abstract class TInclusiveGatewayMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object _default;
	}
	
	abstract class TInputOutputBindingMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object inputDataRef;
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object outputDataRef;
	}
	
	abstract class TSequenceFlowMixIn {
		
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object sourceRef;
	    
		@JsonTypeInfo(  
			    use = JsonTypeInfo.Id.CLASS,  
			    include = JsonTypeInfo.As.PROPERTY,  
			    property = "@type")  
	    protected Object targetRef;

	}
}
