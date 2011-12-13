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

import javax.jms.Queue;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.DomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate implementation of the
 * <code>OperationInstallationRegistry</code> interface.
 */
public class OperationInstallationRegistryImplementation extends
		DomainModelEntityDAOImplementation<OperationInstallation> implements OperationInstallationRegistry {

	/**
	 * This property keeps a reference to the JMS connection to use for
	 * operation execution.
	 */
	private JmsConnection operationExecutionConnection = null;

	/**
	 * This method sets the JMS connection to use for operation execution
	 * initiation.
	 * 
	 * @param operationExecutionConnection
	 *            the JMS connection.
	 */
	public void setOperationExecutionConnection(JmsConnection operationExecutionConnection) {
		this.operationExecutionConnection = operationExecutionConnection;
	}

	/**
	 * The JMS queue to publish operation calls on.
	 */
	private Queue operationExecutionQueue = null;

	/**
	 * This method sets the JMS queue to public operation calls on for
	 * execution.
	 * 
	 * @param operationExecutionQueue
	 *            the queue to use.
	 */
	public void setOperationExecutionQueue(Queue operationExecutionQueue) {
		this.operationExecutionQueue = operationExecutionQueue;
	}

	public void execute(OperationCall operationCall) {
		operationCall.execute(this.operationExecutionConnection, this.operationExecutionQueue);
	}

	@Override
	protected Criteria entityToCriteria(Session session, OperationInstallation entityTemplate) {
		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getInstalledOperationCode() != null)
			criteria.add(Restrictions.eq("installedOperationCode", entityTemplate.getInstalledOperationCode()));

		if (entityTemplate.getInstallationLocation() != null)
			criteria.add(Restrictions.eq("installationLocation", entityTemplate.getInstallationLocation()));

		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<OperationInstallation> findAtDeployment(Deployment deployment) {
		return this.unitOfWork.getCurrentSession().createCriteria(OperationInstallation.class).add(
				Restrictions.eq("installationLocation", deployment)).list();
	}

	@Override
	public boolean alreadyPersisted(OperationInstallation entity) {
		return findByDeploymentAndOperation(entity.getInstallationLocation(), entity.getInstalledOperationCode())!=null;
	}
	
	@SuppressWarnings("unchecked")
	public OperationInstallation findByDeploymentAndOperation(Deployment deployment, String operation) {
		List<OperationInstallation> matches = this.unitOfWork.getCurrentSession().createCriteria(
				OperationInstallation.class).add(Restrictions.eq("installationLocation", deployment)).add(
				Restrictions.eq("installedOperationCode", operation)).list();

		return matches.isEmpty() ? null : matches.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<OperationInstallation> findForOperation(String operation) {
		return this.unitOfWork.getCurrentSession().createCriteria(OperationInstallation.class).add(
				Restrictions.eq("installedOperationCode", operation)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(OperationInstallation operationInstallationToDelete) {
		List<Job> assignedJobs = this.unitOfWork.getCurrentSession().createCriteria(Job.class).add(
				Restrictions.eq("scheduledCall.target", operationInstallationToDelete)).list();

		for (Job jobToDelete : assignedJobs) {
			this.unitOfWork.getCurrentSession().delete(jobToDelete);
		}

		super.delete(operationInstallationToDelete);
	}

}
