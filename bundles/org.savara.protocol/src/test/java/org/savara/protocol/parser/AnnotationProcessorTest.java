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

import org.junit.Test;
import org.savara.common.model.annotation.Annotation;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.scribble.common.logging.CachedJournal;
import org.scribble.protocol.model.Interaction;

import static org.junit.Assert.*;

public class AnnotationProcessorTest {

	@Test
	public void testProcessAnnotationWithoutParameters() {
		AnnotationProcessor processor=new AnnotationProcessor();
		
		String name="Interface";
		
		Interaction mobj=new Interaction();
		
		String annotationText=name;
		
		CachedJournal journal=new CachedJournal();
		
		Annotation an=processor.getAnnotation(annotationText, mobj.getProperties(), journal);
		
		if (journal.hasErrors()) {
			fail("Error reported");
		}
		
		mobj.getAnnotations().add(an);
		
		Annotation annotation=AnnotationDefinitions.getAnnotation(mobj.getAnnotations(), name);
		
		if (annotation == null) {
			fail("Annotation not associated with object");
		}
		
		if (annotation.getName().equals(name) == false) {
			fail("Annotation name is incorrect");
		}
		
		if (annotation.getProperties().size() != 0) {
			fail("No properties expected");
		}
	}
	
	@Test
	public void testProcessAnnotationWithParameters() {
		AnnotationProcessor processor=new AnnotationProcessor();
		
		String name="Interface";
		String p1value="{http://www.example.org}Intf";
		String p2value="MyOtherValue";
		
		Interaction mobj=new Interaction();
		
		String annotationText=name+"(p1="+p1value+",p2="+p2value+")";
		
		CachedJournal journal=new CachedJournal();
		
		Annotation an=processor.getAnnotation(annotationText, mobj.getProperties(), journal);
		
		if (journal.hasErrors()) {
			fail("Error reported");
		}
		
		mobj.getAnnotations().add(an);
		
		Annotation annotation=AnnotationDefinitions.getAnnotation(mobj.getAnnotations(), name);
		
		if (annotation == null) {
			fail("Annotation not associated with object");
		}
		
		if (annotation.getName().equals(name) == false) {
			fail("Annotation name is incorrect");
		}
		
		if (annotation.getProperties().size() != 2) {
			fail("Two properties expected");
		}
		
		if (annotation.getProperties().containsKey("p1") == false) {
			fail("p1 property not found");
		}
		
		if (annotation.getProperties().get("p1").equals(p1value) == false) {
			fail("p1 value not correct");
		}
		
		if (annotation.getProperties().containsKey("p2") == false) {
			fail("p2 property not found");
		}

		if (annotation.getProperties().get("p2").equals(p2value) == false) {
			fail("p2 value not correct");
		}
	}
	
	@Test
	public void testProcessAnnotationWithParametersMissingComma() {
		AnnotationProcessor processor=new AnnotationProcessor();
		
		String name="Interface";
		String p1value="{http://www.example.org}Intf";
		String p2value="MyOtherValue";
		
		Interaction mobj=new Interaction();
		
		String annotationText=name+"(p1="+p1value+" p2="+p2value+")";
		
		CachedJournal journal=new CachedJournal();
		
		processor.getAnnotation(annotationText, mobj.getProperties(), journal);
		
		if (journal.hasErrors() == false) {
			fail("Error expected");
		}
	}
	
	@Test
	public void testProcessAnnotationWithParametersMissingAssign() {
		AnnotationProcessor processor=new AnnotationProcessor();
		
		String name="Interface";
		
		Interaction mobj=new Interaction();
		
		String annotationText=name+"(p1)";
		
		CachedJournal journal=new CachedJournal();
		
		processor.getAnnotation(annotationText, mobj.getProperties(), journal);
		
		if (journal.hasErrors() == false) {
			fail("Error expected");
		}
	}
	
	@Test
	public void testProcessAnnotationWithParametersMissingEndBracket() {
		AnnotationProcessor processor=new AnnotationProcessor();
		
		String name="Interface";
		String p1value="{http://www.example.org}Intf";
		
		Interaction mobj=new Interaction();
		
		String annotationText=name+"(p1="+p1value;
		
		CachedJournal journal=new CachedJournal();
		
		processor.getAnnotation(annotationText, mobj.getProperties(), journal);
		
		if (journal.hasErrors() == false) {
			fail("Error expected");
		}
	}
}
