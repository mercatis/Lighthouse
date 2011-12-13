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
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;

/**
 * This class implements the REST resource capturing processes / tasks in a
 * RESTful way by the environment service.
 */
@Path("/ProcessTask")
public class ProcessTaskResource extends DeploymentCarryingDomainModelEntityResource<ProcessTask> {

	@Override
	public DeploymentCarryingDomainModelEntityDAO<ProcessTask> getEntityDAO() {
		return this.getServiceContainer().getDAO(ProcessTaskRegistry.class);
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
				return "viewProcessTask";
			case PERSIST:
				return "createProcessTask";
			case UPDATE:
				return "modifyProcessTask";
			case DELETE:
				return "deleteProcessTask";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/ProcessTask/ProcessTask");
	}

	@Override
	protected StringBuilder getEntityContext(ProcessTask e) {
		return formatContext(e.getCode(), null);
	}

}