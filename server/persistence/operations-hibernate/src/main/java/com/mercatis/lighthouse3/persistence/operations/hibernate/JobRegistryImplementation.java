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
package com.mercatis.lighthouse3.persistence.operations.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides a hibernate implementation for job registries.
 */
public class JobRegistryImplementation extends
		CodedDomainModelEntityDAOImplementation<Job> implements JobRegistry {

	@SuppressWarnings("unchecked")
	public List<Job> findAtDeployment(Deployment deployment) {
		return this.unitOfWork
				.getCurrentSession()
				.createQuery(
						"from Job where scheduledCall.target.installationLocation = :deployment")
				.setParameter("deployment", deployment).list();
	}

	@Override
	protected Criteria entityToCriteria(Session session, Job entityTemplate) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getLongName() != null)
			criteria.add(Restrictions.eq("longName", entityTemplate
					.getLongName()));

		if (entityTemplate.getDescription() != null)
			criteria.add(Restrictions.eq("description", entityTemplate
					.getDescription()));

		if (entityTemplate.getContact() != null)
			criteria.add(Restrictions
					.eq("contact", entityTemplate.getContact()));

		if (entityTemplate.getContactEmail() != null)
			criteria.add(Restrictions.eq("contactEmail", entityTemplate
					.getContactEmail()));

		if (entityTemplate.getScheduleExpression() != null)
			criteria.add(Restrictions.eq("scheduleExpression", entityTemplate
					.getScheduleExpression()));

		return criteria;
	}

}
