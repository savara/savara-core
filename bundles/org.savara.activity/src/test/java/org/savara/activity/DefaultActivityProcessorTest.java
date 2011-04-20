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
package org.savara.activity;

import org.junit.Test;
import org.savara.activity.ActivityAnalyser;
import org.savara.activity.ActivityProcessor;
import org.savara.activity.ActivityValidator;
import org.savara.activity.DefaultActivityProcessor;
import org.savara.activity.model.Activity;
import org.savara.common.config.Configuration;

import static org.junit.Assert.*;

public class DefaultActivityProcessorTest {

	@Test
	public void testAddRemoveAnalyser() {
		DefaultActivityProcessor avm=new DefaultActivityProcessor();
		
		TestAnalyser ta=new TestAnalyser();		
		avm.getAnalysers().add(ta);
		
		if (avm.getAnalysers().contains(ta) == false) {
			fail("Analyser not found");
		}
		
		avm.getAnalysers().remove(ta);
		
		if (avm.getAnalysers().contains(ta) == true) {
			fail("Analyser not removed");
		}
	}
		
	@Test
	public void testAddRemoveValidator() {
		DefaultActivityProcessor avm=new DefaultActivityProcessor();
		
		TestValidator tv=new TestValidator();		
		avm.getValidators().add(tv);
		
		if (avm.getValidators().contains(tv) == false) {
			fail("Validator not found");
		}
		
		avm.getValidators().remove(tv);
		
		if (avm.getValidators().contains(tv) == true) {
			fail("Validator not removed");
		}
	}
		
	@Test
	public void testAnalysisBeforeValidate() {
		ActivityProcessor avm=new DefaultActivityProcessor();
		
		TestOrderAnalyserValidator tav=new TestOrderAnalyserValidator();
		
		avm.getAnalysers().add(tav);
		avm.getValidators().add(tav);
		
		Activity act=new Activity();
		
		avm.process(act);
	}
	
	@Test
	public void testNoFilterStore() {
		ActivityProcessor ap=new DefaultActivityProcessor();
		
		TestStore store=new TestStore();
		
		ap.getStores().add(store);
		
		Activity act=new Activity();
		
		ap.process(act);
		
		if (store.getStore().size() == 0) {
			fail("Activity event not stored");
		}
	}
	
	@Test
	public void testOneTrueFilterStore() {
		ActivityProcessor ap=new DefaultActivityProcessor();
		
		TestStore store=new TestStore();
		TestFilter filter=new TestFilter(true);
		
		ap.getFilters().add(filter);
		ap.getStores().add(store);
		
		Activity act=new Activity();
		
		ap.process(act);
		
		if (store.getStore().size() == 0) {
			fail("Activity event not stored");
		}
	}
	
	@Test
	public void testOneFalseFilterStore() {
		ActivityProcessor ap=new DefaultActivityProcessor();
		
		TestStore store=new TestStore();
		TestFilter filter=new TestFilter(false);
		
		ap.getFilters().add(filter);
		ap.getStores().add(store);
		
		Activity act=new Activity();
		
		ap.process(act);
		
		if (store.getStore().size() != 0) {
			fail("Activity event should not have been stored");
		}
	}
	
	
	@Test
	public void testOneFalseOneTrueFilterStore() {
		ActivityProcessor ap=new DefaultActivityProcessor();
		
		TestStore store=new TestStore();
		TestFilter filterFalse=new TestFilter(false);
		TestFilter filterTrue=new TestFilter(true);
		
		ap.getFilters().add(filterFalse);
		ap.getFilters().add(filterTrue);
		ap.getStores().add(store);
		
		Activity act=new Activity();
		
		ap.process(act);
		
		if (store.getStore().size() == 0) {
			fail("Activity event should have been stored");
		}
	}
	public class TestAnalyser implements ActivityAnalyser {

		private Activity m_activity=null;
		
		public Activity getActivity() {
			return(m_activity);
		}
		
		public void analyse(Activity activity) {
			m_activity = activity;
		}
		
	}
	
	public class TestFilter implements ActivityFilter {
		
		boolean m_isRelevant=false;
		
		public TestFilter(boolean relevant) {
			m_isRelevant = relevant;
		}

		public boolean isRelevant(Activity activity) {
			return(m_isRelevant);
		}
	}

	public class TestValidator implements ActivityValidator {

		private Activity m_activity=null;
		
		public Activity getActivity() {
			return(m_activity);
		}
		
		public void validate(Activity activity) {
			m_activity = activity;
		}
	}
	
	public class TestStore implements ActivityStore {
		
		private java.util.List<Activity> m_store=new java.util.Vector<Activity>();

		public void initialize(Configuration config) {
		}

		public void store(Activity activity) {
			m_store.add(activity);
		}
		
		public java.util.List<Activity> getStore() {
			return(m_store);
		}

		public Activity queryById(String id) {
			return(null);
		}

		public void close() {
		}		
	}

	public class TestOrderAnalyserValidator implements ActivityAnalyser, ActivityValidator {

		private Activity m_activity=null;
		
		public void validate(Activity activity) {
			if (m_activity != activity) {
				// Should be activity that was analysed
				fail("Activity did not match");
			}
		}

		public void analyse(Activity activity) {
			if (m_activity != null) {
				fail("Activity should not be set");
			}
			m_activity = activity;
		}
		
	}
}
