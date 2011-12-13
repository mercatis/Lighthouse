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
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * This class provides a resource event listener that publishes resource events
 * on a JMS topic.
 * 
 * The publisher reacts to the following servlet init parameters:
 * 
 * <ul>
 * <li>
 * <code>com.mercatis.lighthouse3.service.{entityServiceName}.jms.JmsProvider</code>
 * for passing the class name of the implementation of the
 * <code>JmsProvider</code> interface for the JMS broker to use.
 * <li>
 * <code>com.mercatis.lighthouse3.service.{entityServiceName}.jms.JmsConfigResource</code>
 * for passing the resource name of the configuration file with the JMS
 * settings.
 * <li>
 * <code>com.mercatis.lighthouse3.service.{entityServiceName}.jms.JmsConfigFileLocation</code>
 * for passing the path to the configuration file with the JMS settings.
 * </ul>
 * 
 * <code>{entityServiceName}</code> denotes the lowercase last package element
 * of the domain model entities managed by the resource observed by the present
 * resource event topic publisher.
 * 
 * The JMS configuration file expects the following properties:
 * <ul>
 * <li> <code>JmsProvider.URL</code> the JMS provider URL to use for publication.
 * <li> <code>JmsProvider.User</code> the JMS user to use.
 * <li> <code>JmsProvider.Password</code> the JMS user's password to use.
 * <li> <code>JmsProvider.Topic.{EntityClass}Updates</code> the name of the topic
 * to use for publication. <code>{EntityClass}</code> denotes the camel case
 * class name of the domain model entities managed by the resource observed by
 * the present resource event topic publisher.
 * </ul>
 */
public class ResourceEventTopicPublisher implements ResourceEventListener {

	/**
	 * The service container managing the resource being observed.
	 */
	private DomainModelEntityRestServiceContainer serviceContainer = null;

	/**
	 * The class of the resource observed.
	 */
	private Class<? extends DomainModelEntity> entityClass = null;

	private String entityServiceName() {
		String packageName = this.entityClass.getPackage().getName();
		String serviceName = packageName.substring(packageName.lastIndexOf(".")).toLowerCase().substring(1);
		return serviceName;
	}

	private String entityClassName() {
		return this.entityClass.getSimpleName();
	}

	private String getJmsProviderPropertyName() {
		Logger.getLogger(ResourceEventTopicPublisher.class).debug("JmsProviderPropertyName: '" + "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsProvider" + "'.");
		return "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsProvider";
	}

	private String getJmsConfigFileLocationPropertyName() {
		Logger.getLogger(ResourceEventTopicPublisher.class).debug("JmsConfigFileLocationPropertyName: '" + "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsConfigFileLocation" + "'.");
		return "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsConfigFileLocation";
	}

	private String getJmsConfigResourcePropertyName() {
		Logger.getLogger(ResourceEventTopicPublisher.class).debug("JmsConfigResourcePropertyName: '" + "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsConfigResource" + "'.");
		return "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".jms.JmsConfigResource";
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
		Logger.getLogger(ResourceEventTopicPublisher.class).debug("JmsConfigResourcePropertyName: '" + "Topic." + this.entityClassName() + "Updates" + "'.");
		return "Topic." + this.entityClassName() + "Updates";
	}

	private String getInitParameter(String parameter) {
		return this.serviceContainer.getInitParameter(parameter);
	}

	/**
	 * This property keeps a reference to the JMS provider to use.
	 */
	private JmsProvider jmsProvider = null;

	/**
	 * This property keeps a reference to an open JMS connection for publishing
	 * resource events to the notification topic.
	 */
	private JmsConnection jmsConnection = null;

	/**
	 * This property refers to the topic for publishing resource events
	 */
	private Topic resourceEventPublicationTopic = null;

