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
package org.savara.activity.store.rdbms;

import java.io.InputStream;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.savara.activity.model.Activity;
import org.savara.activity.model.Context;
import org.savara.activity.model.InteractionActivity;
import org.savara.activity.store.rdbms.model.CorrelationIDEntity;
import org.savara.activity.util.ActivityModelUtil;

/**
 * @author: Jeff Yu
 * @date: 20/04/11
 */
public class ActivityStoreImplTest extends Assert {


    private static ActivityStoreImpl activityStore;

    private static Activity activity;

    @BeforeClass
    public static void setUp() throws Exception {
       	Class.forName("org.h2.Driver");
		DriverManager.getConnection("jdbc:h2:target/db/h2", "sa", "");

        activityStore = new ActivityStoreImpl();

        InputStream is = ActivityStoreImplTest.class.getResourceAsStream("/interactionActivity.xml");
        activity = ActivityModelUtil.deserialize(is);
    }

    @Test
    public void testSaveCorrelationIDEntity() throws Exception {
        List<CorrelationIDEntity> entities = activityStore.saveCorrelationIDEntity(activity.getCorrelation());
        assertEquals(1, entities.size());
        assertEquals("orderId=1,supplierId=1", entities.get(0).getValue());
    }


    @Test
    public void testSaveInteractionActivity() throws Exception {
        activityStore.save(activity);

        Activity theAct = activityStore.find(activity.getId());
        assertEquals(1, theAct.getContext().size());
        assertTrue(theAct.getType() instanceof InteractionActivity);

        List<Activity> acts = activityStore.findByCorrelation(activity.getCorrelation().get(0));
        assertEquals("Activity-1", acts.get(0).getId());

        List<Activity> actContexts = activityStore.findByContext(activity.getContext());
        assertEquals("Activity-1", actContexts.get(0).getId());

        List<Context> condition = new ArrayList<Context>();
        condition.add(activity.getContext().get(0));

        List<Activity> result = activityStore.findByContext(condition);
        assertEquals("Activity-1", result.get(0).getId());
    }



    @Test
    public void testFindByContext() throws Exception {
        InputStream is = getClass().getResourceAsStream("/interactionActivity2.xml");
        Activity theAct = ActivityModelUtil.deserialize(is);
        activityStore.save(theAct);

        List<Activity> result = activityStore.findByContext(theAct.getContext());
        
        boolean resultFlag = false;
        for (Activity act : result) {
        	if ("Activity-2".equals(act.getId())) {
        		resultFlag = true;
        	}
        }
        
        assertTrue(resultFlag);
    }


    @AfterClass
    public static void tearDown() throws Exception {
        activityStore.close();
    }

}
