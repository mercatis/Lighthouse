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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntity;

/**
 * This class gathers common functionality of Hierarchical Domain Model Entity
 * DAOs for Hibernate.
 */
public abstract class HierarchicalDomainModelEntityDAOImplementation<Entity extends HierarchicalDomainModelEntity<Entity>>
		extends CodedDomainModelEntityDAOImplementation<Entity> {

	@Override
	public void delete(Entity entityInDB) {
		if (entityInDB.getParentEntity() != null) {
			entityInDB.getParentEntity().removeSubEntity(entityInDB);
		}
		super.delete(entityInDB);
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllTopLevelComponentCodes() {
		return this.unitOfWork.getCurrentSession().createQuery(
				"select e.code from " + this.getEntityTypeName() + " e where e.parentEntity is null").list();
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAllTopLevelComponentIds() {
		return this.unitOfWork.getCurrentSession().createQuery(
				"select e.id from " + this.getEntityTypeName() + " e where e.parentEntity is null").list();
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

		Criteria criteria = super.entityToCriteria(session, entityTemplate);

		if (entityTemplate.getParentEntity() != null)
			criteria.add(Restrictions.eq("parentEntity", entityTemplate.getParentEntity()));

		return criteria;
	}
}