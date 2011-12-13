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
 * The present interface exposes persistent units of work, such as database
 * transactions. It should be implemented for the various persistence mechanisms
 * used.
 * 
 * Units of work are intended to be shared amongst all DAOs involved in the same
 * unit.
 * 
 * Implementations have to ensure that all exceptions occurring at runtime are
 * mapped to unchecked <code>PersistenceExceptions</code> and rethrown.
 */
public interface UnitOfWork {

	/**
	 * Calling this method flushes - but does not yet commit - any pending write
	 * operations of the current unit of work to the persistence backend.
	 * 
	 * @throws PersistenceException
	 *             in case of errors occurring during flush
	 */
	public void flush();

	/**
	 * Calling this method flushes commits the current unit of work persisting
	 * all pending write operations. Afterwards, a new unit of work is opened.
	 * 
	 * @throws PersistenceException
	 *             in case of errors occurring during commit
	 */
	public void commit();

	/**
	 * Calling this method rolls back the current unit of work undoing any
	 * previously flushed write operations. Afterwards, a new unit of work is
	 * opened.
	 * 
	 * @throws PersistenceException
	 *             in case of errors occurring during rollback
	 */
	public void rollback();

	public void setReadOnly(Object o, boolean readOnly);
}
