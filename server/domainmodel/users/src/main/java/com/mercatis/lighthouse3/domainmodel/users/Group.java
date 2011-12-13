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
package com.mercatis.lighthouse3.domainmodel.users;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class represents a group of users and will be used for access control
 * purposes.
 */
public class Group extends CodedDomainModelEntity {

	private static final long serialVersionUID = 1118819211004084506L;

	/**
	 * This set is used to decouple the entity User from Groups for persisting
	 * in Hibernate. A mandatory step if Users are managed in some other space
	 * (like LDAP).
	 */
	private Set<UserCodeRegistration> userCodes = new HashSet<UserCodeRegistration>();

	/**
	 * Private read access only to this list as it is needed for xml transport.
	 * 
	 * @return the codes representing a member of this group
	 */
	public Set<UserCodeRegistration> getUserCodes() {
		return userCodes;
	}

	/**
	 * This property captures a more readable name of the environment in
	 * question compared to the unique code name of the environment.
	 */
	private String longName = null;

	/**
	 * @return the longName
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * @param longName
	 *            the longName to set
	 */
	public void setLongName(String longName) {
		this.longName = longName;
	}

	/**
	 * This property stores contact information for the environment in question
	 * for inquiries.
	 */
	private String contact = null;

	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param contact
	 *            the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * This property stores an email address of a contact that can be used for
	 * inquiries.
	 */
	private String contactEmail;

	/**
	 * @return the contactEmail
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * Stores a brief textual description of the group in question.
	 */
	private String description = null;

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This Set holds the members of this group.
	 */
	private Set<User> members = new HashSet<User>();

	/**
	 * Add an user as a member for this group.
	 * 
	 * @param member
	 *            the user to add as member
	 */
	public void addMember(User member) {
		UserCodeRegistration userCode = new UserCodeRegistration().setUserCode(member.getCode());
		addMember(member, userCode);
	}
	
	/**
	 * Add an user as a member for this group.
	 * 
	 * @param member
	 *            the user to add as member
	 */
	public void addMember(User member, UserCodeRegistration userCode) {
		members.add(member);
		getUserCodes().add(userCode);
	}

	/**
	 * Remove a member from this group.
	 * 
	 * @param member
	 *            to remove from this group
	 */
	public void removeMember(User member) {
		members.remove(member);
		getUserCodes().remove(new UserCodeRegistration().setUserCode(member.getCode()));
	}

	/**
	 * This method delivers the set of users in this group. <br />
	 * Changes in this set will affect the group - it's not a copy.
	 * 
	 * @return a <code>Set<User></code> containing current members of this group
	 */
	public Set<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		getMembers().clear();
		getUserCodes().clear();
		for (User member : members) {
			addMember(member);
		}
	}

	/**
	 * Check if this group contains the given member.
	 * 
	 * @param member
	 * @return true if this group contains the given member.
	 */
	public boolean hasMember(User member) {
		return members.contains(member);
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);
		if (getLongName() != null) {
			xml.writeEntityWithText("longName", getLongName());
		}
		if (getContact() != null) {
			xml.writeEntityWithText("contact", getContact());
		}
		if (getContactEmail() != null) {
			xml.writeEntityWithText("contactEmail", getContactEmail());
		}
		if (getDescription() != null) {
			xml.writeEntityWithText("description", getDescription());
		}
		if (!getUserCodes().isEmpty()) {
			xml.writeEntity("members");
			for (UserCodeRegistration userGroupMapping : getUserCodes()) {
				xml.writeEntityWithText("memberCode", userGroupMapping.getUserCode());
			}
			xml.endEntity();
		}
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);
		setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		setContact(xmlDocument.readValueFromXml("/*/:contact"));
		setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
		setDescription(xmlDocument.readValueFromXml("/*/description"));
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (getLongName() != null) {
			parameters.put("longName", getLongName());
		}
		if (getContact() != null) {
			parameters.put("contact", getContact());
		}
		if (getContactEmail() != null) {
			parameters.put("contactEmail", getContactEmail());
		}
		if (getDescription() != null) {
			parameters.put("description", getDescription());
		}
		return parameters;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument, DomainModelEntityDAO... resolversForEntityReferences) {
		if ((resolversForEntityReferences.length <= 0) || !resolversForEntityReferences[0].getManagedType().equals(Group.class)) {
			throw new XMLSerializationException("XML deserialization of group requires reference to GroupRegistry as 1st resolverForEntityReferences.", null);
		}
		if ((resolversForEntityReferences.length <= 1) || !resolversForEntityReferences[1].getManagedType().equals(User.class)) {
			throw new XMLSerializationException("XML deserialization of group requires reference to UserRegistry as 2nd resolverForEntityReferences.", null);
		}

		getMembers().clear();
		getUserCodes().clear();
		GroupRegistry groupRegistry = (GroupRegistry) resolversForEntityReferences[0];
		UserRegistry userRegistry = (UserRegistry) resolversForEntityReferences[1];
		List<String> memberCodes = xmlDocument.readValuesFromXml("/*/:members/:memberCode");
		for (String memberCode : memberCodes) {
			User user = userRegistry.findByCode(memberCode);
			if (user != null) {
				UserCodeRegistration userCode = groupRegistry.getUserCodeRegistration(user.getCode());
				addMember(user, userCode);
			}
		}
	}
}
