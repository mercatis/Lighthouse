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
import com.mercatis.lighthouse3.service.eventlogger.EventLoggerService;

/**
 * This class implements a JMS listener for incoming events. It forwards these
 * to the event logger service.
 */
public class IncomingEventListener implements MessageListener {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	private EventLoggerService eventLoggerService = null;

	public void onMessage(Message message) {
		long traceTimestamp = 0l;
		if (log.isDebugEnabled()) {
			log.debug("Received event to log via JMS");
			traceTimestamp = System.currentTimeMillis();
		}

		try {
			TextMessage textMessage = (TextMessage) message;

			if (log.isDebugEnabled()) {
				log.debug("Event XML to log via JMS:" + textMessage.getText());
			}
			
			this.eventLoggerService.log(textMessage.getText());
			this.eventLoggerService.getEventRegistry().getUnitOfWork().commit();
			
			if (log.isDebugEnabled()) {
				log.debug("Event logged via JMS: " + (System.currentTimeMillis() - traceTimestamp) + "ms.");
			}
		} catch (ClassCastException e) {
			log.error("Non-text message received by event logger", e);
			this.eventLoggerService.getEventRegistry().getUnitOfWork().rollback();
		} catch (JMSException e) {
			log.error("JMS exception caught while logging event via JMS.", e);
			this.eventLoggerService.getEventRegistry().getUnitOfWork().rollback();
		} catch (PersistenceException e) {
			log.error("Persistence exception caught while logging event via JMS", e);
			this.eventLoggerService.getEventRegistry().getUnitOfWork().rollback();
		} catch (XMLSerializationException e) {
			log.error("XML serialization exception caught while logging event via JMS", e);
			this.eventLoggerService.getEventRegistry().getUnitOfWork().rollback();
		}
	}

	public IncomingEventListener(EventLoggerService eventLoggerService) {
		this.eventLoggerService = eventLoggerService;
	}
}
