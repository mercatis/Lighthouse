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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;

/**
 * This abstract base class subsumes domain model entities that are hierarchical
 * in nature.
 * 
 * This hierarchical domain model entities offer support for property
 * inheritance. Properties of hierarchical domain model entities can be
 * inherited upwards or downwards the hierarchy.
 * 
 * It is the task of the concrete implementations of this base class to define
 * what these properties are. For this purpose, the methods
 * 
 * <ul>
 * <li><code>getDownwardInheritableProperties()</code>
 * <li><code>getUpwardInheritableProperties()</code>
 * <li><code>getLocalProperties()</code>
 * </ul>
 * 
 * need to be overridden to return the objects capturing the downward
 * inheritable, upward inheritable properties, or the non-inheritable local
 * properties of a concrete hierarchical domain model entity instance,
 * respectively.
 */
public abstract class HierarchicalDomainModelEntity<Entity extends HierarchicalDomainModelEntity<Entity>>
		extends CodedDomainModelEntity {

	private static final long serialVersionUID = 8069766736800779369L;

	private Set<Entity> directSubEntities = new HashSet<Entity>();

	/**
	 * This predicate checks whether the given entity is a direct sub entity of
	 * the present hierarchical entity
	 * 
	 * @param entity
	 *            the entity to check.
	 * @return <code>true</code> iff the passed entity is a direct sub entity.
	 */
	public boolean isDirectSubEntity(Entity entity) {
		return entity.getParentEntity() == this;
	}

	/**
	 * This predicate checks whether the given entity is a sub entity - direct
	 * or indirect - of the present hierarchical entity
	 * 
	 * @param entity
	 *            the entity to check.
	 * @return <code>true</code> iff the passed entity is a direct or indirect
	 *         sub entity.
	 */
	public boolean isSubEntity(Entity entity) {
		Entity currentParentEntity = entity;

		do {
			currentParentEntity = currentParentEntity.getParentEntity();
		} while ((currentParentEntity != this) && (currentParentEntity != null));

		return currentParentEntity == this;
	}

	/**
	 * This method is used to add a new entity as a subentity to the present
	 * hierarchical entity. If the subentity is already amongst the direct
	 * subentities of the hierarchical entity, nothing happens.
	 * 
	 * @param subEntity
	 *            the subentity to add.
	 */
	@SuppressWarnings("unchecked")
	public void addSubEntity(Entity subEntity) {
		this.directSubEntities.add(subEntity);
		subEntity.setParentEntity((Entity) this);
	}

	/**
	 * This method removes a subentity from a hierarchical entity. If the entity
	 * to be removed is not amongst the direct subentities of the hierarchical
	 * entity, nothing happens.
	 * 
	 * @param subEntity
	 *            the direct subentity to be removed.
	 */
	public void removeSubEntity(Entity subEntity) {
		if (this.isDirectSubEntity(subEntity)) {
			this.directSubEntities.remove(subEntity);
			subEntity.setParentEntity(null);
		}
	}

	/**
	 * This method removes all direct subentities from the present hierarchical
	 * entity, making them root entities again.
	 */
	public void removeAllSubEntities() {
		for (Entity subEntity : new HashSet<Entity>(this.getDirectSubEntities())) {
			this.removeSubEntity(subEntity);
		}
	}

	/**
	 * This method returns all direct subentities of the present hierarchical
	 * entity.
	 * 
	 * @return the set of all direct subentities.
	 */
	@SuppressWarnings("unchecked")
	public Set<Entity> getDirectSubEntities() {
		Set<Entity> subEntities = new HashSet<Entity>();
		
		for (Entity directSubentity : this.directSubEntities) {
			directSubentity.setParentEntity((Entity) this);
			subEntities.add(directSubentity);
		}

		return subEntities;
	}

	/**
	 * This method returns all subentities - direct and indirect - of the
	 * present hierarchical entity.
	 * 
	 * @return the set of all subentities.
	 */
	public Set<Entity> getSubEntities() {
		Set<Entity> subEntities = new HashSet<Entity>();

		for (Entity directSubentity : this.getDirectSubEntities()) {
			subEntities.add(directSubentity);
			subEntities.addAll(directSubentity.getSubEntities());
		}

		return subEntities;
	}

	/**
	 * Stores the parent of the present hierarchical domain entity.
	 */
	private Entity parentEntity = null;

	/**
	 * This method returns the parent entity of the present hierarchical domain
	 * entity.
	 * 
	 * @return the parent entity
	 */
	public Entity getParentEntity() {
		return this.parentEntity;
	}

	/**
	 * Sets the parent entity of the present hierarchical domain model entity.
	 * Take care that you do not call this method unless you know what you are
	 * doing. Use the child entity related methods for moving entities around.
	 * 
	 * @param parentEntity
	 *            the new parent entity
	 */
	public void setParentEntity(Entity parentEntity) {
		this.parentEntity = parentEntity;
	}

	/**
	 * This method extract the topmost parent entity of the present entity.
	 * 
	 * @return the root entity.
	 */
	@SuppressWarnings("unchecked")
	public Entity getRootEntity() {
		if (this.isRootEntity()) {
			return (Entity) this;
		} else {
			return this.getParentEntity().getRootEntity();
		}
	}

	/**
	 * This predicate determines whether the given hierarchical domain model
	 * entity is a root entity with no parent.
	 * 
	 * @return <code>true</code> iff the given hierarchical domain model entity
	 *         is a root entity
	 */
	public boolean isRootEntity() {
		return this.getParentEntity() == null;
	}

	/**
	 * This predicate determines whether the given hierarchical domain model
	 * entity is a complex entity with sub entities.
	 * 
	 * @return <code>true</code> iff the given hierarchical domain model entity
	 *         has direct subentities.
	 */
	public boolean isComplexEntity() {
		return this.getDirectSubEntities().size() != 0;
	}

	/**
	 * This method can be overridden to return the objects that represent the
	 * entity property values that should be inherited downwards the domain
	 * model entity hierarchy.
	 * 
	 * @return the downward inheritable entity properties
	 */
	@SuppressWarnings("rawtypes")
	protected Set getDownwardInheritableProperties() {
		return new HashSet();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Set getDownwardInheritableProperties(Class filterByClass) {
		Set result = new HashSet();

		for (Object downwardInheritableProperty : this
				.getDownwardInheritableProperties()) {
			if (filterByClass.isInstance(downwardInheritableProperty))
				result.add(downwardInheritableProperty);
		}

		return result;
	}

	/**
	 * This method can be overridden to return the objects that represent the
	 * entity property values that should be inherited upwards the domain model
	 * entity hierarchy.
	 * 
	 * @return the upward inheritable entity properties
	 */
	@SuppressWarnings("rawtypes")
	protected Set getUpwardInheritableProperties() {
		return new HashSet();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set getUpwardInheritableProperties(Class filterByClass) {
		Set result = new HashSet();

		for (Object upwardInheritableProperty : this
				.getUpwardInheritableProperties()) {
			if (filterByClass.isInstance(upwardInheritableProperty))
				result.add(upwardInheritableProperty);
		}

		return result;
	}

	/**
	 * This method can be overridden to return the objects that represent local
	 * entity property values that should not be inherited upwards or downwards
	 * the domain model entity hierarchy.
	 * 
	 * @return the local entity properties
	 */
	@SuppressWarnings("rawtypes")
	public Set getLocalProperties() {
		return new HashSet();
	}

	/**
	 * This method returns the objects that represent local entity property
	 * values, filtered by a given class.
	 * 
	 * @param filterByClass
	 *            the class to which the returned properties must belong.
	 * @return the local entity properties
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getLocalProperties(Class filterByClass) {
		Set result = new HashSet();

		for (Object localProperty : this.getLocalProperties()) {
			if (filterByClass.isInstance(localProperty))
				result.add(localProperty);
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set calculateInheritedPropertiesFromParentEntities(
			Class filterByClass) {
		Set result = new HashSet();

		if (this.getParentEntity() != null) {
			result.addAll(this.getParentEntity()
					.calculateInheritedPropertiesFromParentEntities(
							filterByClass));
			result.addAll(this.getParentEntity()
					.getDownwardInheritableProperties(filterByClass));
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set calculateInheritedPropertiesFromChildEntities(
			Class filterByClass) {
		Set result = new HashSet();

		for (Entity subEntity : this.getDirectSubEntities()) {
			result
					.addAll(subEntity
							.calculateInheritedPropertiesFromChildEntities(filterByClass));
			result.addAll(subEntity
					.getUpwardInheritableProperties(filterByClass));
		}

		return result;
	}

	/**
	 * This method returns the properties of the present hierarchical domain
	 * model entity, filtered by their membership to a specific class.
	 * 
	 * This method includes the downward inheritable properties inherited from
	 * the parent entities of the present entity, the local properties, as well
	 * as the upward inheritable properties of the sub entities of the present
	 * entity.
	 * 
	 * @param filterByClass
	 *            the class to which the returned properties must belong.
	 * @return the properties of the present hierarchical domain model entity.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set getProperties(Class filterByClass) {
		Set result = this
				.calculateInheritedPropertiesFromParentEntities(filterByClass);

		result.addAll(this
				.calculateInheritedPropertiesFromChildEntities(filterByClass));
		result.addAll(this.getLocalProperties(filterByClass));

		return result;
	}

	/**
	 * This method returns the properties of the present hierarchical domain
	 * model entity.
	 * 
	 * This method includes the downward inheritable properties inherited from
	 * the parent entities of the present entity, the local properties, as well
	 * as the upward inheritable properties of the sub entities of the present
	 * entity.
	 * 
	 * @return the properties of the present hierarchical domain model entity.
	 */
	@SuppressWarnings("rawtypes")
	public Set getProperties() {
		return this.getProperties(Object.class);
	}

	/**
	 * This method returns a path identifier for the given hierarchical domain
	 * model entity. It has a URL-like form:
	 * <code>entityClassInLowerCase://a/path</code>. For hierarchical domain
	 * model entities the path is just the concatenation of codes of the
	 * hierarchy down to the entity.
	 * 
	 * @return the path for the entity.
	 */
	@Override
	public String getPath() {
		if (this.isRootEntity())
			return super.getPath();
		else
			return this.getParentEntity().getPath() + "/" + this.getCode();
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (!this.isRootEntity()) {
			this.getParentEntity().writeEntityReference(
					"parent" + this.getClass().getSimpleName(), xml);
		}

		if (!this.getDirectSubEntities().isEmpty()) {
			xml.writeEntity("child" + this.getClass().getSimpleName() + "s");

			for (HierarchicalDomainModelEntity<Entity> subEntity : this
					.getDirectSubEntities()) {
				subEntity.writeEntityReference("child"
						+ this.getClass().getSimpleName(), xml);
			}

			xml.endEntity();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {
		super.resolveEntityReferencesFromXml(xmlDocument,
				resolversForEntityReferences);

		if ((resolversForEntityReferences.length == 0)
				|| !(resolversForEntityReferences[0] instanceof HierarchicalDomainModelEntityDAO)) {
			throw new XMLSerializationException(
					"XML deserialization of hierarchical domain model entities requires reference to HierarchicalDomainModelEntityDAO as 1st resolverForEntityReferences.",
					null);
		}

		HierarchicalDomainModelEntityDAO<Entity> hierarchicalDao = (HierarchicalDomainModelEntityDAO<Entity>) resolversForEntityReferences[0];

		if (!(hierarchicalDao instanceof CodeCacheDAOWrapper))
			hierarchicalDao = new CodeCacheDAOWrapper<Entity>(hierarchicalDao);

		((CodeCacheDAOWrapper<Entity>) hierarchicalDao).interestedInCache();

		try {
			((CodeCacheDAOWrapper<Entity>) hierarchicalDao).cacheForCode(
					(Entity) this, this.getCode());

			String parentCode = xmlDocument.readValueFromXml("/*/:parent"
					+ this.getClass().getSimpleName() + "/:code");

			if (parentCode != null) {
				Entity parent = hierarchicalDao.findByCode(parentCode);

				if (parent == null)
					throw new XMLSerializationException(
							"XML serialization of hierarchical domain model entity references parent entity with unknown code.",
							null);

				if (parent.getCode() != null)
					this.parentEntity = parent;
			}

			this.removeAllSubEntities();

			List<String> childCodes = xmlDocument.readValuesFromXml("/*/:child"
					+ this.getClass().getSimpleName() + "s/:child"
					+ this.getClass().getSimpleName() + "/:code");

			Set<Entity> childrenToAdd = new HashSet<Entity>();

			for (String childCode : childCodes) {
				Entity child = hierarchicalDao.findByCode(childCode);

				if (child == null)
					throw new XMLSerializationException(
							"XML serialization of hierarchical domain model entity references child entity with unknown code.",
							null);
				if (child.getCode() != null)
					childrenToAdd.add(child);
			}

			for (Entity childToAdd : childrenToAdd) {
				this.addSubEntity(childToAdd);
			}

		} finally {
			((CodeCacheDAOWrapper<Entity>) hierarchicalDao)
					.noLongerInterestedInCache();
		}
	}
}
