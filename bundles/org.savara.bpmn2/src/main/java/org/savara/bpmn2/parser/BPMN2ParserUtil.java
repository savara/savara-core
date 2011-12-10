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
package org.savara.bpmn2.parser;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.savara.bpmn2.model.TChoreography;
import org.savara.bpmn2.model.TDefinitions;
import org.savara.bpmn2.model.TFlowElement;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TMessageFlow;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.model.TRootElement;
import org.savara.bpmn2.parser.rules.Scope;

public class BPMN2ParserUtil {

	/**
	 * This method creates a scope based on the supplied BPMN2
	 * definitions.
	 * 
	 * @param defns The definitions
	 * @return The scope
	 */
	public static Scope createScope(TDefinitions defns) {
		return(new Scope(defns));
	}
	
	/**
	 * This method initializes the supplied scope based on the
	 * supplied BPMN2 model element.
	 * 
	 * @param scope The scope
	 * @param defns The BPMN definition
	 */
	public static void initializeScope(Scope scope, TDefinitions defns) {
		
		for (JAXBElement<? extends TRootElement> elem : defns.getRootElement()) {
			if (elem.getDeclaredType() == TMessage.class) {
				TMessage mesg=(TMessage)elem.getValue();
				
				scope.register(mesg.getId(), mesg);
			}
		}
	}
	
	/**
	 * This method initializes the supplied scope based on the
	 * supplied BPMN2 model element.
	 * 
	 * @param scope The scope
	 * @param choreo The choreography
	 */
	public static void initializeScope(Scope scope, TChoreography choreo) {
		initializeParticipants(scope, choreo.getParticipant());
		
		initializeFlowElements(scope, choreo.getFlowElement());
		
		initializeMessageFlows(scope, choreo.getMessageFlow());
	}
	
	private static void initializeParticipants(Scope scope,
						List<TParticipant> participants) {
		for (TParticipant p : participants) {
			scope.register(p.getId(), p);
		}
	}
	
	private static void initializeMessageFlows(Scope scope,
					List<TMessageFlow> mflows) {
		for (TMessageFlow mflow : mflows) {
			scope.register(mflow.getId(), mflow);
		}
	}

	private static void initializeFlowElements(Scope scope,
				List<JAXBElement<? extends TFlowElement>> flowElements) {
		for (JAXBElement<? extends TFlowElement> jaxb : flowElements) {
			TFlowElement fe=jaxb.getValue();
			
			scope.register(fe.getId(), fe);
		}
	}
}
