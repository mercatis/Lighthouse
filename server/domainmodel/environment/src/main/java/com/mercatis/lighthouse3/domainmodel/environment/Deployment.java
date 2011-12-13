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
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class captures deployments of software components. Each deployment is
 * uniquely identified by the deployed component plus its location description.
 * 
 * A deployment is a possible status carrier.
 */
public class Deployment extends DomainModelEntity implements StatusCarrier {

	private static final long serialVersionUID = 1263096752235955320L;

	public Set<StatusCarrier> getDirectSubCarriers() {
		return new HashSet<StatusCarrier>();
	}

	public Set<StatusCarrier> getSubCarriers() {
		return new HashSet<StatusCarrier>();
	}

	public Set<Deployment> getAssociatedDeployments() {
		Set<Deployment> associatedDeployments = new HashSet<Deployment>();

		associatedDeployments.add(this);

		return associatedDeployments;
	}

	/**
	 * The software component deployed.
	 */
	private SoftwareComponent deployedComponent = null;

	/**
	 * Returns the software component deployed.
	 * 
	 * @return the software component deployed.
	 */
	public SoftwareComponent getDeployedComponent() {
		return this.deployedComponent;
	}

	/**
	 * Set the software component deployed.
	 * 
	 * @param deployedComponent
	 *            the component deployed.
	 */
	public void setDeployedComponent(SoftwareComponent deployedComponent) {
		this.deployedComponent = deployedComponent;
	}

	/**
	 * A description of the location where the component is deployed, e.g., the
	 * IP address of the machine. Should not be null as this is used for the
	 * identification of deployments together with the deployed component
	 * itself.
	 */
	private String location = null;

	/**
	 * Returns the deployment location description, e.g., the IP address of the
	 * deployment machine.
	 * 
	 * @return The deployment location
	 */
	public String getLocation() {
		return this.location;
	}

	/**
	 * Sets the deployment location description, e.g., the IP address of the
	 * deployment machine.
	 * 
	 * @param location
	 *            The deployment location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Stores a brief textual description or remarks on the deployment in
	 * question.
	 */
	private String description = null;

	/**
	 * Returns a brief textual description or remarks on the deployment in
	 * question.
	 * 
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a brief textual description or remarks on the deployment in
	 * question.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This property stores contact information for the deployment in question
	 * for inquiries.
	 */
	private String contact = null;

	/**
	 * Returns contact information for the deployment in question for inquiries.
	 * 
	 * @return the contact information
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * Sets the contact information for the deployment in question for
	 * inquiries.
	 * 
	 * @param contact
	 *            the contact information to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * This property stores an email address of a contact that can be used for
	 * inquiries.
	 */
	private String contactEmail = null;

	/**
	 * Returns an email address of a contact that can be used for inquiries.
	 * 
	 * @return the contact email
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * Sets the email address of a contact that can be used for inquiries.
	 * 
	 * @param contactEmail
	 *            the contact email to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@Override
	public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
		if (this.getLocation() != null && this.getDeployedComponent() != null
				&& this.getDeployedComponent().getCode() != null) {
			xml.writeEntity(referenceTagName);
			if (this.getDeployedComponent() != null && this.getDeployedComponent().getCode() != null)
				xml.writeEntityWithText("deployedComponentCode", this.getDeployedComponent().getCode());
			if (this.getLocation() != null)
				xml.writeEntityWithText("deploymentLocation", this.getLocation());
			xml.endEntity();
		}
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getDeployedComponent() != null) {
			this.getDeployedComponent().writeEntityReference("deployedComponent", xml);
		}

		if (this.getLocation() != null) {
			xml.writeEntityWithText("location", this.getLocation());
		}

		if (this.getDescription() != null) {
			xml.writeEntityWithText("description", this.getDescription());
		}

		if (this.getContact() != null) {
			xml.writeEntityWithText("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			xml.writeEntityWithText("contactEmail", this.getContactEmail());
		}
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.setLocation(xmlDocument.readValueFromXml("/*/:location"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length == 0)
				|| !resolversForEntityReferences[0].getManagedType().equals(SoftwareComponent.class)) {
			throw new XMLSerializationException(
					"XML deserialization of deployment requires reference to SoftwareComponentRegistry as 1st resolverForEntityReferences.",
					null);
		}

		super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);

		SoftwareComponentRegistry softwareComponentRegistry = (SoftwareComponentRegistry) resolversForEntityReferences[0];

		String deployedComponentCode = xmlDocument.readValueFromXml("/*/:deployedComponent/:code");

		if (deployedComponentCode != null) {
			SoftwareComponent deployedComponent = softwareComponentRegistry.findByCode(deployedComponentCode);

			if (deployedComponent == null)
				throw new XMLSerializationException("XML serialization of deployment references software component with unknown code:" + deployedComponentCode, null);

			this.setDeployedComponent(deployedComponent);
		}
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getDeployedComponent() != null) {
			parameters.put("code", this.getDeployedComponent().getCode());
		}

		if (this.getLocation() != null) {
			parameters.put("location", this.getLocation());
		}

		if (this.getDescription() != null) {
			parameters.put("description", this.getDescription());
		}

		if (this.getContact() != null) {
			parameters.put("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			parameters.put("contactEmail", this.getContactEmail());
		}

		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!this.getClass().isInstance(obj))
			return false;

		if (this.getLocation() == null)
			return false;

		if (this.getDeployedComponent() == null)
			return false;

		Deployment that = (Deployment) obj;

		return this.getLocation().equals(that.getLocation())
				&& (this.getDeployedComponent().equals(that.getDeployedComponent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((this.getLocation() == null) ? 0 : this.getLocation().hashCode());
		result = prime * result + ((this.getDeployedComponent() == null) ? 0 : this.getDeployedComponent().hashCode());

		return result;
	}

	/**
	 * This method returns a path identifier for the given status. Its URL-like
	 * form is constructed like this:
	 * deployment://deploymentLocation/deployedComponentCode
	 * 
	 * @return the path for the deployment.
	 */
	@Override
	public String getPath() {
		return this.getClass().getSimpleName().toLowerCase() + "://" + this.getLocation() + "/"
				+ this.getDeployedComponent().getCode();
	}
}
