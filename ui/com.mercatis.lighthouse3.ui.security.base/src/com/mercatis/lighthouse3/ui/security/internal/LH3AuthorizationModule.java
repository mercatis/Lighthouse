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
package com.mercatis.lighthouse3.ui.security.internal;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.persistence.users.rest.ContextRoleAssignmentRegistryImplementation;
import com.mercatis.lighthouse3.persistence.users.rest.UserRegistryImplementation;
import com.mercatis.lighthouse3.security.SecurityBinding;
import com.mercatis.lighthouse3.security.SecurityBindingBuilder;
import com.mercatis.lighthouse3.security.api.AuthorizationModule;


public class LH3AuthorizationModule implements AuthorizationModule {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.api.AuthorizationModule#authorize(java.lang.String, com.mercatis.lighthouse3.security.SecurityBinding)
	 */
	public SecurityBinding authorize(String context, SecurityBinding binding) {
		String username = binding.getName(context);
		String password = new String(binding.getPassword(context));
		String serverUrl = findServerUrlForContext(context);
		
		UserRegistryImplementation registry = new UserRegistryImplementation(serverUrl, username, password);
		User user = registry.authenticate(username, password);

		SecurityBindingBuilder builder = new SecurityBindingBuilder();
		ContextRoleAssignmentRegistryImplementation permissionRegistry = new ContextRoleAssignmentRegistryImplementation(serverUrl, username, password);
		List<ContextRoleAssignment> permissions = permissionRegistry.findAllFor(user);
		for (ContextRoleAssignment permission : permissions) {
			String ctxTail = permission.getContext();
			String role = permission.getRole();
			builder.role(context + ctxTail, role);
		}
		
		return builder.build();
	}
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.api.AuthorizationModule#deauthorize(com.mercatis.lighthouse3.security.SecurityBinding)
	 */
	public boolean deauthorize(SecurityBinding binding) {
		return true;
	}

	protected String findServerUrlForContext(String context) {
		String serverDomainKey = context.substring(context.lastIndexOf("/") + 1);
		String url = null;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
			if (preferences.get("DOMAIN_SERVER_DOMAIN_KEY", "").equals(serverDomainKey)) {
				url = preferences.get("DOMAIN_URL", "").trim();
				break;
			}
		}

		return url;
	}
}
