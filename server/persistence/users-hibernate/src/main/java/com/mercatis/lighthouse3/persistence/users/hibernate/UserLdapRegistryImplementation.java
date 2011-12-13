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

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.commons.ldap.CodedDomainModelEntityLdapDAOImplementation;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class provides an user registry implementation. The registry connects to a directory server via LDAP.
 */
public class UserLdapRegistryImplementation extends CodedDomainModelEntityLdapDAOImplementation<User> implements UserRegistry {

    private String surnameAttribute;
    private String givenNameAtribute;
    private String contactEmailAttribute;
    private String passwordAttribute;
    private String userLoginNameAttribute;

    @Override
    public void configure(Properties configuration) {
	givenNameAtribute = configuration.getProperty("givenNameAttribute");
	surnameAttribute = configuration.getProperty("surnameAttribute");
	contactEmailAttribute = configuration.getProperty("contactEmailAttribute");
	passwordAttribute = configuration.getProperty("passwordAttribute");
	userLoginNameAttribute = configuration.getProperty("userLoginNameAttribute");
	super.configure(configuration);
    }

    public List<String> findAllUserCodes() {
	return findAllTopLevelComponentCodes();
    }

    public User authenticate(String userLogin, String password) {
        if (codeAttribute == null || passwordAttribute == null) {
            throw new UnsupportedOperationException("Not supported - user password not mapped to ldap attribute.");
        }

	String userCode = null;
	try {
            String foundDN = "";
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = String.format("%1s=%2s", userLoginNameAttribute == null ? codeAttribute : userLoginNameAttribute, userLogin);
            DirContext ctx = getContext();
            NamingEnumeration<SearchResult> results = ctx.search(searchBase, filter, controls);
            ctx.close();
            if (results.hasMore()) {
		SearchResult result = results.next();
                foundDN = result.getNameInNamespace();
		Attributes attributes = result.getAttributes();
		userCode = (String)attributes.get(codeAttribute).get();
            } else {
                throw new AuthenticationException("Principal " + userLogin + " not found in " + searchBase);
            }
            if (results.hasMore()) {
                throw new AuthenticationException("Principal " + userLogin + " not unique in " + searchBase);
            }

	    Hashtable<String, String> config = new Hashtable<String, String>();
	    config.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    config.put(Context.PROVIDER_URL, this.serverUrl);
	    config.put(Context.SECURITY_AUTHENTICATION, "simple");
	    config.put(Context.SECURITY_PRINCIPAL, foundDN);
	    config.put(Context.SECURITY_CREDENTIALS, password);

	    new InitialDirContext(config).close();
	    return findByCode(userCode);
	}
	catch (AuthenticationException ae) {
	    log.log(Level.ERROR, null,
                    new AuthenticationException("Not authenticated due: " + ae.getMessage()));
            if (internalFallback) {
                Logger.getLogger(UserLdapRegistryImplementation.class.getName()).debug("Trying fallback registry");
                return getDAOProvider().getDAO(UserRegistry.class, true).authenticate(userLogin, password);
            }
	}
	catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
        return null;
    }

    @Override
    protected Attributes getAttributesForEntity(User entityTemplate) {
	Attributes attributes = super.getAttributesForEntity(entityTemplate);
        if (entityTemplate.getGivenName() != null && givenNameAtribute != null) {
            attributes.put(givenNameAtribute, entityTemplate.getGivenName());
        }
        if (entityTemplate.getSurName() != null && surnameAttribute != null) {
            attributes.put(surnameAttribute, entityTemplate.getSurName());
        }
        if (entityTemplate.getContactEmail() != null && contactEmailAttribute != null) {
            attributes.put(contactEmailAttribute, entityTemplate.getContactEmail());
        }
        if (entityTemplate.getPassword() != null && passwordAttribute != null) {
            attributes.put(passwordAttribute, entityTemplate.getPassword());
        }
	return attributes;
    }

    @Override
    protected String getContextForEntity(User entity) {
	return super.getContextForEntity(entity);
    }

    @Override
    protected User getEntityFromAttributes(Attributes attributes, boolean dereferenceEntities) {
        User user = new User();
        try {
            if (idAttribute != null) {
                Attribute attribute = attributes.get(idAttribute);
                if (attribute != null) {
                    user.setId(Long.parseLong((String) attribute.get()));
                }
            }

            if (codeAttribute != null) {
                Attribute attribute = attributes.get(codeAttribute);
                if (attribute != null) {
                    user.setCode((String) attribute.get());
                }
            }

            if (givenNameAtribute != null) {
                Attribute attribute = attributes.get(givenNameAtribute);
                if (attribute != null) {
                    user.setGivenName((String) attribute.get());
                }
            }

            if (surnameAttribute != null) {
                Attribute attribute = attributes.get(surnameAttribute);
                if (attribute != null) {
                    user.setSurName((String) attribute.get());
                }
            }

            if (contactEmailAttribute != null) {
                Attribute attribute = attributes.get(contactEmailAttribute);
                if (attribute != null) {
                    user.setContactEmail((String) attribute.get());
                }
            }

            if (passwordAttribute != null) {
                Attribute attribute = attributes.get(passwordAttribute);
                if (attribute != null) {
                    user.setPassword(new String((byte[]) attribute.get()));
                }
            }
        } catch (NamingException ex) {
            log.log(Level.ERROR, null, ex);
        }
        return user;
    }

    @Override
    protected long createNewIdForEntity(User entity) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("rawtypes")
	@Override
    protected Class<? extends DomainModelEntityDAO> getRegistryInterface() {
        return UserRegistry.class;
    }
}
