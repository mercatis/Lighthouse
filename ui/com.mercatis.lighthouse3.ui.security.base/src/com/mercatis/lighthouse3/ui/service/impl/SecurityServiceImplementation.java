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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignmentRegistry;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.users.rest.ContextRoleAssignmentRegistryImplementation;
import com.mercatis.lighthouse3.persistence.users.rest.GroupRegistryImplementation;
import com.mercatis.lighthouse3.persistence.users.rest.UserRegistryImplementation;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;
import com.mercatis.lighthouse3.ui.security.internal.Security;
import com.mercatis.lighthouse3.ui.security.internal.SecurityConfiguration;
import com.mercatis.lighthouse3.ui.service.SecurityModelChangedListener;
import com.mercatis.lighthouse3.ui.service.SecurityService;


public class SecurityServiceImplementation implements SecurityService {

	private Map<IProject, ContextRoleAssignmentRegistry> contextRoleAssignmentRegistries = new HashMap<IProject, ContextRoleAssignmentRegistry>();
	private Map<IProject, UserRegistry> userRegistries = new HashMap<IProject, UserRegistry>();
	private Map<IProject, GroupRegistry> groupRegistries = new HashMap<IProject, GroupRegistry>();
	private List<SecurityModelChangedListener> modelChangedListeners = new LinkedList<SecurityModelChangedListener>();

	private String getServerDomainKey(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
		return preferences.get("DOMAIN_SERVER_DOMAIN_KEY", "").trim();
	}
	
	private ContextRoleAssignmentRegistry getContextRoleAssignmentRegistry(IProject iProject) {
		ContextRoleAssignmentRegistry registry = this.contextRoleAssignmentRegistries.get(iProject);
		if (registry == null) {
			SecurityConfiguration config = Security.getDefault().getSecurityConfiguration(iProject);
			String serverDomainKey = getServerDomainKey(iProject);
			String context = new StringBuilder("//LH3/").append(serverDomainKey).toString();
			String user = com.mercatis.lighthouse3.security.Security.getLoginName(context);
			String password = new String(com.mercatis.lighthouse3.security.Security.getLoginPassword(context));
			registry = new ContextRoleAssignmentRegistryImplementation(config.getServerUrl(), user, password);
			contextRoleAssignmentRegistries.put(iProject, registry);
		}
		return registry;
	}
	
	private UserRegistry getUserRegistry(IProject iProject) {
		UserRegistry userRegistry = userRegistries.get(iProject);
		if (userRegistry == null) {
			SecurityConfiguration config = Security.getDefault().getSecurityConfiguration(iProject);
			String serverDomainKey = getServerDomainKey(iProject);
			String context = new StringBuilder("//LH3/").append(serverDomainKey).toString();
			String user = com.mercatis.lighthouse3.security.Security.getLoginName(context);
			String password = new String(com.mercatis.lighthouse3.security.Security.getLoginPassword(context));
			userRegistry = new UserRegistryImplementation(config.getServerUrl(), user, password);
			userRegistries.put(iProject, userRegistry);
		}
		return userRegistry;
	}
	
	private GroupRegistry getGroupRegistry(IProject iProject) {
		GroupRegistry groupRegistry = groupRegistries.get(iProject);
		if (groupRegistry == null) {
			SecurityConfiguration config = Security.getDefault().getSecurityConfiguration(iProject);
			String serverDomainKey = getServerDomainKey(iProject);
			String context = new StringBuilder("//LH3/").append(serverDomainKey).toString();
			String user = com.mercatis.lighthouse3.security.Security.getLoginName(context);
			String password = new String(com.mercatis.lighthouse3.security.Security.getLoginPassword(context));
			groupRegistry = new GroupRegistryImplementation(config.getServerUrl(), (UserRegistryImplementation) getUserRegistry(iProject), user, password);
			groupRegistries.put(iProject, groupRegistry);
		}
		return groupRegistry;
	}
	
