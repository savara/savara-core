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

import org.savara.bpel.model.*;

public class PartnerLinkUtil {

	public static TPartnerLink getPartnerLink(TProcess process, String name) {
		TPartnerLink ret=null;
		
		if (process.getPartnerLinks() == null) {
			process.setPartnerLinks(new TPartnerLinks());
		}
		
		java.util.List<TPartnerLink> pls=process.getPartnerLinks().getPartnerLink();
		for (int i=0; ret == null && i < pls.size(); i++) {
			if (pls.get(i).getName().equals(name)) {
				ret = pls.get(i);
			}
		}
		
		return(ret);
	}

	/**
	 * This method attempts to identify the client's partner role name.
	 * 
	 * @param The partner link name
	 * @return The partner role
	 */
	public static String getClientPartnerRole(String partnerLink) {
		String ret=partnerLink;
		int index=-1;
		
		if (ret != null && (index=ret.indexOf("To")) != -1) {
			ret = ret.substring(index+2);
		}
		
		return(ret);
	}

	/**
	 * This method attempts to identify the server's partner role name.
	 * 
	 * @param The partner link name
	 * @return The partner role
	 */
	public static String getServerPartnerRole(String partnerLink) {
		String ret=partnerLink;
		int index=-1;
		
		if (ret != null && (index=ret.indexOf("To")) != -1) {
			ret = ret.substring(0, index);
		}
		
		return(ret);
	}
}
