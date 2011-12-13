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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * This class provides a code caching wrapper around hierarchical domain model
 * entity DAOs so that potential recursions when parsing parent entities are
 * avoided.
 */
public class CodeCacheDAOWrapper<Entity extends HierarchicalDomainModelEntity<Entity>> implements
		HierarchicalDomainModelEntityDAO<Entity> {

	static private Map<Thread, Map<String, Object>> perThreadCodeCache = Collections
			.synchronizedMap(new IdentityHashMap<Thread, Map<String, Object>>());

	static private Map<Thread, Integer> perThreadCodeCacheInterests = Collections
			.synchronizedMap(new IdentityHashMap<Thread, Integer>());

	private HierarchicalDomainModelEntityDAO<Entity> innerDAO = null;

	public CodeCacheDAOWrapper(HierarchicalDomainModelEntityDAO<Entity> innerDAO) {
		this.innerDAO = innerDAO;

		if (!perThreadCodeCache.containsKey(Thread.currentThread())) {
			perThreadCodeCache.put(Thread.currentThread(), new HashMap<String, Object>());
			perThreadCodeCacheInterests.put(Thread.currentThread(), 0);
		}
	}

	public void interestedInCache() {
		if (perThreadCodeCacheInterests.containsKey(Thread.currentThread())) {
			int newInterests = perThreadCodeCacheInterests.get(Thread.currentThread()) + 1;
			perThreadCodeCacheInterests.put(Thread.currentThread(), newInterests);
		}
	}

	public void noLongerInterestedInCache() {
		if (perThreadCodeCacheInterests.containsKey(Thread.currentThread())) {
			int newInterests = perThreadCodeCacheInterests.get(Thread.currentThread()) - 1;
			perThreadCodeCacheInterests.put(Thread.currentThread(), newInterests);

			if (newInterests <= 0) {
				perThreadCodeCacheInterests.remove(Thread.currentThread());
				perThreadCodeCache.remove(Thread.currentThread());
			}
		}
	}

	public void cacheForCode(Entity entity, String code) {
		if (perThreadCodeCache.containsKey(Thread.currentThread()))
			perThreadCodeCache.get(Thread.currentThread()).put(code, entity);
	}

	public List<String> findAllTopLevelComponentCodes() {
		return this.innerDAO.findAllTopLevelComponentCodes();
	}

	public List<Long> findAllTopLevelComponentIds() {
		return this.innerDAO.findAllTopLevelComponentIds();
	}

	@SuppressWarnings("unchecked")
	public Entity findByCode(String code) {
		Map<String, Object> threadCodeCache = perThreadCodeCache.get(Thread.currentThread());

		if ((threadCodeCache == null) || !threadCodeCache.containsKey(code)) {
			return this.innerDAO.findByCode(code);
		} else {
			return (Entity) threadCodeCache.get(code);
		}
	}

	public void delete(Entity entityToDelete) {
		this.innerDAO.delete(entityToDelete);
	}

	public Entity find(long id) {
		return this.innerDAO.find(id);
	}

	public List<Entity> findByTemplate(Entity template) {
		return this.innerDAO.findByTemplate(template);
	}
	
	public boolean alreadyPersisted(Entity entity) {
		return this.innerDAO.alreadyPersisted(entity);
	};

	@SuppressWarnings("rawtypes")
	public Class getManagedType() {
		return this.innerDAO.getManagedType();
	}

	public UnitOfWork getUnitOfWork() {
		return this.innerDAO.getUnitOfWork();
	}

	public void persist(Entity entityToPersist) {
		this.innerDAO.persist(entityToPersist);

	}

	public void update(Entity entityToUpdate) {
		this.innerDAO.update(entityToUpdate);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#findAll()
	 */
	public List<Entity> findAll() {
		return this.innerDAO.findAll();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getLighthouseDomain()
	 */
	public String getLighthouseDomain() {
		return this.innerDAO.getLighthouseDomain();
	}

}