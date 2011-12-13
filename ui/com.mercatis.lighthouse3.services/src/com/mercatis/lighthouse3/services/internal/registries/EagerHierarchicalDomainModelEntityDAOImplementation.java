/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;


public abstract class EagerHierarchicalDomainModelEntityDAOImplementation<Entity extends HierarchicalDomainModelEntity<Entity>>
	extends EagerCodedDomainModelEntityDAOImplementation<Entity>
	implements HierarchicalDomainModelEntityDAO<Entity>{
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO#findAllTopLevelComponentCodes()
	 */
	public List<String> findAllTopLevelComponentCodes() {
		ArrayList<String> results = new ArrayList<String>();
		for (Entity entity : cache.values()) {
			if (entity.isRootEntity())
				results.add(entity.getCode());
		}
		
		return results;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO#findAllTopLevelComponentIds()
	 */
	public List<Long> findAllTopLevelComponentIds() {
		ArrayList<Long> results = new ArrayList<Long>();
		for (Entity entity : cache.values()) {
			if (entity.isRootEntity())
				results.add(entity.getId());
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#invalidate()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void invalidate() {
		cache.clear();
		
		List<Entity> entities = delegateRegistry().findAll();
		for (Entity entity : entities) {
			if (entity != null)
				cache.put(entity.getCode(), entity);
		}
		
		List<Entity> unboundEntities = new ArrayList<Entity>(cache.values());
		for (Entity unboundEntity : unboundEntities) {
			Entity boundEntity = ((LateBindingDomainModelEntity<Entity>) unboundEntity).bind().getDelegateEntity();
			if (boundEntity != null)
				cache.put(boundEntity.getCode(), boundEntity);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void persist(Entity entityToPersist) {
		delegateRegistry().persist(entityToPersist);
		
		Entity unboundEntity = ((HierarchicalDomainModelEntityDAO<Entity>) delegateRegistry()).findByCode(entityToPersist.getCode());
		if (unboundEntity != null)
			this.cache.put(unboundEntity.getCode(), (Entity) unboundEntity);
		
		Entity boundEntity = ((LateBindingDomainModelEntity<Entity>) unboundEntity).bind().getDelegateEntity();
		if (boundEntity != null)
			this.cache.put(boundEntity.getCode(), (Entity) boundEntity);
	}

}
