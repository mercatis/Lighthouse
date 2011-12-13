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

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;

/**
 * This class implements a listener for the status change topic that not just
 * performs basic deserialization and notification of status change messages
 * published by the status change monitor service but also resolves the status
 * to which the status change refers.
 */
public class ResolvingStatusChangeTopicListener extends BasicStatusChangeTopicListener {

	private ResolvingStatusChangeMonitor getResolvingStatusChangeMonitor() {
		return ((ResolvingStatusChangeMonitor) this.statusChangeMonitor);
	}

	private StatusRegistry getStatusRegistry() {
		if (this.getResolvingStatusChangeMonitor() == null)
			return null;
		else
			return this.getResolvingStatusChangeMonitor().getStatusRegistry();
	}

	@Override
	public StatusChangeNotification assembleStatusChangeNotification(String message) {
		StatusChangeNotification notification = super.assembleStatusChangeNotification(message);

		if (this.getStatusRegistry() == null)
			throw new PersistenceException("No status registry available with resolving status change monitor", null);

		Status status = getStatusRegistry().findByCode(notification.getStatusCode());

		if (status == null)
			throw new PersistenceException("Could not find status with code: " + notification.getStatusCode(), null);

		notification.getStatusChange().setStatus(status);
		
		if (notification.getPriorChange() != null) {
			notification.getPriorChange().setStatus(status);
		}

		return notification;
	}

	/**
	 * The constructor.
	 * 
	 * @param statusChangeMonitor
	 *            the resolving status change monitor installing the status
	 *            change topic listener
	 */
	public ResolvingStatusChangeTopicListener(ResolvingStatusChangeMonitor statusChangeMonitor, DeploymentRegistry deploymentRegistry, SoftwareComponentRegistry softwareComponentRegistry) {
		super(statusChangeMonitor, deploymentRegistry, softwareComponentRegistry);
	}

}
