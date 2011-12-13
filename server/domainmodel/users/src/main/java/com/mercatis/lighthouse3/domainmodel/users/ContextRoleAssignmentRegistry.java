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
package com.mercatis.lighthouse3.domainmodel.users;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import java.util.List;

/**
 * The context role assignment registry interface abstractly defines the contract for a
 * persistent repository of ContextRoleAssignments. It provides basic CRUD functionality.
 * It can be implemented using different persistence technologies.
 *
 * With regard to persistence, the following rules must apply for
 * implementations:
 *
 * <ul>
 * <li>ContextRoleAssignments are uniquely identified through all fields.</li>
 * <li>Either groupCode or userCode must be set.</li>
 * </ul>
 */
public interface ContextRoleAssignmentRegistry extends DomainModelEntityDAO<ContextRoleAssignment> {

    /**
     * Find the context role assignments for the given user.
     *
     * @param user
     * @return context role assignments found for this user
     */
    public List<ContextRoleAssignment> findFor(User user);

    /**
     * Find the context role assignments for the given group.
     *
     * @param group
     * @return context role assignments found for this group
     */
    public List<ContextRoleAssignment> findFor(Group group);

    /**
     * Find the context role assignments for the given context.
     *
     * @param context
     * @return context role assignments found for this context
     */
    public List<ContextRoleAssignment> findFor(String context);

    /**
     * Find the context role assignments for the given context and user.
     *
     * @param context
     * @param user
     * @return context role assignments found for this context and user
     */
    public List<ContextRoleAssignment> findFor(String context, User user);

    /**
     * Find the context role assignments for the given context and group.
     *
     * @param context
     * @param group
     * @return context role assignments found for this context and group
     */
    public List<ContextRoleAssignment> findFor(String context, Group group);

    /**
     * Find all the context role assignments for the given user.
     * <br />This method will consider the context role assignments inherited from the users groups.
     *
     * @param user
     * @return context role assignments found for this user
     */
    public List<ContextRoleAssignment> findAllFor(User user);

    /**
     * Find all the context role assignments for the given context and user.
     * <br />This method will consider the context role assignments inherited from the users groups.
     *
     * @param context
     * @param user
     * @return context role assignments found for this context and user
     */
    public List<ContextRoleAssignment> findAllFor(String context, User user);

    /**
     * Grant the role and context for the given user.
     *
     * @param user
     * @param role
     * @param context
     */
    public void grant(User user, String role, String context);

    /**
     * Deny the role and context for the given user.
     *
     * @param user
     * @param role
     * @param context
     */
    public void deny(User user, String role, String context);

    /**
     * Grant the role and context for the given group.
     *
     * @param group
     * @param role
     * @param context
     */
    public void grant(Group group, String role, String context);

    /**
     * Deny the role and context for the given group.
     *
     * @param group
     * @param role
     * @param context
     */
    public void deny(Group group, String role, String context);
}
