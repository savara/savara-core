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
import org.savara.activity.DefaultActivityProcessor;
import org.savara.activity.model.Activity;
import org.savara.activity.model.Context;
import org.savara.activity.model.Correlation;
import org.savara.common.config.Configuration;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultActivityProcessorTest {

	@Test
	public void testNoFilterStore() {
		ActivityProcessor ap=new DefaultActivityProcessor();
		
		TestStore store=new TestStore();
		
		ap.setStore(store);
		
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
		
		ap.setFilter(filter);
		ap.setStore(store);
		
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
		
		ap.setFilter(filter);
		ap.setStore(store);
		
		Activity act=new Activity();
		
		ap.process(act);
		
		if (store.getStore().size() != 0) {
			fail("Activity event should not have been stored");
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

	public class TestStore implements ActivityStore {
		
		private java.util.List<Activity> m_store=new java.util.Vector<Activity>();

		public void setConfiguration(Configuration config) {
		}

		public void save(Activity activity) {
			m_store.add(activity);
		}
		
		public java.util.List<Activity> getStore() {
			return(m_store);
		}

		public Activity find(String id) {
			return(null);
		}

        public List<Activity> findByCorrelation(Correlation correlation) {
            return null;
        }

        public List<Activity> findByContext(List<Context> contexts) {
            return null;
        }

        public void close() {
		}		
	}
}
