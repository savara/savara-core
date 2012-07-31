/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.protocol.internal.aggregator;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.protocol.aggregator.ProtocolAggregator;
import org.savara.protocol.util.JournalProxy;
import org.savara.protocol.util.ProtocolServices;
import org.scribble.common.logging.CachedJournal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.MessageSignature;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeReference;
import org.scribble.protocol.model.Choice;

public class ProtocolAggregatorImplTest {

	// Protocols generated from BPEL
	// SAVARA-350 - currently does not aggregate the behaviour correctly
	@Test
	public void testPurchaseGoods() {
		testAggregateGlobalModel("PurchaseGoods", new String[] {
				"Store", "CreditAgency", "Logistics"});
	}

	@Test
	public void testMergePathsSimple() {
		Block b1=new Block();
		Block b2=new Block();
		
		Interaction i1=new Interaction(new Role("CreditAgency"),
				new MessageSignature(new TypeReference("CreditOk")));
		Interaction i2=new Interaction(new Role("Seller"),
				new MessageSignature(new TypeReference("Confirm")));
		
		b1.add(i1);
		b1.add(i2);
		
		b2.add(i1);
		b2.add(i2);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		if (targetPath.size() != 2) {
			fail("Target path should have two components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i1)) {
			fail("First component was unexpected");
		}
		
		if (!targetPath.get(1).equals(i2)) {
			fail("Second component was unexpected");
		}
	}
	
	@Test
	public void testMergePathsSmallDeviation() {
		Block b1=new Block();
		Block b2=new Block();
		
		Interaction i1=new Interaction(new Role("P1"),
				new MessageSignature(new TypeReference("A")));
		Interaction i2=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("B")));
		Interaction i3=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("C")));
		Interaction i4=new Interaction(new Role("P3"),
				new MessageSignature(new TypeReference("D")));
		
		b1.add(i1);
		b1.add(i2);
		b1.add(i4);
		
		b2.add(i1);
		b2.add(i3);
		b2.add(i4);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		if (targetPath.size() != 3) {
			fail("Target path should have three components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i1)) {
			fail("First component was unexpected");
		}
		
		if (!(targetPath.get(1) instanceof Choice)) {
			fail("Second component should be a choice");
		}
		
		Choice c=(Choice)targetPath.get(1);
		
		if (c.getRole() == null) {
			fail("Role not set");
		}
		
		if (!c.getRole().getName().equals("P2")) {
			fail("Choice role not P2");
		}
		
		if (c.getPaths().size() != 2) {
			fail("Two choice paths expected");
		}

		Block rb1=c.getPaths().get(0);
		if (rb1.size() != 1) {
			fail("Choice block 1 should only have single entry: "+rb1.size());
		}
		
		if (!rb1.get(0).equals(i2)) {
			fail("Choice block 1 should contain i2");
		}
		
		Block rb2=c.getPaths().get(1);
		if (rb2.size() != 1) {
			fail("Choice block 2 should only have single entry: "+rb2.size());
		}
		
		if (!rb2.get(0).equals(i3)) {
			fail("Choice block 2 should contain i3");
		}
		
		if (!targetPath.get(2).equals(i4)) {
			fail("Third component was unexpected");
		}
	}
	
	@Test
	public void testMergePathsOptional() {
		Block b1=new Block();
		Block b2=new Block();
		
		Interaction i1=new Interaction(new Role("P1"),
				new MessageSignature(new TypeReference("A")));
		Interaction i2=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("B")));
		Interaction i4=new Interaction(new Role("P4"),
				new MessageSignature(new TypeReference("D")));
		
		b1.add(i1);
		b1.add(i2);
		b1.add(i4);
		
		b2.add(i1);
		b2.add(i4);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		if (targetPath.size() != 3) {
			fail("Target path should have three components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i1)) {
			fail("First component was unexpected");
		}
		
		if (!(targetPath.get(1) instanceof Choice)) {
			fail("Second component should be a choice");
		}
		
		Choice c=(Choice)targetPath.get(1);
		
		if (c.getPaths().size() != 2) {
			fail("Two choice paths expected");
		}

		Block rb1=c.getPaths().get(0);
		if (rb1.size() != 1) {
			fail("Choice block 1 should only have single entry: "+rb1.size());
		}
		
		if (!rb1.get(0).equals(i2)) {
			fail("Choice block 1 should contain i2");
		}
		
		Block rb2=c.getPaths().get(1);
		if (rb2.size() != 0) {
			fail("Choice block 2 should only have no entry: "+rb2.size());
		}
		
		if (!targetPath.get(2).equals(i4)) {
			fail("Third component was unexpected");
		}
	}
	
	@Test
	public void testMergePathsMultiCommonDeviation() {
		Block b1=new Block();
		Block b2=new Block();
		Block b3=new Block();
		
		Interaction i1=new Interaction(new Role("P1"),
				new MessageSignature(new TypeReference("A")));
		Interaction i2=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("B")));
		Interaction i3=new Interaction(new Role("P3"),
				new MessageSignature(new TypeReference("C")));
		Interaction i4=new Interaction(new Role("P3"),
				new MessageSignature(new TypeReference("D")));
		Interaction i5=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("E")));
		Interaction i6=new Interaction(new Role("P6"),
				new MessageSignature(new TypeReference("F")));
		
		b1.add(i1);
		b1.add(i2);
		b1.add(i3);
		b1.add(i6);
		
		b2.add(i1);
		b2.add(i5);
		b2.add(i6);
		
		b3.add(i1);
		b3.add(i2);
		b3.add(i4);
		b3.add(i6);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		sourcePaths.add(b3);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		if (targetPath.size() != 3) {
			fail("Target path should have three components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i1)) {
			fail("First component was unexpected");
		}
		
		if (!(targetPath.get(1) instanceof Choice)) {
			fail("Second component should be a choice");
		}
		
		if (!targetPath.get(2).equals(i6)) {
			fail("Third component was unexpected");
		}
		
		Choice c=(Choice)targetPath.get(1);
		
		if (c.getRole() == null) {
			fail("Role not set");
		}
		
		if (!c.getRole().getName().equals("P2")) {
			fail("Choice role not P2");
		}
		
		if (c.getPaths().size() != 2) {
			fail("Two choice paths expected");
		}

		Block rb1=c.getPaths().get(0);
		if (rb1.size() != 2) {
			fail("Choice block 1 should only have 2 entries: "+rb1.size());
		}
		
		if (!rb1.get(0).equals(i2)) {
			fail("Choice block 1 should contain i2");
		}
		
		if (!(rb1.get(1) instanceof Choice)) {
			fail("Second component in first choice path should be another choice");
		}
		
		Choice innerChoice=(Choice)rb1.get(1);
		
		if (innerChoice.getRole() == null) {
			fail("Role not set");
		}
		
		if (!innerChoice.getRole().getName().equals("P3")) {
			fail("Choice role not P3");
		}
		
		if (innerChoice.getPaths().size() != 2) {
			fail("Inner choice should have two paths");
		}
		
		Block innerrb1=innerChoice.getPaths().get(0);
		if (innerrb1.size() != 1) {
			fail("Choice inner block 1 should only have 1 entry: "+innerrb1.size());
		}
		
		if (!innerrb1.get(0).equals(i3)) {
			fail("Choice inner block 1 should contain i3");
		}
		
		Block innerrb2=innerChoice.getPaths().get(1);
		if (innerrb2.size() != 1) {
			fail("Choice inner block 2 should only have 1 entry: "+innerrb2.size());
		}
		
		if (!innerrb2.get(0).equals(i4)) {
			fail("Choice inner block 2 should contain i4");
		}
		
		Block rb2=c.getPaths().get(1);
		if (rb2.size() != 1) {
			fail("Choice block 2 should only have 1 entry: "+rb2.size());
		}

		if (!rb2.get(0).equals(i5)) {
			fail("Choice block 2 should contain i5");
		}
	}
	
	@Test
	public void testMergePathsRepeatWithBeforeAndAfterInteractions() {
		Block b1=new Block();
		Block b2=new Block();
		Block b3=new Block();
		
		Interaction i1=new Interaction(new Role("P1"),
				new MessageSignature(new TypeReference("A")));
		Interaction i2=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("B")));
		Interaction i3=new Interaction(new Role("P3"),
				new MessageSignature(new TypeReference("C")));
		Interaction i4=new Interaction(new Role("P4"),
				new MessageSignature(new TypeReference("D")));
		
		b1.add(i1);
		b1.add(i2);
		b1.add(i3);
		b1.add(i4);
		
		b2.add(i1);
		b2.add(i2);
		b2.add(i3);
		b2.add(i2);
		b2.add(i3);
		b2.add(i4);
		
		b3.add(i1);
		b3.add(i4);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		sourcePaths.add(b3);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		aggregator.postProcessMerged(targetPath, handler);
		
		if (targetPath.size() != 3) {
			fail("Target path should have three components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i1)) {
			fail("First component was unexpected");
		}
		
		if (!(targetPath.get(1) instanceof Repeat)) {
			fail("Second component should be a repeat");
		}
		
		if (!targetPath.get(2).equals(i4)) {
			fail("Third component was unexpected");
		}
		
		Repeat r=(Repeat)targetPath.get(1);
		
		if (r.getRoles().size() == 0) {
			fail("Role not set");
		}
		
		if (!r.getRoles().get(0).getName().equals("P2")) {
			fail("Repeat role not P2");
		}
		
		if (r.getBlock().size() != 2) {
			fail("Repeat should have two components: "+r.getBlock().size());
		}

		if (!r.getBlock().get(0).equals(i2)) {
			fail("Repeat component 1 should contain i2");
		}
		
		if (!r.getBlock().get(1).equals(i3)) {
			fail("Repeat component 2 should contain i3");
		}
	}
	
	
	@Test
	public void testMergePathsRepeatWithoutBeforeAndAfterInteractions() {
		Block b1=new Block();
		Block b2=new Block();
		Block b3=new Block();
		
		Interaction i2=new Interaction(new Role("P2"),
				new MessageSignature(new TypeReference("B")));
		Interaction i3=new Interaction(new Role("P3"),
				new MessageSignature(new TypeReference("C")));
		
		b1.add(i2);
		b1.add(i3);
		
		b2.add(i2);
		b2.add(i3);
		b2.add(i2);
		b2.add(i3);
		
		b3.add(i2);
		b3.add(i3);
		b3.add(i2);
		b3.add(i3);
		b3.add(i2);
		b3.add(i3);
		
		Block targetPath=new Block();
		
		FeedbackHandler handler=new DefaultFeedbackHandler();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		sourcePaths.add(b1);
		sourcePaths.add(b2);
		sourcePaths.add(b3);
		
		ProtocolAggregatorImpl aggregator=new ProtocolAggregatorImpl();
		aggregator.mergePaths(sourcePaths, targetPath, handler);
		
		aggregator.postProcessMerged(targetPath, handler);
		
		if (targetPath.size() != 3) {
			fail("Target path should have three components: "+targetPath.size());
		}
		
		if (!targetPath.get(0).equals(i2)) {
			fail("Component 1 should be i2");
		}
		
		if (!targetPath.get(1).equals(i3)) {
			fail("Component 2 should be i3");
		}
		
		if (!(targetPath.get(2) instanceof Repeat)) {
			fail("Component 3 should be a repeat");
		}
		
		Repeat repeat=(Repeat)targetPath.get(2);
		
		if (repeat.getRoles().size() == 0) {
			fail("Role not set");
		}
		
		if (!repeat.getRoles().get(0).getName().equals("P2")) {
			fail("Repeat role not P2");
		}
		
		if (repeat.getBlock().size() != 2) {
			fail("Repeat should only have 2 components");
		}
		
		if (!repeat.getBlock().get(0).equals(i2)) {
			fail("Repeat Component 1 should be i2");
		}
		
		if (!repeat.getBlock().get(1).equals(i3)) {
			fail("Repeat Component 2 should be i3");
		}
		
	}
	
	@Test
	public void testPurchasingLocalAggregationAtBuyer() {
		testAggregateLocalModel("Purchasing", "Buyer", new String[]{
				"SuccessfulPurchase", "CustomerUnknown", "InsufficientCredit"});
	}
	
	@Test
	public void testPurchasingLocalAggregationAtStore() {
		testAggregateLocalModel("Purchasing", "Store", new String[]{
				"SuccessfulPurchase", "CustomerUnknown", "InsufficientCredit"});
	}
	
	@Test
	public void testPurchasingLocalAggregationAtCreditAgency() {
		testAggregateLocalModel("Purchasing", "CreditAgency", new String[]{
				"SuccessfulPurchase", "CustomerUnknown", "InsufficientCredit"});
	}
	
	@Test
	public void testPurchasingLocalAggregationAtLogistics() {
		testAggregateLocalModel("Purchasing", "Logistics", new String[]{
				"SuccessfulPurchase"});
	}
	
	@Test
	public void testPurchasing() {
		testAggregateGlobalModel("Purchasing", new String[] {
				"Buyer", "Store", "CreditAgency", "Logistics"});
	}

	/* Behaviour must be identical but imports and annotations maybe in different order
	@Test
	public void testPurchasingReorderedRoles() {
		testAggregateGlobalModel("Purchasing", new String[] {
				"CreditAgency", "Logistics", "Buyer", "Store"});
	}
	*/

	@Test
	public void testPurchasing2() {
		testAggregateGlobalModel("Purchasing2", new String[] {
				"Buyer", "Store", "CreditAgency", "Logistics"});
	}
	
	@Test
	public void testMultiPartyInteractionsAndChoice() {
		testAggregateGlobalModel("MultiPartyInteractionsAndChoice", new String[] {
				"Buyer", "Broker", "CreditAgency", "Seller"});
	}
	
	@Test
	public void testBarterLocalAggregationAtBuyer() {
		testAggregateLocalModel("Barter", "Buyer", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept"});
				// TODO: Allow alternate paths after repetition
				//"FirstOfferRejected", "SecondOfferRejected", "ThirdOfferRejected"});
	}

	@Test
	public void testBarterLocalAggregationAtSeller() {
		testAggregateLocalModel("Barter", "Seller", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept"});
				// TODO: Allow alternate paths after repetition
				//"FirstOfferRejected", "SecondOfferRejected", "ThirdOfferRejected"});
	}

	@Test
	public void testBarter() {
		testAggregateGlobalModel("Barter", new String[] {
				"Buyer", "Seller"});
	}

	@Test
	public void testBarterLocalAggregationWithRejectPathAtBuyer() {
		testAggregateLocalModel("BarterWithReject", "Buyer", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected"});
	}
	
	@Test
	public void testBarterLocalAggregationWithRejectPathAtSeller() {
		testAggregateLocalModel("BarterWithReject", "Seller", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected"});
	}

	@Test
	@Ignore("SAVARA-337")
	public void testBarterLocalAggregationWithReject2PathAtBuyer() {
		testAggregateLocalModel("BarterWithReject2", "Buyer", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "SecondOfferRejected"});
	}
	
	@Test
	@Ignore("SAVARA-337")
	public void testBarterLocalAggregationWithReject2PathAtSeller() {
		testAggregateLocalModel("BarterWithReject2", "Seller", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "SecondOfferRejected"});
	}

	@Test
	public void testBarterLocalAggregationWithReject3PathAtBuyer() {
		testAggregateLocalModel("BarterWithReject3", "Buyer", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "ThirdOfferRejected"});
	}
	
	@Test
	public void testBarterLocalAggregationWithReject3PathAtSeller() {
		testAggregateLocalModel("BarterWithReject3", "Seller", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "ThirdOfferRejected"});
	}
	
	@Test
	public void testBarterLocalAggregationWithReject4PathAtBuyer() {
		testAggregateLocalModel("BarterWithReject4", "Buyer", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "SecondOfferRejected", "ThirdOfferRejected"});
	}
	
	@Test
	public void testBarterLocalAggregationWithReject4PathAtSeller() {
		testAggregateLocalModel("BarterWithReject4", "Seller", new String[]{
				"FirstOfferAccept", "SecondOfferAccept", "ThirdOfferAccept",
				"FirstOfferRejected", "SecondOfferRejected", "ThirdOfferRejected"});
	}
	
	@Test
	public void testBarterWithReject() {
		testAggregateGlobalModel("BarterWithReject", new String[] {
				"Buyer", "Seller"});
	}
	
	protected void testAggregateLocalModel(String localName, String role,
						String[] scenarioLocalModels) {
		
		java.util.List<ProtocolModel> locals=new java.util.Vector<ProtocolModel>();
		
		for (String localModel : scenarioLocalModels) {
			String filename="testmodels/protocol/aggregator/scenario/"+localModel+"@"+role+".spr";

			ProtocolModel model=getModel(filename);
			
			if (model != null) {
				locals.add(model);
			}
		}
		
		ProtocolAggregator aggregator=new ProtocolAggregatorImpl();
		
		DefaultFeedbackHandler feedback=new DefaultFeedbackHandler();

		ProtocolModel aggregated=aggregator.aggregateLocalModel(localName, locals, feedback);
		
		if (feedback.getIssues().size() > 0) {
			fail("Issues detected");
		}
		
		if (aggregated == null) {
			fail("Aggregated model is null");
		}
		
		org.scribble.protocol.export.text.TextProtocolExporter exporter=
				new org.scribble.protocol.export.text.TextProtocolExporter();
			
		java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
		
		CachedJournal journal=new CachedJournal();
		
		exporter.export(aggregated, journal, os);
		
		try {
			os.close();
		} catch(Exception e) {
			fail("Failed to close stream");
		}
		
		String str=os.toString();
		
		checkResults(localName+"@"+role, "local", str);
	}
	
	protected void testAggregateGlobalModel(String globalName, String[] roles) {
		
		java.util.List<ProtocolModel> locals=new java.util.Vector<ProtocolModel>();
		
		for (String role : roles) {
			String filename="testmodels/protocol/aggregator/local/"+globalName+"@"+role+".spr";

			ProtocolModel model=getModel(filename);
			
			if (model != null) {
				locals.add(model);
			}
		}
		
		ProtocolAggregator aggregator=new ProtocolAggregatorImpl();
		
		DefaultFeedbackHandler feedback=new DefaultFeedbackHandler();

		ProtocolModel aggregated=aggregator.aggregateGlobalModel(globalName,
							locals, feedback);
		
		if (feedback.getIssues().size() > 0) {
			fail("Issues detected");
		}
		
		if (aggregated == null) {
			fail("Aggregated model is null");
		}
		
		org.scribble.protocol.export.text.TextProtocolExporter exporter=
				new org.scribble.protocol.export.text.TextProtocolExporter();
			
		java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
		
		CachedJournal journal=new CachedJournal();
		
		exporter.export(aggregated, journal, os);
		
		try {
			os.close();
		} catch(Exception e) {
			fail("Failed to close stream");
		}
		
		String str=os.toString();
		
		checkResults(globalName, "global", str);
	}

	protected void checkResults(String aggregatedProtocolName, String subfolder, String protocol) {
		boolean f_valid=false;

		// Load aggregated model
		String filename="testmodels/protocol/aggregator/"+subfolder+"/"+aggregatedProtocolName+".spr";
		
		java.io.InputStream is=
				ClassLoader.getSystemResourceAsStream(filename);
		
		if (is != null) {
			
			try {
				byte[] b=new byte[is.available()];
			
				is.read(b);
				
				is.close();
				
				String orig=new String(b);
				
				f_valid = orig.equals(protocol);
			} catch(Exception e) {
				fail("Failed to load protocol model: "+e);
			}
		} else {
			fail("Protocol '"+filename+
							"' not found for comparison");
		}
		
		if (f_valid == false) {			
			java.net.URL url=ClassLoader.getSystemResource(filename);
			
			if (url != null) {
				// URL will point to copy of test models in the classes folder, so need
				// to obtain reference back to source version
				java.io.File f=null;
				
				if (url.getFile().indexOf("target/test-classes") != -1) {
					f = new java.io.File(url.getFile().replaceFirst("target/test-classes","src/test/resources"));
				} else if (url.getFile().indexOf("classes") != -1) {
					f = new java.io.File(url.getFile().replaceFirst("classes","src/test/resources"));
				} else if (url.getFile().indexOf("bin") != -1) {						
					f = new java.io.File(url.getFile().replaceFirst("bin","src/test/resources"));
				} else {
					fail("Could not locate results folder to record expected result");
				}
				
				if (f != null && f.exists()) {
					f = f.getParentFile();
					
					java.io.File resultFile=new java.io.File(f,
										aggregatedProtocolName+".generated");
					
					if (resultFile.exists() == false) {
						try {
							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
							
							fos.write(protocol.getBytes());
							
							fos.flush();
							fos.close();
							
						} catch(Exception e){
							fail("Failed to write generated protocol: "+e);
						}
					} else {
						System.err.println("NOTE: Generated output '"+resultFile+
									"' already exists - not being overwritten");
					}
				} else {
					fail("Unable to obtain URL for protocol model '"+
							filename+"': "+url);
				}
			}
			
			fail("Aggregated protocol '"+aggregatedProtocolName+"' did not match expected description");
		}
	}
	
	protected ProtocolModel getModel(String filename) {
		java.net.URL url=
				ClassLoader.getSystemResource(filename);
	
		if (url == null) {
			fail("Unable to locate resource: "+filename);
		}
		
		FeedbackHandler feedback=new DefaultFeedbackHandler();
			
		org.scribble.protocol.model.ProtocolModel model=null;
		
		try {
			Content content=new ResourceContent(url.toURI());
			
			model = ProtocolServices.getParserManager().parse(null, content,
							new JournalProxy(feedback));
		} catch(Exception e) {
			fail("Parsing choreography failed");
		}
	
		return(model);
	}
}
