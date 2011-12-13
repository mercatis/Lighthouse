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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.domainmodel.users.UserRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides a Hibernate implementation of the
 * <code>UserRegistry</code> interface.
 */
public class UserRegistryImplementation extends CodedDomainModelEntityDAOImplementation<User> implements UserRegistry {

	private static final String LDAP_AUTHENTICATION_PROPERTY = "com.mercatis.lighthouse3.service.ldap.authenticate";
	private static final String LDAP_CONFIG_FILE = "com.mercatis.lighthouse3.service.commons.rest.LDAPConfigResource.User";
	private UserRegistry ldapRegistry;

	@Override
	protected Criteria entityToCriteria(Session session, User entityTemplate) {

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getContactEmail() != null) {
			criteria.add(Restrictions.eq("contactEmail", entityTemplate.getContactEmail()));
		}

		if (entityTemplate.getGivenName() != null) {
			criteria.add(Restrictions.eq("givenName", entityTemplate.getGivenName()));
		}

		if (entityTemplate.getSurName() != null) {
			criteria.add(Restrictions.eq("surName", entityTemplate.getSurName()));
		}

		return criteria;
	}

	@Override
	public void delete(User entityToDelete) {
		unitOfWork.getCurrentSession().createSQLQuery("delete from GROUP_USER where USR_CODE = :userCode").setParameter("userCode", entityToDelete.getCode()).executeUpdate();
		unitOfWork.getCurrentSession().createSQLQuery("delete from USER_CODES where USR_CODE = :userCode").setParameter("userCode", entityToDelete.getCode()).executeUpdate();
		unitOfWork.getCurrentSession().createSQLQuery("delete from CONTEXT_ROLE_ASSIGNMENTS where USR_CODE = :userCode").setParameter("userCode", entityToDelete.getCode()).executeUpdate();
		super.delete(entityToDelete);
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllUserCodes() {
		return this.unitOfWork.getCurrentSession().createQuery("select code from User").setCacheable(true).list();
	}

	public User authenticate(String userCode, String password) {
		User user = null;
		if (useLdapAuthentication()) {
			user = authenticateLdap(userCode, password);
			return user;
		} else {
			user = this.findByCode(userCode);
		}
		if (user == null || !user.comparePassword(password)) {
			throw new PersistenceException("User/password combination not found", null);
		}
		return user;
	}

	private boolean useLdapAuthentication() {
		if (this.getInitParams() != null && this.getInitParams().containsKey(LDAP_AUTHENTICATION_PROPERTY)) {
			String propertyValue = this.getInitParams().get(LDAP_AUTHENTICATION_PROPERTY);
			if (propertyValue != null && propertyValue.equals("true")) {
				return true;
			}
		}
		return false;
	}

	private User authenticateLdap(String userCode, String password) {
		return getLdapRegistry().authenticate(userCode, password);
	}

	private UserRegistry getLdapRegistry() {
		if (ldapRegistry == null) {
			ldapRegistry = new UserLdapRegistryImplementation();
			String ldapConfig = getInitParams().get(LDAP_CONFIG_FILE);
			Properties configuration = new Properties();
			try {
				configuration.load(UserRegistryImplementation.class.getResourceAsStream(ldapConfig));
			} catch (IOException ex) {
				Logger.getLogger(UserRegistryImplementation.class.getName()).log(Level.ERROR, null, ex);
			}
			((UserLdapRegistryImplementation) ldapRegistry).configure(configuration);
		}
		return ldapRegistry;
	}
}
