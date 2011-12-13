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
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.operations.rest.OperationInstallationRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerOperationInstallationRegistryImplementation extends EagerDomainModelEntityDAOImplementation<OperationInstallation>
	implements OperationInstallationRegistry {

	private OperationInstallationRegistryImplementation delegateRegistry = null;
	
	public EagerOperationInstallationRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, String user, String password) {
		this.delegateRegistry = new OperationInstallationRegistryImplementation(serverUrl, deploymentRegistry, user, password);
		this.invalidate();
	}
	
	public EagerOperationInstallationRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry) {
		this(serverUrl, deploymentRegistry, null, null);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<OperationInstallation> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#keyForEntity(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	@Override
	protected String keyForEntity(OperationInstallation entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.getInstalledOperationCode());
		builder.append("#");
		builder.append(entity.getInstallationLocation().getDeployedComponent().getCode());
		builder.append("@");
		builder.append(entity.getInstallationLocation().getLocation());
		
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return OperationInstallation.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry#execute(com.mercatis.lighthouse3.domainmodel.operations.OperationCall)
	 */
	public void execute(OperationCall operationCall) {
		delegateRegistry.execute(operationCall);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry#findAtDeployment(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public List<OperationInstallation> findAtDeployment(Deployment deployment) {
		ArrayList<OperationInstallation> installations = new ArrayList<OperationInstallation>();
		for (OperationInstallation operationInstallation : cache.values()) {
			if (operationInstallation.getInstallationLocation().equals(deployment)) {
				installations.add(operationInstallation);
			}
		}
		
		return installations;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry#findByDeploymentAndOperation(com.mercatis.lighthouse3.domainmodel.environment.Deployment, java.lang.String)
	 */
	public OperationInstallation findByDeploymentAndOperation(Deployment deployment, String code) {
		for (OperationInstallation installation : cache.values()) {
			if (installation.getInstalledOperationCode().equals(code) && installation.getInstallationLocation().equals(deployment)) {
				return installation;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry#findForOperation(java.lang.String)
	 */
	public List<OperationInstallation> findForOperation(String code) {
		ArrayList<OperationInstallation> installations = new ArrayList<OperationInstallation>();
		for (OperationInstallation operationInstallation : cache.values()) {
			if (operationInstallation.getInstalledOperationCode().equals(code)) {
				installations.add(operationInstallation);
			}
		}
		
		return installations;
	}

}
