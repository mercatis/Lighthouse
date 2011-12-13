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
package com.mercatis.lighthouse3.service.environment.rest;

import javax.ws.rs.Path;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;

/**
 * This class implements the REST resource capturing environments in the
 * environment domain model of Lighthouse. As such, CRUD functionality on
 * environments is made available in a RESTful way by the environment service.
 */
@Path("/Environment")
public class EnvironmentResource extends DeploymentCarryingDomainModelEntityResource<Environment> {

	@Override
	public DeploymentCarryingDomainModelEntityDAO<Environment> getEntityDAO() {
		return this.getServiceContainer().getDAO(EnvironmentRegistry.class);
	}

	@Override
	protected String getContextRole(CtxOp op) {
		switch (op) {
			case FIND:
				return "viewEnvironment";
			case PERSIST:
				return "createEnvironment";
			case UPDATE:
				return "modifyEnvironment";
			case DELETE:
				return "deleteEnvironment";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/Environment/Environment");
	}

	@Override
	protected StringBuilder getEntityContext(Environment e) {
		return formatContext(e.getCode(), null);
	}

}