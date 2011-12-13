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

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;


public abstract class EagerCodedDomainModelEntityDAOImplementation<Entity extends CodedDomainModelEntity>
	extends EagerDomainModelEntityDAOImplementation<Entity>
	implements CodedDomainModelEntityDAO<Entity> {

	protected String keyForEntity(Entity entity) {
		return entity.getCode();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO#findByCode(java.lang.String)
	 */
	public Entity findByCode(String code) {
		return cache.get(code);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void persist(Entity entityToPersist) {
		delegateRegistry().persist(entityToPersist);
		CodedDomainModelEntity persistedEntity = ((CodedDomainModelEntityDAO<CodedDomainModelEntity>) delegateRegistry()).findByCode(entityToPersist.getCode());
		if (persistedEntity != null)
			this.cache.put(persistedEntity.getCode(), (Entity) persistedEntity);
	}
	
}
