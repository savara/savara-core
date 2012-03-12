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

import java.util.logging.Logger;

import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.protocol.aggregator.ProtocolAggregator;
import org.savara.protocol.internal.aggregator.LocalProtocolUnit.ActivityCursor;
import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;
import org.scribble.protocol.model.Choice;
import org.scribble.protocol.model.DefaultVisitor;
import org.scribble.protocol.model.ImportList;
import org.scribble.protocol.model.Interaction;
import org.scribble.protocol.model.Introduces;
import org.scribble.protocol.model.ParameterDefinition;
import org.scribble.protocol.model.Protocol;
import org.scribble.protocol.model.ProtocolImportList;
import org.scribble.protocol.model.ProtocolModel;
import org.scribble.protocol.model.Repeat;
import org.scribble.protocol.model.Role;
import org.scribble.protocol.model.TypeImport;
import org.scribble.protocol.model.TypeImportList;
import org.scribble.protocol.util.RoleUtil;

/**
 * This is the default implementation of the protocol aggregator
 * implementation.
 *
 */
public class ProtocolAggregatorImpl implements ProtocolAggregator {

	private static final Logger LOG=Logger.getLogger(ProtocolAggregatorImpl.class.getName());
	
	/**
	 * {@inheritDoc}
	 */
	public ProtocolModel aggregateGlobalModel(java.util.Collection<ProtocolModel> locals,
							FeedbackHandler handler) {
		ProtocolModel ret=new ProtocolModel();
		
		Protocol protocol=new Protocol();		
		ret.setProtocol(protocol);
		
		// Merge imports
		mergeImports(ret, locals, handler);
		
		// Check all local protocols have same name, and different roles
		java.util.Map<Role, LocalProtocolUnit> protocolUnits=
						new java.util.HashMap<Role, LocalProtocolUnit>();
		
		GlobalProtocolUnit gpu=new GlobalProtocolUnit(ret);
		
		for (ProtocolModel local : locals) {
			if (protocol.getName() == null) {
				protocol.setName(local.getProtocol().getName());
			} else if (!protocol.getName().equals(local.getProtocol().getName())) {
				throw new IllegalArgumentException("All local models must have the same protocol name");
			}
			
			if (local.getProtocol().getLocatedRole() == null) {
				throw new IllegalArgumentException("Located role not defined");
			}
			
			if (protocolUnits.containsKey(local.getProtocol().getLocatedRole())) {
				throw new IllegalArgumentException("Local model for role '"+
						local.getProtocol().getLocatedRole()+"' already exists");
			}
			
			LocalProtocolUnit lpu=new LocalProtocolUnit(local);
			
			protocolUnits.put(local.getProtocol().getLocatedRole(), lpu);
		}
		
		while (processProtocolUnits(gpu, protocolUnits, handler));
		
		return(ret);
	}
	
	protected Activity createActivity(Activity act) {
		Activity ret=null;
		
		if (act instanceof Introduces) {
			ret = new Introduces((Introduces)act);
		}
		
		return(ret);
	}
	
	protected boolean processProtocolUnits(GlobalProtocolUnit gpu,
				java.util.Map<Role, LocalProtocolUnit> lpus, FeedbackHandler handler) {
		boolean ret=false;
		
		// Find sending protocol unit
		for (LocalProtocolUnit lpu : lpus.values()) {
			ActivityCursor cursor = lpu.getSendingCursor();

			if (cursor != null) {
				Activity send=cursor.peek();
				
				LocalProtocolUnit receiver=getReceiver(send, lpus);
				
				ActivityCursor receiveCursor=null;
				
				if (receiver != null) {
					receiveCursor = receiver.findReceivingCursor(send);
				} else {
					LOG.warning("Couldn't find receive for sending action '"+send+"'");
				}
				
				if (receiveCursor != null) {
					Activity receive=receiveCursor.getReceiveAction(send);
					
					if (receive != null) {
						gpu.process(send, cursor, receive, receiveCursor);
						
						ret = true;
						
						receiveCursor.next();
						cursor.next();
					}
				}
			}
			
			if (ret) {
				break;
			}
		}
		
		// If no send/receive match, then check for individual actions that may
		// help make progress through the local projections
		if (!ret) {
			
			// Check for endpoint specific actions that can be transferred
			for (LocalProtocolUnit lpu : lpus.values()) {
				ActivityCursor individualCursor=null;
				
				do {
					individualCursor=lpu.getIndividualCursor();
					
					if (individualCursor != null) {
						Activity individual=individualCursor.peek();
						
						gpu.process(individual, individualCursor);
						
						individualCursor.next();
						
						ret = true;
					}
				} while (individualCursor != null);
			}
		}
		
		return(ret);
	}
	
