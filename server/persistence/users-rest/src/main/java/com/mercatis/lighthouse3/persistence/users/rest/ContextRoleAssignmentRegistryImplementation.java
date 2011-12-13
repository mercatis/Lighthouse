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
package com.mercatis.lighthouse3.persistence.users.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;
import static com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment.DENY;
import static com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment.GRANT;
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignment;
import com.mercatis.lighthouse3.domainmodel.users.ContextRoleAssignmentRegistry;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides an context role assignment registry implementation. The
 * implementation acts as an HTTP client to a RESTful web service providing the
 * DAO storage functionality.
 */
public class ContextRoleAssignmentRegistryImplementation extends DomainModelEntityDAOImplementation<ContextRoleAssignment> implements ContextRoleAssignmentRegistry {

    public ContextRoleAssignmentRegistryImplementation() {
        super();
    }

    public ContextRoleAssignmentRegistryImplementation(String serverUrl) {
        super();
        setServerUrl(serverUrl);
    }
    
    public ContextRoleAssignmentRegistryImplementation(String serverUrl, String user, String password) {
	this(serverUrl);
	this.user = user;
	this.password = password;
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new DomainModelEntityDAO[]{this};
    }

    public List<ContextRoleAssignment> findFor(User user) {
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/User", user.getCode()), HttpMethod.GET, null, null);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findFor(Group group) {
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/Group", group.getCode()), HttpMethod.GET, null, null);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findFor(String context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("context", context);
        String xml = this.executeHttpMethod("/ContextRoleAssignment/Context", HttpMethod.GET, null, params);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findFor(String context, User user) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("context", context);
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/User", user.getCode()), HttpMethod.GET, null, params);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findFor(String context, Group group) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("context", context);
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/Group", group.getCode()), HttpMethod.GET, null, params);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findAllFor(User user) {
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/All/User", user.getCode()), HttpMethod.GET, null, null);
        return this.resolveWebServiceResultList(xml);
    }

    public List<ContextRoleAssignment> findAllFor(String context, User user) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("context", context);
        String xml = this.executeHttpMethod(appendPathElementToUrl("/ContextRoleAssignment/All/User", user.getCode()), HttpMethod.GET, null, params);
        return this.resolveWebServiceResultList(xml);
    }

    public void grant(User user, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setUserCode(user.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(GRANT);
        this.persist(contextRoleAssignment);
    }

    public void deny(User user, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setUserCode(user.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(DENY);
        this.persist(contextRoleAssignment);
    }

    public void grant(Group group, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setGroupCode(group.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(GRANT);
        this.persist(contextRoleAssignment);
    }

    public void deny(Group group, String role, String context) {
        ContextRoleAssignment contextRoleAssignment = new ContextRoleAssignment();
        contextRoleAssignment.setGroupCode(group.getCode());
        contextRoleAssignment.setRole(role);
        contextRoleAssignment.setContext(context);
        contextRoleAssignment.setPermissionType(DENY);
        this.persist(contextRoleAssignment);
    }
}
