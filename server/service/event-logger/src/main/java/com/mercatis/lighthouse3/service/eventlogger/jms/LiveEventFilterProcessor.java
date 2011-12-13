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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.service.eventlogger.EventFilter;
import com.mercatis.lighthouse3.service.eventlogger.EventLoggerService;

/**
 * This class implements a JMS listener for matching logged events against all
 * registered live event filters.
 */
public class LiveEventFilterProcessor implements MessageListener {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * The event logger service in the context of which the filter match
	 * processor is running.
	 */
	protected EventLoggerService eventLoggerService = null;

	public void onMessage(Message loggedEventMessage) {
		if (log.isDebugEnabled())
			log.debug("Received logged event to match against all registered live event filters");
		
		String eventXml = null;
		try {
			TextMessage textMessage = (TextMessage) loggedEventMessage;
			eventXml = textMessage.getText();
		} catch (ClassCastException e) {
			log.error("Non-text message received by live event filter", e);

			return;
		} catch (JMSException e) {
			log.error("JMS exception accessing logged event message received by live event filters.", e);

			return;
		}

		Event loggedEvent = new Event();
		try {
			try {
				loggedEvent.fromXml(eventXml, this.eventLoggerService.getDeploymentRegistry(), this.eventLoggerService.getSoftwareComponentRegistry());
			} catch (XMLSerializationException e) {
				log.error("XML serialization exception caught while parsing event to match against live event filters.", e);
				log.error("Rolling back unit of work.");
	
				try {
					this.eventLoggerService.getDeploymentRegistry().getUnitOfWork().rollback();
					this.eventLoggerService.getSoftwareComponentRegistry().getUnitOfWork().rollback();
				} catch (PersistenceException e1) {
					log.error("Could not rollback unit of work.", e1);
				}
				return;
			}

			for (EventFilter eventFilter : this.eventLoggerService.getRegisteredEventFilters()) {
				
				if (log.isDebugEnabled()) {
					log.debug("Checking expiry of filter.");
				}
				if (eventFilter.hasExpired(this.eventLoggerService.getEventFilterExpiryIntervalInMsecs())) {
					if (log.isDebugEnabled()) {
						log.debug("The filter has expired. Deregistering it");
					}
					this.eventLoggerService.deregisterEventFilter(eventFilter.getUuid());
	
					return;
				}
	
				if (log.isDebugEnabled()) {
					log.debug("Matching event against against live event filter: " + eventFilter.getUuid());
				}
				if (loggedEvent.matches(eventFilter.getTemplate())) {
					if (log.isDebugEnabled()) {
						log.debug("Event matched against live event filter: " + eventFilter.getUuid());
					}
					this.eventLoggerService.getEventFilterMatchHandler().handleMatch(eventFilter, loggedEvent);
				}
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("Rolling back unit of work (Default Behaviour).");
			}
			
			try {
				this.eventLoggerService.getDeploymentRegistry().getUnitOfWork().rollback();
			} catch (PersistenceException e) {
				log.error("Could not rollback unit of work.", e);
			}
		}
				
		if (log.isDebugEnabled()) {
			log.debug("Received logged event matched against all registered live event filters");
		}
	}

	/**
	 * The constructor.
	 * 
	 * @param eventLoggerService
	 *            the event logger service in the context of which the filter
	 *            processor is running.
	 */
	public LiveEventFilterProcessor(EventLoggerService eventLoggerService) {
		this.eventLoggerService = eventLoggerService;
	}
}
