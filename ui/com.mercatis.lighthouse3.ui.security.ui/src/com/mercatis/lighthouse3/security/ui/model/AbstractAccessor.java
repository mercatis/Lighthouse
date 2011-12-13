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
package com.mercatis.lighthouse3.security.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This class is used to encapsulate an user or a group for editing.
 * The main purpose is to handle role
 */
public abstract class AbstractAccessor {

	/**
	 * Used for persisting operations
	 */
	protected LighthouseDomain lighthouseDomain;
	
	/**
	 * Maps a {@link Set} of roles to a specific context.
	 * The keyset of this map is used to determine in which roles the accessor is present.
	 */
	private Map<String, Set<String>> rolesAtContexts = null;
	
	/**
	 * Added {@link ContextRoleAssignment}a since last persisting operation
	 */
	private Set<ContextRoleAssignment> addedAssignments = new HashSet<ContextRoleAssignment>();
	
	/**
	 * Removed {@link ContextRoleAssignment}a since last persisting operation
	 */
	private Set<ContextRoleAssignment> removedAssignments = new HashSet<ContextRoleAssignment>();

	public AbstractAccessor(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	/**
	 * Gets this accessors label for displaying
	 * 
	 * @return The label for this accessor
	 */
	public abstract String getLabel();

	/**
	 * Gets this accessors image for displaying
	 * 
	 * @return The image for this accessor
	 */
	public abstract Image getImage();

	/**
	 * Checks if this accessor has a role at a specific context.
	 * 
	 * @param role The role to check
	 * @param context The context where this role is expected
	 * @return true if this accessor has the given context and the role assignet to it
	 */
	public boolean hasRole(String role, String context) {
		if (context == null)
			return false;
		Set<String> roles = getRoles(context);
		return roles != null && roles.contains(role);
	}

	/**
	 * Adds a role to this accessor at the given context.
	 * 
	 * @param role The role to add
	 * @param context The context where to add the role
	 */
	public void addRole(String role, String context) {
		//add the role to rolesAtContexts
		getRoles(context).add(role);
		
		//create a new ContextRoleAssignment for persisting
		ContextRoleAssignment assignment = new ContextRoleAssignment();
		setCodeOnAssignment(assignment);
		assignment.setContext(context);
		assignment.setRole(role);
		assignment.setPermissionType(ContextRoleAssignment.GRANT);
		
		//if it was a removed assingment, it has not to be persisted again
		if (!removedAssignments.remove(assignment))
			addedAssignments.add(assignment);
	}

	/**
	 * Removes a role from this accessor at the given context.
	 * 
	 * @param role The role to remove
	 * @param context The context from where to remove the role
	 */
	public void removeRole(String role, String context) {
		//remove the role from rolesAtContexts
		getRoles(context).remove(role);
		
		//create a new ContextRoleAssignment for deleting from database
		ContextRoleAssignment assignment = new ContextRoleAssignment();
		setCodeOnAssignment(assignment);
		assignment.setContext(context);
		assignment.setRole(role);
		assignment.setPermissionType(ContextRoleAssignment.GRANT);
		
		//if the assignment was added before, it's not in the database and therefore should not be deleted
		if (!addedAssignments.remove(assignment))
			removedAssignments.add(assignment);
	}

	/**
	 * Gets the map {@link #rolesAtContexts} and initializes it if it was not loaded yet.
	 * 
	 * @return The map {@link #rolesAtContexts}
	 */
	private Map<String, Set<String>> getRolesAtContexts() {
		if (rolesAtContexts == null) {
			rolesAtContexts = new HashMap<String, Set<String>>();
			
			//load all contexts for this accessor and put them into rolesAtContexts
			//so we can use the keyset as reference in which contexts the accessor is present
			for (String context : loadContexts()) {
				//the assigned roles are loaded lazily, so we put null in here
				rolesAtContexts.put(context, null);
			}
		}
		return rolesAtContexts;
	}

	/**
	 * Loads all contexts belonging to this accessor from the LH3 security server.
	 * 
	 * @return The {@link List} of contexts
	 */
	protected abstract List<String> loadContexts();

	/**
	 * Removes a whole context whith all roles from this accessor.
	 * 
	 * @param context Context to remove
	 */
	public void removeContext(String context) {
		//mark each role in this context for deletion
		for (String role : getRoles(context).toArray(new String[] {})) {
			removeRole(role, context);
		}
		getRolesAtContexts().remove(context);
	}

	/**
	 * Gets all contexts belonging to this accessor.
	 * 
	 * @return The {@link List} of contexts
	 */
	public List<String> getContexts() {
		return new ArrayList<String>(getRolesAtContexts().keySet());
	}

	/**
	 * Adds a context to this accessor.
	 * 
	 * @param context Context to add
	 */
	public void addContext(String context) {
		if (!getRolesAtContexts().containsKey(context)) {
			getRolesAtContexts().put(context, new HashSet<String>());
		}
	}

	/**
	 * Checks if this accessor was edited. It is dirty if at least an assignment was deleted or added. 
	 * 
	 * @return true if this accessor was edited
	 */
	public boolean isDirty() {
		return !addedAssignments.isEmpty() || !removedAssignments.isEmpty();
	}

	/**
	 * Saves changes on this accessor to the LH3 security server.
	 * <ol>
	 * <li>Delete all from {@link #removedAssignments}</li>
	 * <li>Persist all in {@link #addedAssignments}</li>
	 * </ol> 
	 */
	public void save() {
		for (ContextRoleAssignment template : removedAssignments) {
			//before deleting, me must load the assignment from the server to get the ID filled
			ContextRoleAssignment assignment = Security.getService().findByTemplate(lighthouseDomain.getProject(),
					template).iterator().next();
			Security.getService().deleteAssignment(lighthouseDomain.getProject(), assignment);
		}
		removedAssignments.clear();
		for (ContextRoleAssignment assignment : addedAssignments) {
			Security.getService().persistAssignment(lighthouseDomain.getProject(), assignment);
		}
		addedAssignments.clear();
	}

	/**
	 * Gets a {@link Set} of roles for the given context. If the context is <code>null</code>, a new {@link HashSet} will be returned.
	 * If there is no {@link Set} in {@link #rolesAtContexts}, it will be loaded from LH3 security server.
	 * 
	 * @param context Context to get the roles for
	 * @return The {@link Set} of roles at this context
	 */
	public Set<String> getRoles(String context) {
		if (context == null)
			return new HashSet<String>();
		Set<String> roles = getRolesAtContexts().get(context);
		if (roles == null) {
			roles = loadRolesForContext(context);
			getRolesAtContexts().put(context, roles);
		}
		return roles;
	}

	/**
	 * Loads a {@link Set} of roles for the given context from the LH3 security server.
	 * 
	 * @param context Context to load the roles for
	 * @return The {@link Set} of roles at this context - never returns <code>null</code>, at least an empty {@link HashSet}
	 */
	private Set<String> loadRolesForContext(String context) {
		Set<String> roles = new HashSet<String>();
		for (ContextRoleAssignment assignment : getAssignments(context)) {
			if (assignment.getPermissionType() == ContextRoleAssignment.GRANT) {
				roles.add(assignment.getRole());
			}
		}
		return roles;
	}

	/**
	 * Gets the {@link ContextRoleAssignment}s for this accessor at the given context from the LH3 security server.
	 * 
	 * @param context Context to get the assignments for
	 * @return A {@link List} of {@link ContextRoleAssignment}s
	 */
	protected abstract List<ContextRoleAssignment> getAssignments(String context);

	/**
	 * Sets the code on a {@link ContextRoleAssignment}. As we don't know if the assignment is attached to a {@link User}
	 * or a {@link Group}, we need the subclasses to do this job.
	 * 
	 * @param assignment Assignment to set the code on
	 */
	protected abstract void setCodeOnAssignment(ContextRoleAssignment assignment);

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}
