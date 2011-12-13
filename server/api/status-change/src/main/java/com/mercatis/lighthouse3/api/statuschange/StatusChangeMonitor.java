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

import java.util.List;

/**
 * This interface offers an API for client applications to connect to the status
 * monitor service. It allows those clients to be notified about status changes
 * occurring on the server side and broadcasted on the status change topic. This
 * API, for instance, can be used to implement traffic light-like visualizations
 * of status.
 * 
 * There are two implementation variants of the API:
 * 
 * <ul>
 * <li>a basic one which just notifies clients about status changes, leaving the
 * responsibility to get information about the status that changed to the
 * clients.
 * <li>a resolving variant which notifies clients about status changes,
 * including all data about the status that changed.
 * </ul>
 */
public interface StatusChangeMonitor {

	/**
	 * Register a status change listener with the monitor.
	 * 
	 * @param statusChangeListener
	 *            the listener to register.
	 */
	public void registerListener(StatusChangeListener statusChangeListener);

	/**
	 * Removes a registered status change listener.
	 * 
	 * @param statusChangeListener
	 *            the status change listener to remove.
	 */
	public void unregisterListener(StatusChangeListener statusChangeListener);

	/**
	 * Removes all registered status change listeners.
	 */
	public void unregisterAllListeners();

	/**
	 * This method returns all listeners registered with the present status
	 * change monitor.
	 * 
	 * @return the list of registerd listeners.
	 */
	public List<StatusChangeListener> getRegisteredListeners();

	/**
	 * This method starts the activity of the status change monitor.
	 */
	public void start();

	/**
	 * This method stops the activity of the status change monitor.
	 */
	public void stop();
}
