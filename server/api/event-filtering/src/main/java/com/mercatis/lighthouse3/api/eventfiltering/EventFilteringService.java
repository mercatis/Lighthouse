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
package com.mercatis.lighthouse3.api.eventfiltering;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.HttpException;
import com.mercatis.lighthouse3.commons.commons.HttpRequest;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This class provides decent access to the event filtering part of the event
 * logger REST web service. This service permits registration of event templates
 * as filters with the event logger. After registration, matching events, both
 * live and past events, are then published back to the caller via a JMS topic
 * in an asynchronous fashion. Registered filters have to be refreshed regularly
 * lest they expire. The expiration time interval is configured with the event
 * logger service.
 * 
 * This class supports two modes of operation:
 * <ul>
 * <li>a base mode, in which clients receive the JMS message with XML format of
 * matching events directly from the JMS topic.
 * <li>a resolved mode, in which clients received the readily resolved and
 * parsed <code>Event</code> objects of matching events. While more comfortable
 * to use, this mode has the disadvantage that it fully relies on the
 * implementations the deployment and software component registries that need to
 * be passed for resolving the event context. This may be inefficient if the
 * registries do not implement a kind of caching (which the default REST
 * implementations for instance don't).
 * </ul>
 * 
 * The following protocol should be obeyed:
 * <ul>
 * <li>Call one of the constructors to setup WS and JMS connectivity in base or
 * resolved mode.
 * <li>call the <code>startEventListening()</method> to really connect to JMS.
 * <li> register an arbitrary number of templates as filter via <code>registerEventFilter()</code>
 * <li>refresh registered filters as you see fit via
 * <code>refreshEventFilter()</code>
 * <li>unregister templates as you see fit via
 * <code>deregisterEventFilter()</code>
 * <li>call the <code>stopEventListening()</method> to disconnect from JMS.
 * </ul>
 */
public class EventFilteringService {

	private class EventListenerAdapter implements MessageListener {

		private EventListener adaptedEventListener = null;

		private DeploymentRegistry deploymentRegistry = null;

		private SoftwareComponentRegistry softwareComponentRegistry = null;

		public void onMessage(Message eventMessage) {
			String messageText = null;

			try {
				messageText = ((TextMessage) eventMessage).getText();
			} catch (JMSException e) {
				return;
			}
			
			String filterUUID = null;
			try {
				filterUUID = eventMessage.getJMSCorrelationID();
			} catch (JMSException e) {
				filterUUID = null;
			}

			List<Event> eventsInMessage = new LinkedList<Event>();

			String[] messageParts = messageText
					.split("((<events>)|(</events>)|(<separator/>))");

			for (int i = 0; i < messageParts.length; i++) {
				String messagePart = messageParts[i];

				if (!messagePart.equals("")) {
					Event event = new Event();
					event.fromXml(messagePart, this.deploymentRegistry,
							this.softwareComponentRegistry);
					event.setFilterOfOrigin(filterUUID);
					eventsInMessage.add(event);
				}

			}

			this.adaptedEventListener.onEvents(eventsInMessage);
		}

		public EventListenerAdapter(EventListener adaptedEventListener,
				DeploymentRegistry deploymentRegistry,
				SoftwareComponentRegistry softwareComponentRegistry) {
			this.adaptedEventListener = adaptedEventListener;
			this.deploymentRegistry = deploymentRegistry;
			this.softwareComponentRegistry = softwareComponentRegistry;
		}
	}

	/**
	 * Optional user to send to eventlogger when registering an eventfilter
	 */
	private String wsUser = null;
	
	/**
	 * Optional password to send to eventlogger when registering an eventfilter
	 */
	private String wsPassword = null;
	
	/**
	 * This property maintains the client ID with which to connect to the JMS
	 * event notification topic.
	 */
	private String jmsClientId = null;

	/**
	 * This property maintains a reference to JMS topic listening connection.
	 */
	private JmsConnection jmsConnection = null;

	/**
	 * This property keeps a reference to the JMS provider used
	 */
	private JmsProvider jmsProvider = null;

	/**
	 * This property maintains a reference to the URL of the event filter web
	 * service within the event logger service.
	 */
	private String eventLoggerServiceUrl = null;

	/**
	 * Here we keep a reference to the name of the JMS topic used for event
	 * notification.
	 */
	private String jmsEventNotificationTopic = null;

	/**
	 * The event notification listener being called when an event arrives on the
	 * topic.
	 */
	private MessageListener jmsEventNotificationListener = null;

	/**
	 * This method returns the event notification listener being called when an
	 * event arrives on the topic.
	 * 
	 * @return the listener
	 */
	public MessageListener getJmsEventNotificationListener() {
		return this.jmsEventNotificationListener;
	}

	/**
	 * The event listener being called when a matching event arrives in resolved
	 * mode.
	 */
	private EventListener eventNotificationListener = null;

	/**
	 * This method returns the event listener being called when a matching event
	 * arrives in resolved mode.
	 * 
	 * @return the listener
	 */
	public EventListener getEventNotificationListener() {
		return this.eventNotificationListener;
	}

	/**
	 * This method starts listening for events on the JMS topic.
	 * 
	 * @throws EventFilteringException
	 *             in case of JMS problems.
	 */
	public void startEventListening() {
		try {
			this.jmsConnection = new JmsConnection(this.jmsProvider);
			this.jmsConnection.registerDestinationConsumer(this.jmsProvider.getTopic(this.jmsEventNotificationTopic), "LH3TargetClientId = '" + this.jmsClientId + "'", this.jmsEventNotificationListener);
		} catch (Exception e) {
			throw new EventFilteringException("Could not start event topic listener", e);
		}
	}

	/**
	 * This method starts listening for events on the JMS topic.
	 * 
	 * @throws EventFilteringException
	 *             in case of JMS problems.
	 */
	public void startEventListeningWithException() throws EventFilteringException {
		try {
			this.jmsConnection = new JmsConnection(this.jmsProvider, false);
			this.jmsConnection.registerDestinationConsumer(this.jmsProvider.getTopic(this.jmsEventNotificationTopic), "LH3TargetClientId = '" + this.jmsClientId + "'", this.jmsEventNotificationListener);
		} catch (Exception e) {
			throw new EventFilteringException("Could not start event topic listener", e);
		}
	}

	/**
	 * This method stops listening for events on the JMS topic.
	 * 
	 * @throws EventFilteringException
	 *             in case of JMS problems.
	 */
	public void stopEventListening() {
		try {
			this.jmsConnection.close();
		} catch (Exception e) {
			throw new EventFilteringException(
					"Could not stop event topic listener", e);
		}
	}

	/**
	 * Via this method you can register an event filter with the event logger
	 * service.
	 * 
	 * @param template
	 *            the event template to register as a filter.
	 * @param limit
	 *            the number of events to fetch max
	 * @return the UUID event filter registration handle as assigned by the
	 *         event logger service.
	 * @throws EventFilteringException
	 *             in the registration request returns anything else than 200
	 *             OK.
	 */
	public String registerEventFilter(Event template, int limit) {
		StringWriter postBody = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(postBody);
		try {
			xml.writeEntity("EventListener");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);
			xml.writeEntityWithText("clientId", this.jmsClientId);
			xml.writeEntity("eventTemplate");
			template.fillRootElement(xml);
			xml.endEntity();
			xml.endEntity();
		} catch (IOException e) {
		}
		String requestResultBody = null;
		try {
			requestResultBody = new HttpRequest(wsUser, wsPassword).execute(
					this.eventLoggerServiceUrl + "/EventLogger/EventFilter?limit=" + limit,
					HttpMethod.POST, postBody.toString(), null);
		} catch (HttpException httpException) {
			throw new EventFilteringException(
					"Call of event listener registration web service failed",
					httpException);
		}

		return XmlMuncher.readValueFromXml(requestResultBody, "/*//:uuid");

	}

	/**
	 * Via this method you can refresh an event filter previously registered
	 * with the event logger service.
	 * 
	 * @param eventFilterRegistrationHandle
	 *            the uuid of registered event filter
	 * @throws EventFilteringException
	 *             in the refresh request returns anything else than 200 OK.
	 */
	public void refreshEventFilter(String eventFilterRegistrationHandle) {
		try {
			new HttpRequest(wsUser, wsPassword)
					.execute(this.eventLoggerServiceUrl
							+ "/EventLogger/EventFilter/"
							+ eventFilterRegistrationHandle, HttpMethod.PUT,
							null, null);
		} catch (HttpException httpException) {
			throw new EventFilteringException(
					"Call of event listener refresh web service failed",
					httpException);
		}
	}

	/**
	 * Via this method you can unregister an event filter previously registered
	 * with the event logger service.
	 * 
	 * @param eventFilterRegistrationHandle
	 *            the uuid of registered event filter
	 * @throws EventFilteringException
	 *             in the unregister request returns anything else than 200 OK.
	 */
	public void deregisterEventFilter(String eventFilterRegistrationHandle) {
		try {
			new HttpRequest(wsUser, wsPassword).execute(this.eventLoggerServiceUrl
					+ "/EventLogger/EventFilter/"
					+ eventFilterRegistrationHandle, HttpMethod.DELETE, null,
					null);
		} catch (HttpException httpException) {
			throw new EventFilteringException(
					"Call of event listener degregistration web service failed",
					httpException);
		}

	}

	/**
	 * This is the core constructor for setting up the event filtering service.
	 * It sets up the event filtering service in base mode. I.e., client
	 * listeners directly receive the JMS text messages about matching events
	 * from the JMS topic.
	 * 
	 * It requires a few parameters:
	 * 
	 * @param eventLoggerServiceUrl
	 *            the URL where the event logger RESTful web service is running.
	 * @param jmsProviderClass
	 *            the class of the JMS provider on which basis the JMS topic
	 *            notification is implemented. Such as
	 *            <code>ActiveMQProvider</code> or <code>EMSProvider</code>.
	 * @param jmsProviderUrl
	 *            the URL used to connect to the JMS provider
	 * @param jmsUser
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsPassword
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsEventNotificationTopic
	 *            the JMS topic on which event notification takes place. Usually
	 * 
	 *            <code>com.mercatis.lighthouse3.service.eventlogger.events.notification</code>
	 *            .
	 * @param jmsClientId
	 *            the client Id to use for connecting to the JMS topic.
	 * @param eventNotificationListener
	 *            the callback to which the XML representation of received
	 *            events is passed.
	 * 
	 * @throws EventFilteringException
	 *             in case of initialization problems.
	 */
	public EventFilteringService(String eventLoggerServiceUrl,
			Class<? extends JmsProvider> jmsProviderClass,
			String jmsProviderUrl, String jmsUser, String jmsPassword,
			String jmsEventNotificationTopic, String jmsClientId,
			MessageListener jmsEventNotificationListener) {

		this.jmsClientId = jmsClientId;
		this.eventLoggerServiceUrl = eventLoggerServiceUrl;
		this.jmsEventNotificationTopic = jmsEventNotificationTopic;
		this.jmsEventNotificationListener = jmsEventNotificationListener;

		this.jmsProvider = null;
		try {
			this.jmsProvider = jmsProviderClass.newInstance();
			this.jmsProvider.setProviderUrl(jmsProviderUrl);
			this.jmsProvider.setProviderUser(jmsUser);
			this.jmsProvider.setProviderUserPassword(jmsPassword);
			this.jmsProvider.setClientId(this.jmsClientId);
		} catch (InstantiationException e) {
			throw new EventFilteringException(
					"Could not instantiate JMS provider", e);
		} catch (IllegalAccessException e) {
			throw new EventFilteringException("Could not access JMS provider",
					e);
		}
	}

	/**
	 * This is the constructor for setting up the event filtering service in
	 * resolved mode. I.e., clients receive fully resolved <code>Event</code>
	 * objects.
	 * 
	 * It requires a few parameters:
	 * 
	 * @param eventLoggerServiceUrl
	 *            the URL where the event logger RESTful web service is running.
	 * @param jmsProviderClass
	 *            the class of the JMS provider on which basis the JMS topic
	 *            notification is implemented. Such as
	 *            <code>ActiveMQProvider</code> or <code>EMSProvider</code>.
	 * @param jmsProviderUrl
	 *            the URL used to connect to the JMS provider
	 * @param jmsUser
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsPassword
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsEventNotificationTopic
	 *            the JMS topic on which event notification takes place. Usually
	 * 
	 *            <code>com.mercatis.lighthouse3.service.eventlogger.events.notification</code>
	 *            .
	 * @param jmsClientId
	 *            the client Id to use for connecting to the JMS topic.
	 * @param eventNotificationListener
	 *            the callback to which the received events are passed.
	 * @param softwareComponentRegistry
	 *            the software component registry to use for resolving events.
	 * @param deploymentRegistry
	 *            the deployment registry to use for resolving events.
	 * @param wsUser
	 *            the user passed to the RESTservice for authentication
	 * @param wsPassword
	 *            the password passed to the RESTservice for authentication
	 * @throws Exception 
	 * 
	 * @throws EventFilteringException
	 *             in case of initialization problems.
	 */
	public EventFilteringService(String eventLoggerServiceUrl,
			Class<? extends JmsProvider> jmsProviderClass,
			String jmsProviderUrl, String jmsUser, String jmsPassword,
			String jmsEventNotificationTopic, String jmsClientId,
			EventListener eventNotificationListener,
			SoftwareComponentRegistry softwareComponentRegistry,
			DeploymentRegistry deploymentRegistry, String wsUser, String wsPassword) {
			
		this(eventLoggerServiceUrl, jmsProviderClass, jmsProviderUrl, jmsUser,
				jmsPassword, jmsEventNotificationTopic, jmsClientId, null);

		this.wsUser = wsUser;
		this.wsPassword = wsPassword;

		this.eventNotificationListener = eventNotificationListener;

		this.jmsEventNotificationListener = new EventListenerAdapter(
				this.eventNotificationListener, deploymentRegistry,
				softwareComponentRegistry);
	}

	/**
	 * This is the constructor for setting up the event filtering service in
	 * resolved mode. I.e., clients receive fully resolved <code>Event</code>
	 * objects.
	 * 
	 * It requires a few parameters:
	 * 
	 * @param eventLoggerServiceUrl
	 *            the URL where the event logger RESTful web service is running.
	 * @param jmsProviderClass
	 *            the class of the JMS provider on which basis the JMS topic
	 *            notification is implemented. Such as
	 *            <code>ActiveMQProvider</code> or <code>EMSProvider</code>.
	 * @param jmsProviderUrl
	 *            the URL used to connect to the JMS provider
	 * @param jmsUser
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsPassword
	 *            the user name to use for JMS connection. May be
	 *            <code>null</code>.
	 * @param jmsEventNotificationTopic
	 *            the JMS topic on which event notification takes place. Usually
	 * 
	 *            <code>com.mercatis.lighthouse3.service.eventlogger.events.notification</code>
	 *            .
	 * @param jmsClientId
	 *            the client Id to use for connecting to the JMS topic.
	 * @param eventNotificationListener
	 *            the callback to which the received events are passed.
	 * @param softwareComponentRegistry
	 *            the software component registry to use for resolving events.
	 * @param deploymentRegistry
	 *            the deployment registry to use for resolving events.
	 * @param wsUser
	 *            the user passed to the RESTservice for authentication
	 * @param wsPassword
	 *            the password passed to the RESTservice for authentication
	 * @throws Exception 
	 * 
	 * @throws EventFilteringException
	 *             in case of initialization problems.
	 */
	public EventFilteringService(String eventLoggerServiceUrl,
			Class<? extends JmsProvider> jmsProviderClass,
			String jmsProviderUrl, String jmsUser, String jmsPassword,
			String jmsEventNotificationTopic, String jmsClientId,
			EventListener eventNotificationListener,
			SoftwareComponentRegistry softwareComponentRegistry,
			DeploymentRegistry deploymentRegistry) {
	    this(eventLoggerServiceUrl, jmsProviderClass, jmsProviderUrl, jmsUser, jmsPassword, jmsEventNotificationTopic, jmsClientId, eventNotificationListener, softwareComponentRegistry, deploymentRegistry, null, null);
	}
}
