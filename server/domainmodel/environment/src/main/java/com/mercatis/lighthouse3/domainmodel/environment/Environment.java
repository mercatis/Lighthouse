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
import java.util.Map;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class captures environments into which software components are deployed.
 * Environments may be both technical (e.g., a specific server) or
 * logical/organizational (e.g., a company's integration test environment) in
 * kind.
 * 
 * Environments may be associated with sets of deployments. These sets are
 * inherited upwards the environment hierarchy.
 */
public class Environment extends DeploymentCarryingDomainModelEntity<Environment> {

	private static final long serialVersionUID = 1108656607076905646L;

	/**
	 * This property captures a more readable name of the environment in
	 * question compared to the unique code name of the environment.
	 */
	private String longName = null;

	/**
	 * Returns a more readable name of the environment in question compared to
	 * the unique code name of the environment.
	 * 
	 * @return the long name
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * Sets a more readable name of the environment in question compared to the
	 * unique code name of the environment.
	 * 
	 * @param longName
	 *            the long name to set
	 */
	public void setLongName(String longName) {
		this.longName = longName;
	}

	/**
	 * Stores a brief textual description of the environment in question.
	 */
	private String description = null;

	/**
	 * Returns a brief textual description of the environment in question.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a brief textual description of the environment in question.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This property stores contact information for the environment in question
	 * for inquiries.
	 */
	private String contact = null;

	/**
	 * Returns contact information for the environment in question for
	 * inquiries.
	 * 
	 * @return the contact information
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * Sets the contact information for the environment in question for
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
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getLongName() != null) {
			xml.writeEntityWithText("longName", this.getLongName());
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

		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length == 0)
				|| !resolversForEntityReferences[0].getManagedType().equals(Environment.class)) {
			throw new XMLSerializationException(
					"XML deserialization of environment requires reference to EnvironmentRegistry as 1st resolverForEntityReferences.",
					null);
		}

		super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getLongName() != null) {
			parameters.put("longName", this.getLongName());
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

}
