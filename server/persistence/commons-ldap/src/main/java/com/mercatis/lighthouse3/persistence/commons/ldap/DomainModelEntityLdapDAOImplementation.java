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

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.persistence.commons.AbstractDomainModelEntityDAO;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class provides an abstract implementation of common methods for Domain
 * Model Entity DAOs for LDAP.
 */
public abstract class DomainModelEntityLdapDAOImplementation<Entity extends DomainModelEntity> extends AbstractDomainModelEntityDAO {

    /**
     * The logger for this class
     */
    protected Logger log = Logger.getLogger(DomainModelEntityLdapDAOImplementation.class.getName());

    /**
     * The LDAP directory server url
     */
    protected String serverUrl;

    /**
     * True if this DAO should not write any data to the directory
     */
    protected boolean readonly;

    /**
     * The location of the entities for this DAO
     */
    protected String searchBase;

    /**
     * The id set in the DomainModelEntity is mapped to this LDAP attribute
     */
    protected String idAttribute;

    /**
     * A flag that indicates to use the internal registries if LDAP lookup fails.
     */
    protected boolean internalFallback;

    /**
     * Flag to indicate the usage of a JNDI cache
     */
    private boolean cachUse;

    /**
     * JNDI cache expiry time in milliseconds
     */
    private long cacheExpiry;

    /**
     * The JNDI properties to create a new DirContext
     */
    private Properties config;

    /**
     * The shared cache to be used at the different CachingDirContexts
     */
    private JndiCache cache;

    /**
     * LDAP filter string - hopefully with a placeholder (%s)
     */
    private String filterString;

    /**
     * This method sets some mandatory fields used for creating {@link DirContext}. You may override, but must supercall it.
     *
     * @param configuration
     */
    public void configure(Properties configuration) {
	serverUrl = configuration.getProperty("serverUrl");
	searchBase = configuration.getProperty("searchBase");
	readonly = configuration.getProperty("readonly") != null ? configuration.getProperty("readonly").equals("true") : true;
	idAttribute = configuration.getProperty("idAttribute");
	internalFallback = configuration.getProperty("internalFallback") != null ? configuration.getProperty("internalFallback").equals("true") : false;
        cachUse = configuration.getProperty("cacheUse") != null ? configuration.getProperty("cacheUse").equals("true") : true;
        cacheExpiry = configuration.getProperty("cacheExpiry") != null ? Long.parseLong(configuration.getProperty("cacheExpiry")) * 60000 : 7200000;
        filterString = configuration.getProperty("filterString", "(&%s)");
        cache = new JndiCache(cacheExpiry);

        String technicalUser = configuration.getProperty("technicalUserDN");
	config = new Properties();
	config.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	config.put(Context.PROVIDER_URL, this.serverUrl);
        config.put("com.sun.jndi.ldap.connect.pool", "true");
        if (technicalUser != null) {
            String technicalUserPassword = configuration.getProperty("technicalUserPassword");
	    config.put(Context.SECURITY_AUTHENTICATION, "simple");
	    config.put(Context.SECURITY_PRINCIPAL, technicalUser);
	    config.put(Context.SECURITY_CREDENTIALS, technicalUserPassword);
        }
    }

