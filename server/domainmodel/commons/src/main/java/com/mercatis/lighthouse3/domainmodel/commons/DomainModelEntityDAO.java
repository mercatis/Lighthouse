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
package com.mercatis.lighthouse3.domainmodel.commons;

import java.util.List;

/**
 * This interface provides basic DAO functionality for domain model entities.
 * Domain model entities are uniquely identified by their <code>id</code>s.
 * 
 * Each domain model entity DAO internally references a unit of work, which it
 * uses for its persistence operations.
 * 
 * Note that in case of persistence errors at runtime all methods may throw
 * unchecked <code>PersistenceException</code>s.
 * 
 * The interface can be implemented using different persistence technologies. The
 * implementation technology must match the implementation technology of the
 * unit of work associated with the domain model entity DAO. Furthermore, all
 * possibly occurring exceptions have to be caught by implementations and
 * rethrown as unchecked <code>PersistenceException</code>s.
 */
public interface DomainModelEntityDAO<Entity extends DomainModelEntity> {

	/**
	 * 
	 * Returns the class of the entity type managed by the DAO.
	 * 
	 * @return The class.
	 */
	@SuppressWarnings("rawtypes")
	public Class getManagedType();

	
	/**
     * This method returns the lighthouse domain of the server.
     *
     * @return the lighthouse domain.
     */
	public String getLighthouseDomain();
	
	/**
	 * Returns the unit of work associated with the domain entity DAO.
	 * 
	 * @return the unit of work or <code>null</code> if the DAO does not support
	 *         units of work.
	 */
	public UnitOfWork getUnitOfWork();

	/**
	 * Makes a transient entity persistent. Should the passed entity already be
	 * persistent (meaning: an entity with the same id already exists), an
	 * exception is thrown.
	 * 
	 * @param entityToPersist
	 *            the entity to persist.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public void persist(Entity entityToPersist);
	
	/**
     * This method should be overridden appropriately to check whether an entity
     * is already persisted.
     *
     * @param entity
     * @return <code>true</code> iff the entity is already persistent.
     */
    public boolean alreadyPersisted(Entity entity);

	/**
	 * Calling this method updates the passed entity in persistent store. Note
	 * that the entity must already have been persisted before, otherwise an
	 * exception is thrown.
	 * 
	 * @param entityToUpdate
	 *            the persistent entity to update.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public void update(Entity entityToUpdate);

	/**
	 * This method deletes the passed entity from persistent storage. Note that
	 * the entity must haven been persistend before, otherwise an exception is
	 * thrown.
	 * 
	 * @param entityToDelete
	 *            the persistent entity to delete
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public void delete(Entity entityToDelete);

	/**
	 * The method looks up and returns the persistent entity with the passed id.
	 * 
	 * @param id
	 *            the id of the entity to look up.
	 * @return the entity retrieved or <code>null</code> in the case a
	 *         persistent entity with the id cannot be found.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public Entity find(long id);
	
	/**
	 * This method returns all persistent entities.
	 * 
	 * @return a (possible empty) <code>List</code> of entities
	 * 
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public List<Entity> findAll();

	/**
	 * The methods looks up all persistent entities that match a given template.
	 * A template is an instance of the entity class, where attributes of
	 * interest have been set to the values of interest. If an attribute is not
	 * of interest, it is set to <code>null</code>. A persistent entity
	 * qualifies as a match for the template iff it matches all of the non-
	 * <code>null</code> attributes of the template.
	 * 
	 * @param template
	 *            the entity template to match
	 * @return the set of persistent entities matching the template. The set is
	 *         empty, when not matches have been found.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public List<Entity> findByTemplate(Entity template);
}
