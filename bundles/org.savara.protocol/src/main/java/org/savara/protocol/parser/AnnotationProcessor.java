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
package org.savara.protocol.parser;

import org.savara.common.model.annotation.Annotation;
import org.scribble.common.logging.Journal;

public class AnnotationProcessor implements org.scribble.protocol.parser.AnnotationProcessor {

	public Annotation getAnnotation(String annotation, java.util.Map<String, Object> properties,
								Journal journal) {
		java.util.StringTokenizer st=new java.util.StringTokenizer(annotation); //, "(),=");
		
		String name=st.nextToken(" (");
		
		Annotation an=new Annotation(name);
		
		if (st.hasMoreTokens()) {
			String token=st.nextToken("()");
			
			if (token != null) {
				String[] props=token.split(",");
				
				for (int i=0; i < props.length; i++) {
					String[] vals=props[i].split("=");
					
					if (vals.length != 2) {
						journal.error("Name/Value of annotation could not be derived", properties);											
					} else {
						an.getProperties().put(vals[0], vals[1]);
					}
				}
				
				// Find position following annotation parameters
				int index=annotation.indexOf(token);
				
				if (index != -1) {
					index = annotation.indexOf(')', index+token.length());
					
					if (index == -1) {
						journal.error("')' not found in annotation", properties);
					}
				}
			} else {
				journal.error("'(' not found in annotation", properties);
			}
		}
		
		return(an);
	}

}
