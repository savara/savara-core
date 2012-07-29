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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.savara.common.logging.DefaultFeedbackHandler;
import org.savara.common.logging.FeedbackHandler;
import org.savara.common.model.annotation.AnnotationDefinitions;
import org.savara.protocol.aggregator.ProtocolAggregator;
import org.savara.protocol.internal.aggregator.LocalProtocolUnit.ActivityCursor;
import org.savara.protocol.model.util.RepeatUtil;
import org.scribble.common.model.Annotation;
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
	public ProtocolModel aggregateGlobalModel(String protocolName,
				java.util.Collection<ProtocolModel> locals,
							FeedbackHandler handler) {
		ProtocolModel ret=new ProtocolModel();
		
		Protocol protocol=new Protocol();
		protocol.setName(protocolName);
		
		ret.setProtocol(protocol);
		
		// Define protocol namespace
		org.savara.common.model.annotation.Annotation protocolAnn=
					new org.savara.common.model.annotation.Annotation(AnnotationDefinitions.PROTOCOL);
		protocolAnn.getProperties().put(AnnotationDefinitions.NAMESPACE_PROPERTY, "http://namespace");
		
		protocol.getAnnotations().add(protocolAnn);
		
		// Merge imports
		mergeImports(ret, locals, handler);
		
		// Check all local protocols have same name, and different roles
		java.util.Map<Role, LocalProtocolUnit> protocolUnits=
						new java.util.HashMap<Role, LocalProtocolUnit>();
		
		GlobalProtocolUnit gpu=new GlobalProtocolUnit(ret);
		
		for (ProtocolModel local : locals) {
			
			// Merge annotations
			mergeAnnotations(ret.getProtocol().getAnnotations(),
					local.getProtocol().getAnnotations(), handler);
			
			if (protocol.getName() == null) {
				protocol.setName(local.getProtocol().getName());
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
		
		// Post process global model
		postProcessGlobal(ret);
		
		// TODO: Should check for incomplete local protocols?
		
		return(ret);
	}
	
	/**
	 * This method post-processes a global model to complete the
	 * description.
	 * 
	 * @param global The global model
	 */
	protected void postProcessGlobal(ProtocolModel global) {
		
		global.visit(new DefaultVisitor() {
			
		    public void end(Repeat elem) {
		    	if (elem.getRoles().size() == 0) {
		    		// Find decision maker
		    		Role decisionMaker=RepeatUtil.getDecisionMaker(elem);
		    		
		    		if (decisionMaker != null) {
		    			elem.getRoles().add(decisionMaker);
		    			
		    		} else if (LOG.isLoggable(Level.FINE)) {
		    			LOG.fine("Could not find decision maker for repeat: "+elem);
		    		}
		    	}
		    }
		});
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
		
		// Check for repetition
		
		for (Role r : lpus.keySet()) {
			java.util.List<Role> roles=new java.util.LinkedList<Role>();
			java.util.List<Role> processed=new java.util.LinkedList<Role>();
			java.util.Map<Role, ActivityCursor> cursors=new java.util.HashMap<Role, ActivityCursor>();

			roles.add(r);
			
			for (int i=0; i < roles.size(); i++) {
				Role rr=roles.get(i);

				LocalProtocolUnit lpu=lpus.get(rr);
				ActivityCursor cursor=lpu.getRepetitionCursor();
				
				if (cursor != null) {
					cursors.put(rr, cursor);
					
					for (Role deprole : cursor.getRepetitionRoles()) {
						if (!roles.contains(deprole)) {
							roles.add(deprole);
						}
					}
					
					processed.add(rr);
				} else {
					break;
				}
			}
			
			// Repetition is detected if roles contains more than the
			// original role. All relevant roles will be synchronized
			// if the number of roles is equal to the number processed.
			// Otherwise ignore for now.
			if (roles.size() > 1 && roles.size() == processed.size()) {
				// Generate global repetition and process 
				gpu.processRepetition(cursors.values());
				
				for (ActivityCursor cursor : cursors.values()) {
					cursor.next();
				}
				
				ret = true;
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
			/*
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
			*/
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
			
			// Merge annotations
			mergeAnnotations(ret.getProtocol().getAnnotations(),
					lm.getProtocol().getAnnotations(), handler);
			
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
		
		postProcessMerged(b, handler);
		
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
	
	protected void mergeAnnotations(java.util.List<Annotation> main,
			java.util.List<Annotation> source, FeedbackHandler handler) {
		int ansCount=0;
		
		for (Annotation ann : source) {
			if (ann instanceof org.savara.common.model.annotation.Annotation) {
				if (!main.contains(ann)) {
					
					// If annotation is a Type, then need to check if namespace
					// has already been declare
					if (!((org.savara.common.model.annotation.Annotation) ann).getName().equals(
							AnnotationDefinitions.TYPE) ||
							AnnotationDefinitions.getAnnotationWithProperty(
									main,
							AnnotationDefinitions.TYPE, AnnotationDefinitions.NAMESPACE_PROPERTY,
								((org.savara.common.model.annotation.Annotation) ann).getProperties().
									get( AnnotationDefinitions.NAMESPACE_PROPERTY)) == null) {
						
						org.savara.common.model.annotation.Annotation newAnn=
								new org.savara.common.model.annotation.Annotation(
								(org.savara.common.model.annotation.Annotation)ann);
						
						// Check if prefix is duplicated
						if (AnnotationDefinitions.getAnnotationWithProperty(main,
							AnnotationDefinitions.TYPE, AnnotationDefinitions.PREFIX_PROPERTY,
								((org.savara.common.model.annotation.Annotation) ann).getProperties().
									get( AnnotationDefinitions.PREFIX_PROPERTY)) != null) {
							newAnn.getProperties().put(AnnotationDefinitions.PREFIX_PROPERTY,
									"ans"+(ansCount++));
						}
						
						main.add(newAnn);
					}
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
	
	/**
	 * This method recursively determines whether the choice is an
	 * appropriate structure to represent a repetition. A valid
	 * structure has at most one child path containing a choice,
	 * and the choice must be preceded by activities which represent
	 * the repeated activities. The choice may be optionally
	 * followed by activities that would follow the repetition.
	 * 
	 * @param choice The choice
	 * @return Whether the structure is valid for a repetition
	 */
	protected boolean isChoiceValidForRepetition(Choice choice) {
		boolean ret=true;
		
		Choice child=null;
		
		for (Block path : choice.getPaths()) {
		
			for (int i=0; i < path.getContents().size(); i++) {
				
				if (path.getContents().get(i) instanceof Choice) {
					if (child != null) {
						ret = false;
					} else {
						child = (Choice)path.getContents().get(i);
						
						if (i == 0) {
							ret = false; // Other activities should preceed the choice
							
						} else if (!choice.getRole().equals(child.getRole())) {
							ret = false; // Needs to be same decision maker
						}
					}
				}
				
				if (!ret) {
					break;
				}
			}
			
			if (!ret) {
				break;
			}
		}
		
		if (ret && child == null) {
			ret = false;
		}
		
		return(ret);
	}
	
	protected void checkForRepetition(Choice choice) {
		
		// Check if top level choice and has a valid structure to
		// represent a repetition
		if ((choice.getParent() == null || choice.getParent().getParent() == null ||
				!(choice.getParent().getParent() instanceof Choice)) &&
				isChoiceValidForRepetition(choice)) {
			final java.util.List<Block> repeated=new java.util.Vector<Block>();
			final java.util.List<Block> nonrepeated=new java.util.Vector<Block>();
			
			choice.visit(new DefaultVisitor() {
				
				public boolean start(Block block) {
					boolean ret=false;
					
					// Check if contains a choice
					for (Activity act : block.getContents()) {
						if (act instanceof Choice) {
							ret = true;
							break;
						}
					}
					
					if (ret) {
						// Separate the activities before and after the choice
						// into repeating and non-repeating blocks
						Block b=new Block();
						
						for (Activity act : block.getContents()) {
							if (act instanceof Choice) {
								repeated.add(b);
								b = new Block();
							} else {
								b.add(act);
							}
						}
						
						if (b.size() > 0) {
							nonrepeated.add(b);
						}
						
					} else {
						// Check if choice is a terminating repetition
						// condition, where one path is the repeating activities
						// and the other is empty
						Choice parent=(Choice)block.getParent();
						
						if (parent.getPaths().size() == 2 &&
								(parent.getPaths().get(0).size() == 0 ||
								parent.getPaths().get(1).size() == 0)) {
							// Repeating block - but only the one with contents
							// is relevant
							if (block.size() > 0) {
								repeated.add(block);
							}
						} else {
							// Non repeating block
							nonrepeated.add(block);							
						}
					}
					
					return(ret);
				}
			});

			//Role locatedRole=choice.getEnclosingProtocol().getLocatedRole();
			
			Block parent=(Block)choice.getParent();
			int pos=parent.indexOf(choice);
			
			Repeat repeat=new Repeat();
			Block merged=new Block();

			repeat.setBlock(merged);
			
			parent.getContents().add(pos+1, repeat);

			mergePaths(repeated, merged, new DefaultFeedbackHandler());
			
			parent.getContents().remove(pos);
			
			// Get decision role
    		Role decisionMaker=RepeatUtil.getDecisionMaker(repeat);
    		
    		if (decisionMaker != null) {
    			repeat.getRoles().add(decisionMaker);
    		} else {
    			decisionMaker = repeat.getEnclosingProtocol().getLocatedRole();
    			
    			if (decisionMaker != null) {
    				repeat.getRoles().add(decisionMaker);
    			}
    		}
			
			Repeat repeat2=new Repeat();
			Block merged2=new Block();

			repeat2.setBlock(merged2);
			
			// Need to create a temporary protocol to provide the located role information
			// to the merging algorithm
			/*
			Protocol p=new Protocol();
			p.setLocatedRole(locatedRole);
			
			Choice c2=new Choice();
			c2.getPaths().addAll(nonrepeated);				

			p.getBlock().add(c2);
			*/
			parent.getContents().add(pos+1, repeat2);	

			mergePaths(nonrepeated, merged2, new DefaultFeedbackHandler());
			
			parent.getContents().remove(pos+1);
			parent.getContents().addAll(pos+1, merged2.getContents());	
		}
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
