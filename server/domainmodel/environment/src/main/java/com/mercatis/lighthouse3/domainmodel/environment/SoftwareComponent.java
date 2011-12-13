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
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * 
 * This class captures software components that are deployed in operation
 * environments and monitored by Lighthouse.
 * 
 * Software components are identified by the a unique code string and can be
 * hierarchically structured to facilitate the modeling of child components.
 */
public class SoftwareComponent extends HierarchicalDomainModelEntity<SoftwareComponent> {

	private static final long serialVersionUID = 7337587843937728961L;

	/**
	 * This property keeps a version designator string of the software component
	 * in question.
	 */
	private String version = null;

	/**
	 * Returns a version designator string of the software component in
	 * question.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version designator string of the software component in question
	 * 
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * This property captures a more readable name of the software component in
	 * question compared to code name of the component.
	 */
	private String longName = null;

	/**
	 * Returns a more readable name of the software component in question
	 * compared to code name of the component.
	 * 
	 * @return the long name
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * Sets a more readable name of the software component in question.
	 * 
	 * @param longName
	 *            the long name to set
	 */
	public void setLongName(String longName) {
		this.longName = longName;
	}

	/**
	 * Stores a brief textual description of the software component in question.
	 */
	private String description = null;

	/**
	 * Returns a brief textual description of the software component in
	 * question.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a brief textual description of the software component in question.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This property stores a copyright statement for the software component in
	 * question.
	 */
	private String copyright = null;

	/**
	 * Returns a copyright statement for the software component in question.
	 * 
	 * @return the copyright statement
	 */
	public String getCopyright() {
		return copyright;
	}

	/**
	 * Sets the copyright statement for the software component in question.
	 * 
	 * @param copyright
	 *            the copyright statement to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * This property stores contact information for the software component in
	 * question for inquiries.
	 */
	private String contact = null;

	/**
	 * Returns contact information for the software component in question for
	 * inquiries.
	 * 
	 * @return the contact information
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * Sets the contact information for the software component in question for
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

		if (this.getVersion() != null) {
			xml.writeEntityWithText("version", this.getVersion());
		}

		if (this.getLongName() != null) {
			xml.writeEntityWithText("longName", this.getLongName());
		}

		if (this.getDescription() != null) {
			xml.writeEntityWithText("description", this.getDescription());
		}

		if (this.getCopyright() != null) {
			xml.writeEntityWithText("copyright", this.getCopyright());
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

		this.setVersion(xmlDocument.readValueFromXml("/*/:version"));
		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setCopyright(xmlDocument.readValueFromXml("/*/:copyright"));
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
					"XML deserialization of software components requires reference to SoftwareComponentRegistry as 1st resolverForEntityReferences.",
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

		if (this.getVersion() != null) {
			parameters.put("version", this.getVersion());
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

		if (this.getCopyright() != null) {
			parameters.put("copyright", this.getCopyright());
		}

		return parameters;
	}

	@Override
	public String toString() {
		return "SoftwareComponent [version=" + version + ", longName="
				+ longName + ", description=" + description + ", copyright="
				+ copyright + ", contact=" + contact + ", contactEmail="
				+ contactEmail + ", getCode()=" + getCode() + ", getId()="
				+ getId() + "]";
	}
	
	

}
