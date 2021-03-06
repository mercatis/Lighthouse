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
package com.mercatis.lighthouse3.persistence.environment.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO;
import com.mercatis.lighthouse3.persistence.commons.hibernate.HierarchicalDomainModelEntityDAOImplementation;

/**
 * This abstract base class implements the additional methods of the
 * <code>DeploymentCarryingDomainModelEntityDAO</code> interface.
 */
public abstract class DeploymentCarryingDomainModelEntityDAOImplementation<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
		extends HierarchicalDomainModelEntityDAOImplementation<Entity> implements
		DeploymentCarryingDomainModelEntityDAO<Entity> {

	@Override
	protected Criteria entityToCriteria(Session session, Entity entityTemplate) {

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (!entityTemplate.getDeployments().isEmpty()) {
			Criteria deploymentCriteria = criteria.createCriteria("attachedDeployments");

			for (Deployment deployment : entityTemplate.getAllDeployments()) {
				deploymentCriteria.add(Restrictions.eq("id", deployment.getId()));
			}
		}

		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<Entity> findForDeployment(Deployment deployment) {
		return this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).createCriteria(
				"attachedDeployments").add(Restrictions.eq("id", deployment.getId())).list();
	}

}
