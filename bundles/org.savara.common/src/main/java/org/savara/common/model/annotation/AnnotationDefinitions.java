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
package org.savara.common.model.annotation;

/**
 * This class provides annotation definitions and helper functions.
 *
 */
public class AnnotationDefinitions {

	// General annotation property names
	public static final String NAME_PROPERTY="name";
	public static final String ROLE_PROPERTY="role";
	public static final String ID_PROPERTY="id";
	public static final String PREFIX_PROPERTY="prefix";
	public static final String NAMESPACE_PROPERTY="namespace";
	public static final String LOCATION_PROPERTY="location";
	public static final String REQUEST_PROPERTY = "request";
	public static final String REPLY_TO_PROPERTY = "replyTo";
	public static final String EXPRESSION_PROPERTY="expression";
	public static final String LANGUAGE_PROPERTY="language";
		
	
	// Protocol annotation (has namespace property)
	// Defines protocol level properties
	public static final String PROTOCOL = "Protocol";

	// Interface annotation (has name property, and optional namespace and role properties)
	// Can be used at the protocol level, to define the interface qname associated with
	// a role, and also at the interaction level to indicate the specific interface name
	// where a role has multiple interfaces.
	public static final String INTERFACE = "Interface";
		
	// Fault annotation (has name property)
	public static final String FAULT = "Fault";

	// XSD sub types
	public static final String XSD_ELEMENT = "XSDElement";
	public static final String XSD_TYPE = "XSDType";
	
	// Correlation properties (request and replyTo)
	public static final String CORRELATION = "Correlation";

	// Type annotation and properties (prefix, namespace and location)
	public static final String TYPE="Type";

	// Source Component annotation (uses id property, and optional language property)
	public static final String SOURCE_COMPONENT="SourceComponent";

	// Assertion properties (expression and language)
	public static final String ASSERTION="Assertion";
	
	
	/**
	 * This method returns the annotation, from the supplied list, with the
	 * supplied name.
	 * 
	 * @param annotations The list of annotations
	 * @param name The name
	 * @return The annotation, or null if not found
	 */
	public static Annotation getAnnotation(java.util.List<? extends org.scribble.common.model.Annotation> annotations,
									String name) {
		Annotation ret=null;
		
		for (org.scribble.common.model.Annotation an : annotations) {
			if (an instanceof Annotation && ((Annotation)an).getName().equals(name)) {
				ret = (Annotation)an;
				break;
			}
		}
		
		return(ret);
	}

	/**
	 * This method returns the annotation, from the supplied list, with the
	 * supplied name and property details.
	 * 
	 * @param annotations The list of annotations
	 * @param name The annotation name
	 * @param propName The property name
	 * @param propValue The property value
	 * @return The annotation, or null if not found
	 */
	public static Annotation getAnnotationWithProperty(java.util.List<? extends org.scribble.common.model.Annotation> annotations,
						String name, String propName, Object propValue) {
		Annotation ret=null;
		
		for (org.scribble.common.model.Annotation an : annotations) {
			if (an instanceof Annotation && ((Annotation)an).getName().equals(name)) {
				ret = (Annotation)an;
				
				if (ret.getProperties().containsKey(propName) &&
						ret.getProperties().get(propName).equals(propValue)) {
					break;
				} else {
					ret = null;
				}
			}
		}
		
		return(ret);
	}

	/**
	 * This method returns the list of annotations, from the supplied list, with the
	 * supplied name.
	 * 
	 * @param annotations The list of annotations
	 * @param name The name
	 * @return The annotations with the specified name
	 */
	public static java.util.List<Annotation> getAnnotations(java.util.List<? extends org.scribble.common.model.Annotation> annotations,
									String name) {
		java.util.List<Annotation> ret=new java.util.Vector<Annotation>();
		
		for (org.scribble.common.model.Annotation an : annotations) {
			if (an instanceof Annotation && ((Annotation)an).getName().equals(name)) {
				ret.add((Annotation)an);
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method copies annotations from the 'from' list, to the 'to' list, if they
	 * have the specified annotation name.
	 * 
	 * @param fromAnnotations The 'from' annotations list
	 * @param toAnnotations The 'to' annotations list
	 * @param name The name of the annotation to copy
	 */
	public static void copyAnnotations(java.util.List<org.scribble.common.model.Annotation> fromAnnotations,
			java.util.List<org.scribble.common.model.Annotation> toAnnotations, String name) {
		
		for (org.scribble.common.model.Annotation an : fromAnnotations) {
			if (an instanceof Annotation && ((Annotation)an).getName().equals(name)) {
				toAnnotations.add((Annotation)an);
			}
		}
	}
}
