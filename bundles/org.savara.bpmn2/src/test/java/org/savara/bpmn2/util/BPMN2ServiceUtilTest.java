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
package org.savara.bpmn2.util;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.savara.bpmn2.model.TError;
import org.savara.bpmn2.model.TInterface;
import org.savara.bpmn2.model.TMessage;
import org.savara.bpmn2.model.TOperation;
import org.savara.bpmn2.model.TParticipant;
import org.savara.bpmn2.util.BPMN2ServiceUtil.ModelInfo;

public class BPMN2ServiceUtilTest {

	@Test
	public void testIntrospect() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoodsNoInterfaces.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}
			
			java.util.Map<TParticipant, TInterface> intfs=BPMN2ServiceUtil.introspect(defns);
			
			if (intfs == null) {
				fail("Null returned");
			}
			
			if (intfs.size() != 3) {
				fail("Expected 3 participants: "+intfs.size());
			}
			
			ModelInfo modelInfo=new ModelInfo(null, null, null, defns.getRootElement());
			
			for (TParticipant p : intfs.keySet()) {
				TInterface intf=intfs.get(p);
				if (intf == null) {
					fail("No interface for participant: "+p.getName());
				}
				if (!intf.getName().equals(BPMN2ServiceUtil.getInterfaceName(p))) {
					fail("Interface name '"+intf.getName()+"' is incorrect");
				}
				if (intf.getOperation().size() != 1) {
					fail("Should only be one operation:"+intf.getOperation().size());
				}
				
				TOperation op=intf.getOperation().get(0);
				
				if (op.getOutMessageRef() == null || op.getInMessageRef() == null) {
					fail("Expected in and out message ref");
				}
				
				String reqType=null;
				String respType=null;
				java.util.List<String> errorCodes=new java.util.Vector<String>();

				if (p.getName().equals("Store")) {
					reqType = "BuyRequest";
					respType = "BuyConfirmed";
					errorCodes.add("AccountNotFound");
					errorCodes.add("InsufficientCredit");
				} else if (p.getName().equals("CreditAgency")) {
					reqType = "CreditCheck";
					respType = "CreditRating";
					errorCodes.add("CustomerUnknown");
				} else if (p.getName().equals("Logistics")) {
					reqType = "DeliveryRequest";
					respType = "DeliveryConfirmed";
				} else {
					fail("Unknown participant: "+p.getName());
				}

				TMessage m=modelInfo.getMessage(op.getInMessageRef().getLocalPart());
				if (!m.getName().equals(reqType)) {
					fail("Expected "+reqType+": "+m.getName());
				}
				
				m = modelInfo.getMessage(op.getOutMessageRef().getLocalPart());
				if (!m.getName().equals(respType)) {
					fail("Expected "+respType+": "+m.getName());
				}
				
				for (QName qname : op.getErrorRef()) {
					TError err = modelInfo.getError(qname.getLocalPart());
					if (!errorCodes.contains(err.getErrorCode())) {
						fail("Invalid error code");
					} else {
						errorCodes.remove(err.getErrorCode());
					}
				}
				
				if (errorCodes.size() > 0) {
					fail("Not all expected error codes found");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
	
	@Test
	public void testGetOperationName() {
		TMessage m=new TMessage();
		m.setName("BuyRequest");
		
		String opName=BPMN2ServiceUtil.getOperationName(m);
		
		if (opName == null) {
			fail("No op name");
		}
		
		if (!opName.equals("buy")) {
			fail("Expecting 'buy', but got: "+opName);
		}
	}
	
	@Test
	public void testMergeNew() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoodsNoInterfaces.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}
			
			java.util.Map<TParticipant, TInterface> intfs=BPMN2ServiceUtil.introspect(defns);
			
			if (intfs == null) {
				fail("Null returned");
			}
			
			if (intfs.size() != 3) {
				fail("Expected 3 participants: "+intfs.size());
			}

			BPMN2ServiceUtil.merge(defns, intfs);
			
			java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
			
			BPMN2ModelUtil.serialize(defns, baos, null);
			
			baos.close();
			
			String text=new String(baos.toByteArray());
			
			checkResults("PurchaseGoodsWithInterfaces", text);

		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
	
	@Test
	public void testMergeExisting() {
		java.io.InputStream is=
			ClassLoader.getSystemResourceAsStream("testmodels/bpmn2/choreo/PurchaseGoodsNoInterfaces2.bpmn");

		try {
			org.savara.bpmn2.model.TDefinitions defns=BPMN2ModelUtil.deserialize(is);
		
			if (defns == null) {
				fail("No definitions returned");
			}
			
			java.util.Map<TParticipant, TInterface> intfs=BPMN2ServiceUtil.introspect(defns);
			
			if (intfs == null) {
				fail("Null returned");
			}
			
			if (intfs.size() != 3) {
				fail("Expected 3 participants: "+intfs.size());
			}

			BPMN2ServiceUtil.merge(defns, intfs);
			
			java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
			
			BPMN2ModelUtil.serialize(defns, baos, null);
			
			baos.close();
			
			String text=new String(baos.toByteArray());
			
			checkResults("PurchaseGoodsWithInterfaces2", text);

		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to load BPMN2 definition: "+e);
		}
	}
	
	protected void checkResults(String name, String result) {
		boolean f_valid=false;
		String error=null;

		String filename="results/bpmn2/choreo/"+name+".bpmn";
		
		java.io.InputStream is=
				ClassLoader.getSystemResourceAsStream(filename);
		
		if (is != null) {
			
			try {
				byte[] b=new byte[is.available()];
			
				is.read(b);
				
				is.close();
				
				String orig=new String(b);
				
				if (orig.equals(result) == false) {
					error = "Generated BPMN2 model does not match stored version";
				} else {
					f_valid = true;
				}
			} catch(Exception e) {
				fail(e.toString());
			}
		} else {
			System.err.println("Generating file, as comparison file not found");
		}
		
		if (f_valid == false) {
			String bpmn2file="testmodels/bpmn2/choreo";
			
			java.net.URL url=ClassLoader.getSystemResource(bpmn2file);
			
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
					f = f.getParentFile().getParentFile().getParentFile();
					
					java.io.File resultsDir=new java.io.File(f, "results/bpmn2/choreo");
					
					if (resultsDir.exists() == false) {
						resultsDir.mkdirs();
					}
					
					java.io.File resultFile=new java.io.File(resultsDir,
										name+".generated");
					
					if (resultFile.exists() == false) {
						try {
							java.io.FileOutputStream fos=new java.io.FileOutputStream(resultFile);
							
							fos.write(result.getBytes());
							
							fos.flush();
							fos.close();
							
						} catch(Exception e){
							fail(e.toString());
						}
					} else {
						System.err.println("NOTE: Generated output '"+resultFile+
									"' already exists - not being overwritten");
					}
				} else {
					fail("Unable to obtain URL for BPMN2 model source '"+
							name+"': "+url);
				}
			}
		}
		
		if (error != null) {
			fail(error);
		}
	}
}
