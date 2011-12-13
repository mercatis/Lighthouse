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

import java.util.LinkedList;
import java.util.List;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides an user registry implementation. The
 * implementation acts as an HTTP client to a RESTful web service providing the
 * DAO storage functionality.
 */
public class UserRegistryImplementation extends CodedDomainModelEntityDAOImplementation<User> implements UserRegistry {

    public UserRegistryImplementation() {
        super();
    }

    public UserRegistryImplementation(String serverUrl) {
        super();
        setServerUrl(serverUrl);
    }
    
    public UserRegistryImplementation(String serverUrl, String user, String password) {
	this(serverUrl);
	this.user = user;
	this.password = password;
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new CodedDomainModelEntityDAO[]{this};
    }

    public List<String> findAllUserCodes() {
        String result = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, null);
        return new LinkedList<String>(XmlMuncher.readValuesFromXml(result, "//:code"));
    }

    public User authenticate(String userCode, String password) {
    	StringBuilder postBody = new StringBuilder();
    	postBody.append(userCode).append("\n");
    	postBody.append(password).append("\n");
        String xml = this.executeHttpMethod("/User/Authenticate", HttpMethod.POST, postBody.toString(), null);
        User user = new User();
        user.fromXml(xml, this);
        return user;
    }
}
