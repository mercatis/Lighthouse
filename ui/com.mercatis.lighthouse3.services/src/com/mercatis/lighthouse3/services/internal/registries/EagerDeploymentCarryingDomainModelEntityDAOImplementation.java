/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.List;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO;


public abstract class EagerDeploymentCarryingDomainModelEntityDAOImplementation<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
	extends EagerHierarchicalDomainModelEntityDAOImplementation<Entity>
	implements DeploymentCarryingDomainModelEntityDAO<Entity>{

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO#findForDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public List<Entity> findForDeployment(Deployment deployment) {
		List<Entity> result = new ArrayList<Entity>();
		for (Entity entity : cache.values()) {
			if (entity.hasAttachedDeployment(deployment)) {
				result.add(entity);
			}
		}
		
		return result;
	}

}
