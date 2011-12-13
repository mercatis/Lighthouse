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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * This class captures an operation that can be called by lighthouse clients
 * (e.g., in form of executing a manual retry) or used in scheduled jobs (e.g.
 * health checks). The implementations of operations are functional plugins that
 * reside on the server-side. They are realized as OSGi bundles.
 * 
 * Operations have a signature consisting of named parameters which can be
 * passed when calling an operation, see also <code>OperationCall</code>.
 * Operations do not return a result value. Instead, their method of interacting
 * with LH3 is by producing events.
 * 
 * Operations are installed at deployments. Operations can only be started in
 * the context of such a deployment which is also passed to the operation
 * implementation when the operation is executed. This is necessary since in
 * order to be able to produce events the operation implementation needs an
 * event context. One operation can be installed at multiple deployments.
 * 
 * The <code>code</code> property of the operation is a unique name for the
 * operation, by which the correct implementation can be looked up when
 * performing an operation call.
 */
public class Operation extends CodedDomainModelEntity {
	private static final long serialVersionUID = -8682438253296469L;

	/**
	 * The functional category, to which the operation belongs. This can be used
	 * for grouping the list of available operations.
	 */
	private String category = null;

	/**
	 * Returns the functional category, to which the operation belongs. This can
	 * be used for grouping the list of available operations.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return this.category;
	}

	/**
	 * Sets the functional category, to which the operation belongs. This can be
	 * used for grouping the list of available operations.
	 * 
	 * @param category
	 *            the category to set.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * This is an optional, human readable name of the operation.
	 */
	private String longName = null;

	/**
	 * This method returns an optional, human readable name of the operation.
	 * 
	 * @return the name of the operation
	 */
	public String getLongName() {
		return this.longName;
	}

	/**
	 * This method can be used to set a new operation name.
	 * 
	 * @param name
	 *            the new operation name.
	 */
	public void setLongName(String name) {
		this.longName = name;
	}

	/**
	 * This property keeps an optional textual description of the purpose of the
	 * operation.
	 */
	private String description = null;

	/**
	 * Call this method to obtain a textual description of the operation.
	 * 
	 * @return a description of the present operation.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * This method can be used to provide an optional description for the
	 * present operation.
	 * 
	 * @param description
	 *            the description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * An optional contact responsible for the operation. E.g., the user who
	 * created the operation or support.
	 */
	private String contact = null;

	/**
	 * This method returns a contact responsible for the operation. E.g., the
	 * user who created the operation or support.
	 * 
	 * @return the contact
	 */
	public String getContact() {
		return this.contact;
	}

	/**
	 * This method sets contact information for the operation. E.g., the user
	 * who created the operation or support.
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
	 * responsible for the operation.
	 * 
	 * @return the contact email.
	 */
	public String getContactEmail() {
		return this.contactEmail;
	}

	/**
	 * This method can be used to set an optional email address for contacting
	 * someone responsible for the operation.
	 * 
	 * @param contactEmail
	 *            the contact email.
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * This property stores a copyright statement for the operation in question.
	 */
	private String copyright = null;

	/**
	 * Returns a copyright statement for the operation in question.
	 * 
	 * @return the copyright statement
	 */
	public String getCopyright() {
		return copyright;
	}

	/**
	 * Sets the copyright statement for the operation in question.
	 * 
	 * @param copyright
	 *            the copyright statement to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * A version designator string of the operation in question.
	 */
	private String version = null;

	/**
	 * Returns a version designator string of the operation in question.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version designator string of the operation in question
	 * 
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * This is the collection of parameters of the present operation.
	 */
	private List<Parameter> parameters = new LinkedList<Parameter>();

	/**
	 * Returns the input parameters of the given operation.
	 * 
	 * @return the parameters of the operation
	 */
	public List<Parameter> getParameters() {
		return this.parameters;
	}

	/**
	 * This method returns the parameter of the present operation that has a
	 * given name
	 * 
	 * @param name
	 *            the name of the parameter to return
	 * @return the parameter or <code>null</code> in case no parameter with the
	 *         given name can be found.
	 */
	public Parameter getParameter(String name) {
		if (!this.hasParameter(name))
			return null;

		for (Parameter parameter : this.parameters)
			if (parameter.getName().equals(name))
				return parameter;

		return null;
	}

