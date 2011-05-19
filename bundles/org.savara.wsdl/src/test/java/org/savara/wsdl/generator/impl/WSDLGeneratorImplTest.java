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
package org.savara.wsdl.generator.impl;

import javax.wsdl.Part;
import javax.xml.namespace.QName;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.DefaultFeedbackHandler.IssueDetails;
import org.savara.common.logging.DefaultFeedbackHandler.IssueType;
import org.savara.common.logging.MessageFormatter;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.contract.model.*;
import org.savara.wsdl.generator.WSDLBinding;
import org.savara.wsdl.generator.impl.WSDLGeneratorImpl;
import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
import org.savara.wsdl.generator.soap.SOAPRPCWSDLBinding;

import junit.framework.TestCase;

public class WSDLGeneratorImplTest extends TestCase {

	private static final String TEST_LOCALPART = "test.localpart";
	private static final String HTTP_TEST_NAMESPACE = "http://test.namespace";
	private static final String TEST_TYPE_NS = "testTypeNS";
	private static final String TEST_TYPE_LP = "testTypeLP";
	private static final String TEST_TYPE_LP2 = "testTypeLP2";
	private static final String TEST_TYPE_LP3 = "testTypeLP3";
	private static final String TEST_NAME_SPACE = "testNameSpace";
	private static final String TEST_NAME_SPACE2 = "testNameSpace2";
	private static final String TEST_NAME_SPACE3 = "testNameSpace3";
	private static final String TEST_NAME = "testName";

	public void testDefnNameAndNamespace() {
		
		Contract c=new Contract();
		
		c.setName(TEST_NAME);
		c.setNamespace(TEST_NAME_SPACE);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		java.util.List<javax.wsdl.Definition> defns=gen.generate(c, null, handler);
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (defns.size() != 1) {
			fail("Only one Definition found: "+defns.size());
		}
		
		javax.wsdl.Definition defn=defns.get(0);
		
		if (defn.getTargetNamespace() == null) {
			fail("Target namespace not set");
		}
		
		if (TEST_NAME_SPACE.equals(defn.getTargetNamespace()) == false) {
			fail("Target namespace not correct: "+defn.getTargetNamespace());
		}
		
		if (defn.getQName() == null) {
			fail("QName not set");
		}
		
		if (defn.getQName().getLocalPart() == null) {
			fail("QName localpart not set");
		}
		
		if (TEST_NAME.equals(defn.getQName().getLocalPart()) == false) {
			fail("QName localpart not correct: "+defn.getQName().getLocalPart());
		}
	}
	
	public void testPortTypeNameAndNamespace() {
		Interface src=new Interface();
		
		src.setName(TEST_NAME);
		src.setNamespace(TEST_NAME_SPACE);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		javax.wsdl.PortType result=gen.createPortType(defns, new Contract(), src, null, handler);
		
		if (result == null) {
			fail("PortType is null");
		}
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (result.getQName() == null) {
			fail("QName not set");
		}
		
		if (result.getQName().getNamespaceURI() == null) {
			fail("QName namespace not set");
		}
		
		if (TEST_NAME_SPACE.equals(result.getQName().getNamespaceURI()) == false) {
			fail("Namespace not correct: "+result.getQName().getNamespaceURI());
		}
		
		if (result.getQName().getLocalPart() == null) {
			fail("QName localpart not set");
		}
		
		if (TEST_NAME.equals(result.getQName().getLocalPart()) == false) {
			fail("QName localpart not correct: "+result.getQName().getLocalPart());
		}
	}
	
	public void testPortTypeBindingNameAndNamespace() {
		Interface src=new Interface();
		
		src.setName(TEST_NAME);
		src.setNamespace(TEST_NAME_SPACE);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		javax.wsdl.Binding result=gen.createBinding(defns, new Contract(), src, null, null, handler);
		
		if (result == null) {
			fail("PortType is null");
		}
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (result.getQName() == null) {
			fail("QName not set");
		}
		
		if (result.getQName().getNamespaceURI() == null) {
			fail("QName namespace not set");
		}
		
		if (TEST_NAME_SPACE.equals(result.getQName().getNamespaceURI()) == false) {
			fail("Namespace not correct: "+result.getQName().getNamespaceURI());
		}
		
		if (result.getQName().getLocalPart() == null) {
			fail("QName localpart not set");
		}
		
		if (result.getQName().getLocalPart().equals(TEST_NAME+WSDLGeneratorImpl.BINDING_SUFFIX) == false) {
			fail("QName localpart not correct: "+result.getQName().getLocalPart());
		}
	}
	
