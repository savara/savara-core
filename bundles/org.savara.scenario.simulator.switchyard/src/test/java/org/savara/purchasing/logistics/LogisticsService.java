
/*
 * 
 */

package org.savara.purchasing.logistics;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.4.0
 * 2012-09-02T18:03:53.711+01:00
 * Generated source version: 2.4.0
 * 
 */


@WebServiceClient(name = "LogisticsService", 
                  wsdlLocation = "wsdl/Purchasing_Logistics.wsdl",
                  targetNamespace = "http://www.savara.org/Purchasing/Logistics") 
public class LogisticsService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://www.savara.org/Purchasing/Logistics", "LogisticsService");
    public final static QName LogisticsPort = new QName("http://www.savara.org/Purchasing/Logistics", "LogisticsPort");
    static {
        URL url = null;
        try {
            url = new URL("wsdl/Purchasing_Logistics.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(LogisticsService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "wsdl/Purchasing_Logistics.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public LogisticsService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public LogisticsService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public LogisticsService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     * 
     * @return
     *     returns Logistics
     */
    @WebEndpoint(name = "LogisticsPort")
    public Logistics getLogisticsPort() {
        return super.getPort(LogisticsPort, Logistics.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Logistics
     */
    @WebEndpoint(name = "LogisticsPort")
    public Logistics getLogisticsPort(WebServiceFeature... features) {
        return super.getPort(LogisticsPort, Logistics.class, features);
    }

}
