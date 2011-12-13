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
package com.mercatis.lighthouse3.persistence.users.hibernate;

import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignmentRegistry;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.persistence.commons.hibernate.DomainModelEntityDAOImplementation;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * This class provides a Hibernate implementation of the
 * <code>ContextRoleAssignmentRegistry</code> interface.
 */
public class ContextRoleAssignmentRegistryImplementation extends DomainModelEntityDAOImplementation<ContextRoleAssignment> implements ContextRoleAssignmentRegistry {

    @Override
    protected Criteria entityToCriteria(Session session, ContextRoleAssignment entityTemplate) {
        Criteria criteria = super.entityToCriteria(session, entityTemplate);

        if (entityTemplate.getGroupCode() != null) {
            criteria.add(Restrictions.eq("groupCode", entityTemplate.getGroupCode()));
        }
        if (entityTemplate.getUserCode() != null) {
            criteria.add(Restrictions.eq("userCode", entityTemplate.getUserCode()));
        }
        if (entityTemplate.getRole() != null) {
            criteria.add(Restrictions.eq("role", entityTemplate.getRole()));
        }
        if (entityTemplate.getContext() != null) {
            criteria.add(Restrictions.eq("context", entityTemplate.getContext()));
        }

        return criteria;
    }

    @SuppressWarnings("unchecked")
	public List<ContextRoleAssignment> findFor(User user) {
        List<ContextRoleAssignment> matches = this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).add(
                Restrictions.eq("userCode", user.getCode())).list();
        return matches;
    }

    @SuppressWarnings("unchecked")
	public List<ContextRoleAssignment> findFor(Group group) {
        List<ContextRoleAssignment> matches = this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).add(
                Restrictions.eq("groupCode", group.getCode())).list();
        return matches;
    }

    @SuppressWarnings("unchecked")
	public List<ContextRoleAssignment> findFor(String context) {
        List<ContextRoleAssignment> matches = this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).add(
                Restrictions.eq("context", context)).list();
        return matches;
    }

    @SuppressWarnings("unchecked")
	public List<ContextRoleAssignment> findFor(String context, User user) {
        List<ContextRoleAssignment> matches = this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).add(
                Restrictions.eq("context", context)).add(
                Restrictions.eq("userCode", user.getCode())).list();
        return matches;
    }

    @SuppressWarnings("unchecked")
	public List<ContextRoleAssignment> findFor(String context, Group group) {
        List<ContextRoleAssignment> matches = this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).add(
                Restrictions.eq("context", context)).add(
                Restrictions.eq("groupCode", group.getCode())).list();
        return matches;
    }

    public List<ContextRoleAssignment> findAllFor(User user) {
        return findAllFor(null, user);
    }

	public List<ContextRoleAssignment> findAllFor(String context, User user) {
        List<ContextRoleAssignment> matches = new LinkedList<ContextRoleAssignment>();

        if (context == null) {
            matches.addAll(findFor(user));
        } else {
            matches.addAll(findFor(context, user));
        }

	List<Group> groups = getDAOProvider().getDAO(GroupRegistry.class).getGroupsForUserCode(user.getCode());

        for (Group group : groups) {
            if (context == null) {
                matches.addAll(findFor(group));
            } else {
                matches.addAll(findFor(context, group));
            }
        }

        return matches;
    }

    public void grant(User user, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setUserCode(user.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(ContextRoleAssignment.GRANT);

        this.persist(contextRoleAssignment);
    }

    public void deny(User user, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setUserCode(user.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(ContextRoleAssignment.DENY);

        this.persist(contextRoleAssignment);
    }

    public void grant(Group group, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setGroupCode(group.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(ContextRoleAssignment.GRANT);

        this.persist(contextRoleAssignment);
    }

    public void deny(Group group, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setGroupCode(group.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(ContextRoleAssignment.DENY);

        this.persist(contextRoleAssignment);
    }

    public String getLighthouseDomain() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<ContextRoleAssignment> findAll() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
