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

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.operations.rest.JobRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerJobRegistryImplementation extends EagerCodedDomainModelEntityDAOImplementation<Job>
	implements JobRegistry {

	private JobRegistryImplementation delegateRegistry = null;
	
	public EagerJobRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry, DeploymentRegistry deploymentRegistry, OperationInstallationRegistry operationInstallationRegistry, String user, String password) {
		this.delegateRegistry = new JobRegistryImplementation(serverUrl, softwareComponentRegistry, deploymentRegistry, operationInstallationRegistry, user, password);
		this.invalidate();
	}
	
	public EagerJobRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry, DeploymentRegistry deploymentRegistry, OperationInstallationRegistry operationInstallationRegistry) {
		this(serverUrl, softwareComponentRegistry, deploymentRegistry, operationInstallationRegistry, null, null);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Job> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return Job.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.JobRegistry#findAtDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public List<Job> findAtDeployment(Deployment deployment) {
		ArrayList<Job> jobs = new ArrayList<Job>();
		for (Job job : cache.values()) {
			if (job.getScheduledCall().getTarget().getInstallationLocation().equals(deployment)) {
				jobs.add(job);
			}
		}
		
		return jobs;
	}
}
