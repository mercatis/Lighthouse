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
 * This interface extends the coded domain model entity DAO functionality with
 * functions for hierarchical domain model entities.
 */
public interface HierarchicalDomainModelEntityDAO<Entity extends HierarchicalDomainModelEntity<Entity>> extends
		CodedDomainModelEntityDAO<Entity> {
	/**
	 * Returns the ids of what is considered a top level hierarchical domain
	 * entity. This depends on the particular kind of hierarchical domain model
	 * entities.
	 * 
	 * @return the ids of the top level entities.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public List<Long> findAllTopLevelComponentIds();

	/**
	 * Returns the codes uniquely identifying what is considered a top level
	 * hierarchical domain entity. This depends on the particular kind of
	 * hierarchical domain model entities.
	 * 
	 * @return the codes of the top level entities.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public List<String> findAllTopLevelComponentCodes();
}
