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

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.persistence.commons.hibernate.DomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate implementation of the
 * <code>DeploymentRegistry</code> interface.
 */
public class DeploymentRegistryImplementation extends DomainModelEntityDAOImplementation<Deployment> implements DeploymentRegistry {

	@Override
	protected Criteria entityToCriteria(Session session, Deployment entityTemplate) {

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getDeployedComponent() != null)
			criteria.add(Restrictions.eq("deployedComponent", entityTemplate.getDeployedComponent()));

		if (entityTemplate.getLocation() != null)
			criteria.add(Restrictions.eq("location", entityTemplate.getLocation()));

		if (entityTemplate.getDescription() != null)
			criteria.add(Restrictions.eq("description", entityTemplate.getDescription()));

		if (entityTemplate.getContact() != null)
			criteria.add(Restrictions.eq("contact", entityTemplate.getContact()));

		if (entityTemplate.getContactEmail() != null)
			criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));

		criteria.setCacheable(true);
		
		return criteria;
	}

	public Deployment deploy(SoftwareComponent component, String location, String description, String contact, String contactEmail) {
		Deployment result = new Deployment();

		result.setDeployedComponent(component);
		result.setLocation(location);
		result.setDescription(description);
		result.setContact(contact);
		result.setContactEmail(contactEmail);

		this.persist(result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#
	 * alreadyPersisted
	 * (com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	public boolean alreadyPersisted(Deployment deployment) {
		return findByComponentAndLocation(deployment.getDeployedComponent(), deployment.getLocation()) != null;
	}

	@SuppressWarnings("unchecked")
	public List<Deployment> findAtLocation(String location) {
		return this.unitOfWork.getCurrentSession().createCriteria(Deployment.class).add(Restrictions.eq("location", location)).setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	public List<Deployment> findByComponent(SoftwareComponent component) {
		List<Deployment> deployments = this.unitOfWork.getCurrentSession().createCriteria(Deployment.class)
				.add(Restrictions.eq("deployedComponent", component)).setCacheable(true).list();

		if (deployments.isEmpty() && !component.isRootEntity())
			return this.findByComponent(component.getParentEntity());
		else
			return deployments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#
	 * findByComponentCodeAndLocation(java.lang.String, java.lang.String)
	 */
	public Deployment findByComponentCodeAndLocation(String componentCode, String location) {
		return (Deployment) this.unitOfWork.getCurrentSession()
				.createQuery("from Deployment as entity where entity.location = :location and entity.deployedComponent.code = :componentCode")
				.setString("location", location).setString("componentCode", componentCode).setCacheable(true).uniqueResult();
	}

	public Deployment findByComponentAndLocation(SoftwareComponent component, String location) {
		return (Deployment) this.unitOfWork.getCurrentSession()
				.createQuery("from Deployment as entity where entity.location = :location and entity.deployedComponent = :component")
				.setString("location", location).setEntity("component", component).setCacheable(true).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllLocations() {
		return this.unitOfWork.getCurrentSession().createQuery("select d.location from Deployment d").setCacheable(true).list();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void delete(Deployment deployment) {
		Deployment deploymentToDelete = find(deployment.getId());
		if (deploymentToDelete == null) {
			throw new PersistenceException("Entity not persistent", null);
		}
		Session session = unitOfWork.getCurrentSession();
		if (!session.contains(deploymentToDelete))
			deploymentToDelete = (Deployment) session.merge(deploymentToDelete);

		List<DeploymentCarryingDomainModelEntity> attachedTo = session.createCriteria(DeploymentCarryingDomainModelEntity.class)
				.createCriteria("attachedDeployments").add(Restrictions.eq("id", deploymentToDelete.getId())).setCacheable(true).list();

		for (DeploymentCarryingDomainModelEntity entity : attachedTo) {
			entity.detachDeployment(deploymentToDelete);
			session.saveOrUpdate(entity);
		}

		try {
			unitOfWork.flush();
			session.createSQLQuery("delete from STATUS_METADATA where MD_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS_NOTIFICATION_CHANNELS where SNC_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS set LATEST_STATUS_CHANGE = null where STA_CONTEXT_ID = :deployment_id")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set NEXT_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set PREVIOUS_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS_CHANGES where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS where STA_CONTEXT_ID = :deployment_id")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup status tables for deployment: " + deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation(), ex);
			}
		}

		try {
			unitOfWork.flush();
			session.createSQLQuery("delete pie from PROCESS_INSTANCES_EVENTS pie join EVENTS e on pie.EVT_ID = e.EVT_ID where e.EVT_CONTEXT_ID = :deployment_id")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup process instance tables for deployment: " + deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation(), ex);
			}
		}

		try {
			unitOfWork.flush();

			session.createSQLQuery("delete from EVENT_TAGS where EVT_ID in (select distinct EVT_ID from EVENTS where EVT_CONTEXT_ID = " + deploymentToDelete.getId()
									+ ")").executeUpdate();
			session.createSQLQuery("delete from EVENT_TRANSACTION_IDS where EVT_ID in (select distinct EVT_ID from EVENTS where EVT_CONTEXT_ID = "
									+ deploymentToDelete.getId() + ")").executeUpdate();

			session.createSQLQuery("delete from EVENT_UDFS where EVT_ID in (select distinct EVT_ID from EVENTS where EVT_CONTEXT_ID = " + deploymentToDelete.getId()
									+ ")").executeUpdate();
			session.createSQLQuery("delete from EVENTS where EVT_CONTEXT_ID = :deployment_id")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup event tables for deployment: " + deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation(), ex);
			}
		}

		try {
			unitOfWork.flush();

			session.createSQLQuery("delete from JOB_PARAMETER_VALUES where JPV_JOB_ID in (select distinct JOB_ID from JOBS where JOB_CALL_TARGET_ID in (select distinct OPI_ID from OPERATION_INSTALLATIONS where OPI_INSTALLATION_LOCATION_ID = :deployment_id))")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();

			session.createSQLQuery("delete from JOBS where JOB_CALL_TARGET_ID in (select distinct OPI_ID from OPERATION_INSTALLATIONS where OPI_INSTALLATION_LOCATION_ID = :deployment_id)")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup job tables for deployment: " + deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation(), ex);
			}
		}

		try {
			unitOfWork.flush();

			session.createSQLQuery("delete from OPERATION_INSTALLATIONS where OPI_INSTALLATION_LOCATION_ID = :deployment_id")
					.setParameter("deployment_id", deploymentToDelete.getId()).executeUpdate();
		} catch (Exception ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to cleanup operation installation tables for deployment: " + deployment.getDeployedComponent().getCode() + " @ " + deployment.getLocation(), ex);
			}
		}

		super.delete(deploymentToDelete);
	}

	public void undeploy(Deployment deployment) {
		this.delete(deployment);
	}

	@Override
	public void persist(Deployment entityToPersist) {
		if (findByComponentAndLocation(entityToPersist.getDeployedComponent(), entityToPersist.getLocation())!=null)
			throw new PersistenceException("Entity already persistent", null);
		super.persist(entityToPersist);
	}
}