	protected LocalProtocolUnit getReceiver(Activity act,
						java.util.Map<Role, LocalProtocolUnit> lpus) {
		if (act instanceof Interaction) {
			if (((Interaction)act).getToRoles().size() > 0) {
				return(lpus.get(((Interaction)act).getToRoles().get(0)));
			}
		} else if (act instanceof Choice) {
			
			// Need to find a role, that is not the choice decision maker,
			// that has the choice associated with the decision maker
			Role decisionMaker=((Choice)act).getRole();
			
			for (Role role : lpus.keySet()) {
				if (!role.equals(decisionMaker)) {
					LocalProtocolUnit lpu=lpus.get(role);
					
					ActivityCursor cursor=lpu.findReceivingCursor(act);
					
					if (cursor != null) {
						return(lpu);
					}
				}
			}
		}
		
		return(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ProtocolModel aggregateLocalModel(String protocolName,
				java.util.Collection<ProtocolModel> locals, FeedbackHandler handler) {
		ProtocolModel ret=new ProtocolModel();
		
		Protocol protocol=new Protocol();
		protocol.setName(protocolName);
		
		ret.setProtocol(protocol);
		
		// Merge imports
		mergeImports(ret, locals, handler);
		
		// Verify all local models are associated with the same role
		Role role=null;
		String introducingRole=null;
		
		for (ProtocolModel lm : locals) {
			if (role == null) {
				role = lm.getProtocol().getLocatedRole();
			} else if (!role.equals(lm.getProtocol().getLocatedRole())) {
				throw new IllegalArgumentException("Local models must all be associated with the same located role");
			}
			
			if (introducingRole == null) {
				if (lm.getProtocol().getParameterDefinitions().size() > 0) {
					introducingRole = lm.getProtocol().getParameterDefinitions().get(0).getName();
				}
			} else if (lm.getProtocol().getParameterDefinitions().size() > 0) {
				if (!introducingRole.equals(lm.getProtocol().getParameterDefinitions().get(0).getName())) {
					throw new IllegalArgumentException("Local models must all be associated with the same role parameter");
				}
			}
		}
		
		protocol.setLocatedRole(new Role(role));
		
		if (introducingRole != null) {
			ParameterDefinition pd=new ParameterDefinition();
			pd.setName(introducingRole);
			protocol.getParameterDefinitions().add(pd);
		}
		
		Block b=new Block();
		protocol.setBlock(b);
		
		// Build list of external roles being interacted with
		java.util.List<Role> introducedRoles=new java.util.Vector<Role>();
		
		java.util.List<Block> sourcePaths=new java.util.Vector<Block>();
		
		for (ProtocolModel lm : locals) {
			Block lb=lm.getProtocol().getBlock();
			
			if (lb.size() > 0 &&
					lb.get(0) instanceof Introduces) {
				Introduces i=(Introduces)lb.get(0);
				for (Role r : i.getIntroducedRoles()) {
					if (!introducedRoles.contains(r)) {
						introducedRoles.add(new Role(r));
					}
				}
				
				// Remove introduces
				lb.getContents().remove(0);
			}
			
			sourcePaths.add(lb);
		}
		
		// Merge paths from individual local models into single local model
		mergePaths(sourcePaths, b, handler);
		
		// Establish 'introduces' clauses in the correct scopes
		for (Role ir : introducedRoles) {
			
			// Find enclosing block associated with the roles
			Block block=RoleUtil.getEnclosingBlock(protocol, ir, false);
			
			Introduces intro=null;
			if (block.size() > 0 && block.get(0) instanceof Introduces) {
				intro = (Introduces)block.get(0);
			} else {
				intro = new Introduces();
				intro.setIntroducer(new Role(role));
				block.getContents().add(0, intro);
			}
			
			intro.getIntroducedRoles().add(ir);
		}
		
		return(ret);
	}
	
	protected void mergeImports(ProtocolModel aggregated, java.util.Collection<ProtocolModel> locals,
							FeedbackHandler handler) {
		java.util.List<String> typeNames=new java.util.Vector<String>();
		
		for (ProtocolModel local : locals) {
			for (ImportList implist : local.getImports()) {
				
				if (implist instanceof TypeImportList) {
					TypeImportList til=(TypeImportList)implist;
					
					for (TypeImport ti : til.getTypeImports()) {
						if (!typeNames.contains(ti.getName())) {
							typeNames.add(ti.getName());
							
							TypeImport copy=new TypeImport(ti);
							TypeImportList til2=new TypeImportList();
							til2.setFormat(til.getFormat());
							til2.setLocation(til.getLocation());
							til2.getTypeImports().add(copy);
							
							aggregated.getImports().add(til2);
						}
					}
				} else if (implist instanceof ProtocolImportList) {
					// TODO:
				}
			}
		}
	}
	
	protected void mergePaths(java.util.List<Block> sourcePaths, Block targetPath,
							FeedbackHandler handler) {
		if (sourcePaths.size() == 0) {
			return;
		} else if (sourcePaths.size() == 1) {
			targetPath.getContents().addAll(sourcePaths.get(0).getContents());
			return;
		}
		
		while (transferCommonComponent(targetPath, targetPath.size(), sourcePaths, 0,
							handler));
		
		if (sourcePaths.size() > 0) {
			Choice choice=new Choice();
			
			targetPath.add(choice);
			
			int pos=targetPath.indexOf(choice);
			
			while (transferCommonComponent(targetPath, pos+1, sourcePaths, -1,
					handler));
			
			// Group into paths with common first interaction
			boolean optional=false;
			boolean content=false;
			while (sourcePaths.size() > 0) {
				Block path=sourcePaths.get(0);
				if (path.size() == 0) {
					optional = true;
				} else {
					content = true;
					Object component=path.get(0);
					
					java.util.List<Block> sps=new java.util.Vector<Block>();
					sps.add(path);
					
					// Check if other paths have the same initial component
					for (int i=1; i < sourcePaths.size(); i++) {
						Block path2=sourcePaths.get(i);
						
						if (path2.size() > 0 && path2.get(0).equals(component)) {
							sps.add(path2);
							sourcePaths.remove(i);
							i--; // Decrement due to removed element
						}
					}
					
					if (sps.size() == 1) {
						choice.getPaths().add(sps.get(0));
					} else {
						// Merge paths
						Block tp=new Block();
						choice.getPaths().add(tp);
						mergePaths(sps, tp, handler);
					}
				}
				sourcePaths.remove(0);
			}
			
			// If no content, then remove the choice construct
			if (!content) {
				targetPath.remove(choice);
			} else {
				if (optional) {
					// Add empty path
					choice.getPaths().add(new Block());
				}
				
				// Check for located role
				Role role=null;
				
				for (Block b : choice.getPaths()) {
					if (b.size() > 0 && b.get(0) instanceof Interaction) {
						Interaction in=(Interaction)b.get(0);
						
						if (in.getFromRole() == null) {
							role = in.getEnclosingProtocol().getLocatedRole();
							break;
						} else {
							role = in.getFromRole();
							break;
						}
					}
				}
				
				choice.setRole(new Role(role));
			}
		}
	}
	
	protected void postProcessMerged(Block targetPath, FeedbackHandler handler) {
		
		targetPath.visit(new DefaultVisitor() {
			public boolean start(Choice choice) {
				checkForRepetition(choice);
				return(true);
			}
		});
	}
	
	protected void checkForRepetition(Choice choice) {
		
		// Check if choice has two paths, one optional, and the other only
		// containing interations (i.e. no sub-choices)
		if (choice.getPaths().size() == 2) {
			Block b=null;
			
			if (choice.getPaths().get(0).size() == 0) {
				b = choice.getPaths().get(1);
			} else if (choice.getPaths().get(1).size() == 0) {
				b = choice.getPaths().get(0);
			}
			
			if (b != null && b.size() > 0) {
				Interaction in=(Interaction)b.get(0);
				boolean candidate=true;
				
				for (int i=1; i < b.size(); i++) {
					if (b.get(i) instanceof Choice) {
						candidate = false;
						break;
					}
				}
				
				if (candidate) {
					java.util.List<Block> sourcePaths=
								new java.util.Vector<Block>();
					
					sourcePaths.add(b);
					
					// Work up hierarchy accumulating relevant
					// paths and identifying root choice to be
					// replaced by repeat.
					Choice root=checkChoiceParent(in, choice, sourcePaths);
					
					Block merged=new Block();
					mergePaths(sourcePaths, merged, new DefaultFeedbackHandler());
					
					Repeat repeat=new Repeat();
					repeat.setBlock(merged);
					
					Block parent=(Block)root.getParent();
					int pos=parent.indexOf(root);
					
					parent.getContents().remove(pos);
					parent.getContents().add(pos, repeat);
					
					// Check for located role
					if (merged.size() > 0 && merged.get(0) instanceof Interaction) {
						Role role=null;
						Interaction in2=(Interaction)merged.get(0);
							
						if (in2.getFromRole() == null) {
							role = in2.getEnclosingProtocol().getLocatedRole();
						} else {
							role = in2.getFromRole();
						}
						if (role != null) {							
							repeat.getRoles().add(new Role(role));
						}
					}
				}
			}
		}
	}
	
	protected Choice checkChoiceParent(Interaction in, Choice choice,
							java.util.List<Block> sourcePaths) {
		Choice ret=choice;
		
		if (choice.getParent() instanceof Block
					&& choice.getParent().getParent() instanceof Choice) {
			Block b=(Block)choice.getParent();
			Choice parent=(Choice)choice.getParent().getParent();
			
			// Check if parent choice has two paths and
			// parent choice has an optional path and
			// supplied choice is last element in block
			// and block starts with the same interaction
			if (parent.getPaths().size() == 2 &&
					(parent.getPaths().get(0).size() == 0 ||
					parent.getPaths().get(1).size() == 0) &&
					b.indexOf(choice) == b.size()-1 &&
								b.get(0).equals(in)) {
				
				Block copy=new Block();
				for (int i=0; i < b.size()-1; i++) {
					copy.add(new Interaction((Interaction)b.get(i)));
				}
				
				sourcePaths.add(copy);
				
				ret = checkChoiceParent(in, parent, sourcePaths);
			}
		}
		
		return(ret);
	}
		
	protected boolean transferCommonComponent(Block targetPath, int targetPos,
					java.util.List<Block> sourcePaths, int sourcePos,
							FeedbackHandler handler) {
		boolean ret=false;
		
		if (sourcePaths.size() > 0 && sourcePaths.get(0).size() > 0) {
			Interaction component=null;
			
			if (sourcePos == -1) {
				// Get last element
				component = (Interaction)sourcePaths.get(0).get(sourcePaths.get(0).size()-1);
			} else {
				// Get component at source position
				component = (Interaction)sourcePaths.get(0).get(sourcePos);
			}
			
			for (int i=1; i < sourcePaths.size(); i++) {
				Interaction ref=null;
				
				if (sourcePaths.get(i).size() > 0) {
					if (sourcePos == -1) {
						// Get last element
						ref = (Interaction)sourcePaths.get(i).get(sourcePaths.get(i).size()-1);
					} else {
						// Get ref component at source position
						ref = (Interaction)sourcePaths.get(i).get(sourcePos);
					}
					
					ret = component.equals(ref);
					
					if (!ret) {
						break;
					}
				} else {
					ret = false;
					break;
				}
			}
			
			if (ret) {
				Interaction in=new Interaction(component);
				targetPath.getContents().add(targetPos, in);
				
				// Remove common components
				for (int i=sourcePaths.size()-1; i >= 0; i--) {
					if (sourcePos == -1) {
						sourcePaths.get(i).getContents().remove(sourcePaths.get(i).size()-1);
					} else {
						sourcePaths.get(i).getContents().remove(sourcePos);
					}
				}
			}
		}
		
		return(ret);
	}
}
