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
package com.mercatis.lighthouse3.persistence.commons.rest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;

/**
 * This class provides an abstract implementation of common methods for
 * hierarchical domain model entity DAOs. The implementation acts as an HTTP
 * client to a RESTful web service providing the DAO storage functionality.
 */
public abstract class HierarchicalDomainModelEntityDAOImplementation<Entity extends HierarchicalDomainModelEntity<Entity>>
		extends CodedDomainModelEntityDAOImplementation<Entity> implements HierarchicalDomainModelEntityDAO<Entity> {

	@SuppressWarnings("rawtypes")
	@Override
	protected DomainModelEntityDAO[] getRealEntityResolvers() {
		return new DomainModelEntityDAO[] { this };
	}

	public List<String> findAllTopLevelComponentCodes() {
		String result = this.executeHttpMethod(this.urlForEntityClass() + "/Code/toplevel", HttpMethod.GET, null, null);
		return new LinkedList<String>(XmlMuncher.readValuesFromXml(result, "//:code"));
	}

	public List<Long> findAllTopLevelComponentIds() {
		throw new PersistenceException("Method not supported", null);
	}

	public String findByCode(String code, Entity entity) {
		return this.executeHttpMethod(this.urlForEntityCode(code), HttpMethod.GET, null, null);
	}

	@Override
	public boolean alreadyPersisted(Entity entity) {
		Entity foundEntity = findByCode(entity.getCode());
		return foundEntity != null;
	}

	
	@Override
	public void persist(Entity entityToPersist) {
		if (alreadyPersisted(entityToPersist))
			throw new PersistenceException("Entity already persistent", null);
		recursivePersist(entityToPersist);
	}
	
	// this method saves bandwidth (on large trees)
	private void recursivePersist(Entity entityToPersist) {
		Set<Entity> children = entityToPersist.getDirectSubEntities();
		List<Entity> newChildren = new ArrayList<Entity>(children.size());
		for (Entity child : children) {
			if (!alreadyPersisted(child)) {
				entityToPersist.removeSubEntity(child);
				recursivePersist(child);
				newChildren.add(child);
			}
		}
		super.persist(entityToPersist);
		for (Entity child : newChildren) {
			entityToPersist.addSubEntity(child);
			super.update(child);
		}
		if (newChildren.size()>0)
			super.update(entityToPersist);
	}

	@Override
	public void update(Entity entityUpdate) {
		for (Entity child : entityUpdate.getDirectSubEntities())
			update(child);
		super.update(entityUpdate);
	}

}
