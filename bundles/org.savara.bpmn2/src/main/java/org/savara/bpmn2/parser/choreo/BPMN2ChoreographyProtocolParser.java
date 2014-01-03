/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpmn2.parser.choreo;

import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.savara.bpmn2.internal.parser.choreo.BPMN2ChoreographyParserUtil;
import org.savara.bpmn2.internal.parser.choreo.rules.BPMN2ParserContext;
import org.savara.bpmn2.internal.parser.choreo.rules.BPMN2ParserRule;
import org.savara.bpmn2.internal.parser.choreo.rules.DefaultBPMN2ParserContext;
import org.savara.bpmn2.internal.parser.choreo.rules.ParserRuleFactory;
import org.savara.bpmn2.internal.parser.choreo.rules.Scope;
import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TImport;
import org.savara.bpmn2.model.TItemDefinition;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.model.util.BPMN2ModelUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.protocol.util.FeedbackHandlerProxy;
import org.savara.protocol.util.ProtocolUtils;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.DataType;
import org.scribble.protocol.model.ModelProperties;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.TypeImport;
import org.scribble.protocol.model.TypeImportList;
import org.scribble.protocol.parser.AnnotationProcessor;
import org.scribble.protocol.parser.ProtocolParser;

/**
 * This class provides the model parser for the BPMN2 notation.
 * 
 */
public class BPMN2ChoreographyProtocolParser implements ProtocolParser {

	private static final String BPMN_FILE_EXTENSION = "bpmn";

	private static Logger logger = Logger.getLogger(BPMN2ChoreographyProtocolParser.class.getName());

	public boolean isSupported(Content content) {
		return(content.hasExtension(BPMN_FILE_EXTENSION));
	}
	
	public ProtocolModel parse(ProtocolContext context, Content content, Journal journal)
							throws java.io.IOException {
		ProtocolModel ret=null;
		
		java.io.InputStream is=content.getInputStream();
		
		org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
		is.close();
		
		Scope scope=BPMN2ChoreographyParserUtil.createScope(defns);
		
		BPMN2ChoreographyParserUtil.initializeScope(scope, defns);		
		
		BPMN2ParserContext parserContext=
				new DefaultBPMN2ParserContext(new FeedbackHandlerProxy(journal), scope);

		for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
			if (elem.getDeclaredType() == TChoreography.class) {
				TChoreography choreo=(TChoreography)elem.getValue();
				
				ProtocolModel pm=new ProtocolModel();
				
				// Construct the choreography behaviour
				Protocol p=new Protocol();
				p.setName(choreo.getName());
				
				p.setBlock(new Block());
				
				pm.setProtocol(p);
				
				initialize(pm, defns);
				
				// Create annotation to link the protocol to the source choreography
				Annotation pann=new Annotation(AnnotationDefinitions.PROTOCOL);

				pann.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
							defns.getTargetNamespace());
				
				p.getAnnotations().add(pann);
				
				// Create annotation to link the protocol to the source choreography
				Annotation scann=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

				scann.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
							choreo.getId());
				
				p.getAnnotations().add(scann);
				
				p.getProperties().put(ModelProperties.URI, choreo.getId());
				
				// Push scope when processing the choreography
				parserContext.pushScope();
				
				BPMN2ChoreographyParserUtil.initializeScope(parserContext.getScope(), choreo);
				
				// Process the flow element list
				BPMN2ParserRule rule=ParserRuleFactory.getParserRule(choreo);
				
				rule.parse(parserContext, choreo, p.getBlock());
				
				parserContext.popScope();
				
				// Localise the role introductions to the inner most
				// block within which their behaviour exists
				ProtocolUtils.localizeRoleIntroductions(pm);
				
				ret = pm;
				
				break;
			}
		}
		
		return(ret);
	}
	
	public void setAnnotationProcessor(AnnotationProcessor ap) {
		// Not required
	}
	
	protected void initialize(ProtocolModel pm, org.savara.bpmn2.model.TDefinitions defns) {
		java.util.Map<String, String> nsprefix=new java.util.HashMap<String, String>();
		java.util.List<String> defaultprefix=new java.util.Vector<String>();
		java.util.Map<String, String> nslocation=new java.util.HashMap<String, String>();
		
		for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
			
			if (elem.getDeclaredType() == TMessage.class) {
				TMessage message=(TMessage)elem.getValue();
				
				if (message.getItemRef() == null) {
					logger.severe("No item definition set for message '"+message.getName()+"'");
					continue;
				}
				
				TItemDefinition itemdefn=getItemDefinition(message.getItemRef().getLocalPart(), defns);
				
				if (itemdefn == null) {
					logger.severe("No item definition found for message '"+message.getName()+"'");
					continue;
				}
				
				if (itemdefn.getStructureRef() == null) {
					logger.severe("Item definition has no structure ref for message '"+message.getName()+"'");
					continue;
				}
				
				// Store namespace - and if prefix is not set, use a default, but ensure
				// only one default prefix is used per namespace uri
				String prefix=itemdefn.getStructureRef().getPrefix();
				if (prefix == null || prefix.trim().length() == 0) {
					if (!defaultprefix.contains(itemdefn.getStructureRef().getNamespaceURI())) {
						prefix = "defns"+nsprefix.size();
						defaultprefix.add(itemdefn.getStructureRef().getNamespaceURI());
					} else {
						prefix = null;
					}
				}
				
				if (prefix != null) {
					nsprefix.put(prefix, itemdefn.getStructureRef().getNamespaceURI());
				}
				
				// Create the type import
				TypeImport ti=new TypeImport();
				ti.setName(message.getName());
				
				DataType dt=new DataType();
				dt.setDetails(itemdefn.getStructureRef().toString());
				
				ti.setDataType(dt);
				
				TypeImportList tilist=new TypeImportList();
				tilist.setFormat(TypeSystem.XSD);
				
				String location=null;
				
				for (TImport imp : defns.getImport()) {
					if (imp.getNamespace().equals(itemdefn.getStructureRef().getNamespaceURI())) {
						
						if (imp.getLocation() != null) {
							location = imp.getLocation();
						}
						
						break;
					}
				}

				tilist.setLocation(location);

				if (location != null && prefix != null) {
					nslocation.put(prefix, location);
				}
				
				tilist.getTypeImports().add(ti);
				
				pm.getImports().add(tilist);
			}
		}
		
		for (String prefix : nsprefix.keySet()) {
			String ns=nsprefix.get(prefix);
			String location=nslocation.get(prefix);
			
			Annotation pann=new Annotation(AnnotationDefinitions.TYPE);

			pann.getProperties().put(AnnotationDefinitions.PREFIX_PROPERTY,
					prefix);
			pann.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY,
					ns);
			
			if (location != null) {
				pann.getProperties().put(AnnotationDefinitions.LOCATION_PROPERTY,
							location);
			}
			
			pm.getProtocol().getAnnotations().add(pann);
		}
	}

	protected TItemDefinition getItemDefinition(String id, org.savara.bpmn2.model.TDefinitions defns) {
		
		for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
			
			if (elem.getDeclaredType() == TItemDefinition.class) {
				TItemDefinition itemdefn=(TItemDefinition)elem.getValue();
				
				if (itemdefn.getId().equals(id)) {
					return(itemdefn);
				}
			}
		}
		
		return(null);
	}
}
