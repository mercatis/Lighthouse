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
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;

/**
 * This class provides a Hibernate implementation of the
 * <code>SoftwareComponentRegistry</code> interface.
 */
public class ProcessTaskRegistryImplementation extends
		DeploymentCarryingDomainModelEntityDAOImplementation<ProcessTask> implements ProcessTaskRegistry {

	@Override
	protected Criteria entityToCriteria(Session session, ProcessTask entityTemplate) {
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

		for (Entry<String, String> swimlane : entityTemplate.getSwimlaneData().entrySet()) {
			criteria
					.add(Restrictions
							.sqlRestriction(
									"exists (select psl.* from PROCESS_SWIMLANES psl where {alias}.STC_ID = psl.PRO_ID and psl.SWIMLANE = ? and psl.PRO_CODE = ?)",
									new String[] { swimlane.getValue(), swimlane.getKey() }, new Type[] {
											StringType.INSTANCE, StringType.INSTANCE }));
		}

		for (Tuple<String, String> transition : entityTemplate.getTransitionData()) {
			criteria
					.add(Restrictions
							.sqlRestriction(
									"exists (select pst.* from PROCESS_TRANSITIONS pst where {alias}.STC_ID = pst.PRO_ID and pst.FROM_PRO_CODE = ? and pst.TO_PRO_CODE = ?)",
									new String[] { transition.getA(), transition.getB() }, new Type[] {
											StringType.INSTANCE, StringType.INSTANCE }));
		}
		
		criteria.setCacheable(true);
		
		return criteria;
	}

	@Override
	public void delete(ProcessTask entityToDelete) {
		try {
			Session session = unitOfWork.getCurrentSession();
			session.createSQLQuery("delete from STATUS_METADATA where MD_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS_NOTIFICATION_CHANNELS where SNC_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS set LATEST_STATUS_CHANGE = null where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set NEXT_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("update STATUS_CHANGES set PREVIOUS_STATUS_CHANGE = null where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS_CHANGES where SCH_STATUS_ID in (select distinct STA_ID from STATUS where STA_CONTEXT_ID = :entityToDelete_id)").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from STATUS where STA_CONTEXT_ID = :entityToDelete_id").setParameter("entityToDelete_id", entityToDelete.getId()).executeUpdate();
			session.createSQLQuery("delete from PROCESS_SWIMLANES where PRO_CODE = :entityToDelete_code").setParameter("entityToDelete_code", entityToDelete.getCode()).executeUpdate();
			session.createSQLQuery("delete from PROCESS_TRANSITIONS where FROM_PRO_CODE = :entityToDelete_code or TO_PRO_CODE = :entityToDelete_code").setParameter("entityToDelete_code", entityToDelete.getCode()).executeUpdate();
		} catch (Exception e) {
		}
		super.delete(entityToDelete);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessTask> findForDeployment(Deployment deployment) {
		return this.unitOfWork.getCurrentSession().createCriteria(this.getManagedType()).createCriteria("attachedDeployments").add(Restrictions.eq("id", deployment.getId())).setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTopLevelComponentCodes() {
		return this.unitOfWork.getCurrentSession().createQuery("select p.code from ProcessTask p where p.parentEntity is null").setCacheable(true).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllTopLevelComponentIds() {
		return this.unitOfWork.getCurrentSession().createQuery("select p.id from ProcessTask p where p.parentEntity is null").setCacheable(true).list();
	}

	@Override
	public ProcessTask findByCode(String code) {
		return (ProcessTask) this.unitOfWork.getCurrentSession().createQuery("from ProcessTask as p where p.code = :code").setString("code", code).setCacheable(true).uniqueResult();
	}
	
}