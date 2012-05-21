/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.protocol.internal.aggregator;

import org.savara.protocol.internal.aggregator.GlobalProtocolUnit.Container;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.util.RoleUtil;

public class LocalProtocolUnit {
	
	private ActivityCursor _cursor=null;

	public LocalProtocolUnit(ProtocolModel model) {
		_cursor = new ActivityCursor(model.getProtocol().getLocatedRole(),
						model.getProtocol().getBlock());
	}
	
	public ActivityCursor getRootCursor() {
		return(_cursor);
	}
	
	public ActivityCursor getSendingCursor() {
		return(_cursor.getSendingCursor());
	}
	
	public ActivityCursor findReceivingCursor(Activity send) {
		return(_cursor.findReceivingCursor(send));
	}
	
	public ActivityCursor getIndividualCursor() {
		return(_cursor.getIndividualCursor());
	}
	
	public ActivityCursor getRepetitionCursor() {
		return(_cursor.getRepetitionCursor());
	}
	
	public class ActivityCursor {
		
		private Role _role=null;
		private Block _block=null;
		private int _position=0;
		private Container _container=null;
		private ActivityCursor _parent=null;
		private java.util.List<ActivityCursor> _cursors=new java.util.Vector<ActivityCursor>();
		
		public ActivityCursor(Role r, Block b) {
			_role = r;
			_block = b;
		}
		
		public ActivityCursor(ActivityCursor parent, Role r, Block b) {
			this(r, b);
			_parent = parent;
		}
		
		public Role getRole() {
			return (_role);
		}
		
		public ActivityCursor createCursor(Block b) {
			ActivityCursor ret=new ActivityCursor(this, _role, b);
			_cursors.add(ret);
			return(ret);
		}
		
		public Container getContainer() {
			return(_container);
		}
		
		public ActivityCursor getParent() {
			return(_parent);
		}
		
		public void setContainer(Container c) {
			_container = c;
		}
		
		private ActivityCursor getSendingCursor() {
			ActivityCursor ret=null;
			
			if (_cursors.size() > 0) {
				for (ActivityCursor cur : _cursors) {
					ret = cur.getSendingCursor();
					if (ret != null) {
						break;
					}
				}
			} else if (isSendingAction()) {
				ret = this;
			}
			
			return(ret);
		}
		
		private ActivityCursor getIndividualCursor() {
			ActivityCursor ret=null;
			
			if (_cursors.size() > 0) {
				for (ActivityCursor cur : _cursors) {
					ret = cur.getIndividualCursor();
					if (ret != null) {
						break;
					}
				}
			} else if (isIndividualAction()) {
				ret = this;
			}
			
			return(ret);
		}
		
		private ActivityCursor getRepetitionCursor() {
			ActivityCursor ret=null;
			
			if (_cursors.size() > 0) {
				for (ActivityCursor cur : _cursors) {
					ret = cur.getRepetitionCursor();
					if (ret != null) {
						break;
					}
				}
			} else if (isRepetitionAction()) {
				ret = this;
			}
			
			return(ret);
		}
		
		protected boolean isSendingAction() {
			boolean ret=false;
			Activity act=peek();
			
			if (act instanceof Interaction) {
				ret = ((Interaction)act).getFromRole() == null;
				/*
			} else if (act instanceof Choice) {
				ret = ((Choice)act).getRole() != null &&
						((Choice)act).getRole().equals(_role);
						*/
			}
			
			return(ret);
		}
		
		protected boolean isIndividualAction() {
			boolean ret=false;
			Activity act=peek();
			
			if (act instanceof Introduces) {
				ret = true;
			} else if (act instanceof Choice) {
				//Choice choice=(Choice)act;
				
				// Ok to treat as individual action if the choice
				// is not associated with the decision maker
				// NOTE: Currently decision makers (and matching receive
				// choices) are handled as matched actions, but
				// in the future may want to simplify so that both
				// are treated as individual actions? Benefit of
				// current approach is that it may delay establishing
				// the global choice until the decision maker has
				// a matching receive choice.
				//ret = (choice.getRole() == null ||
				//		!choice.getRole().equals(_role));
				ret = true;
			}
			
			return(ret);
		}
		
		protected boolean isRepetitionAction() {
			boolean ret=false;
			Activity act=peek();
			
			if (act instanceof Repeat) {
				ret = true;
			}
			
			return(ret);
		}
		
		/**
		 * This method determines the list of roles involved in
		 * the repetition construct.
		 * 
		 * @return The roles
		 */
		public java.util.Set<Role> getRepetitionRoles() {
			java.util.Set<Role> ret=null;
			
			Activity act=peek();
			
			if (act != null && isRepetitionAction()) {
				ret = RoleUtil.getUsedRoles(act);
			} else {
				ret = new java.util.HashSet<Role>();
			}
			
			return (ret);
		}
		
		public Activity peek() {
			return(_position < _block.size() ? _block.get(_position) : null);
		}
		
		public boolean next() {
			_position++;
			
			boolean ret=_position < _block.size();
			
			if (!ret) {
				delete();
			}
			
			return(ret);
		}
		
		protected void delete() {
			if (_cursors.size() == 0 && _parent != null) {
				_parent.deleteCursor(this);
			}
		}
		
		protected void deleteCursor(ActivityCursor cursor) {
			_cursors.remove(cursor);
			
			// Check if should propagate deletion
			if (_position >= _block.size()) {
				delete();
			}
		}
		
		public ActivityCursor findReceivingCursor(Activity send) {
			ActivityCursor ret=null;
			
			if (_cursors.size() > 0) {
				for (ActivityCursor cur : _cursors) {
					ret = cur.findReceivingCursor(send);
					if (ret != null) {
						break;
					}
				}
			} else if (getReceiveAction(send) != null) {
				ret = this;
			}
			
			return(ret);
		}
		
		public Activity getReceiveAction(Activity send) {
			Activity ret=null;
			
			Activity cur=peek();
			
			if (cur instanceof Interaction && send instanceof Interaction) {
				Interaction receive=(Interaction)cur;
				
				if (((Interaction)send).getMessageSignature().equals(receive.getMessageSignature()) &&
						receive.getFromRole() != null) {
					ret = receive;
				}
			} else if (cur instanceof Choice && send instanceof Choice) {
				Choice receive=(Choice)cur;
					
				if (((Choice)send).getRole().equals(receive.getRole())) {
					ret = receive;
				}
			} else {
				// TODO: Log error
			}

			return(ret);
		}
	}
}
