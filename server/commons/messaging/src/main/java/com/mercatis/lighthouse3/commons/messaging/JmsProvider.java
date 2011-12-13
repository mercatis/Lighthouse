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
package com.mercatis.lighthouse3.commons.messaging;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * This interface hides JMS provider-specific code from the event logger
 * service.
 */
public interface JmsProvider extends Cloneable {

	/**
	 * Call this method to set the URL of the JMS provider to use.
	 * 
	 * @param providerUrl
	 *            the URL to set.
	 */
	public void setProviderUrl(String providerUrl);

	/**
	 * Call this method to set the user name under which to access the provider.
	 * 
	 * @param user
	 *            the user name to set.
	 */
	public void setProviderUser(String user);

	/**
	 * Call this method to set the password for the user under which to access
	 * the JMS provider.
	 * 
	 * @param password
	 *            the password to set.
	 */
	public void setProviderUserPassword(String password);

	/**
	 * This method returns the configured client Id with which to connect to the
	 * provider.
	 * 
	 * @return the client Id.
	 */
	public String getClientId();
	
	/**
	 * Call this method to set the client Id with which to connect to the JMS
	 * provider.
	 * 
	 * @param clientId
	 *            the client Id
	 */
	public void setClientId(String clientId);

	/**
	 * This method returns a connection factory for a given JMS provider using
	 * the given provider URL, user, and password.
	 * 
	 * @return the connection factory
	 * 
	 */
	public ConnectionFactory getConnectionFactory();

	/**
	 * This method is to construct a JMS queue destination.
	 * 
	 * @param queueName
	 *            the name of the JMS queue destination
	 * @return the JMS queue destination
	 */
	public Queue getQueue(String queueName);

	/**
	 * This method is to construct a JMS topic destination.
	 * 
	 * @param topicName
	 *            the name of the JMS topic destination
	 * @return the JMS topic destination
	 */
	public Topic getTopic(String topicName);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone();
	
}
