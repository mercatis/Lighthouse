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
package com.mercatis.lighthouse3.service.commons.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;

/**
 * This class provides a default implementation of a JMS topic listener
 * listening to resource events published by the resource event topic publisher.
 * 
 * What it more or less does is that it connects to the topic in question,
 * receives and parses the message and then delegates to a resource event
 * listener instance.
 */
public class ResourceEventTopicSubscriber {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * The service container managing the resource being observed.
	 */
	private DomainModelEntityRestServiceContainer serviceContainer = null;

	/**
	 * The class of the resource observed.
	 */
	private Class<? extends DomainModelEntity> entityClass = null;

	/**
	 * The resource event listener to which resource events received via the
	 * topic will be delegated.
	 */
	private ResourceEventListener resourceEventListener = null;
	
	/**
	 * The settings loaded from the Configuration Server
	 */
	private Properties configuration = null;
	
	private String serviceName() {
		String packageName = ((Object) this.resourceEventListener).getClass().getPackage().getName();
		String serviceName = packageName.substring(packageName.lastIndexOf(".")).toLowerCase().substring(1);
		return serviceName;
	}

	private String entityClassName() {
		return this.entityClass.getSimpleName();
	}

	private String getJmsProviderPropertyName() {
		return "com.mercatis.lighthouse3.service." + this.serviceName() + ".jms.JmsProvider";
	}

	private String getJmsConfigFileLocationPropertyName() {
		return "com.mercatis.lighthouse3.service." + this.serviceName() + ".jms.JmsConfigFileLocation";
	}

	private String getJmsConfigResourcePropertyName() {
		return "com.mercatis.lighthouse3.service." + this.serviceName() + ".jms.JmsConfigResource";
	}

	private String getJmsProviderUrlPropertyName() {
		return "JmsProvider.URL";
	}

	private String getJmsProviderUserPropertyName() {
		return "JmsProvider.User";
	}

	private String getJmsProviderPasswordPropertyName() {
		return "JmsProvider.Password";
	}

	private String getNotificationTopicPropertyName() {
		return "Topic." + this.entityClassName() + "Updates";
	}

	private String getInitParameter(String parameter) {
		if(this.configuration != null) {
			return configuration.getProperty(parameter);
		}
		return this.serviceContainer.getInitParameter(parameter);
	}

	/**
	 * This property keeps a reference to the JMS provider to use.
	 */
	private JmsProvider jmsProvider = null;

	/**
	 * This property keeps a reference to an open JMS connection for listening
	 * to resource events to the notification topic.
	 */
	private JmsConnection jmsConnection = null;

	/**
	 * This property refers to the topic where resource events are published
	 */
	private Topic resourceEventPublicationTopic = null;

