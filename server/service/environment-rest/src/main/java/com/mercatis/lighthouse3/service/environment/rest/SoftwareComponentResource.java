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

import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.service.commons.rest.HierarchicalDomainModelEntityResource;

/**
 * This class implements the REST resource capturing software components in the
 * environment domain model of Lighthouse. As such, CRUD functionality on
 * software components is made available in a RESTful way by the environment
 * service.
 */
@Path("/SoftwareComponent")
public class SoftwareComponentResource extends HierarchicalDomainModelEntityResource<SoftwareComponent> {

	@Override
	public HierarchicalDomainModelEntityDAO<SoftwareComponent> getEntityDAO() {
		return this.getServiceContainer().getDAO(SoftwareComponentRegistry.class);
	}

	@Override
	protected String getContextRole(com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource.CtxOp op) {
		switch (op) {
			case FIND:
				return "viewSoftwareComponent";
			case PERSIST:
				return "createSoftwareComponent";
			case UPDATE:
				return "modifySoftwareComponent";
			case DELETE:
				return "deleteSoftwareComponent";
			default:
				throw new PersistenceException("unsupported operation in role lookup", null);
		}
	}

	@Override
	protected StringBuilder getContextString() {
		return new StringBuilder("/SoftwareComponent/SoftwareComponent");
	}
}