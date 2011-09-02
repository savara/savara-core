package org.savara.activity.store.rdbms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Author: Jeff Yu
 * Date: 20/04/11
 */

@Entity
@DiscriminatorValue("INTERACTION")
public class InteractionActivityEntity extends  ActivityEntity{


}