	/**
	 * This method sets up the JMS provider to use for publishing resource
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
		String initParameter = this.getInitParameter(this.getJmsConfigResourcePropertyName());
		if (initParameter != null) {
			Logger.getLogger(ResourceEventTopicPublisher.class).debug("Loading resources '" + initParameter + "'.");
			try {
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(initParameter);
				jmsConfig.load(resourceAsStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Logger.getLogger(ResourceEventTopicPublisher.class).debug("No resources configured.");
		}

		if (this.getInitParameter(this.getJmsConfigFileLocationPropertyName()) != null) {
			Logger.getLogger(ResourceEventTopicPublisher.class).debug("Loading config file '" + this.getInitParameter(this.getJmsConfigFileLocationPropertyName()) + "'.");
			try {
				jmsConfig.load(new FileInputStream(this.getInitParameter(this.getJmsConfigFileLocationPropertyName())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Logger.getLogger(ResourceEventTopicPublisher.class).debug("No config file configured.");
		}

		if (jmsConfig.get(this.getJmsProviderUrlPropertyName()) != null) {
			String providerUrl = (String) jmsConfig.get(this.getJmsProviderUrlPropertyName());
			Logger.getLogger(ResourceEventTopicPublisher.class).debug("Setting ProviderURL to '" + providerUrl + "'.");
			this.jmsProvider.setProviderUrl(providerUrl);
		}

		if (jmsConfig.get(this.getJmsProviderUserPropertyName()) != null) {
			this.jmsProvider.setProviderUser((String) jmsConfig.get(this.getJmsProviderUserPropertyName()));
		}

		if (jmsConfig.get(this.getJmsProviderPasswordPropertyName()) != null) {
			this.jmsProvider.setProviderUserPassword((String) jmsConfig.get(this.getJmsProviderPasswordPropertyName()));
		}

		String topicStatusUpdatesName = "com.mercatis.lighthouse3.service." + this.entityServiceName() + ".updates";

		if (jmsConfig.get(this.getNotificationTopicPropertyName()) != null)
			topicStatusUpdatesName = (String) jmsConfig.get(this.getNotificationTopicPropertyName());

		String clientId = null;
		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#service-" + this.entityServiceName()
					+ "-updates#" + System.currentTimeMillis();
			this.jmsProvider.setClientId(clientId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.jmsConnection = new JmsConnection(jmsProvider);
		this.resourceEventPublicationTopic = jmsProvider.getTopic(topicStatusUpdatesName);
	}

	/**
	 * This method publishes a resource event about an entity to the
	 * notification topic.
	 * 
	 * @param updateKind
	 *            the kind of the update, either <code>ENTITY_CREATED</code>,
	 *            <code>ENTITY_UPDATED</code>, or <code>ENTITY_DELETED</code>.
	 * @param entityCode
	 *            the id of the updated entity.
	 */
	private void publishResourceEventNotificationOnTopic(final int updateKind, final String entityCode) {
		this.jmsConnection.sendToDestination(resourceEventPublicationTopic, new JmsMessageCreator() {
			public Message createMessage(Session jmsSession) throws JMSException {
				StringWriter notification = new StringWriter();

				try {
					XmlWriter xml = new XmlEncXmlWriter(notification);
					xml.writeEntity(entityClassName() + "Update");
					xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

					String updateType = null;
					if (updateKind == ENTITY_CREATED)
						updateType = "persisted";
					else if (updateKind == ENTITY_DELETED)
						updateType = "deleted";
					else
						updateType = "updated";

					xml.writeEntityWithText("updateKind", updateType);
					xml.writeEntityWithText(entityClassName().toLowerCase() + "Code", entityCode);

					xml.endEntity();
				} catch (IOException e) {
					throw new JMSException("Could not create resource event notification message.");
				}

				TextMessage message = jmsSession.createTextMessage();
				message.setText(notification.toString());

				return message;
			}

		});
	}

	public void entityCreated(DomainModelEntityResource<?> resource, String entityIdOrCode) {
		this.publishResourceEventNotificationOnTopic(ENTITY_CREATED, entityIdOrCode);
	}

	public void entityDeleted(DomainModelEntityResource<?> resource, String entityIdOrCode) {
		this.publishResourceEventNotificationOnTopic(ENTITY_DELETED, entityIdOrCode);
	}

	public void entityUpdated(DomainModelEntityResource<?> resource, String entityIdOrCode) {
		this.publishResourceEventNotificationOnTopic(ENTITY_UPDATED, entityIdOrCode);
	}

	/**
	 * The constructor for the resource event topic publisher. It connects to
	 * the JMS provider and topic.
	 * 
	 * @param entityClass
	 *            the entity class managed by the resource whose events are
	 *            being observed.
	 * @param serviceContainer
	 *            the service container managing the resource whose events are
	 *            being observed.
	 */
	public <Entity extends DomainModelEntity> ResourceEventTopicPublisher(Class<Entity> entityClass,
			Class<? extends DomainModelEntityDAO<Entity>> entityRegistryClass,
			DomainModelEntityRestServiceContainer serviceContainer) {
		this.entityClass = entityClass;
		this.serviceContainer = serviceContainer;
		this.setUpJmsProvider();
		this.setUpJmsConnectionAndTopic();
	}
}
