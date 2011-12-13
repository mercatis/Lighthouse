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

import java.io.IOException;
import java.util.Map;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;

/**
 * Each operation can only be executed within the context of a deployment. An
 * operation installation captures the installation of an operation into this
 * context.
 */
public class OperationInstallation extends DomainModelEntity {
	private static final long serialVersionUID = -2759550759155001990L;

	/**
	 * The code of the operation being installed at a deployment
	 */
	private String installedOperationCode = null;

	/**
	 * Returns the operation installed at a deployment.
	 * 
	 * @return the operation code
	 */
	public String getInstalledOperationCode() {
		return this.installedOperationCode;
	}

	/**
	 * This method sets the code of the operation installed at a deployment
	 * 
	 * @param operationCode
	 *            the code of the operation
	 */
	public void setInstalledOperationCode(String operationCode) {
		this.installedOperationCode = operationCode;
	}

	/**
	 * Sets the operation installed at a deployment.
	 * 
	 * @param operation
	 *            the code of the operation
	 */
	public void setInstalledOperation(Operation operation) {
		this.setInstalledOperationCode(operation.getCode());
	}

	/**
	 * The deployment forming the location where the operation installed.
	 */
	private Deployment installationLocation = null;

	/**
	 * Returns the deployment forming the location where the operation
	 * installed.
	 * 
	 * @return the deployment where the operation is installed
	 */
	public Deployment getInstallationLocation() {
		return this.installationLocation;
	}

	/**
	 * Sets the deployment forming the location where the operation installed.
	 * 
	 * @param deployment
	 *            the deployment where the operation is installed
	 */
	public void setInstallationLocation(Deployment deployment) {
		this.installationLocation = deployment;
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getInstalledOperationCode() != null)
			xml.writeEntityWithText("installedOperationCode", this.getInstalledOperationCode());

		if (this.getInstallationLocation() != null)
			this.getInstallationLocation().writeEntityReference("installationLocation", xml);
	}

	@Override
	public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
		xml.writeEntity(referenceTagName);
		if (this.getInstalledOperationCode() != null)
			xml.writeEntityWithText("installedOperationCode", this.getInstalledOperationCode());

		if (this.getInstallationLocation() != null)
			this.getInstallationLocation().writeEntityReference("installationLocation", xml);
		xml.endEntity();
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.installedOperationCode = xmlDocument.readValueFromXml("/*/:installedOperationCode");
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length <= 0)
				|| !resolversForEntityReferences[0].getManagedType().equals(Deployment.class))
			throw new XMLSerializationException(
					"XML deserialization of operation installations requires reference to DeploymentRegistry as 1st resolverForEntityReferences.",
					null);

		super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);

		DeploymentRegistry deploymentRegistry = (DeploymentRegistry) resolversForEntityReferences[0];

		String deployedComponentCode = xmlDocument.readValueFromXml("/*/:installationLocation/:deployedComponentCode");
		String deploymentLocation = xmlDocument.readValueFromXml("/*/:installationLocation/:deploymentLocation");

		if (deployedComponentCode == null || deploymentLocation == null)
			throw new XMLSerializationException(
					"XML deserialization of operation installation has incomplete deployment reference.", null);

		Deployment deployment = deploymentRegistry.findByComponentCodeAndLocation(deployedComponentCode, deploymentLocation);
		this.setInstallationLocation(deployment);

		if (this.getInstallationLocation() == null)
			throw new XMLSerializationException(
					"XML deserialization of operation references deployment of software component with unknown code.",
					null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((installationLocation == null) ? 0 : installationLocation.hashCode());
		result = prime * result + ((installedOperationCode == null) ? 0 : installedOperationCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationInstallation other = (OperationInstallation) obj;
		if (installationLocation == null) {
			if (other.installationLocation != null)
				return false;
		} else if (!installationLocation.equals(other.installationLocation))
			return false;
		if (installedOperationCode == null) {
			if (other.installedOperationCode != null)
				return false;
		} else if (!installedOperationCode.equals(other.installedOperationCode))
			return false;
		return true;
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> queryParameters = super.toQueryParameters();

		if (this.getInstallationLocation() != null) {
			Deployment location = this.getInstallationLocation();

			if (location.getLocation() != null)
				queryParameters.put("deploymentLocation", location.getLocation());

			if (location.getDeployedComponent() != null && location.getDeployedComponent().getCode() != null)
				queryParameters.put("deployedComponentCode", this.getInstallationLocation().getDeployedComponent()
						.getCode());
		}
		if (this.getInstalledOperationCode() != null)
			queryParameters.put("installedOperationCode", this.getInstalledOperationCode());

		return queryParameters;
	}

	@Override
	public void fromQueryParameters(Map<String, String> queryParameters) {
		super.fromQueryParameters(queryParameters);

		if (queryParameters.containsKey("installedOperationCode"))
			this.setInstalledOperationCode(queryParameters.get("installedOperationCode"));
		
	}

}
