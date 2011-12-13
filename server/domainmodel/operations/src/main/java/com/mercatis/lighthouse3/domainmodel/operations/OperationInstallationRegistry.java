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
package com.mercatis.lighthouse3.domainmodel.operations;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;

/**
 * An interface representing a persistent registry for operation installations.
 * 
 * With regard to persistence, the following rules must apply for
 * implementations:
 * 
 * <ul>
 * <li>operation installations are uniquely identified by their <code>id</code>.
 * <li>operation installations are uniquely identified by the
 * <code>location</code> and the <code>deployedComponent</code> of the
 * deployment they are attached to plus the <code>code</code> of the operation
 * installed.
 * <li>operation installations are always fully loaded including the deployment
 * they are installed to. The deployment's rules for loading software components
 * apply here as well.
 * <li>operation installations are persisted along with their associated
 * deployments.
 * <li>when deleting a deployment, all operation installations for that
 * deployment are deleted as well.
 * </ul>
 */
public interface OperationInstallationRegistry extends DomainModelEntityDAO<OperationInstallation> {

	/**
	 * This method returns the operation installation given a deployment and an
	 * operation code.
	 * 
	 * @param deployment
	 *            the deployment where the operation is supposed to be
	 *            installed.
	 * @param operation
	 *            the code of the installed operation
	 * @return the operation installation or <code>null</code> in case no such
	 *         installation exists.
	 */
	public OperationInstallation findByDeploymentAndOperation(Deployment deployment, String operation);

	/**
	 * This method returns all operation installations at a given deployment.
	 * 
	 * @param deployment
	 *            the deployment of interest.
	 * @return the operation installations at that deployment.
	 */
	public List<OperationInstallation> findAtDeployment(Deployment deployment);

	/**
	 * This method looks up all installations of a given operation
	 * 
	 * @param operation
	 *            the code of the operation of interest
	 * @return the installations of that operation.
	 */
	public List<OperationInstallation> findForOperation(String operation);

	/**
	 * This method initiates the execution of a given operation call. This is
	 * done by publishing the call on a JMS queue on which it will be consumed
	 * by operation executor services.
	 * 
	 * @param operationCall
	 *            the operation call to execute.
	 * @throws OperationCallException
	 *             in case the call could not be executed.
	 */
	public void execute(OperationCall operationCall);

}
