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
	 * @param elem The element
	 */
	public static void initializeScope(Scope scope, Object elem) {
		
		if (elem.getClass() == TChoreography.class) {
			TChoreography choreo=(TChoreography)elem;
			
			initializeFlowElements(scope, choreo.getFlowElement());
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
