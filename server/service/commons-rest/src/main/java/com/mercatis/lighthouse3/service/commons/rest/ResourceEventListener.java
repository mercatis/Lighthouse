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
package com.mercatis.lighthouse3.service.commons.rest;


/**
 * This interface has to be implemented by listeners who want to attach
 * themselves to domain model entity resources in order to be informed about
 * creation, update, and deletion of entities via those resources.
 */
public interface ResourceEventListener {

	/**
	 * Notification indicator for entity created notifications.
	 */
	static public int ENTITY_CREATED = 0;
	/**
	 * Notification indicator for entity updated notifications.
	 */
	static public int ENTITY_UPDATED = 1;
	/**
	 * Notification indicator for entity deleted notifications.
	 */
	static public int ENTITY_DELETED = 2;

	/**
	 * An entity was created via a resource, i.e., a POST request was performed.
	 * 
	 * @param resource
	 *            the resource through which the creation occurred
	 * @param entityIdCode
	 *            the code or id of the entity created.
	 */
	public void entityCreated(DomainModelEntityResource<?> resource, String entityIdCode);

	/**
	 * An entity was updated via a resource, i.e., a PUT request was performed.
	 * 
	 * @param resource
	 *            the resource through which the update occurred
	 * @param entityIdCode
	 *            the code or id of the entity updated.
	 */
	public void entityUpdated(DomainModelEntityResource<?> resource, String entityIdCode);

	/**
	 * An entity was deleted via a resource, i.e., a DELETE request was
	 * performed.
	 * 
	 * @param resource
	 *            the resource through which the deletion occurred
	 * @param entityIdCode
	 *            the code or id of the entity deleted.
	 */
	public void entityDeleted(DomainModelEntityResource<?> resource, String entityIdCode);
}
