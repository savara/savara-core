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
package org.savara.bpel.model.change;

import org.scribble.protocol.util.ActivityUtil;
import org.savara.protocol.model.util.InteractionUtil;
import org.scribble.protocol.model.*;

/**
 * This class provides utility functions for detecting
 * patterns related to interactions.
 */
public class InteractionPatterns {

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
					
					if (act instanceof org.scribble.protocol.model.Choice) {
						
						// Need to check if each path receives a
						// response (normal and fault)
						org.scribble.protocol.model.Choice choice=
							(org.scribble.protocol.model.Choice)act;
						
						if (choice.getWhens().size() > 0) {
							ret = true;							
						}
						
						for (int i=0; ret &&
								i < choice.getWhens().size(); i++) {
							
							ret = !InteractionUtil.isRequest(choice.getWhens().get(i));
							/*
							org.scribble.protocol.model.Block path=
								choice.getPaths().get(i);
							
							if (path.getContents().size() == 0 ||
									(path.getContents().get(0) instanceof Interaction) == false ||
									InteractionUtil.isRequest((Interaction)
											path.getContents().get(0))) {
								ret = false;
							}
							*/
						}
					}
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
		boolean ret=choice.getWhens().size() > 0;
		
		// Check if all paths are responses with same reply label
		String label=null;
		
		for (int i=0; ret && i < choice.getWhens().size(); i++) {
			When path=choice.getWhens().get(i);
			
			if (i == 0) {
				label = InteractionUtil.getReplyToLabel(path);
				
				if (label == null || InteractionUtil.isRequest(path)
						|| InteractionUtil.isSend(path)) {
					ret = false;
				}
			} else {
				String replyTo=InteractionUtil.getReplyToLabel(path);
				
				if (replyTo == null ||
						replyTo.equals(label) == false ||
						InteractionUtil.isRequest(path) ||
						InteractionUtil.isSend(path)) {
					ret = false;
				}
			}
		}
		
		/*
		
		// Obtain interaction prior to 'If'
		if (choice.getPaths().size() > 0 &&
				choice.getParent() instanceof
				org.scribble.protocol.model.Block) {
			org.scribble.protocol.model.Block block=
				(org.scribble.protocol.model.Block)choice.getParent();
		
			int pos=block.getContents().indexOf(choice);
		
			if (pos != -1 && pos > 0) {
				org.scribble.protocol.model.Activity act=
					block.getContents().get(pos-1);
			
				if (act instanceof Interaction &&
						InteractionUtil.isSend((Interaction)act) &&
						InteractionUtil.isRequest((Interaction)act) &&
						getRequestLabel((Interaction)act) != null) {
					
					// Check if each path has a response/fault associated
					// with the preceding request
					String requestLabel=getRequestLabel((Interaction)act);
					
					java.util.List<When> paths=choice.getWhens();
					int matched=0;
					
					for (int i=0; i < paths.size(); i++) {
						When when=paths.get(i);
						
						if (InteractionUtil.isResponse(when) &&
								getReplyToLabel(when).equals(requestLabel)) {
							matched++;
						}
					}
					
					if (matched == paths.size()) {
						ret = true;
					}
				}
			}
		}
		*/

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
				Protocol defn=((Run)sub).getProtocol();
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
		} else if (InteractionUtil.isSend(in) == false &&
				path.getParent() instanceof org.scribble.protocol.model.Protocol &&
				path.getParent().getParent() instanceof Block &&
				path.getParent().getParent().getParent() instanceof 
							org.scribble.protocol.model.Protocol) {
			ret = true;
		}
		
		return(ret);
	}
	
	public static boolean isSwitch(Choice choice) {
		boolean ret=false;
		
		// For a choice to be considered a 'switch', it only needs
		// to be a receiver
		if (choice.getFromRole() != null && choice.getToRole() == null) {
			ret = true;
		}
		
		/*
		if (choice.getPaths().size() > 0) {
			java.util.List<When> paths=choice.getWhens();
			int matched=0;
			
			for (int i=0; i < paths.size(); i++) {
				if (paths.get(i).getContents().size() > 0) {
					Interaction in=getPickPathInteraction(paths.get(i));
					
					if (in != null &&
							InteractionUtil.isSend(in) == false) {
						matched++;
					}
				}
			}
			
			if (matched == paths.size()) {
				ret = true;
			}
		}
		*/

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
					}
				}
			}	
		}
		
		return(ret);
	}
	
	public static boolean isResponseInFaultHandler(Interaction interaction) {
		return(getRequestForResponseInFaultHandler(interaction) != null);
	}
	
	/*
	public static String getMessageTypeLocalPart(Interaction interaction) {
		return((String)interaction.
			getMessageSignature().getProperties().get(MESSAGE_TYPE_LOCALPART));
	}
	
	public static String getMessageTypeNameSpace(Interaction interaction) {
		return((String)interaction.
			getMessageSignature().getProperties().get(MESSAGE_TYPE_NAMESPACE));
	}
	*/
	
	/*
	public static String getVariableName(Interaction interaction) {
		String varName=getMessageTypeLocalPart(interaction);
		
		if (varName != null) {
			int ind=varName.lastIndexOf('}');
		
			if (ind != -1) {
				varName = varName.substring(ind+1);
			}
			
			varName += "Var";
			
			if (Character.isLowerCase(varName.charAt(0)) == false) {
				varName = Character.toLowerCase(varName.charAt(0))+
							varName.substring(1);
			}
		}
		
		return(varName);
	}
	*/
	
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
}