    protected DirContext getContext() {
        DirContext ctx = null;
	try {
            if (cachUse) {
                ctx = new CachingDirContext(cache, config);
            } else {
                ctx = new InitialLdapContext(config, null);
            }
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
        return ctx;
    }

    /**
     * This method parses an attribute string to {@link Attributes}.
     * <ul>
     * <li>Attributes are separated by ";"</li>
     * <li>Attribute names and their values are separeted by ":"</li>
     * <li>Multiple values are separated by ","</li>
     * </ul>
     * Example: <code>attr1:val11,val12;attr2:val21;attr3:val31,val32,val33</code>
     *
     * @param attributeString
     * @return the parsed attributes
     */
    protected Attributes getAttributeFromString(String attributeString) {
	Attributes attributes = new BasicAttributes();
	if (attributeString != null) {
	    for (String staticAttribute : Pattern.compile(";").split(attributeString)) {
		String attributeName = staticAttribute.substring(0, staticAttribute.indexOf(":"));
		Attribute attribute = new BasicAttribute(attributeName);
		String attributeValues = staticAttribute.substring(staticAttribute.indexOf(":") + 1);
		Pattern p = Pattern.compile(",");
		for (String value : p.split(attributeValues)) {
		    attribute.add(value);
		}
		attributes.put(attribute);
	    }
	}
	return attributes;
    }
    
    /**
     * Keeps a handle to the real class used with the <CODE>Entity</CODE> type
     * variable.
     */
    @SuppressWarnings("rawtypes")
    private Class entityType;

    @SuppressWarnings("rawtypes")
    public Class getManagedType() {
	return entityType;
    }

    /**
     * Returns the class name of the entity type.
     *
     * @return The class name.
     */
    protected String getEntityTypeName() {
	return entityType.getName();
    }

    /**
     * Generic constructor, necessary for setting the <CODE>entityType</CODE>
     * property at runtime.
     */
    @SuppressWarnings("rawtypes")
	public DomainModelEntityLdapDAOImplementation() {
	try {
	    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
	    Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
	    this.entityType = (Class) actualTypeArguments[0];
	} catch (Throwable t) {
	    this.entityType = DomainModelEntity.class;
	}
    }

    /**
     * Must be overridden by subclasses so that a partially filled entity
     * template is converted into an equivalent bunch of JNDI attributes.
     *
     * @param entityTemplate
     *            the partially filled entity
     * @return the JNDI attributes
     */
    protected Attributes getAttributesForEntity(Entity entityTemplate) {
	Attributes attributes = new BasicAttributes();
	if (idAttribute != null) {
	    attributes.put(idAttribute, entityTemplate.getId());
	}
	return attributes;
    }

    /**
     * Looks up a list of entities in the given context and attribute.
     *
     * @param context
     * @param attribute
     * @param dereferenceEntities If true, this method may load entities from foreign registries
     * @return matching entities
     */
    protected List<Entity> findByContextAndAttribute(String context, Attribute attribute, boolean dereferenceEntities) {
	Attributes attributes = new BasicAttributes();
	attributes.put(attribute);
	return findByContextAndAttributes(context, attributes, dereferenceEntities);
    }

    /**
     * Looks up a list of entities in the given context and attributes.
     *
     * @param context
     * @param attributes
     * @param dereferenceEntities If true, this method may load entities from foreign registries
     * @return matching entities
     */
    protected List<Entity> findByContextAndAttributes(String context, Attributes attributes, boolean dereferenceEntities) {
	List<Entity> entities = new LinkedList<Entity>();
	try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = getFilterFromAttributes(attributes);
            log.debug(String.format("context: %s - filter: %s", context, filter));
            DirContext ctx = getContext();
	    NamingEnumeration<SearchResult> searchResult = ctx.search(context, filter, controls);
            ctx.close();
	    while (searchResult.hasMore()) {
		Entity entity = getEntityFromAttributes(searchResult.next().getAttributes(), dereferenceEntities);
		entities.add(entity);
	    }
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
	return entities;
    }

    private String getFilterFromAttributes(Attributes attributes) {
        String filter = "(&";
        try {
            if (attributes != null) {
                NamingEnumeration<? extends Attribute> attributesEnum = attributes.getAll();
                while (attributesEnum.hasMore()) {
                    Attribute attribute = attributesEnum.next();
                    filter = filter.concat(convertAttributeToFilterString(attribute));
                }
            } else {
                filter = "";
            }
        } catch (NamingException ex) {
            log.log(Level.ERROR, null, ex);
        }
        filter = filter.concat(")");
        return String.format(filterString, filter);
    }

    /**
     * Converts an attribute to an ldap filter ready string
     *
     * @param attribute the attribute to convert
     * @param mustHave true if entity must contain this value, false if it must not contain this value
     * @return
     */
    @SuppressWarnings("unchecked")
	private String convertAttributeToFilterString(Attribute attribute) {
        String attributeFilterString = "";
        try {
            NamingEnumeration<String> values = (NamingEnumeration<String>) attribute.getAll();
            String valueString = "";
            while (values.hasMore()) {
                String value = values.next();
                if (value == null)
                    value = "*";
                if (valueString.length() > 0) {
                    valueString = valueString.concat(",");
                }
                valueString = valueString.concat(value);
            }
            if (valueString.length() == 0) {
                valueString = "*";
            }
            attributeFilterString = String.format("(%s=%s)", attribute.getID(), valueString);
        } catch (NamingException e) {
            log.log(Level.ERROR, null, e);
        } catch (NoSuchElementException e) {
            log.log(Level.DEBUG, "Skipping attribute without value: " + attribute.getID(), null);
        }
        return attributeFilterString;
    }

    /**
     * Constructs an entity from the given JNDI attributes.
     *
     * @param attributes
     * @param dereferenceEntities If true, this method may load entities from foreign registries
     * @return the constructed entity
     */
    abstract protected Entity getEntityFromAttributes(Attributes attributes, boolean dereferenceEntities);

    /**
     * Gets a context for an entity. The conext is a unique path in a ldap directory.
     *
     * @param entity
     * @return context in String representation
     */
    protected String getContextForEntity(Entity entity) {
	return (idAttribute != null ? idAttribute + ":" + entity.getId() : "") + searchBase;
    }

    /**
     * When persisting a new entity, you may want to assign an id.
     *
     * @param entity
     * @return the id to persist
     */
    abstract protected long createNewIdForEntity(Entity entity);

    public List<Entity> findByTemplate(Entity template) {
	Attributes templateAttributes = getAttributesForEntity(template);
	List<Entity> entities = findByContextAndAttributes(searchBase, templateAttributes, false);
	return entities;
    }

    public void persist(Entity entityToPersist) {
	if (readonly) {
	    throw new UnsupportedOperationException("Not supported - registry is set to readonly");
	}
	try {
	    if (idAttribute != null) {
		entityToPersist.setId(createNewIdForEntity(entityToPersist));
	    }
            DirContext ctx = getContext();
	    ctx.createSubcontext(getContextForEntity(entityToPersist), getAttributesForEntity(entityToPersist));
            ctx.close();
	} catch (NameAlreadyBoundException e) {
	    throw new PersistenceException("Entity already persistent", e);
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
    }

    public void update(Entity entityToUpdate) {
	if (readonly) {
	    throw new UnsupportedOperationException("Not supported - registry is set to readonly");
	}
	if (!alreadyPersisted(entityToUpdate)) {
	    throw new PersistenceException("Entity not persistent.", null);
	}
	try {
            DirContext ctx = getContext();
	    ctx.modifyAttributes(getContextForEntity(entityToUpdate), DirContext.REPLACE_ATTRIBUTE, getAttributesForEntity(entityToUpdate));
            ctx.close();
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
    }

    public void delete(Entity entityToDelete) {
	if (readonly) {
	    throw new UnsupportedOperationException("Not supported - registry is set to readonly");
	}
	try {
	    if (!alreadyPersisted(entityToDelete)) {
		throw new PersistenceException("Entity not persistent.", null);
	    }
            DirContext ctx = getContext();
	    ctx.destroySubcontext(getContextForEntity(entityToDelete));
            ctx.close();
	} catch (NamingException ex) {
	    log.log(Level.ERROR, null, ex);
	}
    }

    public boolean alreadyPersisted(Entity entity) {
	return findByTemplate(entity).isEmpty();
    }

    public List<Entity> findAll() {
	return findByContextAndAttributes(searchBase, null, false);
    }

    public String getLighthouseDomain() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public UnitOfWork getUnitOfWork() {
	return null;
    }

    public Entity find(long id) {
	if (idAttribute == null) {
	    throw new UnsupportedOperationException("Not supported - entity id not mapped to ldap attribute.");
	}
	List<Entity> entities = findByContextAndAttribute(searchBase, new BasicAttribute(idAttribute, id), true);
	if (entities.isEmpty()) {
	    return null;
	}
	if (entities.size() > 1) {
	    throw new RuntimeException("Found multiple entities for same id.");
	}
	return entities.get(0);
    }

    @SuppressWarnings("rawtypes")
	protected abstract Class<? extends DomainModelEntityDAO> getRegistryInterface();
}
