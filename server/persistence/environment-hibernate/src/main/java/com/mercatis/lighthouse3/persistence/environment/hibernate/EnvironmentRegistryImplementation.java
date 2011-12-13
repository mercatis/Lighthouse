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
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;

/**
 * This class provides a Hibernate implementation of the
 * <code>SoftwareComponentRegistry</code> interface.
 */
public class EnvironmentRegistryImplementation extends
		DeploymentCarryingDomainModelEntityDAOImplementation<Environment> implements EnvironmentRegistry {

	@Override
	protected Criteria entityToCriteria(Session session, Environment entityTemplate) {

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getLongName() != null)
			criteria.add(Restrictions.eq("longName", entityTemplate.getLongName()));

		if (entityTemplate.getDescription() != null)
			criteria.add(Restrictions.eq("description", entityTemplate.getDescription()));

		if (entityTemplate.getContact() != null)
			criteria.add(Restrictions.eq("contact", entityTemplate.getContact()));

		if (entityTemplate.getContactEmail() != null)
			criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));

		criteria.setCacheable(true);
		
		return criteria;
	}

	@Override
	public void delete(Environment entityToDelete) {
		try {
			Session session = unitOfWork.getCurrentSession();
			session.createSQLQuery("delete from STATUS_METADATA where MD_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
			session.createSQLQuery("delete from STATUS_NOTIFICATION_CHANNELS where SNC_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
			session.createSQLQuery("update STATUS set LATEST_STATUS_CHANGE = null where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set NEXT_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set PREVIOUS_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
			session.createSQLQuery("delete from STATUS_CHANGES where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
			session.createSQLQuery("delete from STATUS where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", entityToDelete.getId()).setReadOnly(false).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup status tables for environment: " + entityToDelete.getCode(), ex);
			}
		}
		super.delete(entityToDelete);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Environment> findForDeployment(Deployment deployment) {
		return this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).createCriteria("attachedDeployments").add(Restrictions.eq("id", deployment.getId())).setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTopLevelComponentCodes() {
		return this.unitOfWork.getCurrentSession().createQuery("select p.code from Environment p where p.parentEntity is null").setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTopLevelComponentIds() {
		return this.unitOfWork.getCurrentSession().createQuery("select p.id from Environment p where p.parentEntity is null").setCacheable(true).list();
	}

	@Override
	public Environment findByCode(String code) {
		return (Environment) this.unitOfWork.getCurrentSession().createQuery("from Environment as p where p.code = :code").setString("code", code).setCacheable(true).uniqueResult();
	}
}