	public void testDefnWithPortTypesAndBindings() {
		
		Contract c=new Contract();
		c.setNamespace(TEST_NAME_SPACE);
		c.setName(TEST_NAME);

		Interface i1=new Interface();
		i1.setName("I1");
		i1.setNamespace(TEST_NAME_SPACE);
		
		// Need to associate a message, to ensure port type
		// is generated
		OneWayRequestMEP m1=new OneWayRequestMEP();
		
		m1.setOperation(TEST_NAME);
		
		Type tref1=new Type();
		tref1.setName(TEST_TYPE_LP);
		m1.getTypes().add(tref1);
		
		TypeDefinition ref1=new TypeDefinition();
		ref1.setName(TEST_TYPE_LP);
		ref1.setDataType(new QName(TEST_NAME_SPACE,TEST_TYPE_LP).toString());
		ref1.setTypeSystem(TypeSystem.XSD);
		
		ref1.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		
		c.getTypeDefinitions().add(ref1);
		
		i1.getMessageExchangePatterns().add(m1);
		
		Interface i2=new Interface();
		i2.setName("I2");
		i2.setNamespace(TEST_NAME_SPACE);
		
		OneWayRequestMEP m2=new OneWayRequestMEP();
		
		m2.setOperation(TEST_NAME);
		
		Type tref2=new Type();
		
		/*
		TypeDefinition ref2=new TypeDefinition();
		tref2.getTypeDefinitions().add(ref1);
		ref2.setName(TEST_TYPE_LP);
		ref2.setNamespace(TEST_NAME_SPACE);
		ref2.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		
		tref2.setName(TEST_TYPE_LP);
		m2.getTypes().add(tref2);
		
		i2.getMessageExchangePatterns().add(m2);
		
		Interface i3=new Interface();
		i3.setName("I3");
		i3.setNamespace(TEST_NAME_SPACE);
		
		OneWayRequestMEP m3=new OneWayRequestMEP();
		
		m3.setOperation(TEST_NAME);
		
		Type tref3=new Type();
		
		/*
		TypeDefinition ref3=new TypeDefinition();
		tref3.getTypeDefinitions().add(ref1);
		ref3.setName(TEST_TYPE_LP);
		ref3.setNamespace(TEST_NAME_SPACE);
		ref3.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		
		tref3.setName(TEST_TYPE_LP);
		m3.getTypes().add(tref3);
		
		i3.getMessageExchangePatterns().add(m3);
		
		c.getInterfaces().add(i1);
		c.getInterfaces().add(i2);
		c.getInterfaces().add(i3);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		java.util.List<javax.wsdl.Definition> defns=gen.generate(c, new SOAPRPCWSDLBinding(), handler);
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (defns.size() != 1) {
			fail("Only one Definition found: "+defns.size());
		}
		
		javax.wsdl.Definition defn=defns.get(0);

		if (defn.getPortTypes().size() != c.getInterfaces().size()) {
			fail("Number of port types ("+defn.getPortTypes().size()+
					") does not match number of interfaces ("+
					c.getInterfaces().size()+")");
		}

		if (defn.getBindings().size() != c.getInterfaces().size()) {
			fail("Number of port type bindings ("+defn.getBindings().size()+
					") does not match number of interfaces ("+
					c.getInterfaces().size()+")");
		}
	}
	
	public void testOperationOneWay() {
		javax.wsdl.Definition defn=null;
		try {
			javax.wsdl.factory.WSDLFactory fact=
						javax.wsdl.factory.WSDLFactory.newInstance();
			
			defn = fact.newDefinition();
			
		} catch(Exception e) {
			fail("Failed to get definition");
		}
		
		Contract c=new Contract();
		
		OneWayRequestMEP src=new OneWayRequestMEP();
		
		src.setOperation(TEST_NAME);
		
		Type tref=new Type();
		
		TypeDefinition ref=new TypeDefinition();
		ref.setName(TEST_TYPE_LP);
		ref.setDataType(new QName(TEST_TYPE_NS,TEST_TYPE_LP).toString());
		ref.setTypeSystem(TypeSystem.XSD);
		ref.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(ref);

		/*
		TypeDefinition ref=new TypeDefinition();
		tref.getTypeDefinitions().add(ref);
		ref.setName(TEST_TYPE_LP);
		ref.setNamespace(TEST_TYPE_NS);
		ref.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		tref.setName(ref.getName());
		src.getTypes().add(tref);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		defns.add(defn);
		
		defn.setTargetNamespace(TEST_NAME_SPACE);
		
		javax.wsdl.PortType ptype=defn.createPortType();
		ptype.setQName(new javax.xml.namespace.QName(TEST_NAME_SPACE, TEST_NAME));
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		javax.wsdl.Operation result=gen.createOperation(defns, c, ptype, src,
						new SOAPRPCWSDLBinding(), handler);
		
		if (result == null) {
			fail("Operation is null");
		}
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (TEST_NAME.equals(result.getName()) == false) {
			fail("Operation name mismatch: "+result.getName());
		}
		
		if (result.getInput() == null) {
			fail("Input not set");
		}
		
		if (result.getOutput() != null) {
			fail("Output should NOT be set");
		}
		
		if (result.getFaults().size() != 0) {
			fail("Faults should NOT be defined");
		}
	}
	
	public void testOperationReqRespFaults() {
		javax.wsdl.Definition defn=null;
		try {
			javax.wsdl.factory.WSDLFactory fact=
						javax.wsdl.factory.WSDLFactory.newInstance();
			
			defn = fact.newDefinition();
			
		} catch(Exception e) {
			fail("Failed to get definition");
		}
		
		Contract c=new Contract();
		
		RequestResponseMEP src=new RequestResponseMEP();
		
		src.setOperation(TEST_NAME);
		
		Type tref1=new Type();
				
		TypeDefinition ref1=new TypeDefinition();
		ref1.setName(TEST_TYPE_LP);
		ref1.setDataType(new QName(TEST_TYPE_NS,TEST_TYPE_LP).toString());
		ref1.setTypeSystem(TypeSystem.XSD);
		ref1.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(ref1);

		/*
		TypeDefinition ref1=new TypeDefinition();
		tref1.getTypeDefinitions().add(ref1);
		ref1.setName(TEST_TYPE_LP);
		ref1.setNamespace(TEST_TYPE_NS);
		ref1.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		tref1.setName(TEST_TYPE_LP);
		src.getTypes().add(tref1);
		
		Type tref2=new Type();
		/*
		TypeDefinition ref2=new TypeDefinition();
		tref2.getTypeDefinitions().add(ref2);
		ref2.setName(TEST_TYPE_LP);
		ref2.setNamespace(TEST_TYPE_NS);
		ref2.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/

		tref2.setName(TEST_TYPE_LP);
		src.getResponseTypes().add(tref2);
		
		FaultDetails fd1=new FaultDetails();
		fd1.setName("faultName1");

		Type tref3=new Type();
		
		/*
		TypeDefinition ref3=new TypeDefinition();
		tref3.getTypeDefinitions().add(ref3);
		ref3.setName(TEST_TYPE_LP);
		ref3.setNamespace(TEST_TYPE_NS);
		ref3.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		tref3.setName(TEST_TYPE_LP);
		fd1.getTypes().add(tref3);
		
		src.getFaultDetails().add(fd1);
		
		FaultDetails fd2=new FaultDetails();
		fd2.setName("faultName2");

		Type tref4=new Type();
		tref4.setName(TEST_TYPE_LP);
		/*
		TypeDefinition ref4=new TypeDefinition();
		tref4.getTypeDefinitions().add(ref4);
		ref4.setName(TEST_TYPE_LP);
		ref4.setNamespace(TEST_TYPE_NS);
		ref4.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		fd2.getTypes().add(tref4);
		
		src.getFaultDetails().add(fd2);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		defns.add(defn);
		
		defn.setTargetNamespace(TEST_NAME_SPACE);
		
		javax.wsdl.PortType ptype=defn.createPortType();
		ptype.setQName(new javax.xml.namespace.QName(TEST_NAME_SPACE, TEST_NAME));
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		javax.wsdl.Operation result=gen.createOperation(defns, c, ptype, src,
							new SOAPRPCWSDLBinding(), handler);
		
		if (result == null) {
			fail("Operation is null");
		}
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (TEST_NAME.equals(result.getName()) == false) {
			fail("Operation name mismatch: "+result.getName());
		}
		
		if (result.getInput() == null) {
			fail("Input not set");
		}
		
		if (result.getOutput() == null) {
			fail("Output not set");
		}
		
		if (result.getFaults().size() != src.getFaultDetails().size()) {
			fail("Faults number ("+result.getFaults().size()+
						") does not match contract ("+src.getFaultDetails().size()+")");
		}
	}
	
	public void testMessage() {
		Contract c=new Contract();
		
		TypeDefinition src=new TypeDefinition();
		
		/*
		src.setName(TEST_TYPE_LP);
		src.setNamespace(TEST_TYPE_NS);
		src.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		src.setName(TEST_TYPE_LP);
		src.setDataType(new QName(TEST_TYPE_NS,TEST_TYPE_LP).toString());
		src.setTypeSystem(TypeSystem.XSD);
		
		src.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(src);

		Type t=new Type();
		//t.getTypeDefinitions().add(src);
		t.setName(TEST_TYPE_LP);
		
		java.util.List<Type> refs=new java.util.Vector<Type>();
		refs.add(t);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		
		QName msgname=new QName(HTTP_TEST_NAMESPACE,TEST_LOCALPART);
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		javax.wsdl.Message result=gen.getMessage(defns, c, msgname, refs, 
							new SOAPRPCWSDLBinding(), handler);
		
		if (result == null) {
			fail("Message is null");
		}
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (result.getQName() == null) {
			fail("QName not set");
		}
		
		if (result.getQName().getLocalPart() == null) {
			fail("QName localpart not set");
		}
		
		if (TEST_LOCALPART.equals(result.getQName().getLocalPart()) == false) {
			fail("QName localpart not correct: "+result.getQName().getLocalPart());
		}
		
		if (result.getQName().getNamespaceURI() == null) {
			fail("QName namespace not set");
		}
		
		if (HTTP_TEST_NAMESPACE.equals(result.getQName().getNamespaceURI()) == false) {
			fail("QName namespace not correct: "+result.getQName().getNamespaceURI());
		}
		
		if (result.getParts().size() != 1) {
			fail("Should be one part: "+result.getParts().size());
		}
		
		Part part=(Part)result.getParts().values().toArray()[0];
		
		if (part.getElementName() != null) {
			fail("Element name should not be set");
		}
		
		if (part.getTypeName() == null) {
			fail("Type name not set");
		}
		
		if (TEST_TYPE_NS.equals(part.getTypeName().getNamespaceURI()) == false) {
			fail("Type namespace incorrect: "+part.getTypeName().getNamespaceURI());
		}
		
		if (TEST_TYPE_LP.equals(part.getTypeName().getLocalPart()) == false) {
			fail("Type localpart incorrect: "+part.getTypeName().getLocalPart());
		}
	}
	
	public void testDefnWithPortTypesAndMesgsInDiffNamespaces() {
		
		Contract c=new Contract();
		c.setNamespace(TEST_NAME_SPACE);
		c.setName(TEST_NAME);

		Interface i1=new Interface();
		i1.setName("I1");
		i1.setNamespace(TEST_NAME_SPACE);
		
		// Need to associate a message, to ensure port type
		// is generated
		OneWayRequestMEP m1=new OneWayRequestMEP();
		
		m1.setOperation(TEST_NAME);
		
		Type tref1=new Type();
		TypeDefinition ref1=new TypeDefinition();
		
		ref1.setName(TEST_TYPE_LP);
		ref1.setDataType(new QName(TEST_NAME_SPACE,TEST_TYPE_LP).toString());
		ref1.setTypeSystem(TypeSystem.XSD);
		ref1.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(ref1);
		
		/*
		tref1.getTypeDefinitions().add(ref1);
		ref1.setName(TEST_TYPE_LP);
		ref1.setNamespace(TEST_NAME_SPACE);
		ref1.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		
		tref1.setName(TEST_TYPE_LP);
		m1.getTypes().add(tref1);
		
		i1.getMessageExchangePatterns().add(m1);
		
		Interface i2=new Interface();
		i2.setName("I2");
		i2.setNamespace(TEST_NAME_SPACE2);
		
		OneWayRequestMEP m2=new OneWayRequestMEP();
		
		m2.setOperation(TEST_NAME);
		
		Type tref2=new Type();
		TypeDefinition ref2=new TypeDefinition();

		ref2.setName(TEST_TYPE_LP2);
		ref2.setDataType(new QName(TEST_NAME_SPACE2,TEST_TYPE_LP).toString());
		ref2.setTypeSystem(TypeSystem.XSD);
		ref2.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(ref2);
		
		/*
		tref2.getTypeDefinitions().add(ref2);
		ref2.setName(TEST_TYPE_LP);
		ref2.setNamespace(TEST_NAME_SPACE2);
		ref2.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		tref2.setName(TEST_TYPE_LP2);
		m2.getTypes().add(tref2);
		
		i2.getMessageExchangePatterns().add(m2);
		
		Interface i3=new Interface();
		i3.setName("I3");
		i3.setNamespace(TEST_NAME_SPACE2);
		
		OneWayRequestMEP m3=new OneWayRequestMEP();
		
		m3.setOperation(TEST_NAME);
		
		Type tref3=new Type();
		TypeDefinition ref3=new TypeDefinition();
		
		ref3.setName(TEST_TYPE_LP3);
		ref3.setDataType(new QName(TEST_NAME_SPACE3,TEST_TYPE_LP).toString());
		ref3.setTypeSystem(TypeSystem.XSD);
		ref3.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(ref3);
		
		/*
		tref3.getTypeDefinitions().add(ref3);
		ref3.setName(TEST_TYPE_LP);
		ref3.setNamespace(TEST_NAME_SPACE3);
		ref3.setTypeSystem(TypeDefinition.XSD_TYPE);
		*/
		tref3.setName(TEST_TYPE_LP3);
		m3.getTypes().add(tref3);
		
		i3.getMessageExchangePatterns().add(m3);
		
		c.getInterfaces().add(i1);
		c.getInterfaces().add(i2);
		c.getInterfaces().add(i3);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		java.util.List<javax.wsdl.Definition> defns=gen.generate(c, 
						new SOAPRPCWSDLBinding(), handler);
		
		if (handler.hasErrors() || handler.hasWarnings()) {
			fail("Feedback handler has errors or warnings");
		}
		
		if (defns.size() != 2) {
			fail("Two Definitions expected, but got: "+defns.size());
		}
		
		javax.wsdl.Definition defn1=defns.get(0);

		if (defn1.getPortTypes().size() != 1) {
			fail("1: Number of port types ("+defn1.getPortTypes().size()+
					") does not match number expected (1)");
		}
		
		if (defn1.getBindings().size() != 1) {
			fail("1: Number of port type bindings ("+defn1.getBindings().size()+
					") does not match number expected (1)");
		}
		
		if (defn1.getMessages().size() != 1) {
			fail("1: Number of message ("+defn1.getMessages().size()+
			") does not match number expected (1)");
		}
		
		javax.wsdl.Definition defn2=defns.get(1);

		if (defn2.getPortTypes().size() != 2) {
			fail("2: Number of port types ("+defn2.getPortTypes().size()+
					") does not match number expected (2)");
		}
		
		if (defn2.getBindings().size() != 2) {
			fail("2: Number of port type bindings ("+defn2.getBindings().size()+
					") does not match number expected (2)");
		}
		
		if (defn2.getMessages().size() != 1) {
			fail("2: Number of message ("+defn2.getMessages().size()+
			") does not match number expected (1)");
		}
		
		// Check service only associated with the first defn
		if (defn1.getServices().size() != 1) {
			fail("First definition should only have 1 service: "+defn1.getServices().size());
		}
		
		javax.wsdl.Service s=(javax.wsdl.Service)
					defn1.getServices().values().iterator().next();
		if (s.getPorts().size() != 3) {
			fail("Should be 3 ports: "+s.getPorts().size());
		}
		
		if (defn2.getServices().size() != 0) {
			fail("Second definition should not have any services: "+defn2.getServices().size());
		}
	}
	
