/*
 * Copyright 2011 mercatis Technologies AG
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
 */
package com.mercatis.lighthouse3.security.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;



public class ContextRolesSourceProvider extends AbstractSourceProvider {
	
	public static final String CONTEXT_BASED_ROLES_SOURCE_PROVIDER = "com.mercatis.lighthouse3.security.internal.contextRolesSourceProvider";

	public static final String CONTEXT_ROLES = "mercatis.security.roles";
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map<String,Object> getCurrentState() {
		Map<String,Object> state = new HashMap<String,Object>();
		state.put(CONTEXT_ROLES, SecurityPlugin.getDefault().getSecurityBinding());
		
		return state;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {
		return new String[] { CONTEXT_BASED_ROLES_SOURCE_PROVIDER };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {
	}

}
