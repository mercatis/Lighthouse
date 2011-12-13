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
package com.mercatis.lighthouse3.persistence.commons.hibernate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.persistence.commons.AbstractDomainModelEntityDAO;

/**
 * This class provides an abstract implementation of common methods for Domain
 * Model Entity DAOs for Hibernate.
 */
public abstract class DomainModelEntityDAOImplementation<Entity extends DomainModelEntity> extends AbstractDomainModelEntityDAO {

	/**
	 * Generic constructor, necessary for setting the <CODE>entityType</CODE>
	 * property at runtime.
	 */
	@SuppressWarnings("rawtypes")
	public DomainModelEntityDAOImplementation() {
		try {
			ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
			Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
			this.entityType = (Class) actualTypeArguments[0];
		} catch (Throwable t) {
			this.entityType = DomainModelEntity.class;
		}
	}

	/**
	 * This method should be overridden appropriately to check whether an entity
	 * is already persisted.
	 * 
	 * @param entity
	 * @return <code>true</code> iff the entity is already persistent.
	 */
	public boolean alreadyPersisted(Entity entity) {
		long id = entity.getId();
		if (id!=0)
			return find(id)!=null;
		return findByTemplate(entity).size()>0;
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
	 * This references the unit of work to which the software component registry
	 * is connected for the creation of sessions.
	 */
	protected UnitOfWorkImplementation unitOfWork = null;

	/**
	 * This method sets the session factory to use for Hibernate persistence.
	 * 
	 * @param sessionFactory
	 *            the session factory to use for persistence operations.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.unitOfWork = new UnitOfWorkImplementation();
		this.unitOfWork.setSessionFactory(sessionFactory);
	}

	public UnitOfWork getUnitOfWork() {
		return this.unitOfWork;
	}

	public void reRegister(Entity entityToReRegister) {
		this.update(entityToReRegister);
	}

	public void register(Entity entityToRegister) {
		this.persist(entityToRegister);
	}

	public void unregister(Entity entityToUnregister) {
		this.delete(entityToUnregister);
	}

	public void persist(Entity entityToPersist) {
		if (alreadyPersisted(entityToPersist))
			throw new PersistenceException("Entity already persistent", null);
		try {
			Session sess = unitOfWork.getCurrentSession();
			sess.save(entityToPersist);
		} catch (Exception cause) {
			throw new PersistenceException("Entity already persistent", cause);
		}
	}

	public void update(Entity entityToUpdate) {
		if (!alreadyPersisted(entityToUpdate))
			throw new PersistenceException("Entity not persistent", null);
		try {
			Session sess = unitOfWork.getCurrentSession();
			sess.evict(entityToUpdate);
			sess.update(entityToUpdate);
		} catch (Exception cause) {
			throw new PersistenceException("Update of entity failed", cause);
		}
	}

	@SuppressWarnings("unchecked")
	public void delete(Entity entityToDelete) {
		if (!alreadyPersisted(entityToDelete))
			throw new PersistenceException("Entity not persistent", null);
		try {
			Session sess = unitOfWork.getCurrentSession();
			if (!sess.contains(entityToDelete))
				entityToDelete = (Entity) sess.merge(entityToDelete);
			sess.delete(entityToDelete);
		} catch (Exception cause) {
			throw new PersistenceException("Deletion of entity failed", cause);
		}
	}

	@SuppressWarnings("unchecked")
	public Entity find(long id) {
		Session session = unitOfWork.getCurrentSession();
		Entity entity = (Entity) session.get(this.getEntityTypeName(), id);
		return entity;
	}

	/**
	 * Must be overridden by subclasses such that a partially filled entity
	 * template is converted into an equivalent bunch of Hibernate criteria.
	 * 
	 * @param session
	 *            the Hibernate session of which the criteria will be based
	 * @param entityTemplate
	 *            the partially filled entity
	 * @return the hibernate criteria
	 */
	protected Criteria entityToCriteria(Session session, Entity entityTemplate) {

		Criteria criteria = session.createCriteria(this.getManagedType());

		if (entityTemplate.getId() != 0L)
			criteria.add(Restrictions.eq("id", entityTemplate.getId()));

		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<Entity> findByTemplate(Entity template) {
		return this.entityToCriteria(this.unitOfWork.getCurrentSession(), template).list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#findAll
	 * ()
	 */
	public List<Entity> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#
	 * getLighthouseDomain()
	 */
	public String getLighthouseDomain() {
		throw new RuntimeException("Not Implemented.");
	}
}
