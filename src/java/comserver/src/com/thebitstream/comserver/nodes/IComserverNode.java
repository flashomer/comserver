/*******************************************************************************
 * Copyright 2009-2013 Andy Shaules
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.thebitstream.comserver.nodes;


import java.util.Map;

import com.thebitstream.comserver.stream.IResourceSink;

/**
 * @author Andy Shaules
 * @version 1.0
 */
public interface IComserverNode {
	
	String getNodeId();
	Map<Object, Object> getNodeData();
	String getNodeType();
	void setNodeId(String id);
	void setNodeData(Map<Object, Object> data);
	void setNodeType(String type);
	void setResource(IResourceSink resource);
	void closingResource(IResourceSink feed);
	void invoke(String method, Map<Object,Object> data);
}
