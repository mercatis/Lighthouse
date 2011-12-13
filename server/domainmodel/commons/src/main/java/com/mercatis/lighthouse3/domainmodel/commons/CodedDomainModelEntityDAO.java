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

/**
 * This interface extends the core domain model entity DAO functionality with
 * functions for coded domain model entities.
 * 
 * Coded domain model entities are uniquely identified by their
 * <code>code</code>s.
 */
public interface CodedDomainModelEntityDAO<Entity extends CodedDomainModelEntity> extends DomainModelEntityDAO<Entity> {
	/**
	 * Looks up the persistent entity uniquely identified by the given code.
	 * 
	 * @param code
	 *            the code of the entity to retrieve.
	 * @return the entity retrieved or <code>null</code> in the case a
	 *         persistent entity with the code cannot be found.
	 * @throws PersistenceException
	 *             in case of any errors
	 */
	public Entity findByCode(String code);
}
