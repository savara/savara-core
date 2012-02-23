/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
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
package org.savara.bpel.internal.model.change;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.scribble.protocol.util.ActivityUtil;
import org.scribble.protocol.util.RunUtil;
import org.savara.protocol.model.util.InteractionUtil;
import org.scribble.protocol.model.*;

/**
 * This class provides utility functions for detecting
 * patterns related to interactions.
 */
public class InteractionPatterns {
	
	private static final Logger logger=Logger.getLogger(InteractionPatterns.class.getName());

	/**
	 * This method checks whether the supplied interaction
	 * is a request that requires a scope with associated
	 * fault handlers.
	 * 
	 * @param interaction The interaction
	 * @return Whether the interaction is a request requiring
	 * 			an outer fault handler
	 */
	public static boolean isFaultHandlerRequired(org.scribble.protocol.model.Interaction interaction) {
		boolean ret=false;
		
		// Check if interaction is an invoke, and followed
		// by a choice representing a normal and multiple
		// fault responses
		if (InteractionUtil.isRequest(interaction) &&
				InteractionUtil.getRequestLabel(interaction) != null) {
			
			if (interaction.getParent() instanceof
						org.scribble.protocol.model.Block) {
				org.scribble.protocol.model.Block block=
					(org.scribble.protocol.model.Block)interaction.getParent();
				
				int pos=block.getContents().indexOf(interaction);
				
				if (pos != -1 && pos < block.getContents().size()-1) {
					org.scribble.protocol.model.Activity act=
						block.getContents().get(pos+1);
					
					ret = isFaultHandlerChoice(act);
				}
			}
		}
		
		return(ret);
	}
	
	protected static boolean isFaultHandlerChoice(Activity act) {
		boolean ret=false;
		
		if (act instanceof org.scribble.protocol.model.Choice) {
			
			// Need to check if each path receives a
			// response (normal and fault)
			org.scribble.protocol.model.Choice choice=
				(org.scribble.protocol.model.Choice)act;
			
			if (choice.getPaths().size() > 0) {
				ret = true;							
			}
			
			for (int i=0; ret &&
					i < choice.getPaths().size(); i++) {
				
				// Get initial interaction
				Interaction intn=getFirstInteraction(choice.getPaths().get(0));

				if (intn != null) {
					ret = !InteractionUtil.isRequest(intn);
				}							
			}
		} else if (act instanceof org.scribble.protocol.model.Parallel) {
			org.scribble.protocol.model.Parallel par=(org.scribble.protocol.model.Parallel)act;
			
			for (Block b : par.getPaths()) {
				if (b.size() > 0 && isFaultHandlerChoice(b.get(0))) {
					ret = true;
					break;
				}
			}
		}

		return(ret);
	}
	
	// TODO: Method to detect if normal response, which needs
	// to enhance the invoke (and subsequent activities following
	// the invoke), or a fault response needing to
	// place itself and subsequent activities in a fault handler.
	// Methods need to return (and set) the relevant sequence
	// to use for subsequent activities.
	
	public static boolean isResponseAndFaultHandler(Choice choice) {
		boolean ret=choice.getPaths().size() > 0;
		
		// Check if all paths are responses with same reply label
		String label=null;
		
		for (int i=0; ret && i < choice.getPaths().size(); i++) {
			Block path=choice.getPaths().get(i);
			
			// Get initial interaction
			Interaction interaction=getFirstInteraction(path);

			if (interaction != null) {
				if (i == 0) {
					label = InteractionUtil.getReplyToLabel(interaction);
					
					if (label == null || InteractionUtil.isRequest(interaction)
							|| InteractionUtil.isSend(interaction)) {
						ret = false;
					}
				} else {
					String replyTo=InteractionUtil.getReplyToLabel(interaction);
					
					if (replyTo == null ||
							replyTo.equals(label) == false ||
							InteractionUtil.isRequest(interaction) ||
							InteractionUtil.isSend(interaction)) {
						ret = false;
					}
				}
			}
		}		

		return(ret);
	}
	
	/**
	 * This method obtains the interaction that triggers a
	 * specified path in a BPEL pick activity.
	 * 
	 * @param path The path
	 * @return The interaction that triggers the path in
	 * 				the pick activity
	 */
	public static Interaction getPickPathInteraction(Block path) {
		Interaction ret=null;
		
		// TODO: This method is the opposite of the
		// 'isInteractionPickPathTrigger', so needs to be
		// updated to reflect any changes to that method
		
		if (path.getContents().size() > 0) {
			org.scribble.protocol.model.Activity sub=
				path.getContents().get(0);
								
			// Check if scope
			if (sub instanceof Run) {
				//Protocol defn=((Run)sub).getProtocol();
				Protocol defn=RunUtil.getInnerProtocol(((Run)sub).getEnclosingProtocol(),
						((Run)sub).getProtocolReference());
				org.scribble.protocol.model.Activity b=null;
				
				for (int j=0; b == null &&
							j < defn.getBlock().getContents().size(); j++) {
					if (!ActivityUtil.isDeclaration(defn.getBlock().getContents().get(j))) {
						b = (org.scribble.protocol.model.Activity)
									defn.getBlock().getContents().get(j);
					}
				}
				
				if (b != null) {
					sub = b;
				}
			}
			
			if (sub instanceof Interaction) {
				ret = (Interaction)sub;
			}
		}
		
		return(ret);
	}
	
