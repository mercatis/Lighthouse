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

import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;

/**
 * This class provides a process / task registry implementation. The
 * implementation acts as an HTTP client to a RESTful web service providing the
 * DAO storage functionality.
 */
public class ProcessTaskRegistryImplementation extends DeploymentCarryingDomainModelEntityDAOImplementation<ProcessTask>
		implements ProcessTaskRegistry {

	public void reRegister(ProcessTask entityToReRegister) {
		this.update(entityToReRegister);
	}

	public void register(ProcessTask entityToRegister) {
		this.persist(entityToRegister);
	}

	public void unregister(ProcessTask entityToUnregister) {
		this.delete(entityToUnregister);
	}

	public ProcessTaskRegistryImplementation() {
		super();
	}

	public ProcessTaskRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry) {
		super();
		this.setServerUrl(serverUrl);
		this.setDeploymentRegistry(deploymentRegistry);
	}

	public ProcessTaskRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, String user, String password) {
	    this(serverUrl, deploymentRegistry);
	    this.user = user;
	    this.password = password;
	}
}
