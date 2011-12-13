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
import com.mercatis.lighthouse3.domainmodel.commons.HierarchicalDomainModelEntityDAO;

/**
 * The software component registry interface abstractly defines the contract for
 * a persistent repository of software components. It provides basic CRUD
 * functionality. It can be implemented using different persistence
 * technologies.
 * 
 * With regard to persistence, the following rules must apply for
 * implementations:
 * 
 * <ul>
 * <li>software components are uniquely identified by their <code>id</code>.
 * <li>software components are uniquely identified by their <code>code</code>.
 * <li>software components are always fully loaded including their child
 * components.
 * <li>software components are always fully loaded including their parent
 * components. The parent components do not have to be fully loaded though.
 * <li>software components are persisted along with their child components if
 * those have not yet been persisted.
 * <li>deletion of a software component results in the deletion of all
 * deployments of the component.
 * </ul>
 */
public interface SoftwareComponentRegistry extends HierarchicalDomainModelEntityDAO<SoftwareComponent> {

	/**
	 * A synonym for <code>persist()</code>.
	 * 
	 * @see DomainModelEntityDAO#persist()
	 */
	public void register(SoftwareComponent componentToRegister);

	/**
	 * A synonym for <code>update()</code>.
	 * 
	 * @see DomainModelEntityDAO#update()
	 */
	public void reRegister(SoftwareComponent componentToReRegister);

	/**
	 * A synonym for <code>delete()</code>.
	 * 
	 * @see DomainModelEntityDAO#delete()
	 */
	public void unregister(SoftwareComponent componentToUnregister);

}