	/**
	 * This method checks whether a parameter with a given name exists.
	 * @deprecated
	 * @param name
	 *            the name of the parameter to check
	 * @return <code>true</code> iff a parameter with the given name is
	 *         associated with the present operation.
	 */
	public boolean hasParameter(String name) {
		for (Parameter parameter : this.parameters) {
			if (parameter.getName().compareTo(name) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This method checks whether a parameter with a given name and variant exists.
	 * 
	 * @param name the name of the parameter to check
	 * @param variant the variant of the parameter to check
	 * @return <code>true</code> if a parameter with the given name and variant is
	 *         associated with the present operation.
	 */
	public boolean hasParameter(String name, String variant) {
		Parameter lookup = new Parameter();
		lookup.setName(name);
		lookup.setVariant(variant);

		return this.parameters.contains(lookup);
	}

	/**
	 * This method adds a parameter to the given operation. If a parameter with
	 * the same name already exists it is replaced.
	 * 
	 * @param parameter
	 *            the parameter to add to the operation.
	 */
	public boolean addParameter(Parameter parameter) {
		if (this.parameters.contains(parameter))
			return false;
		
		return this.parameters.add(parameter);
	}

	/**
	 * This method removes the parameter with the given name from the operation.
	 * 
	 * @param name
	 *            the name of the parameter to remove.
	 */
	public boolean removeParameter(String name) {
		Parameter toBeRemoved = this.getParameter(name);

		if (toBeRemoved != null)
			return this.parameters.remove(toBeRemoved);
		return false;
	}

	/**
	 * This method creates a value for one of the operation's parameter given by
	 * its name.
	 * 
	 * @param name
	 *            the name of the parameter for which to create the value.
	 * @param valueInTypedOrStringRepresentation
	 *            the value in typed or string representation
	 * @return the parameter value
	 * @throws ConstraintViolationException
	 *             in case the passed value is invalid for the parameter or a
	 *             parameter with the given name does not exist.
	 */
	public ParameterValue createParameterValue(String name, Object valueInTypedOrStringRepresentation) {
		if (!this.hasParameter(name))
			throw new ConstraintViolationException("A parameter with name " + name + " does not exist for operation "
					+ this.getCode(), null);

		return this.getParameter(name).createValue(valueInTypedOrStringRepresentation);
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getLongName() != null)
			xml.writeEntityWithText("longName", this.getLongName());

		if (this.getDescription() != null)
			xml.writeEntityWithText("description", this.getDescription());

		if (this.getCategory() != null)
			xml.writeEntityWithText("category", this.getCategory());

		if (this.getContact() != null)
			xml.writeEntityWithText("contact", this.getContact());

		if (this.getContactEmail() != null)
			xml.writeEntityWithText("contactEmail", this.getContactEmail());

		if (this.getCopyright() != null)
			xml.writeEntityWithText("copyright", this.getCopyright());

		if (this.getVersion() != null)
			xml.writeEntityWithText("version", this.getVersion());

		xml.writeEntity("parameters");

		for (Parameter parameter : this.getParameters())
			parameter.writeToXmlWriter(xml);

		xml.endEntity();
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getLongName() != null)
			parameters.put("longName", this.getLongName());

		if (this.getDescription() != null)
			parameters.put("description", this.getDescription());

		if (this.getCategory() != null)
			parameters.put("category", this.getCategory());

		if (this.getContact() != null)
			parameters.put("contact", this.getContact());

		if (this.getContactEmail() != null)
			parameters.put("contactEmail", this.getContactEmail());

		if (this.getCopyright() != null)
			parameters.put("copyright", this.getCopyright());

		if (this.getVersion() != null)
			parameters.put("version", this.getVersion());

		return parameters;
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setCategory(xmlDocument.readValueFromXml("/*/:category"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
		this.setCopyright(xmlDocument.readValueFromXml("/*/:copyright"));
		this.setVersion(xmlDocument.readValueFromXml("/*/:version"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {
		super.resolveEntityReferencesFromXml(xmlDocument, resolversForEntityReferences);

		for (XmlMuncher parameterSection : xmlDocument.getSubMunchersForContext("/*/:parameters/:parameter")) {
			Parameter parameter = new Parameter();
			parameter.fromXml(parameterSection);
			this.addParameter(parameter);
		}
	}

	/**
	 * This method returns true iff the present operation matches a given
	 * template.
	 * 
	 * The template is just another operation object describing the structure of
	 * the operation objects of interest.
	 * 
	 * In general, if a property in the template is set to <code>null</code>, it
	 * is not of interest for the match. If it is not <code>null</code> a
	 * matching event must have the property set to an equal value.
	 * 
	 * @param template
	 *            the operation template to match against
	 * @return <code>true</code> iff the template matches.
	 */
	public boolean matches(Operation template) {		
		if (template.getCode() != null)
			if (!template.getCode().equals(this.getCode()))
				return false;
		
		if (template.getLongName() != null)
			if (!template.getLongName().equals(this.getLongName()))
				return false;
		
		if (template.getCategory() != null)
			if (!template.getCategory().equals(this.getCategory()))
				return false;
		
		if (template.getContact() != null)
			if (!template.getContact().equals(this.getContact()))
				return false;
		
		if (template.getContactEmail() != null)
			if (!template.getContactEmail().equals(this.getContactEmail()))
				return false;		
		
		if (template.getCopyright() != null)
			if (!template.getCopyright().equals(this.getCopyright()))
				return false;		
		
		if (template.getVersion() != null)
			if (!template.getVersion().equals(this.getVersion()))
				return false;
		
		return true;
	}
}