	/**
	 * This method sets up the JMS provider to use for receiving resource
	 * events.
	 */
	private void setUpJmsProvider() {
		if (this.getInitParameter(this.getJmsProviderPropertyName()) != null) {
			try {
				this.jmsProvider = (JmsProvider) Class
						.forName(this.getInitParameter(this.getJmsProviderPropertyName())).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method sets up the JMS connection and topic to use for publishing
	 * resource events to the notification topic.
	 */
	private void setUpJmsConnectionAndTopic() {
		Properties jmsConfig = new Properties();
		if(this.configuration != null) {
			jmsConfig = this.configuration;
		} else if (this.getInitParameter(this.getJmsConfigResourcePropertyName()) != null) {
			try {
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(this.getInitParameter(this.getJmsConfigResourcePropertyName()));
				jmsConfig.load(resourceAsStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getInitParameter(this.getJmsConfigFileLocationPropertyName()) != null) {
			try {
				jmsConfig.load(new FileInputStream(this.getInitParameter(this.getJmsConfigFileLocationPropertyName())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (jmsConfig.get(this.getJmsProviderUrlPropertyName()) != null) {
			this.jmsProvider.setProviderUrl((String) jmsConfig.get(this.getJmsProviderUrlPropertyName()));
		}

		if (jmsConfig.get(this.getJmsProviderUserPropertyName()) != null) {
			this.jmsProvider.setProviderUser((String) jmsConfig.get(this.getJmsProviderUserPropertyName()));
		}

		if (jmsConfig.get(this.getJmsProviderPasswordPropertyName()) != null) {
			this.jmsProvider.setProviderUserPassword((String) jmsConfig.get(this.getJmsProviderPasswordPropertyName()));
		}

		String topicStatusUpdatesName = "com.mercatis.lighthouse3.service." + this.entityClassName() + ".updates";

		if (jmsConfig.get(this.getNotificationTopicPropertyName()) != null)
			topicStatusUpdatesName = (String) jmsConfig.get(this.getNotificationTopicPropertyName());

		String clientId = null;
		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#service-" + this.serviceName() + "-"
					+ this.entityClassName().toLowerCase() + "-update-listener#" + System.currentTimeMillis();
			this.jmsProvider.setClientId(clientId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.jmsConnection = new JmsConnection(jmsProvider);
		this.resourceEventPublicationTopic = jmsProvider.getTopic(topicStatusUpdatesName);
	}

	/**
	 * This method sets up a listener resource events on the resource event
	 * publication topic.
	 */
	private void setUpResourceEventListener() {
		this.jmsConnection.registerDestinationConsumer(this.resourceEventPublicationTopic, new MessageListener() {

			public void onMessage(Message resourceEventMessage) {
				if (log.isDebugEnabled())
					log.debug(serviceName() + " received update of " + entityClassName());

				String resourceEventXml = null;
				try {
					TextMessage textMessage = (TextMessage) resourceEventMessage;
					resourceEventXml = textMessage.getText();
				} catch (ClassCastException e) {
					log.error("Non-text message received by " + serviceName(), e);

					return;
				} catch (JMSException e) {
					log.error("JMS exception while accessing " + entityClassName() + " update by " + serviceName()
							+ ".", e);

					return;
				}

				XmlMuncher xmlMuncher = new XmlMuncher(resourceEventXml);
				String updateKind = xmlMuncher.readValueFromXml("/*/:updateKind");
				String entityCodeOrId = xmlMuncher.readValueFromXml("/*/:" + entityClassName().toLowerCase() + "Code");

				if (entityCodeOrId == null) {
					log.error(serviceName() + " received invalid update of " + entityClassName());

					return;
				}

				if ("persisted".equals(updateKind))
					resourceEventListener.entityCreated(null, entityCodeOrId);
				else if ("updated".equals(updateKind))
					resourceEventListener.entityUpdated(null, entityCodeOrId);
				else if ("deleted".equals(updateKind))
					resourceEventListener.entityDeleted(null, entityCodeOrId);
				else {
					log.error(serviceName() + " received invalid update of " + entityClassName()
							+ ": invalid update kind given.");

					return;
				}

				if (log.isDebugEnabled())
					log.debug(serviceName() + " mirrored " + updateKind + " update of " + entityClassName());
			}
		});
	}

	/**
	 * Call this method to close the JMS connection and stop listening to
	 * resource events.
	 */
	public void stopJmsConnection() {
		this.jmsConnection.close();
	}

	/**
	 * The constructor for the topic subscriber.
	 * 
	 * @param entityClass
	 *            the class of the entities represented by the resource whose
	 *            events we are listening to.
	 * @param serviceContainer
	 *            the service container from which the subscriber will get the
	 *            JMS configuration values.
	 * @param resourceEventListener
	 *            the resource event listener to which resource events will be
	 *            delegated.
	 */
	public <Entity extends DomainModelEntity> ResourceEventTopicSubscriber(Class<Entity> entityClass,
			DomainModelEntityRestServiceContainer serviceContainer, ResourceEventListener resourceEventListener) {
		this.entityClass = entityClass;
		this.serviceContainer = serviceContainer;
		this.resourceEventListener = resourceEventListener;

		this.setUpJmsProvider();
		this.setUpJmsConnectionAndTopic();
		this.setUpResourceEventListener();
	}
	
	/**
	 * The constructor for the topic subscriber.
	 * 
	 * @param entityClass
	 *            the class of the entities represented by the resource whose
	 *            events we are listening to.
	 * @param serviceContainer
	 *            the service container from which the subscriber will get the
	 *            JMS configuration values.
	 * @param resourceEventListener
	 *            the resource event listener to which resource events will be
	 *            delegated.
	 * @param configuration
	 * 			  The settings loaded from the Configuration Server
	 */
	public <Entity extends DomainModelEntity> ResourceEventTopicSubscriber(Class<Entity> entityClass,
			DomainModelEntityRestServiceContainer serviceContainer, ResourceEventListener resourceEventListener, Properties configuration) {
		this.entityClass = entityClass;
		this.serviceContainer = serviceContainer;
		this.resourceEventListener = resourceEventListener;
		this.configuration = configuration;
		
		this.setUpJmsProvider();
		this.setUpJmsConnectionAndTopic();
		this.setUpResourceEventListener();
	}
}
