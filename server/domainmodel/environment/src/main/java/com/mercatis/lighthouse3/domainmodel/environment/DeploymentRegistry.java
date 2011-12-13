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
package com.mercatis.lighthouse3.domainmodel.environment;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;

/**
 * The environment registry interface abstractly defines the contract for a
 * persistent repository of environments. It provides basic CRUD functionality.
 * It can be implemented using different persistence technologies.
 * 
 * With regard to persistence, the following rules must apply for
 * implementations:
 * 
 * <ul>
 * <li>deployments are uniquely identified by their <code>id</code>.
 * <li>deployments are uniquely identified by their <code>location</code> and
 * the <code>deployedComponent</code>.
 * <li>deployments are always fully loaded including the deployed component. The
 * rules for loading software components apply here as well.
 * <li>deployments are persisted along with their associated software
 * components.
 * </ul>
 */
public interface DeploymentRegistry extends DomainModelEntityDAO<Deployment> {
	/**
	 * This method creates a deployment of a software component at a given
	 * location.
	 * 
	 * @param component
	 *            the software component to deploy.
	 * @param location
	 *            the location where to deploy the component.
	 * @param description
	 *            an optional textual description of the deployment.
	 * @param contact
	 *            optional textual contact information for the deployment.
	 * @param contactEmail
	 *            an optional email address to use for contacting a person
	 *            responsible for the deployment.
	 * @return the persisted and appropriately initialized deployment.
	 * @throws PersistenceException
	 *             in case the deployment could not be created in the registry.
	 */
	public Deployment deploy(SoftwareComponent component, String location, String description, String contact,
			String contactEmail);

	/**
	 * A synonym for <code>delete()</code>.
	 * 
	 * @see DomainModelEntityDAO#delete()
	 */
	public void undeploy(Deployment deployment);

	/**
	 * This method finds all deployments at a given location.
	 * 
	 * @param location
	 *            the location of interest.
	 * @return The set of deployments at the location of interest.
	 */
	public List<Deployment> findAtLocation(String location);

	/**
	 * This method finds all deployments registered for the given component.
	 * Note that if the component does not have any deployments the deployments
	 * of its parent component (if existent) are returned.
	 * 
	 * @param component
	 *            the component whose deployments are of interest.
	 * @return The set of deployments of the component of interest.
	 */
	public List<Deployment> findByComponent(SoftwareComponent component);

	/**
	 * This method looks up a deployment by deployed component and location.
	 * 
	 * @param component
	 *            the deployed component
	 * @param location
	 *            the location where the component should be deployed
	 * @return the deployment or <code>null</code> if no deployment of the given
	 *         component exists at the given location.
	 */
	public Deployment findByComponentAndLocation(SoftwareComponent component, String location);

	/**
     * This method retrieves a deployment given the code of the deployed
     * software component and the deployment location.
     *
     * @param code
     *            the code of the deployed component
     * @param location
     *            the deployment location
     * @return the retrieved deployment or <code>null</code>, if it could not be
     *         found.
     */
	public Deployment findByComponentCodeAndLocation(String componentCode, String location);
	
	/**
	 * This method returns all locations where software component deployments
	 * exist.
	 * 
	 * @return the set of location names.
	 */
	public List<String> findAllLocations();

}
