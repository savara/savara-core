/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.protocol.export.monitor;

import static org.junit.Assert.*;

import org.savara.protocol.model.Join;
import org.savara.protocol.model.Sync;
import org.scribble.common.logging.CachedJournal;
import org.scribble.protocol.model.*;
import org.scribble.protocol.monitor.util.MonitorModelUtil;

public class ForkJoinMonitorExportVisitorTest {
    
    @org.junit.Test
    public void testSimpleForkJoin() {
        
        ProtocolModel pm=new ProtocolModel();
    
        Role p1=new Role();
        p1.setName("p1");
        
        Role p2=new Role();
        p2.setName("p2");
        
        Protocol p=new Protocol();
        p.setLocatedRole(p1);
        pm.setProtocol(p);
        
        Interaction i1=new Interaction();
        MessageSignature ms1=new MessageSignature();
        ms1.setOperation("op1");
        i1.setMessageSignature(ms1);
        i1.getToRoles().add(p2);
        
        p.getBlock().add(i1);
        
        Parallel par1=new Parallel();
        
        p.getBlock().add(par1);
        
        Block b1=new Block();
        par1.getPaths().add(b1);
        
        Interaction i3=new Interaction();
        MessageSignature ms3=new MessageSignature();
        ms3.setOperation("op3");
        i3.setMessageSignature(ms3);
        i3.getToRoles().add(p2);
        
        b1.add(i3);
        
        Sync fork=new Sync();
        fork.setLabel("link1");
        b1.add(fork);
        
        Block b2=new Block();
        par1.getPaths().add(b2);
        
        Join join=new Join();
        join.getLabels().add("link1");
        b2.add(join);
        
        Interaction i5=new Interaction();
        MessageSignature ms5=new MessageSignature();
        ms5.setOperation("op5");
        i5.setMessageSignature(ms5);
        i5.getToRoles().add(p2);
        
        b2.add(i5);


        Interaction i6=new Interaction();
        MessageSignature ms6=new MessageSignature();
        ms6.setOperation("op6");
        i6.setMessageSignature(ms6);
        i6.setFromRole(p2);
        
        p.getBlock().add(i6);
                
        ForkJoinMonitorExportVisitor exporter=new ForkJoinMonitorExportVisitor();
        
        CachedJournal journal=new CachedJournal();
        exporter.setJournal(journal);
        
        pm.visit(exporter);
        
        if (journal.getIssues().size() > 0) {
            fail("Export should not have raised any issues");
        }
        
        if (exporter.getDescription() == null) {
        	fail("No monitor description generated");
        }
        
        validateMonitor(exporter.getDescription(), "SimpleForkJoin");
    }    

    @org.junit.Test
    public void testMultipleForkJoin() {
        
        ProtocolModel pm=new ProtocolModel();
    
        Role p1=new Role();
        p1.setName("p1");
        
        Role p2=new Role();
        p2.setName("p2");
        
        Protocol p=new Protocol();
        p.setLocatedRole(p1);
        pm.setProtocol(p);
        
        Interaction i1=new Interaction();
        MessageSignature ms1=new MessageSignature();
        ms1.setOperation("op1");
        i1.setMessageSignature(ms1);
        i1.getToRoles().add(p2);
        
        p.getBlock().add(i1);
        
        Parallel par1=new Parallel();
        
        p.getBlock().add(par1);
        
        Block b1=new Block();
        par1.getPaths().add(b1);
        
        Interaction i3=new Interaction();
        MessageSignature ms3=new MessageSignature();
        ms3.setOperation("op3");
        i3.setMessageSignature(ms3);
        i3.getToRoles().add(p2);
        
        b1.add(i3);
        
        Sync fork=new Sync();
        fork.setLabel("link1");
        b1.add(fork);
        
        Block b2=new Block();
        par1.getPaths().add(b2);
        
        Join join=new Join();
        join.getLabels().add("link1");
        join.getLabels().add("link2");
        b2.add(join);
        
        Interaction i5=new Interaction();
        MessageSignature ms5=new MessageSignature();
        ms5.setOperation("op5");
        i5.setMessageSignature(ms5);
        i5.getToRoles().add(p2);
        
        b2.add(i5);

        Block b3=new Block();
        par1.getPaths().add(b3);
        
        Interaction i4=new Interaction();
        MessageSignature ms4=new MessageSignature();
        ms4.setOperation("op4");
        i4.setMessageSignature(ms4);
        i4.getToRoles().add(p2);
        
        b3.add(i4);
        
        Sync fork2=new Sync();
        fork2.setLabel("link2");
        b3.add(fork2);
        

        Interaction i6=new Interaction();
        MessageSignature ms6=new MessageSignature();
        ms6.setOperation("op6");
        i6.setMessageSignature(ms6);
        i6.setFromRole(p2);
        
        p.getBlock().add(i6);
                
        ForkJoinMonitorExportVisitor exporter=new ForkJoinMonitorExportVisitor();
        
        CachedJournal journal=new CachedJournal();
        exporter.setJournal(journal);
        
        pm.visit(exporter);
        
        if (journal.getIssues().size() > 0) {
            fail("Export should not have raised any issues");
        }
        
        if (exporter.getDescription() == null) {
        	fail("No monitor description generated");
        }
        
        validateMonitor(exporter.getDescription(), "MultipleForkJoin");
    }    

