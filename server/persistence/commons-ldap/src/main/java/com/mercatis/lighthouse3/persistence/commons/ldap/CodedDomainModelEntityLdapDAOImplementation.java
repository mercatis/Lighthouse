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
package com.mercatis.lighthouse3.persistence.commons.ldap;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.log4j.Level;

/**
 * This abstract class gathers common functionality of Coded Domain Model Entity
 * DAOs for LDAP.
 */
public abstract class CodedDomainModelEntityLdapDAOImplementation<Entity extends CodedDomainModelEntity> extends DomainModelEntityLdapDAOImplementation<Entity> {

    protected String codeAttribute;

    @SuppressWarnings("unchecked")
	public Entity findByCode(String code) {
        if (codeAttribute == null) {
            throw new UnsupportedOperationException("Not supported - entity code not mapped to ldap attribute.");
        }
	List<Entity> entities = findByContextAndAttribute(searchBase, new BasicAttribute(codeAttribute, code), true);
	if (entities.size() > 1)
	    throw new PersistenceException("Found multiple entities with same code.", new Exception());
	Entity result = entities.size() == 1 ? entities.get(0) : null;
        if (result == null && internalFallback) {
            result = ((CodedDomainModelEntityDAO<Entity>)getDAOProvider().getDAO(getRegistryInterface(), true)).findByCode(code);
        }
        return result;
    }

    public List<String> findAllTopLevelComponentCodes() {
	if (codeAttribute == null) {
            throw new UnsupportedOperationException("Not supported - entity code not mapped to ldap attribute.");
	}
	List<String> codes = new LinkedList<String>();
	try {
            DirContext ctx = getContext();
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    NamingEnumeration<SearchResult> resultEnum = ctx.search(searchBase, null, controls);
            ctx.close();
	    while (resultEnum.hasMore()) {
		SearchResult result = resultEnum.next();
		String code = (String) result.getAttributes().get(codeAttribute).get();
		codes.add(code);
	    }
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
	return codes;
    }

    @Override
    protected Attributes getAttributesForEntity(Entity entityTemplate) {
	Attributes attributes = super.getAttributesForEntity(entityTemplate);
	if (codeAttribute != null) {
	    attributes.put(codeAttribute, entityTemplate.getCode());
	}
	return attributes;
    }

    @Override
    public void configure(Properties configuration) {
	codeAttribute = configuration.getProperty("codeAttribute");
	super.configure(configuration);
    }

    @Override
    protected String getContextForEntity(Entity entity) {
	return codeAttribute + "=" + entity.getCode() + "," + super.getContextForEntity(entity);
    }
}
