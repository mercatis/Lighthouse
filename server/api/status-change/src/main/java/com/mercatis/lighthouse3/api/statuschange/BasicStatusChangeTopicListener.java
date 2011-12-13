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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;

/**
 * This class implements a listener for the status change topic that just
 * performs basic deserialization and notification of status change messages
 * published by the status change monitor service. The status to which a change
 * belongs is not resolved.
 */
public class BasicStatusChangeTopicListener implements MessageListener {

	/**
	 * The status change monitor to which the listener belongs
	 */
	protected StatusChangeMonitorBaseImplementation statusChangeMonitor = null;
	
	protected DeploymentRegistry deploymentRegistry = null;
	
	protected SoftwareComponentRegistry softwareComponentRegistry = null;

	/**
	 * This method assembles a status change notification according to the
	 * semantics of the present status change monitor implementation from a
	 * status change message obtained from the status monitor service.
	 * 
	 * @param message
	 *            the status change message obtained from the status monitor
	 *            service
	 * @return the status change notification assembled
	 * @throws XMLSerializationException
	 *             in case of parsing errors.
	 * @throws PersistenceException
	 *             in case of trouble when accessing any registries if that
	 *             should be required.
	 */
	public StatusChangeNotification assembleStatusChangeNotification(String message) {
		return StatusChangeNotification.parseStatusChangeNotificationMessage(message, deploymentRegistry, softwareComponentRegistry);
	}

	public void onMessage(Message statusChangeMessage) {
		try {
			StatusChangeNotification notification = this.assembleStatusChangeNotification(((TextMessage) statusChangeMessage).getText());

			for (StatusChangeListener listener : this.statusChangeMonitor.getRegisteredListeners())
				listener.statusChanged(notification.getStatusPath(), notification.getStatusCode(), notification
						.getStatusChange(), notification.getPriorChange());

		} catch (Exception e) {
			e.printStackTrace();

			return;
		}
	}

	/**
	 * The constructor.
	 * 
	 * @param statusChangeMonitor
	 *            the status change monitor installing the status change topic
	 *            listener
	 */
	public BasicStatusChangeTopicListener(StatusChangeMonitorBaseImplementation statusChangeMonitor, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {
		this.statusChangeMonitor = statusChangeMonitor;
		this.deploymentRegistry = deploymentRegistry;
		this.softwareComponentRegistry = softwareComponentRegistry;
	}
}
