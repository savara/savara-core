
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package org.savara.examples.store;

import org.jboss.examples.store.BuyRequestType;
import org.jboss.examples.store.BuyConfirmedType;
import org.jboss.examples.creditagency.CreditCheckType;
import org.jboss.examples.creditagency.CreditRatingType;
import org.jboss.examples.logistics.DeliveryRequestType;
import org.jboss.examples.logistics.DeliveryConfirmedType;
import org.savara.examples.creditagency.CustomerUnknownFault;

import java.util.logging.Logger;

@org.switchyard.component.bean.Service(Store.class)
public class StoreImpl implements Store {

    @javax.inject.Inject @org.switchyard.component.bean.Reference
    org.savara.examples.logistics.Logistics _logistics;

    @javax.inject.Inject @org.switchyard.component.bean.Reference
    org.savara.examples.creditagency.CreditAgency _creditAgency;

    private static final Logger LOG = Logger.getLogger(StoreImpl.class.getName());

    /* (non-Javadoc)
     * @see org.savara.examples.store.Store#buy(org.jboss.examples.store.BuyRequestType  content )*
     */
    public org.jboss.examples.store.BuyConfirmedType buy(org.jboss.examples.store.BuyRequestType content) throws InsufficientCreditFault , AccountNotFoundFault    {
        BuyConfirmedType ret=null;

        // TODO: Add code here to handle request (in variable 'content')

        try {
            // TODO: Add code here to initialize request
            CreditCheckType creditCheckReq=null;

            CreditRatingType creditCheckResult = _creditAgency.creditCheck(creditCheckReq);

            if (false) { // TODO: Set expression
                throw new InsufficientCreditFault();
            } else {
                // TODO: Add code here to initialize request
                DeliveryRequestType deliveryReq=null;

                DeliveryConfirmedType deliveryResult = _logistics.delivery(deliveryReq);

                // TODO: Add code here to return response
                // ret = ....;
            }

        } catch (CustomerUnknownFault customerUnknown) {
            throw new AccountNotFoundFault();
        }

        return (ret);
    }

}
