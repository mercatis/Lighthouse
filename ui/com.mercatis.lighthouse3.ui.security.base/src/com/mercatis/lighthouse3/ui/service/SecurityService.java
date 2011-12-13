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
package com.mercatis.lighthouse3.ui.service;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;


public interface SecurityService {

	public Set<User> findUsersWithContext(IProject iProject, String context);
	public List<User> findAllUsers(IProject iProject);
	public List<String> findContextsForUser(IProject iProject, User user);
	public List<ContextRoleAssignment> findAssignmentsForUserAndContext(IProject iProject, User user, String context);

	public List<Group> findAllGroups(IProject iProject);
	public Set<Group> findGroupsWithContext(IProject iProject, String context);
	public List<String> findContextsForGroup(IProject iProject, Group group);
	public List<ContextRoleAssignment> findAssignmentsForGroupAndContext(IProject iProject, Group group, String context);
	
	public void persistUser(IProject iProject, User user);
	public void updateUser(IProject iProject, User user);
	public void deleteUser(IProject iProject, User user);
	
	public void persistGroup(IProject iProject, Group group);
	public void updateGroup(IProject iProject, Group group);
	public void deleteGroup(IProject iProject, Group group);
	
	public void persistAssignment(IProject iProject, ContextRoleAssignment assignment);
	public void deleteAssignment(IProject iProject, ContextRoleAssignment assignment);
	public void deleteAssignmentsByEntity(IProject iProject, Object entity);
	public List<ContextRoleAssignment> findByTemplate(IProject iProject, ContextRoleAssignment template);
	
	public void addSecurityModelChangedListener(SecurityModelChangedListener listener);
	public void removeSecurityModelChangedListener(SecurityModelChangedListener listener);
}
