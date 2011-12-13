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
package com.mercatis.lighthouse3.persistence.users.hibernate;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import org.apache.log4j.Level;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.GroupRegistry;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserCodeRegistration;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.commons.ldap.CodedDomainModelEntityLdapDAOImplementation;

/**
 * This class provides a Ldap implementation of the
 * <code>GroupRegistry</code> interface.
 */
public class GroupLdapRegistryImplementation extends CodedDomainModelEntityLdapDAOImplementation<Group> implements GroupRegistry {

    private String memberAttribute;

    private String memberTemplate;

    private String contactAttribute;

    private String contactEmailAttribute;

    private String descriptionAttribute;

    private String longNameAttribute;

    private String userCodeIdentifier;

    @Override
    public void configure(Properties configuration) {
	memberAttribute = configuration.getProperty("memberAttribute");
        memberTemplate = configuration.getProperty("memberTemplate");
	contactAttribute = configuration.getProperty("contactAttribute");
	contactEmailAttribute = configuration.getProperty("contactEmailAttribute");
	descriptionAttribute = configuration.getProperty("descriptionAttribute");
	longNameAttribute = configuration.getProperty("longNameAttribute");
        userCodeIdentifier = configuration.getProperty("userCodeIdentifier");
	super.configure(configuration);
    }

    @Override
    protected Group getEntityFromAttributes(Attributes attributes, boolean dereferenceEntities) {
	Group group = new Group();
	try {
	    if (idAttribute != null) {
		Attribute attribute = attributes.get(idAttribute);
		if (attribute != null) {
		    group.setId(Long.parseLong((String)attribute.get()));
		}
	    }
	    if (codeAttribute != null) {
		Attribute attribute = attributes.get(codeAttribute);
		if (attribute != null) {
		    group.setCode((String)attribute.get());
		}
	    }
	    if (contactAttribute != null) {
		Attribute attribute = attributes.get(contactAttribute);
		if (attribute != null) {
		    group.setContact((String)attribute.get());
		}
	    }
	    if (contactEmailAttribute != null) {
		Attribute attribute = attributes.get(contactEmailAttribute);
		if (attribute != null) {
		    group.setContactEmail((String)attribute.get());
		}
	    }
	    if (descriptionAttribute != null) {
		Attribute attribute = attributes.get(descriptionAttribute);
		if (attribute != null) {
		    group.setDescription((String)attribute.get());
		}
	    }
	    if (longNameAttribute != null) {
		Attribute attribute = attributes.get(longNameAttribute);
		if (attribute != null) {
		    group.setLongName((String)attribute.get());
		}
	    }
	    if (dereferenceEntities && memberAttribute != null) {
		Attribute attribute = attributes.get(memberAttribute);
		if (attribute != null) {
		    NamingEnumeration<?> values = attribute.getAll();
		    while (values.hasMore()) {
			String value = (String)values.next();
			for (String subValue : Pattern.compile(",").split(value)) {
			    if (subValue.startsWith(userCodeIdentifier)) {
				String userCode = subValue.substring(subValue.indexOf("=") + 1);
				User user = getDAOProvider().getDAO(UserRegistry.class).findByCode(userCode);
				if (user != null)
				    group.addMember(user);
			    }
			}
		    }
		}
	    }
	}
	catch (NamingException ne) {
	    log.log(Level.ERROR, null, ne);
	}
	return group;
    }

    @Override
    protected Attributes getAttributesForEntity(Group entityTemplate) {
	Attributes attributes = super.getAttributesForEntity(entityTemplate);
	if (entityTemplate.getContact() != null && contactAttribute != null) {
	    attributes.put(contactAttribute, entityTemplate.getContact());
	}

	if (entityTemplate.getContactEmail() != null && contactEmailAttribute != null) {
	    attributes.put(contactEmailAttribute, entityTemplate.getContactEmail());
	}

	if (entityTemplate.getDescription() != null && descriptionAttribute != null) {
	    attributes.put(descriptionAttribute, entityTemplate.getDescription());
	}

	if (entityTemplate.getLongName() != null && longNameAttribute != null) {
	    attributes.put(longNameAttribute, entityTemplate.getLongName());
	}

	if (memberAttribute != null) {
	    if (entityTemplate.getMembers() != null) {
		Attribute members = new BasicAttribute(memberAttribute);
		for (User member : entityTemplate.getMembers()) {
		    members.add(String.format(memberTemplate, member.getCode()));
		}
		attributes.put(members);
	    }
	}
	return attributes;
    }

    @Override
    protected long createNewIdForEntity(Group entity) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Group> getGroupsForUserCode(String userCode) {
	List<Group> groups = new LinkedList<Group>();
	if (getDAOProvider().getDAO(UserRegistry.class).findByCode(userCode) == null) {
	    return groups;
	}
        User templateUser = new User();
        templateUser.setCode(userCode);
        Group templateGroup = new Group();
        templateGroup.addMember(templateUser);
        groups.addAll(findByTemplate(templateGroup));
        if (groups.isEmpty() && internalFallback) {
            log.debug("Trying fallback registry");
            return getDAOProvider().getDAO(GroupRegistry.class, true).getGroupsForUserCode(userCode);
        }
	return groups;
    }

    public List<String> findAllGroupCodes() {
	return findAllTopLevelComponentCodes();
    }

    @Override
    public void persist(Group entityToPersist) {
        if (entityToPersist.getMembers().isEmpty()) {
            User useless = new User();
            useless.setCode("empty");
            entityToPersist.addMember(useless);
        }
        super.persist(entityToPersist);
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected Class<? extends DomainModelEntityDAO> getRegistryInterface() {
        return GroupRegistry.class;
    }
    
	public UserCodeRegistration getUserCodeRegistration(String userCode) {
		UserCodeRegistration reg = new UserCodeRegistration();
		reg.setUserCode(userCode);
		return reg;
	}
}
