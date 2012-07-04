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
package org.savara.protocol.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.common.logging.DefaultFeedbackHandler;
import org.scribble.common.logging.Journal;
import org.scribble.common.resource.Content;
import org.scribble.common.resource.ResourceContent;
import org.scribble.protocol.parser.antlr.ANTLRProtocolParser;

public class ProtocolUtilsTest {

	@Test
	public void test() {
		String filename="testmodels/protocol/LocalizeIntroduces.spr";
		
		java.net.URL url=
			ClassLoader.getSystemResource(filename);
		
		if (url == null) {
			fail("Unable to locate resource: "+filename);
		} else {			
			DefaultFeedbackHandler handler=new DefaultFeedbackHandler();
			Journal journal=new JournalProxy(handler);
			
			org.scribble.protocol.model.ProtocolModel model=null;
			
			ANTLRProtocolParser parser=new ANTLRProtocolParser();
			parser.setAnnotationProcessor(new org.savara.protocol.parser.AnnotationProcessor());
			
			try {
				Content content=new ResourceContent(url.toURI());
				
				model = parser.parse(null, content, journal);
			} catch(Exception e) {
				fail("Parsing protocol failed");
			}
			
			if (model == null) {
				fail("Model is null");
			} else {
				ProtocolUtils.localizeRoleIntroductions(model);
				
				org.scribble.protocol.export.text.TextProtocolExporter exporter=
						new org.scribble.protocol.export.text.TextProtocolExporter();
					
				java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
				
				exporter.export(model, journal, os);
				
				try {
					os.close();
				} catch(Exception e) {
					fail("Failed to close stream");
				}
				
				String str=os.toString();

	    		filename="results/protocol/LocalizeIntroduces.spr";
	    		
	    		java.io.InputStream is=
	    				ClassLoader.getSystemResourceAsStream(filename);
	    		
	    		if (is != null) {
	    			
	    			try {
	    				byte[] b=new byte[is.available()];
	    			
	    				is.read(b);
	    				
	    				is.close();
	    				
	    				String expected=new String(b);
	    				
	    				if (!expected.equals(str)) {
	    					System.err.println("Localized introduces not as expected:\r\n"+
	    								expected+"\r\nbut got:\r\n"+str);
	    					
	    					fail("Localized introduces not as expected:\r\n"+
	    								expected+"\r\nbut got:\r\n"+str);
	    				}
	    			} catch (Exception e) {
	    				fail("Failed to load expected protocol");
	    			}
	    		}
			}
		}
	}

}
