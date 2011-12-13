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
package com.mercatis.lighthouse3.api.statuschange;

import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;

/**
 * This class provides a status monitor implementation that resolves the
 * reference to the status to which a status change refers to. For this purpose,
 * it needs a reference to a status registry. Note that if no caching is
 * involved for status carriers like deployments, processes, and environments,
 * using this monitor may be expensive.
 * 
 * Caching status on the client side is not advisable since a status has just
 * changed when a status change is received.
 */
public class ResolvingStatusChangeMonitor extends StatusChangeMonitorBaseImplementation {

	/**
	 * The status registry to use to resolve status references.
	 */
	private StatusRegistry statusRegistry = null;

	/**
	 * This method returns the status registry to use to resolve status
	 * references.
	 * 
	 * @return the status registry.
	 */
	public StatusRegistry getStatusRegistry() {
		return this.statusRegistry;
	}

	/**
	 * The constructor for setting up the resolving status change monitor
	 * implementation.
	 * 
	 * @param jmsProviderClass
	 *            the JMS provider implementation to use to connect to the
	 *            status change notification topic.
	 * @param jmsProviderUrl
	 *            the URL of the JMS provider where the status monitor service
	 *            publishes status changes.
	 * @param jmsUser
	 *            the user to use to connect to the provider.
	 * @param jmsPassword
	 *            the password to use to connect to the provider.
	 * @param jmsStatusChangeTopic
	 *            the name of the topic where the status monitor service
	 *            publishes status changes.
	 * @param statusRegistry
	 *            the status registry to use to resolves the status for a status
	 *            change.
	 * @throws StatusChangeMonitorException
	 *             in case set up failed
	 */
	public ResolvingStatusChangeMonitor(Class<? extends JmsProvider> jmsProviderClass, String jmsProviderUrl,
			String jmsUser, String jmsPassword, String jmsStatusChangeTopic, StatusRegistry statusRegistry, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {

		this.setUpStatusChangeTopicListener(jmsProviderClass, jmsProviderUrl, jmsUser, jmsPassword,
				jmsStatusChangeTopic, new ResolvingStatusChangeTopicListener(this, deploymentRegistry, softwareComponentRegistry));

		this.statusRegistry = statusRegistry;
	}
}
