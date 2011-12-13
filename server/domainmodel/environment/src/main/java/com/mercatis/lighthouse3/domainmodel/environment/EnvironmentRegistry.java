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
package com.mercatis.lighthouse3.domainmodel.environment;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * The environment registry interface abstractly defines the contract for a
 * persistent repository of environments. It provides basic CRUD functionality.
 * It can be implemented using different persistence technologies.
 * 
 * With regard to persistence, the following rules must apply for
 * implementations:
 * 
 * <ul>
 * <li>environments are uniquely identified by their <code>id</code>.
 * <li>environments are uniquely identified by their <code>code</code>.
 * <li>environments are always fully loaded including their child environments.
 * <li>environments are always fully loaded including their parent environments.
 * The parent environments do not have to be fully loaded though.
 * <li>environments are loaded including the deployments attached to them. These
 * in turn follow the usual deployment loading rules.
 * <li>environments are persisted along with their attached deployments.
 * <li>software components are persisted along with their child environments if
 * those have not yet been persisted.
 * </ul>
 */
public interface EnvironmentRegistry extends DeploymentCarryingDomainModelEntityDAO<Environment> {

	/**
	 * A synonym for <code>persist()</code>.
	 * 
	 * @see DomainModelEntityDAO#persist()
	 */
	public void register(Environment environmentToRegister);

	/**
	 * A synonym for <code>update()</code>.
	 * 
	 * @see DomainModelEntityDAO#update()
	 */
	public void reRegister(Environment environmentToReRegister);

	/**
	 * A synonym for <code>delete()</code>.
	 * 
	 * @see DomainModelEntityDAO#delete()
	 */
	public void unregister(Environment environmentToUnregister);

}
