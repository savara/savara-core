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
package org.savara.activity.astore.rdbms;

import org.savara.activity.ActivityStore;
import org.savara.activity.astore.rdbms.model.ActivityEntity;
import org.savara.activity.astore.rdbms.model.ComponentActivityEntity;
import org.savara.activity.astore.rdbms.model.CorrelationIDEntity;
import org.savara.activity.astore.rdbms.model.InteractionActivityEntity;
import org.savara.activity.model.*;
import org.savara.activity.util.ActivityModelUtil;
import org.savara.common.config.Configuration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: Jeff Yu
 * @date: 20/04/11
 */
public class ActivityStoreImpl implements ActivityStore {

    public  static final String TRANSACTION_MANAGER_JNDI_NAME = "transaction.manager.jndi.name";

	private static final Logger logger = Logger.getLogger(ActivityStoreImpl.class.toString());

    private TransactionManager txManager;

    private static EntityManagerFactory emf;

    private EntityManager entityManager;

    private boolean isInitialized = false;

    private Configuration configuration;

    private TxContext txContext;

    public  ActivityStoreImpl() {

    }

    public ActivityStoreImpl(TransactionManager manager) {
        this.txManager = manager;
    }


    private void initialize() {
		if (emf == null) {
			Map<String, String> props = new HashMap<String, String>();
			//TODO: convert some of configuration properties into props map.
			emf = Persistence.createEntityManagerFactory("activity-store-unit", props);
		}
		if (entityManager == null || !entityManager.isOpen()) {
			entityManager = emf.createEntityManager();
		}

		if (configuration != null) {
			String txManagerJndiName = configuration.getProperty(TRANSACTION_MANAGER_JNDI_NAME);
			if (txManagerJndiName != null) {
				getTransactionManagerFromJNDI(txManagerJndiName);
			}
		}
		if (txManager != null) {
			txContext = new JPAJTAContext(txManager, entityManager);
		} else {
			txContext = new JPANonTxContext(entityManager);
		}
        this.isInitialized = true;
    }

    private void getTransactionManagerFromJNDI(String txManagerJndiName) {
		try {
			txManager = (TransactionManager) InitialContext.doLookup(txManagerJndiName);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Error in getting Transaction Manager from JNDI: " + txManagerJndiName);
			throw new RuntimeException(e);
		}
	}

    public void setConfiguration(Configuration config) {
        this.configuration = config;
    }

    public void save(Activity activity) {
       if (!isInitialized) {
           initialize();
       }
       ActivityEntity entity;
       if (activity instanceof InteractionActivity) {
           entity = new InteractionActivityEntity();
       } else if (activity instanceof ComponentActivity) {
           entity = new ComponentActivityEntity();
       } else {
           throw new RuntimeException("Can not save the ActivityEntity: " + activity.getClass().getName());
       }
       try {

           entity.setActId(activity.getId());
           entity.setTimestamp(activity.getTimestamp().toGregorianCalendar().getTime());
           entity.setActivityModel(ActivityModelUtil.serialize(activity));
           entity.setProperties(getContextValue(activity.getContext()));
           List<CorrelationIDEntity> ids = saveCorrelationIDEntity(activity.getCorrelation());

           txContext.begin();
           entity.setCorrelationIds(ids);
           entityManager.persist(entity);
           txContext.commit();
       } catch (Exception e) {
           txContext.rollback();
           throw new RuntimeException("Error in persist ActivityEntity.", e);
       }
    }


    protected List<CorrelationIDEntity> saveCorrelationIDEntity(List<Correlation> correlations) {
        if (!isInitialized) {
            initialize();
        }
        try {
           txContext.begin();
           List<CorrelationIDEntity> result = new ArrayList<CorrelationIDEntity>();
           for(Correlation correlation : correlations) {
               String correlationKeyValue = getCorrelationKeyValue(correlation);
               CorrelationIDEntity entity = findCorrelationIDEntity(correlationKeyValue);
               if (entity == null) {
                   entity = new CorrelationIDEntity();
                   entity.setValue(correlationKeyValue);
                   entityManager.persist(entity);
               }
               result.add(entity);
           }
           txContext.commit();
           return result;
        } catch (Exception e) {
           txContext.rollback();
           throw new RuntimeException("Error in saving CorrelationIDEntity.", e);
        }
    }


    private CorrelationIDEntity findCorrelationIDEntity(String correlationKeyValue) {
          Query query = entityManager.createQuery("select c from CorrelationIDEntity as c where c.value = :value");
          query.setParameter("value", correlationKeyValue);
          List<CorrelationIDEntity> result = query.getResultList();
          if (result.size() < 1 ) {
             return null;
          } else if (result.size() == 1) {
             return result.get(0);
          } else {
             throw new RuntimeException("Founding two correlation keys in the DB. the correlation key value is: " + correlationKeyValue);
          }
    }

    private String getCorrelationKeyValue(Correlation correlation) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (CorrelationKey key : correlation.getKey()) {
            builder.append(key.getName() + "=" + key.getValue());
            i++;
            if (correlation.getKey().size()-1 > i) {
                builder.append(",");
            }
        }
        return builder.toString();
    }


    private String getContextValue(List<Context> contexts) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Context context: contexts) {
            builder.append(context.getName() + "=" + context.getValue());
            i++;
            if (contexts.size() - 1 > i) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public Activity find(String id) {
         if (!isInitialized) {
             initialize();
         }

        ActivityEntity entity = findByID(id);
        if (entity == null) {
            return null;
        }
        try {
            return ActivityModelUtil.deserialize(entity.getActivityModel());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Activity> findByCorrelation(Correlation correlation) {
        if (!isInitialized) {
            initialize();
        }

        String correlationValue = getCorrelationKeyValue(correlation);
        List<Activity> activities = new ArrayList<Activity>();
        try {
            txContext.begin();
            Query query = entityManager.createQuery("select ID from CorrelationIDEntity as ID where ID.value = :value");
            query.setParameter("value", correlationValue);
            List<CorrelationIDEntity> ids =  query.getResultList();
            for (CorrelationIDEntity id : ids) {
                Collection<ActivityEntity> entities = id.getActivities();
                for (ActivityEntity entity : entities) {
                    activities.add(ActivityModelUtil.deserialize(entity.getActivityModel()));
                }
            }
            txContext.commit();
        }catch (Exception e) {
            txContext.rollback();
            throw new RuntimeException("Error in finding by correlation.", e);
        }

        return activities;
    }

    public List<Activity> findByContext(List<Context> contexts) {
        if (!isInitialized) {
            initialize();
        }

        String theContext = getContextValue(contexts);
        List<Activity> activities = new ArrayList<Activity>();
        try {
            txContext.begin();
            Query query = entityManager.createQuery("select a from ActivityEntity as a where a.properties like :contexts");
            query.setParameter("contexts", "%" + theContext + "%");
            List<ActivityEntity> actEntities = query.getResultList();
            for (ActivityEntity entity : actEntities) {
                activities.add(ActivityModelUtil.deserialize(entity.getActivityModel()));
            }
            txContext.commit();
            return activities;
        } catch (Exception e) {
            txContext.rollback();
            throw new RuntimeException("Error in finding by correlation.", e);
        }
    }


    private ActivityEntity findByID(String id) {
         txContext.begin();
         Query query = entityManager.createQuery("select a from ActivityEntity as a where a.actId = :actId");
         query.setParameter("actId", id);
         ActivityEntity entity = (ActivityEntity)query.getSingleResult();
         txContext.commit();
        return entity;
    }

    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
		if (emf != null) {
			emf.close();
			emf = null;
		}
    }
}
