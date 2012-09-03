
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package org.savara.purchasing.store;

import org.jboss.examples.store.AccountNotFoundType;
import org.jboss.examples.store.BuyRequestType;
import org.jboss.examples.store.BuyConfirmedType;
import org.jboss.examples.creditagency.CreditCheckType;
import org.jboss.examples.creditagency.CreditRatingType;
import org.jboss.examples.logistics.DeliveryRequestType;
import org.jboss.examples.logistics.DeliveryConfirmedType;
import org.savara.purchasing.creditagency.CustomerUnknownFault;

import java.math.BigInteger;
import java.util.logging.Logger;

@org.switchyard.component.bean.Service(Store.class)
public class StoreImpl implements Store {

    @javax.inject.Inject @org.switchyard.component.bean.Reference
    org.savara.purchasing.creditagency.CreditAgency _creditAgency;

    @javax.inject.Inject @org.switchyard.component.bean.Reference
    org.savara.purchasing.logistics.Logistics _logistics;

    private static final Logger LOG = Logger.getLogger(StoreImpl.class.getName());

    /* (non-Javadoc)
     * @see org.savara.purchasing.store.Store#buy(org.jboss.examples.store.BuyRequestType  content )*
     */
    public org.jboss.examples.store.BuyConfirmedType buy(org.jboss.examples.store.BuyRequestType content) throws AccountNotFoundFault
					// ,InsufficientCreditFault
												{
    	BuyConfirmedType ret=null;

		// TODO: Add code here to handle request (in variable 'content')
		
		try {
			// TODO: Add code here to initialize request
			CreditCheckType creditCheckReq=new CreditCheckType();
			creditCheckReq.setCustomer(content.getCustomer());
			creditCheckReq.setId(content.getId());
			
System.out.println(">>>> CALL CREDIT CHECK");			
			CreditRatingType creditCheckResult = _creditAgency.creditCheck(creditCheckReq);
System.out.println(">>>> RETURNED="+creditCheckResult);			
		
			if (creditCheckResult.getRating().intValue() <= 5) { // TODO: Set expression
				//throw new InsufficientCreditFault();
			} else {
				// TODO: Add code here to initialize request
				DeliveryRequestType deliveryReq=new DeliveryRequestType();
				deliveryReq.setAddress("1001 Acme Street");
				deliveryReq.setId(content.getId());
				
				DeliveryConfirmedType deliveryResult = _logistics.delivery(deliveryReq);
				
				// TODO: Add code here to return response
				ret = new BuyConfirmedType();
				ret.setId(content.getId());
				
				BigInteger bi=new BigInteger("500");
				
				ret.setAmount(bi);
			}
		
		} catch (CustomerUnknownFault customerUnknown) {
System.out.println(">>>> RETURNED FAULT="+customerUnknown);			
			AccountNotFoundType anfe=new AccountNotFoundType();
			anfe.setId(content.getId());
			anfe.setReason("Customer Unknown");
			throw new AccountNotFoundFault("Customer Unknown", anfe);
		}

		return (ret);
    }

}