	public Set<User> findUsersWithContext(IProject iProject, String context) {
		List<ContextRoleAssignment> assignments = this.getContextRoleAssignmentRegistry(iProject).findFor(context);
		Set<User> users = new HashSet<User>();
		for (ContextRoleAssignment assignment : assignments) {
			if (assignment.getUserCode() != null) {
				UserRegistry userRegistry = getUserRegistry(iProject);
				users.add(userRegistry.findByCode(assignment.getUserCode()));
			}
		}
		return users;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findAllUsers()
	 */
	public List<User> findAllUsers(IProject iProject) {
		List<User> users = new LinkedList<User>();
		for (String userCode : getUserRegistry(iProject).findAllUserCodes()) {
			User user = getUserRegistry(iProject).findByCode(userCode);
			if (user != null)
				users.add(user);
		}
		return users;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findAllContextsForUser(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User)
	 */
	public List<String> findContextsForUser(IProject iProject, User user) {
		List<String> contexts = new LinkedList<String>();
		for (ContextRoleAssignment assignment : getContextRoleAssignmentRegistry(iProject).findFor(user)) {
			contexts.add(assignment.getContext());
		}
		return contexts;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findAssignmentsForUserAndContext(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User, java.lang.String)
	 */
	public List<ContextRoleAssignment> findAssignmentsForUserAndContext(IProject iProject, User user, String context) {
		return getContextRoleAssignmentRegistry(iProject).findFor(context, user);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findAllGroups(org.eclipse.core.resources.IProject)
	 */
	public List<Group> findAllGroups(IProject iProject) {
		List<Group> groups = new LinkedList<Group>();
		for (String groupCode : getGroupRegistry(iProject).findAllGroupCodes()) {
			Group group = getGroupRegistry(iProject).findByCode(groupCode);
			if (group != null)
				groups.add(group);
		}
		return groups;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findAssignmentsForGroupAndContext(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.Group, java.lang.String)
	 */
	public List<ContextRoleAssignment> findAssignmentsForGroupAndContext(IProject iProject, Group group, String context) {
		return getContextRoleAssignmentRegistry(iProject).findFor(context, group);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findContextsForGroup(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User)
	 */
	public List<String> findContextsForGroup(IProject iProject, Group group) {
		List<String> contexts = new LinkedList<String>();
		for (ContextRoleAssignment assignment : getContextRoleAssignmentRegistry(iProject).findFor(group)) {
			contexts.add(assignment.getContext());
		}
		return contexts;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findGroupsWithContext(org.eclipse.core.resources.IProject, java.lang.String)
	 */
	public Set<Group> findGroupsWithContext(IProject iProject, String context) {
		List<ContextRoleAssignment> assignments = this.getContextRoleAssignmentRegistry(iProject).findFor(context);
		Set<Group> groups = new HashSet<Group>();
		for (ContextRoleAssignment assignment : assignments) {
			if (assignment.getGroupCode() != null) {
				GroupRegistry groupRegistry = getGroupRegistry(iProject);
				groups.add(groupRegistry.findByCode(assignment.getGroupCode()));
			}
		}
		return groups;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#deleteGroup(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.Group)
	 */
	public void deleteGroup(IProject iProject, Group group) {
		getGroupRegistry(iProject).delete(group);
		for (SecurityModelChangedListener listener : modelChangedListeners) {
			listener.groupDeleted(group);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#deleteUser(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User)
	 */
	public void deleteUser(IProject iProject, User user) {
		getUserRegistry(iProject).delete(user);
		for (SecurityModelChangedListener listener : modelChangedListeners) {
			listener.userDeleted(user);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#persistGroup(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.Group)
	 */
	public void persistGroup(IProject iProject, Group group) {
		getGroupRegistry(iProject).persist(group);
		
		//refresh the group with generated values from LH3 server
		group = getGroupRegistry(iProject).findByCode(group.getCode());
		
		for (SecurityModelChangedListener listener : modelChangedListeners) {
			listener.groupCreated(group);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#persistUser(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User)
	 */
	public void persistUser(IProject iProject, User user) {
		getUserRegistry(iProject).persist(user);
		
		//refresh the user with generated values from LH3 server
		user = getUserRegistry(iProject).findByCode(user.getCode());
		
		for (SecurityModelChangedListener listener : modelChangedListeners) {
			listener.userCreated(user);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#updateGroup(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.Group)
	 */
	public void updateGroup(IProject iProject, Group group) {
		getGroupRegistry(iProject).update(group);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#updateUser(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.User)
	 */
	public void updateUser(IProject iProject, User user) {
		getUserRegistry(iProject).update(user);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#deleteAssignment(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment)
	 */
	public void deleteAssignment(IProject iProject, ContextRoleAssignment assignment) {
		getContextRoleAssignmentRegistry(iProject).delete(assignment);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#deleteAssignmentsByEntity(org.eclipse.core.resources.IProject, java.lang.Object)
	 */
	public void deleteAssignmentsByEntity(IProject iProject, Object entity) {
		ContextAdapter contextAdapter = (ContextAdapter) Platform.getAdapterManager().getAdapter(entity, ContextAdapter.class);
		String context = contextAdapter.toShortContext(entity);
		
		for (ContextRoleAssignment contextRoleAssignment : getContextRoleAssignmentRegistry(iProject).findFor(context)) {
			getContextRoleAssignmentRegistry(iProject).delete(contextRoleAssignment);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#persistAssignment(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment)
	 */
	public void persistAssignment(IProject iProject, ContextRoleAssignment assignment) {
		getContextRoleAssignmentRegistry(iProject).persist(assignment);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#findByTemplate(org.eclipse.core.resources.IProject, com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment)
	 */
	public List<ContextRoleAssignment> findByTemplate(IProject iProject, ContextRoleAssignment template) {
		return getContextRoleAssignmentRegistry(iProject).findByTemplate(template);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#addSecurityModelChangedListener(com.mercatis.lighthouse3.ui.service.SecurityModelChangedListener)
	 */
	public void addSecurityModelChangedListener(SecurityModelChangedListener listener) {
		modelChangedListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.service.SecurityService#removeSecurityModelChangedListener(com.mercatis.lighthouse3.ui.service.SecurityModelChangedListener)
	 */
	public void removeSecurityModelChangedListener(SecurityModelChangedListener listener) {
		modelChangedListeners.remove(listener);
	}
}
