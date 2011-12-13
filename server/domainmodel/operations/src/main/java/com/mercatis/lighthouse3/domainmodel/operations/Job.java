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
import java.util.regex.Pattern;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;

/**
 * A job is a scheduled operation call. This is useful to implement regular
 * health checks or other sensors. These can be seen as regular calls of
 * operations that perform the check.
 */
public class Job extends CodedDomainModelEntity {
	private static final long serialVersionUID = -3063122768533226540L;

	/**
	 * This is an optional, human readable name of the job.
	 */
	private String longName = null;

	/**
	 * This method returns an optional, human readable name of the job.
	 * 
	 * @return the name of the job
	 */
	public String getLongName() {
		return this.longName;
	}

	/**
	 * This method can be used to set a new job name.
	 * 
	 * @param name
	 *            the new job name.
	 */
	public void setLongName(String name) {
		this.longName = name;
	}

	/**
	 * This property keeps an optional textual description of the purpose of the
	 * job.
	 */
	private String description = null;

	/**
	 * Call this method to obtain a textual description of the job.
	 * 
	 * @return a description of the present job.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * This method can be used to provide an optional description for the
	 * present job.
	 * 
	 * @param description
	 *            the description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * An optional contact responsible for the job. E.g., the user who created
	 * the job or support.
	 */
	private String contact = null;

	/**
	 * This method returns a contact responsible for the job. E.g., the user who
	 * created the job or support.
	 * 
	 * @return the contact
	 */
	public String getContact() {
		return this.contact;
	}

	/**
	 * This method sets contact information for the job. E.g., the user who
	 * created the job or support.
	 * 
	 * @param contact
	 *            the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * An optional email address for contacting someone responsible.
	 */
	private String contactEmail = null;

	/**
	 * This method obtains an optional email address to contact someone
	 * responsible for the job.
	 * 
	 * @return the contact email.
	 */
	public String getContactEmail() {
		return this.contactEmail;
	}

	/**
	 * This method can be used to set an optional email address for contacting
	 * someone responsible for the job.
	 * 
	 * @param contactEmail
	 *            the contact email.
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * This property keeps a cron expression indicating when the job is supposed
	 * to run.
	 */
	private String scheduleExpression = null;

	/**
	 * This method returns the cron expression indicating when the job is
	 * scheduled to run.
	 * 
	 * @return the cron expression
	 */
	public String getScheduleExpression() {
		return this.scheduleExpression;
	}

	/**
	 * This method sets the cron expression indicating when the job is scheduled
	 * to run.
	 * 
	 * @param scheduleExpression
	 *            the cron expression used as the schedule of the job
	 * @throws ConstraintViolationException
	 *             in case an invalid cron expression was given.
	 */
	public void setScheduleExpression(String scheduleExpression) {
		if (!Pattern
				.matches(
						"^([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)(\\s([^\\s]+))?$",
						scheduleExpression))
			throw new ConstraintViolationException(
					"Invalid schedule expression given, must be valid cron string",
					null);

		this.scheduleExpression = scheduleExpression;
	}

	/**
	 * This property keeps the operation call that is being scheduled by the
	 * present job."
	 */
	private OperationCall scheduledCall = null;

	/**
	 * This method returns the operation call scheduled by the job.
	 * 
	 * @return the scheduled call
	 */
	public OperationCall getScheduledCall() {
		return this.scheduledCall;
	}

	/**
	 * This method set the operation call scheduled by the present job.
	 * 
	 * @param scheduledCall
	 *            the scheduled operation call.
	 */
	public void setScheduledCall(OperationCall scheduledCall) {
		this.scheduledCall = scheduledCall;
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getLongName() != null)
			xml.writeEntityWithText("longName", this.getLongName());

		if (this.getDescription() != null)
			xml.writeEntityWithText("description", this.getDescription());

		if (this.getContact() != null)
			xml.writeEntityWithText("contact", this.getContact());

		if (this.getContactEmail() != null)
			xml.writeEntityWithText("contactEmail", this.getContactEmail());

		if (this.getScheduleExpression() != null)
			xml.writeEntityWithText("scheduleExpression", this
					.getScheduleExpression());

		xml.writeEntity("scheduledCall");

		if (this.getScheduledCall() != null)
			this.getScheduledCall().writeXml(xml);

		xml.endEntity();
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
		this.setScheduleExpression(xmlDocument
				.readValueFromXml("/*/:scheduleExpression"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length < 1)
				|| !resolversForEntityReferences[0].getManagedType().equals(
						SoftwareComponent.class)) {
			throw new XMLSerializationException(
					"XML deserialization of Job requires reference to SoftwareComponentRegistry as 1st resolverForEntityReferences.",
					null);
		}

		if ((resolversForEntityReferences.length < 2)
				|| !resolversForEntityReferences[1].getManagedType().equals(
						Deployment.class)) {
			throw new XMLSerializationException(
					"XML deserialization of Job requires reference to DeploymentRegistry as 2nd resolverForEntityReferences.",
					null);
		}

		if ((resolversForEntityReferences.length < 3)
				|| !resolversForEntityReferences[2].getManagedType().equals(
						OperationInstallation.class)) {
			throw new XMLSerializationException(
					"XML deserialization of Job requires reference to OperationInstallationRegistry as 3rd resolverForEntityReferences.",
					null);
		}

		super.resolveEntityReferencesFromXml(xmlDocument,
				resolversForEntityReferences);

		for (XmlMuncher callFragment : xmlDocument
				.getSubMunchersForContext("/*/:scheduledCall/:OperationCall")) {
			OperationCall scheduledCall = new OperationCall();
			scheduledCall
					.fromXml(
							callFragment,
							(SoftwareComponentRegistry) resolversForEntityReferences[0],
							(DeploymentRegistry) resolversForEntityReferences[1],
							(OperationInstallationRegistry) resolversForEntityReferences[2]);

			this.scheduledCall = scheduledCall;
		}
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> queryParameters = super.toQueryParameters();

		if (this.getLongName() != null)
			queryParameters.put("longName", this.getLongName());

		if (this.getDescription() != null)
			queryParameters.put("description", this.getDescription());

		if (this.getContact() != null)
			queryParameters.put("contact", this.getContact());

		if (this.getContactEmail() != null)
			queryParameters.put("contactEmail", this.getContactEmail());

		if (this.getScheduleExpression() != null)
			queryParameters.put("scheduleExpression", this
					.getScheduleExpression());

		return queryParameters;
	}

	@Override
	public void fromQueryParameters(Map<String, String> queryParameters) {

		this.setLongName(queryParameters.get("longName"));
		this.setDescription(queryParameters.get("description"));
		this.setContact(queryParameters.get("contact"));
		this.setContactEmail(queryParameters.get("contactEmail"));
		this.scheduleExpression = queryParameters.get("scheduleExpression");

		super.fromQueryParameters(queryParameters);
	}
}
