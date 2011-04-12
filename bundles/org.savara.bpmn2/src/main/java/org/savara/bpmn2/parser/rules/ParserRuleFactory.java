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
package org.savara.bpmn2.parser.rules;

public class ParserRuleFactory {

	private static java.util.List<BPMN2ParserRule> m_rules=
						new java.util.Vector<BPMN2ParserRule>();
	
	/**
	 * This method registers the supplied rule.
	 * 
	 * @param rule The rule
	 */
	public static void register(BPMN2ParserRule rule) {
		m_rules.add(rule);
	}
	
	/**
	 * This method unregisters the supplied rule.
	 * 
	 * @param rule The rule
	 */
	public static void unregister(BPMN2ParserRule rule) {
		m_rules.remove(rule);
	}
	
	/**
	 * This method returns the BPMN2 parsing rule associated
	 * with the supplied model element.
	 * 
	 * @param elem The element
	 * @return The BPMN2 parsing rule, or null if not supported
	 */
	public static BPMN2ParserRule getParserRule(Object elem) {
		BPMN2ParserRule ret=null;
		
		for (BPMN2ParserRule rule : m_rules) {
			if (rule.isSupported(elem)) {
				ret = rule;
				break;
			}
		}
		
		return(ret);
	}
}
