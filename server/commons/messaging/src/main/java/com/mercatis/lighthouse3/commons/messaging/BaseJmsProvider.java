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
 * This class provides a simple base implementation jacket for JMS providers.
 */
public abstract class BaseJmsProvider implements JmsProvider {

	private String providerUrl = null;
	private String providerUser = null;
	private String providerUserPassword = null;
	private String clientId = null;

	/**
	 * This method returns the configured provider URL.
	 * 
	 * @return the URL.
	 */
	public String getProviderUrl() {
		return this.providerUrl;
	}

	/**
	 * This method returns the configured user name to access the provider.
	 * 
	 * @return the user name.
	 */
	public String getProviderUser() {
		return this.providerUser;
	}

	/**
	 * This method returns the configured password to access the provider.
	 * 
	 * @return the password.
	 */
	public String getProviderUserPassword() {
		return this.providerUserPassword;
	}

	/**
	 * This method returns the configured client Id with which to connect to the
	 * provider.
	 * 
	 * @return the client Id.
	 */
	public String getClientId() {
		return this.clientId;
	}

	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public void setProviderUser(String user) {
		this.providerUser = user;
	}

	public void setProviderUserPassword(String password) {
		this.providerUserPassword = password;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	abstract public ConnectionFactory getConnectionFactory();

	abstract public Queue getQueue(String queueName);

	abstract public Topic getTopic(String topicName);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			BaseJmsProvider clone = (BaseJmsProvider) super.clone();
			clone.clientId = this.clientId;
			clone.providerUrl = this.providerUrl;
			clone.providerUser = this.providerUser;
			clone.providerUserPassword = this.providerUserPassword;
			
			return clone;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

}