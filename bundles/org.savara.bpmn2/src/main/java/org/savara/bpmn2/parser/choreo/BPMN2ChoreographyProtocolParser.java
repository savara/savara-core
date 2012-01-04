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
import org.savara.bpmn2.util.BPMN2ModelUtil;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.model.util.TypeSystem;
import org.savara.protocol.util.FeedbackHandlerProxy;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.protocol.ProtocolContext;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.DataType;
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
				
				initialize(pm, defns);
				
				// Construct the choreography behaviour
				Protocol p=new Protocol();
				p.setName(choreo.getName());
				
				p.setBlock(new Block());
				
				// Create annotation to link the protocol to the source choreography
				Annotation pann=new Annotation(AnnotationDefinitions.SOURCE_COMPONENT);

				pann.getProperties().put(AnnotationDefinitions.ID_PROPERTY,
							choreo.getId());
				
				p.getAnnotations().add(pann);
				
				// Push scope when processing the choreography
				parserContext.pushScope();
				
				BPMN2ChoreographyParserUtil.initializeScope(parserContext.getScope(), choreo);
				
				// Process the flow element list
				BPMN2ParserRule rule=ParserRuleFactory.getParserRule(choreo);
				
				rule.parse(parserContext, choreo, p.getBlock());
				
				parserContext.popScope();
				
				pm.setProtocol(p);
				
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
				
				if (location != null) {
					tilist.setLocation(location);
				}
				
				tilist.getTypeImports().add(ti);
				
				pm.getImports().add(tilist);
			}
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
