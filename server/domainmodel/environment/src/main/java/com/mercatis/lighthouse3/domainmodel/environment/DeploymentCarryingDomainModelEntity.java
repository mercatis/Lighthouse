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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This base class subsumes common functionality of hierarchical domain model
 * entities that can have deployments attached.
 */
public abstract class DeploymentCarryingDomainModelEntity<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
		extends HierarchicalDomainModelEntity<Entity> implements StatusCarrier {

	private static final long serialVersionUID = -7219087999199656395L;

	public Set<StatusCarrier> getDirectSubCarriers() {
		return new HashSet<StatusCarrier>(this.getDirectSubEntities());
	}

	public Set<StatusCarrier> getSubCarriers() {
		return new HashSet<StatusCarrier>(this.getSubEntities());
	}

	/**
	 * The set of deployments attached to the current entity.
	 */
	private Set<Deployment> attachedDeployments = new HashSet<Deployment>();

	/**
	 * This predicate checks whether the given deployment is attached to the
	 * present entity.
	 * 
	 * @param deployment
	 *            the deployment to check.
	 * @return <code>true</code> iff the deployment is attached to the present
	 *         entity.
	 */
	public boolean hasAttachedDeployment(Deployment deployment) {
		return this.attachedDeployments.contains(deployment);
	}

	/**
	 * This method returns <code>true</code> iff the present entity has a
	 * deployment attached.
	 * 
	 * @return <code>true</code> iff the present entity has a deployment
	 *         attached
	 */
	public boolean hasDeployments() {
		return !this.attachedDeployments.isEmpty();
	}

	/**
	 * This method adds another deployment to the present entity. These are
	 * upward inheritable through the entity hierarchy. An deployment can only
	 * be added once to the entity.
	 * 
	 * @param deployment
	 *            the deployment to add
	 */
	public void attachDeployment(Deployment deployment) {
		if (!this.hasAttachedDeployment(deployment))
			this.attachedDeployments.add(deployment);
	}

	/**
	 * This method removes an attached deployment from the present entity. In
	 * case that the deployment is not attached nothing happens.
	 * 
	 * @param deployment
	 *            the deployment to unattach.
	 */
	public void detachDeployment(Deployment deployment) {
		if (this.hasAttachedDeployment(deployment))
			this.attachedDeployments.remove(deployment);
	}

	/**
	 * This method detaches all deployments
	 */
	public void detachDeployments() {
		for (Deployment deploymentToDetach : new HashSet<Deployment>(
				this.attachedDeployments)) {
			this.detachDeployment(deploymentToDetach);
		}
	}

	/**
	 * This method returns all deployments attached to the present entity.
	 * 
	 * @return the attached deployments.
	 */
	public Set<Deployment> getDeployments() {
		return this.attachedDeployments;
	}

	/**
	 * This method returns all deployment either attached to the present entity
	 * or one of its subentity.
	 * 
	 * @return the set of all deployments.
	 */
	@SuppressWarnings("unchecked")
	public Set<Deployment> getAllDeployments() {
		return (Set<Deployment>) this.getProperties(Deployment.class);
	}

	public Set<Deployment> getAssociatedDeployments() {
		return this.getAllDeployments();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Set getUpwardInheritableProperties() {
		return this.getLocalProperties();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set getLocalProperties() {
		Set result = super.getLocalProperties();

		result.addAll(this.attachedDeployments);

		return result;
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (!this.getDeployments().isEmpty()) {
			xml.writeEntity("attachedDeployments");
			for (Deployment attachedDeployment : this.getDeployments()) {
				attachedDeployment.writeEntityReference("attachedDeployment",
						xml);
			}
			xml.endEntity();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length <= 1)
				|| !resolversForEntityReferences[1].getManagedType().equals(
						Deployment.class)) {
			throw new XMLSerializationException(
					"XML deserialization of deployment carrying entity requires reference to DeploymentRegistry as 2nd resolverForEntityReferences.",
					null);
		}

		super.resolveEntityReferencesFromXml(xmlDocument,
				resolversForEntityReferences);

		DeploymentRegistry deploymentRegistry = (DeploymentRegistry) resolversForEntityReferences[1];

		List<String> deployedComponentCodes = xmlDocument
				.readValuesFromXml("/*/:attachedDeployments/:attachedDeployment/:deployedComponentCode");
		List<String> deploymentLocations = xmlDocument
				.readValuesFromXml("/*/:attachedDeployments/:attachedDeployment/:deploymentLocation");

		this.detachDeployments();

		for (int i = 0; i < deployedComponentCodes.size(); i++) {
			String deployedComponentCode = deployedComponentCodes.get(i);
			String deploymentLocation = deploymentLocations.get(i);

			Deployment deployment = deploymentRegistry.findByComponentCodeAndLocation(deployedComponentCode, deploymentLocation);

			if (deployment == null)
				throw new XMLSerializationException(
						"XML deserialization of deployment carrying entity references deployment of software component with unknown code:" + deployedComponentCode + " at location: " + deploymentLocation,
						null);

			this.attachDeployment(deployment);
		}
	}
}