	public void testCreatePartTypeInDocLit() {
		javax.wsdl.Definition defn=null;
		try {
			javax.wsdl.factory.WSDLFactory fact=
						javax.wsdl.factory.WSDLFactory.newInstance();
			
			defn = fact.newDefinition();
			
		} catch(Exception e) {
			fail("Failed to get definition");
		}
		
		Contract c=new Contract();
		
		TypeDefinition src=new TypeDefinition();
		
		QName qname=new QName(TEST_TYPE_NS,TEST_TYPE_LP);
		
		src.setName(TEST_TYPE_LP);
		src.setDataType(qname.toString());
		src.setTypeSystem(TypeSystem.XSD);
		
		src.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(src);

		Type t=new Type();
		//t.getTypeDefinitions().add(src);
		t.setName(TEST_TYPE_LP);
		
		java.util.List<Type> refs=new java.util.Vector<Type>();
		refs.add(t);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		//java.util.List<javax.wsdl.Definition> defns=new java.util.Vector<javax.wsdl.Definition>();
		
		//QName msgname=new QName(HTTP_TEST_NAMESPACE,TEST_LOCALPART);
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		
		WSDLBinding wsdlBinding = new SOAPDocLitWSDLBinding();
		
		javax.wsdl.Part part=gen.createPart(defn, src, qname, wsdlBinding, handler);
		
		if (part == null) {
			fail("Part is null");
		}
		
		if (part.getElementName() != null) {
			fail("Element name should not be set");
		}
		
		if (part.getTypeName() == null) {
			fail("Type name not set");
		}
		
		if (TEST_TYPE_NS.equals(part.getTypeName().getNamespaceURI()) == false) {
			fail("Type namespace incorrect: "+part.getTypeName().getNamespaceURI());
		}
		
		if (TEST_TYPE_LP.equals(part.getTypeName().getLocalPart()) == false) {
			fail("Type localpart incorrect: "+part.getTypeName().getLocalPart());
		}

		if (handler.getIssues().size() != 1) {
			fail("Feedback handler should have 1 issue: "+handler.getIssues().size());
		}
		
		IssueDetails issue=handler.getIssues().get(0);
		
		if (issue.getIssueType() != IssueType.Error) {
			fail("Issue is not an error");
		}
		
		String mesg=MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
				"org.savara.wsdl.Messages"),
					"SAVARA-WSDL-00001", wsdlBinding.getName(), qname.toString());
		
		if (issue.getMessage().equals(mesg) == false) {
			fail("Unexpected issue message: "+issue.getMessage());
		}
	}
	
	public void testCreatePartTypeInRPC() {
		javax.wsdl.Definition defn=null;
		try {
			javax.wsdl.factory.WSDLFactory fact=
						javax.wsdl.factory.WSDLFactory.newInstance();
			
			defn = fact.newDefinition();
			
		} catch(Exception e) {
			fail("Failed to get definition");
		}
		
		Contract c=new Contract();
		
		TypeDefinition src=new TypeDefinition();
		
		QName qname=new QName(TEST_TYPE_NS,TEST_TYPE_LP);
		
		src.setName(TEST_TYPE_LP);
		src.setDataType(qname.toString());
		src.setTypeSystem(TypeSystem.XSD);
		
		src.getAnnotations().add(new Annotation(AnnotationDefinitions.XSD_TYPE));
		c.getTypeDefinitions().add(src);

		Type t=new Type();
		//t.getTypeDefinitions().add(src);
		t.setName(TEST_TYPE_LP);
		
		java.util.List<Type> refs=new java.util.Vector<Type>();
		refs.add(t);
		
		WSDLGeneratorImpl gen=new WSDLGeneratorImpl();
		
		DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
		
		WSDLBinding wsdlBinding = new SOAPRPCWSDLBinding();
		
		javax.wsdl.Part part=gen.createPart(defn, src, qname, wsdlBinding, handler);
		
		if (part == null) {
			fail("Part is null");
		}
		
		if (part.getElementName() != null) {
			fail("Element name should not be set");
		}
		
		if (part.getTypeName() == null) {
			fail("Type name not set");
		}
		
		if (TEST_TYPE_NS.equals(part.getTypeName().getNamespaceURI()) == false) {
			fail("Type namespace incorrect: "+part.getTypeName().getNamespaceURI());
		}
		
		if (TEST_TYPE_LP.equals(part.getTypeName().getLocalPart()) == false) {
			fail("Type localpart incorrect: "+part.getTypeName().getLocalPart());
		}

		if (handler.getIssues().size() != 0) {
			fail("Feedback handler should have 0 issues");
		}
	}
}
