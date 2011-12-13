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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
 * This class implements a simple bridge between two JMS destinations. Text
 * messages from one destination are simply sent on to another.
 * 
 * The message can be assigned a correlation ID while being bridged. That is
 * useful in case you want to bridge a topic to a queue and keep the message
 * assigned to the topic receiver.
 */
public class JmsDestinationBridge {

	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * The connection to use to connect to the source destination.
	 */
	private JmsConnection sourceConnection = null;

	/**
	 * The connection to use to connect to the target destination.
	 */
	private JmsConnection targetConnection = null;

	/**
	 * The source destination.
	 */
	private Destination sourceDestination = null;

	/**
	 * The target destination.
	 */
	private Destination targetDestination = null;

	/**
	 * The correlationId to add to message in the target destination,
	 * <code>null</code> in case the correlation ID.
	 */
	private String correlationId = null;

	/**
	 * A selector for filtering source messages to bridge, may be
	 * <code>null</code> if one's interested in all messages.
	 */
	private String sourceMessageSelector = null;

	private void setUpBridge() {
		this.sourceConnection.registerDestinationConsumer(this.sourceDestination, this.sourceMessageSelector,
				new MessageListener() {
					public void onMessage(Message messageToBridge) {
						if (log.isDebugEnabled())
							log.debug("Receiving message to bridge");

						try {
							final TextMessage textMessage = (TextMessage) messageToBridge;

							if (log.isDebugEnabled())
								log.debug("Bridging text message: " + textMessage.toString());

							targetConnection.sendToDestination(targetDestination, new JmsMessageCreator() {
								public Message createMessage(Session jmsSession) throws JMSException {
									TextMessage bridgedMessage = jmsSession.createTextMessage();

									bridgedMessage.setText(textMessage.getText());

									if (correlationId != null)
										bridgedMessage.setJMSCorrelationID(correlationId);

									if (log.isDebugEnabled())
										log.debug("Message sent to target destination");

									return bridgedMessage;
								}
							});

						} catch (Exception e) {
							log.error("Message to bridge isn't a proper text message", e);
						}

						if (log.isDebugEnabled())
							log.debug("Message bridged");
					}
				});
	}

	/**
	 * The method stops the bridging of text messages.
	 */
	public void stop() {
		this.sourceConnection.close();
		this.targetConnection.close();
	}

	/**
	 * The constructor for the JMS destination bridge.
	 * 
	 * @param sourceConnection
	 *            the connection to use to connect to the source destination
	 * @param sourceDestination
	 *            the source destination
	 * @param targetConnection
	 *            the connection to use to connect to the target destination
	 * @param targetDestination
	 *            the target destination
	 * @param sourceMessageSelector
	 *            a selector for filtering source messages to bridge, may be
	 *            <code>null</code> if one's interested in all messages.
	 * @param correlationId
	 *            the correlationId to add to message in the target destination,
	 *            <code>null</code> in case the correlation ID
	 */
	public JmsDestinationBridge(JmsConnection sourceConnection, Destination sourceDestination,
			JmsConnection targetConnection, Destination targetDestination, String sourceMessageSelector,
			String correlationId) {

		this.sourceConnection = sourceConnection;
		this.targetConnection = targetConnection;
		this.sourceDestination = sourceDestination;
		this.targetDestination = targetDestination;

		this.sourceMessageSelector = sourceMessageSelector;
		this.correlationId = correlationId;

		if (this.sourceConnection != null && this.targetConnection != null && this.sourceDestination != null
				&& this.targetDestination != null)
			this.setUpBridge();
	}
}
