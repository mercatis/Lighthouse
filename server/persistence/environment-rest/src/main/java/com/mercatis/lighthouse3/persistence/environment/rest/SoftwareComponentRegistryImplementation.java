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
package com.mercatis.lighthouse3.persistence.environment.rest;

import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.HierarchicalDomainModelEntityDAOImplementation;

/**
 * This class provides a software component registry implementation. The
 * implementation acts as an HTTP client to a RESTful web service providing the
 * DAO storage functionality.
 */
public class SoftwareComponentRegistryImplementation extends
		HierarchicalDomainModelEntityDAOImplementation<SoftwareComponent> implements SoftwareComponentRegistry {

	public void reRegister(SoftwareComponent entityToReRegister) {
		this.update(entityToReRegister);
	}

	public void register(SoftwareComponent entityToRegister) {
		this.persist(entityToRegister);
	}

	public void unregister(SoftwareComponent entityToUnregister) {
		this.delete(entityToUnregister);
	}

	public SoftwareComponentRegistryImplementation() {
		super();
	}

	public SoftwareComponentRegistryImplementation(String serverUrl) {
		super();
		this.setServerUrl(serverUrl);
	}
	
	public SoftwareComponentRegistryImplementation(String serverUrl, String user, String password) {
	    this(serverUrl);
	    this.user = user;
	    this.password = password;
	}
}