    @org.junit.Test
    public void testRepeatForkJoin() {
        
        ProtocolModel pm=new ProtocolModel();
    
        Role p1=new Role();
        p1.setName("p1");
        
        Role p2=new Role();
        p2.setName("p2");
        
        Protocol p=new Protocol();
        p.setLocatedRole(p1);
        pm.setProtocol(p);
        
        Interaction i1=new Interaction();
        MessageSignature ms1=new MessageSignature();
        ms1.setOperation("op1");
        i1.setMessageSignature(ms1);
        i1.getToRoles().add(p2);
        
        p.getBlock().add(i1);
        
        Repeat repeat1=new Repeat();
        p.getBlock().add(repeat1);
        
        Parallel par1=new Parallel();
        
        repeat1.getBlock().add(par1);
        
        Block b1=new Block();
        par1.getPaths().add(b1);
        
        Interaction i3=new Interaction();
        MessageSignature ms3=new MessageSignature();
        ms3.setOperation("op3");
        i3.setMessageSignature(ms3);
        i3.getToRoles().add(p2);
        
        b1.add(i3);
        
        Sync fork=new Sync();
        fork.setLabel("link1");
        b1.add(fork);
        
        Block b2=new Block();
        par1.getPaths().add(b2);
        
        Join join=new Join();
        join.getLabels().add("link1");
        b2.add(join);
        
        Interaction i5=new Interaction();
        MessageSignature ms5=new MessageSignature();
        ms5.setOperation("op5");
        i5.setMessageSignature(ms5);
        i5.getToRoles().add(p2);
        
        b2.add(i5);


        Interaction i6=new Interaction();
        MessageSignature ms6=new MessageSignature();
        ms6.setOperation("op6");
        i6.setMessageSignature(ms6);
        i6.setFromRole(p2);
        
        repeat1.getBlock().add(i6);
                
        Interaction i7=new Interaction();
        MessageSignature ms7=new MessageSignature();
        ms7.setOperation("op7");
        i7.setMessageSignature(ms7);
        i7.setFromRole(p2);
        
        p.getBlock().add(i7);
                
        ForkJoinMonitorExportVisitor exporter=new ForkJoinMonitorExportVisitor();
        
        CachedJournal journal=new CachedJournal();
        exporter.setJournal(journal);
        
        pm.visit(exporter);
        
        if (journal.getIssues().size() > 0) {
            fail("Export should not have raised any issues");
        }
        
        if (exporter.getDescription() == null) {
        	fail("No monitor description generated");
        }
        
        validateMonitor(exporter.getDescription(), "RepeatForkJoin");
    }    

    protected void validateMonitor(org.scribble.protocol.monitor.model.Description pd, String filename) {
        java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
        String pdtext=null;
        
        try {
            MonitorModelUtil.serialize(pd, os);

            pdtext = new String(os.toByteArray());
            
            os.close();
        } catch (Exception e) {
            fail("Failed to serialize description associated with filename '"+filename+"'");
        }
        
        java.io.InputStream is=
            ForkJoinMonitorExportVisitorTest.class.getResourceAsStream("/monitor/results/"+filename+".txt");
    
        if (is == null) {
            fail("Failed to load protocol '"+filename+"'");
        }
        
        byte[] b=null;
        
        try {
            b = new byte[is.available()];
            
            is.read(b);
            
            is.close();
        } catch (Exception e) {
            fail("Failed to load result '"+filename+": "+e);
        }
        
        String result=new String(b);
        
        if (result.equals(pdtext) == false) {
            System.out.println("["+filename+"]="+pdtext);
            fail("Monitor result incorrect: was expecting (len="+result.length()+") ["+result+
                    "] but got (len="+pdtext.length()+") ["+pdtext+"]");
        }
     }
}
