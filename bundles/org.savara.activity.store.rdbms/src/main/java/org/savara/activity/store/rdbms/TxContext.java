/**
 * 
 */
package org.savara.activity.store.rdbms;

/**
 * Transaction context
 * 
 * @author Jeff Yu
 *
 */
public interface TxContext {
	
	public void begin();
	
	public void commit();
	
	public void rollback();
	
}
