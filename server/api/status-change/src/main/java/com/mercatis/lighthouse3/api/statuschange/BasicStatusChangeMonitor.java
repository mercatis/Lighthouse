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

/**
 * This class provides a basic status change monitor implementation. It merely
 * notifies its listeners about status changes without linking them to the
 * status to which they refer.
 */
public class BasicStatusChangeMonitor extends StatusChangeMonitorBaseImplementation {

	/**
	 * The constructor for setting up the basic status change monitor
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
	 * @throws StatusChangeMonitorException
	 *             in case set up failed
	 */
	public BasicStatusChangeMonitor(Class<? extends JmsProvider> jmsProviderClass, String jmsProviderUrl,
			String jmsUser, String jmsPassword, String jmsStatusChangeTopic, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {

		this.setUpStatusChangeTopicListener(jmsProviderClass, jmsProviderUrl, jmsUser, jmsPassword,
				jmsStatusChangeTopic, new BasicStatusChangeTopicListener(this, deploymentRegistry, softwareComponentRegistry));
	}
}
