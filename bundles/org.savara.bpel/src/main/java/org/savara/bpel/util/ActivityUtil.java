/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.util;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.savara.bpel.model.TActivity;
import org.savara.bpel.model.TExtensibleElements;

public class ActivityUtil {
	
	private static Log logger = LogFactory.getLog(BPELInteractionUtil.class);

	public static TActivity getActivity(TExtensibleElements container) {
		TActivity ret=null;
		
		// TODO: Need to expand list of supported types
		
		try {
			Method method=container.getClass().getMethod("getSequence");
			
			if (method != null) {
				ret = (TActivity)method.invoke(container);
			}
		} catch(Exception e) {
			logger.error("Failed to get contained activity", e);
		}

		return(ret);
	}
}
