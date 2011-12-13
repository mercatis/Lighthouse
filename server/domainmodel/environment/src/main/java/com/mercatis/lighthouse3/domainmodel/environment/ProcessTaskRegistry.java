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

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * The process / task registry interface abstractly defines the contract for a
 * persistent repository of processes / tasks. It provides basic CRUD
 * functionality. It can be implemented using different persistence
 * technologies.
 * 
 * With regard to persistence, the following rules must apply for
 * implementations:
 * 
 * <ul>
 * <li>processes / tasks are uniquely identified by their <code>id</code>.
 * <li>processes / tasks are uniquely identified by their <code>code</code>.
 * <li>processes / tasks are always fully loaded including their child processes
 * / tasks.
 * <li>processes / tasks are always fully loaded including their parent
 * processes / tasks . The parent processes / tasks do not have to be fully
 * loaded though.
 * <li>processes / tasks are loaded including the deployments attached to them.
 * These in turn follow the usual deployment loading rules.
 * <li>processes / tasks are persisted along with their attached deployments.
 * <li>processes / tasks are persisted along with their child processes / tasks
 * if those have not yet been persisted.
 * </ul>
 */
public interface ProcessTaskRegistry extends DeploymentCarryingDomainModelEntityDAO<ProcessTask> {

	/**
	 * A synonym for <code>persist()</code>.
	 * 
	 * @see DomainModelEntityDAO#persist()
	 */
	public void register(ProcessTask processToRegister);

	/**
	 * A synonym for <code>update()</code>.
	 * 
	 * @see DomainModelEntityDAO#update()
	 */
	public void reRegister(ProcessTask processToReRegister);

	/**
	 * A synonym for <code>delete()</code>.
	 * 
	 * @see DomainModelEntityDAO#delete()
	 */
	public void unregister(ProcessTask processToUnregister);

	/**
	 * This method looks up all processes / tasks to which a given deployment is
	 * attached.
	 * 
	 * @param deployment
	 *            the deployment of interest.
	 * @return the set of processes / tasks attached to the deployment.
	 */
	public List<ProcessTask> findForDeployment(Deployment deployment);

}
