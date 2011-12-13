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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

/**
 * This class implements the <code>JmsProvider</code> interface for ActiveMQ.
 */
public class ActiveMQProvider extends BaseJmsProvider {

	public ConnectionFactory getConnectionFactory() {
		ActiveMQConnectionFactory factory = null;
		if (this.getProviderUser() != null && this.getProviderUserPassword() != null) {
			factory = new ActiveMQConnectionFactory(this.getProviderUser(), this.getProviderUserPassword(), this.getProviderUrl());
		} else {
			factory = new ActiveMQConnectionFactory(this.getProviderUrl());
		}

		factory.setClientID(this.getClientId());

		return factory;
	}

	public Queue getQueue(String queueName) {
		return new ActiveMQQueue(queueName);
	}

	public Topic getTopic(String topicName) {
		return new ActiveMQTopic(topicName);
	}
}
