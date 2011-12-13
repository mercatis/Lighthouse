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
package com.mercatis.lighthouse3.service.eventlogger.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsMessageCreator;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.service.eventlogger.EventFilter;
import com.mercatis.lighthouse3.service.eventlogger.EventFilterMatchHandler;

/**
 * This class provides an implementation of an event filter match handler that
 * publishes matches on a JMS Topic.
 */
public class MatchingEventTopicPublisher implements EventFilterMatchHandler {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * Keep a reference to the JMS topic to use for notification.
	 */
	private Topic eventsNotificationTopic = null;

	/**
	 * This property keeps a reference to the JMS connection to use for event
	 * publication.
	 */
	private JmsConnection jmsConnection = null;
    private long timeToLive = 0l;

	/**
	 * This method publishes an events XML message on the topic.
	 * 
	 * @param filter
	 *            the event filter for which to publish the message
	 * @param eventMessage
	 *            the event XML message to publish
	 */
	private void publishJmsMessage(final EventFilter filter, final String eventMessage) {
		if (log.isDebugEnabled())
			log.debug("Publishing matching events message");

		this.jmsConnection.sendToDestination(this.eventsNotificationTopic, new JmsMessageCreator() {

			public Message createMessage(Session jmsSession) throws JMSException {
				TextMessage eventPublicationMessage = jmsSession.createTextMessage(eventMessage);

				eventPublicationMessage.setStringProperty("LH3TargetClientId", filter.getClientId());
				eventPublicationMessage.setJMSCorrelationID(filter.getUuid());
				return eventPublicationMessage;
			}
		}, timeToLive);

		if (log.isDebugEnabled())
			log.debug("Matching events message published");
	}

	private String createNotificationMessage(List<Event> events) {
		StringBuilder notificationMessage = new StringBuilder();
		boolean first = true;

		notificationMessage.append("<events>");

		for (Event event : events) {
			if (!first)
				notificationMessage.append("<separator/>");
			notificationMessage.append(event.toXml());
			first = false;
		}

		notificationMessage.append("</events>");

		return notificationMessage.toString();
	}

	private void publishEventBatchNotification(EventFilter filter, List<Event> events) {
		this.publishJmsMessage(filter, this.createNotificationMessage(events));
	}

	private void publishEventNotification(EventFilter filter, Event event) {
		List<Event> eventBatch = new ArrayList<Event>();
		eventBatch.add(event);

		this.publishJmsMessage(filter, this.createNotificationMessage(eventBatch));
	}

	public void handleMatch(EventFilter filter, Event event) {
		this.publishEventNotification(filter, event);
	}

	public void handleMatches(EventFilter filter, List<Event> events) {
		this.publishEventBatchNotification(filter, events);
	}

	/**
	 * Create a matching event topic publisher.
	 * 
	 * @param jmsProvider
	 *            the JMS Provider to use for the publishing
	 * @param eventsNotificationTopic
	 *            the topic onto which to publish the match
	 * 
	 */
	public MatchingEventTopicPublisher(JmsProvider jmsProvider, Topic eventsNotificationTopic, long timeToLive) {
		this.jmsConnection = new JmsConnection(jmsProvider);
		this.eventsNotificationTopic = eventsNotificationTopic;
        this.timeToLive = timeToLive;
	}

}