	/**
	 * This method determines whether the supplied interaction
	 * is the trigger interaction within a BPEL pick activity.
	 * 
	 * @param in The interaction
	 * @return Whether the interaction is a pick trigger activity
	 */
	public static boolean isInteractionPickPathTrigger(Interaction in) {
		boolean ret=false;
		
		// TODO: Need to determine all situations and ensure
		// is generic enough to cope with extensions to Scribble
		// model
		
		Block path=(Block)in.getParent();
		
		if (path.getParent() instanceof Choice &&
				InteractionPatterns.isSwitch((Choice)path.getParent())) {
			ret = true;
			
		// Check if receive is directly contained within a
		// sub-definition
		// TODO: If first interaction in a nested protocol, then may also
		// need to check whether being generated as first element in an onMessage
		// with the same interaction details, as checking that the nested protocol
		// has been called inside a choice in the protocol may not be adequate,
		// as the same protocol may have also be called outside of a choice - so
		// the context could be relevant.
		} else if (InteractionUtil.isSend(in) == false &&
				path.getParent() instanceof org.scribble.protocol.model.Protocol &&
				path.getParent().getParent() instanceof org.scribble.protocol.model.Protocol) {
			ret = true;
		}
		
		return(ret);
	}
	
	public static boolean isSwitch(Choice choice) {
		boolean ret=false;
		
		// For a choice to be considered a 'switch', it only needs
		// to be a receiver
		if (choice.getRole() == null || !choice.getRole().equals(choice.getEnclosingProtocol().getLocatedRole())) {
			ret = true;
		}
		
		return(ret);
	}
	
	public static Interaction getRequestForResponseInFaultHandler(Interaction interaction) {
		Interaction ret=null;
		
		if (InteractionUtil.isRequest(interaction) == false &&
				InteractionUtil.getReplyToLabel(interaction) != null) {
			
			// Find if contained in 'if' path
			if (interaction.getParent() instanceof Block &&
					interaction.getParent().getParent() instanceof Choice) {
				
				Choice choice=(Choice)interaction.getParent().getParent();
				
				// Obtain interaction prior to 'If'
				if (choice.getParent() instanceof
						org.scribble.protocol.model.Block) {
					org.scribble.protocol.model.Block block=
						(org.scribble.protocol.model.Block)choice.getParent();
				
					int pos=block.getContents().indexOf(choice);
				
					if (pos != -1 && pos > 0) {
						org.scribble.protocol.model.Activity act=
							block.getContents().get(pos-1);
					
						if (act instanceof Interaction &&
								InteractionUtil.isRequest((Interaction)act) &&
								InteractionUtil.getRequestLabel((Interaction)act) != null &&
								InteractionUtil.getReplyToLabel(interaction).equals(
										InteractionUtil.getRequestLabel((Interaction)act))) {
							ret = (Interaction)act;
						}
						
						// Otherwise check if choice wrapped in parallel construct (possibly
						// due to fork/join support)
					} else if (block.getParent() instanceof Parallel &&
							block.getParent().getParent() instanceof Block) {
						Parallel par=(Parallel)block.getParent();
						block = (org.scribble.protocol.model.Block)block.getParent().getParent();
					
						pos = block.getContents().indexOf(par);
					
						if (pos != -1 && pos > 0) {
							org.scribble.protocol.model.Activity act=
								block.getContents().get(pos-1);
						
							if (act instanceof Interaction &&
									InteractionUtil.isRequest((Interaction)act) &&
									InteractionUtil.getRequestLabel((Interaction)act) != null &&
									InteractionUtil.getReplyToLabel(interaction).equals(
											InteractionUtil.getRequestLabel((Interaction)act))) {
								ret = (Interaction)act;
							}
						}
					}
				}
			}	
		}
		
		return(ret);
	}
	
	public static boolean isResponseInFaultHandler(Interaction interaction) {
		return(getRequestForResponseInFaultHandler(interaction) != null);
	}
		
	public static boolean isSyncNormalResponse(Interaction interaction) {
		boolean ret=false;

		if (InteractionUtil.isResponse(interaction) &&
				interaction.getParent() instanceof Block) {
			Block block=(Block)interaction.getParent();
		
			int pos=block.getContents().indexOf(interaction);
			
			if (pos > 0 && block.getContents().get(pos-1) instanceof Interaction) {
				Interaction req=(Interaction)block.getContents().get(pos-1);
				
				ret = isResponseForRequest(interaction, req);
			}
		}
		
		return(ret);
	}
	
	public static boolean isResponseForRequest(Interaction resp,
							Interaction req) {
		boolean ret=false;
		
		if (InteractionUtil.isRequest(req) &&
				InteractionUtil.isResponse(resp) &&
				InteractionUtil.getReplyToLabel(resp).equals(
						InteractionUtil.getRequestLabel(req))) {
			ret = true;
		}
		
		return(ret);
	}
	
	public static Interaction getFirstInteraction(Block path) {
		Interaction ret=null;
		
		// Get initial interactions
		java.util.List<ModelObject> interactions=
				org.scribble.protocol.util.InteractionUtil.getInitialInteractions(path);
		
		if (interactions.size() != 1) {
			// Either no interations, or more than one. Cannot
			// handle either case.
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Path has "+interactions.size()+" interactions");
			}
			
		} else if ((interactions.get(0) instanceof Interaction) == false) {
			
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Path 'interaction' is not of type Interaction");
			}
		} else {
			ret=(Interaction)interactions.get(0);
		}
		
		return(ret);
	}
}
