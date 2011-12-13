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

import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.HierarchicalDomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate implementation of the
 * <code>SoftwareComponentRegistry</code> interface.
 */
public class SoftwareComponentRegistryImplementation extends
HierarchicalDomainModelEntityDAOImplementation<SoftwareComponent> implements SoftwareComponentRegistry {

	@Override
	protected Criteria entityToCriteria(Session session, SoftwareComponent entityTemplate) {

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getLongName() != null)
			criteria.add(Restrictions.eq("longName", entityTemplate.getLongName()));

		if (entityTemplate.getVersion() != null)
			criteria.add(Restrictions.eq("version", entityTemplate.getVersion()));

		if (entityTemplate.getDescription() != null)
			criteria.add(Restrictions.eq("description", entityTemplate.getDescription()));

		if (entityTemplate.getContact() != null)
			criteria.add(Restrictions.eq("contact", entityTemplate.getContact()));

		if (entityTemplate.getContactEmail() != null)
			criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));

		if (entityTemplate.getCopyright() != null)
			criteria.add(Restrictions.eq("copyright", entityTemplate.getCopyright()));

		criteria.setCacheable(true);

		return criteria;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void delete(SoftwareComponent sc) {
		SoftwareComponent component = findByCode(sc.getCode()); 
		if (component==null)
			throw new PersistenceException("Entity not persistent", null);

		Criteria crit = this.unitOfWork.getCurrentSession().createCriteria(Deployment.class);
		crit.add(Restrictions.eq("deployedComponent", component));
		List<Deployment> deployments = crit.setCacheable(true).list();

		for (Deployment deployment : deployments) {
			List<DeploymentCarryingDomainModelEntity> attachedTo = this.unitOfWork.getCurrentSession().createCriteria(
					DeploymentCarryingDomainModelEntity.class).createCriteria("attachedDeployments").add(
							Restrictions.eq("id", deployment.getId())).setCacheable(true).list();

			for (DeploymentCarryingDomainModelEntity entity : attachedTo) {
				entity.detachDeployment(deployment);
				this.unitOfWork.getCurrentSession().saveOrUpdate(entity);
			}

			try {
				this.unitOfWork.getCurrentSession().createSQLQuery("delete from STATUS_METADATA where MD_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("delete from STATUS_NOTIFICATION_CHANNELS where SNC_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("update STATUS set LATEST_STATUS_CHANGE = null where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("update STATUS_CHANGES set NEXT_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("update STATUS_CHANGES set PREVIOUS_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("delete from STATUS_CHANGES where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
				this.unitOfWork.getCurrentSession().createSQLQuery("delete from STATUS where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", deployment.getId()).executeUpdate();
			} catch (Exception ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to cleanup status tables for softwareComponent: " + sc.getCode(), ex);
				}
			}
			this.unitOfWork.getCurrentSession().delete(deployment);
		}

		HashSet<SoftwareComponent> directSubEntities = new HashSet<SoftwareComponent>(component.getDirectSubEntities());

		for (SoftwareComponent childComponent : directSubEntities) {
			component.removeSubEntity(childComponent);
			this.delete(childComponent);
		}

		super.delete(component);
	}

	@Override
	public SoftwareComponent findByCode(String code) {
		Query query = this.unitOfWork.getCurrentSession().createQuery("from SoftwareComponent as component where component.code = :code").setString("code", code).setCacheable(true);
		SoftwareComponent component = (SoftwareComponent) query.uniqueResult();
		return component;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTopLevelComponentCodes() {
		return this.unitOfWork.getCurrentSession().createQuery("select sc.code from SoftwareComponent sc where sc.parentEntity is null").setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTopLevelComponentIds() {
		return this.unitOfWork.getCurrentSession().createQuery("select sc.id from SoftwareComponent sc where sc.parentEntity is null").setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SoftwareComponent> findByTemplate(SoftwareComponent template) {
		Criteria criteria = this.entityToCriteria(this.unitOfWork.getCurrentSession(), template).setCacheable(true);
		List<SoftwareComponent> components = criteria.list();

		return components;
	}

}
