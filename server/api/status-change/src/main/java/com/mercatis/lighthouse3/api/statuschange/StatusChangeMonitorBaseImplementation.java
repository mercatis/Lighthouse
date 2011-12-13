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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.MessageListener;
import javax.jms.Topic;

import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;

/**
 * This base class provides some boilerplate implementation of the basic methods
 * of the status change monitor API.
 */
public abstract class StatusChangeMonitorBaseImplementation implements StatusChangeMonitor {

	/**
	 * This property keeps the registered listeners.
	 */
	private List<StatusChangeListener> registeredListeners = new ArrayList<StatusChangeListener>();

	synchronized public void registerListener(StatusChangeListener statusChangeListener) {
		if (!this.registeredListeners.contains(statusChangeListener))
			this.registeredListeners.add(statusChangeListener);
	}

	synchronized public void unregisterListener(StatusChangeListener statusChangeListener) {
		this.registeredListeners.remove(statusChangeListener);
	}

	synchronized public List<StatusChangeListener> getRegisteredListeners() {
		return new ArrayList<StatusChangeListener>(this.registeredListeners);
	}

	synchronized public void unregisterAllListeners() {
		this.registeredListeners.clear();
	}

	/**
	 * The JMS connection via which the monitor is listening on the status
	 * change topic.
	 */
	private JmsConnection statusChangeTopicListener = null;

	/**
	 * This property keeps a reference to the JMS provide to use for creating
	 * the JMS connection via which the monitor is listening on the status
	 * change topic.
	 */
	private JmsProvider jmsProvider = null;

	/**
	 * This property keeps a reference to the JMS topic via which the monitor is
	 * listening to status changes
	 */
	private Topic statusChangeTopic = null;

	/**
	 * The property references the listener on the JMS topic via which the
	 * monitor is listening to status changes.
	 */
	private MessageListener statusChangeTopicMessageListener = null;

	/**
	 * This method sets us the listener on the JMS topic on which the status
	 * monitor service publishes its status change messages.
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
	public void setUpStatusChangeTopicListener(Class<? extends JmsProvider> jmsProviderClass, String jmsProviderUrl,
			String jmsUser, String jmsPassword, String jmsStatusChangeTopic, MessageListener statusChangeTopicListener) {
		this.jmsProvider = null;

		try {
			this.jmsProvider = jmsProviderClass.newInstance();
			this.jmsProvider.setProviderUrl(jmsProviderUrl);
			this.jmsProvider.setProviderUser(jmsUser);
			this.jmsProvider.setProviderUserPassword(jmsPassword);
			this.jmsProvider.setClientId(InetAddress.getLocalHost().getHostAddress() + "#api-status-change#"
					+ System.currentTimeMillis());
		} catch (InstantiationException e) {
			throw new StatusChangeMonitorException("Could not instantiate JMS provider", e);
		} catch (IllegalAccessException e) {
			throw new StatusChangeMonitorException("Could not access JMS provider", e);
		} catch (UnknownHostException e) {
			throw new StatusChangeMonitorException("Could access host name", e);
		}

		this.statusChangeTopic = this.jmsProvider.getTopic(jmsStatusChangeTopic);
		this.statusChangeTopicMessageListener = statusChangeTopicListener;
	}

	public void start() {
		this.statusChangeTopicListener = new JmsConnection(this.jmsProvider);
		this.statusChangeTopicListener.registerDestinationConsumer(this.statusChangeTopic,
				this.statusChangeTopicMessageListener);
	}

	public void stop() {
		if (this.statusChangeTopicListener != null)
			this.statusChangeTopicListener.close();
	}

}