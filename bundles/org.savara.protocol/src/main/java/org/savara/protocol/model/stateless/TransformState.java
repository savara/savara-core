/*
 * Copyright 2005-8 Pi4 Technologies Ltd
 * Copyright 2012 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * 13 Dec 2008 : Initial version created by gary
 * Feb 2012 : Update based on scribble v2
 */
package org.savara.protocol.model.stateless;

import org.scribble.protocol.model.Activity;
import org.scribble.protocol.model.Block;

/**
 * This class provides the state for the transformation
 * of a stateless model object.
 */
public class TransformState {
	
	private Activity _parent=null;
	private Block _block=null;
	private int _position=0;

	public TransformState(Activity parent,
					Block block, int pos) {
		_parent = parent;
		_block = block;
		_position = pos;
	}

	public Activity getParent() {
		return(_parent);
	}
	
	public Block getBlock() {
		return(_block);
	}
	
	public int getPosition() {
		return(_position);
	}
}
