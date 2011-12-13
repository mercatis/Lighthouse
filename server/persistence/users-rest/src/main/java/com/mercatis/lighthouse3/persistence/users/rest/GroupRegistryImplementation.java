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
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserCodeRegistration;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides an group registry implementation. The
 * implementation acts as an HTTP client to a RESTful web service providing the
 * DAO storage functionality.
 */
public class GroupRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Group> implements GroupRegistry {

    private UserRegistryImplementation userRegistry;

    public GroupRegistryImplementation() {
        super();
    }

    public GroupRegistryImplementation(String serverUrl, UserRegistryImplementation userRegistry) {
        this.userRegistry = userRegistry;
        this.setServerUrl(serverUrl);
    }
    
    public GroupRegistryImplementation(String serverUrl, UserRegistryImplementation userRegistry, String user, String password) {
	this(serverUrl, userRegistry);
	this.user = user;
	this.password = password;
    }

    /**
     * Overrides persist to persist or update users attached to this group.
     * 
     * @param entityToPersist
     */
    @Override
    public void persist(Group entityToPersist) {
        for (User member : entityToPersist.getMembers()) {
            if (this.userRegistry.findByCode(member.getCode()) == null) {
                this.userRegistry.persist(member);
            } else {
                this.userRegistry.update(member);
            }
        }
        super.persist(entityToPersist);
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new DomainModelEntityDAO[]{this, this.userRegistry};
    }

    public List<Group> getGroupsForUserCode(String userCode) {
        String xml = null;
        try {
            xml = this.executeHttpMethod(appendPathElementToUrl("/Group/User", userCode), HttpMethod.GET, null, null);
        } catch (PersistenceException ex) {
            return new LinkedList<Group>();
        }
        return this.resolveWebServiceResultList(xml);
    }

    public List<String> findAllGroupCodes() {
        String result = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, null);
        return new LinkedList<String>(XmlMuncher.readValuesFromXml(result, "//:code"));
    }
    
	public UserCodeRegistration getUserCodeRegistration(String userCode) {
		UserCodeRegistration reg = new UserCodeRegistration();
		reg.setUserCode(userCode);
		return reg;
	}
}
