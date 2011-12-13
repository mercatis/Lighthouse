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
package com.mercatis.lighthouse3.domainmodel.environment;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;

/**
 * This interface subsumes common DAO functionality for domain entities carrying
 * deployments. These are possible status carriers.
 */
public interface DeploymentCarryingDomainModelEntityDAO<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
		extends HierarchicalDomainModelEntityDAO<Entity> {
	/**
	 * This method looks up all domain entities of the given kind to which a
	 * given deployment is attached.
	 * 
	 * @param deployment
	 *            the deployment of interest.
	 * @return the set of entities attached to the deployment.
	 */
	public List<Entity> findForDeployment(Deployment deployment);
}
