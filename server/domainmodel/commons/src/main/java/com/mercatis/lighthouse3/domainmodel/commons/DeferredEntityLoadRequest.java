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
 * This interface encapsulates a deferred load request for a domain model
 * entity.
 */
public interface DeferredEntityLoadRequest {
	/**
	 * Call this method to finally perform the load request.
	 * 
	 * @return the loaded object, should never be <code>null</code>. In the
	 *         latter case an exception should be thrown.
	 * @throws PersistenceException
	 *             in case of trouble with loading the entity (e.g.s, it may
	 *             have been deleted).
	 */
	public DomainModelEntity doLoad();
}
