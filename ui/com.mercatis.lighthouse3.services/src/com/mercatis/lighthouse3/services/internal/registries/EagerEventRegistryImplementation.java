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
package com.mercatis.lighthouse3.services.internal.registries;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.Aggregation;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand;
import com.mercatis.lighthouse3.persistence.events.rest.EventRegistryImplementation;


public class EagerEventRegistryImplementation extends EagerDomainModelEntityDAOImplementation<Event> implements
		EventRegistry {

	private EventRegistry delegateRegistry;
	
	public EagerEventRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry, String user, String password) {
		delegateRegistry = new EventRegistryImplementation(serverUrl, deploymentRegistry, softwareComponentRegistry, user, password);
	}
	
	public EagerEventRegistryImplementation(String serverUrl, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {
		this(serverUrl, deploymentRegistry, softwareComponentRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Event> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries.EagerDomainModelEntityDAOImplementation#keyForEntity(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	@Override
	protected String keyForEntity(Event entity) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#aggregate(com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationCommand)
	 */
	public Aggregation aggregate(AggregationCommand command) {
		return delegateRegistry.aggregate(command);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#getAllDeployments()
	 */
	public Deployment[] getAllDeployments() {
		return delegateRegistry.getAllDeployments();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#getAllUdfNames()
	 */
	public String[] getAllUdfNames() {
		return delegateRegistry.getAllUdfNames();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#log(com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void log(Event event) {
		delegateRegistry.log(event);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#log(com.mercatis.lighthouse3.domainmodel.environment.Deployment, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void log(Deployment context, Event event) {
		delegateRegistry.log(context, event);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.events.EventRegistry#log(com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry, com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry, java.lang.String, java.lang.String, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void log(DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry,
			String location, String componentCode, Event event) {
		delegateRegistry.log(deploymentRegistry, softwareComponentRegistry, location, componentCode, event);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class<?> getManagedType() {
		return Event.class;
	}
}
