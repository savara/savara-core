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
package org.savara.scenario.simulator.sca;

import java.util.HashMap;
import java.util.Map;

public class ServiceStore {
    
    private static Map<String,ServiceInvoker> services = new HashMap<String,ServiceInvoker>();
    private static Map<String,ReferenceInvoker> references = new HashMap<String,ReferenceInvoker>();
    
    
    public static void addService(String uri, ServiceInvoker serviceInvoker) {
        services.put(uri, serviceInvoker);
    }
    
    public static java.util.Collection<ServiceInvoker> getServices() {
    	return(services.values());
    }

    public static ServiceInvoker getService(String uri) {
        return services.get(uri);
    }

    public static void removeService(String uri) {
        services.remove(uri);
    }

    public static void addReference(String uri, ReferenceInvoker refInvoker) {
        references.put(uri, refInvoker);
    }
    
    public static java.util.Collection<ReferenceInvoker> getReferences() {
    	return(references.values());
    }

    public static ReferenceInvoker getReference(String uri) {
        return references.get(uri);
    }

    public static void removeReference(String uri) {
    	references.remove(uri);
    }

}
