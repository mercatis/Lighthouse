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
package com.mercatis.lighthouse3.domainmodel.status;

import com.mercatis.lighthouse3.commons.messaging.SmtpConnection;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;

/**
 * This class captures the configuration values of email notification channels.
 */
public class EMailNotificationChannelConfiguration extends SmtpConnection implements
		ChannelConfiguration<EMailNotification> {

	public Class<EMailNotification> getStatusChangeNotificationChannelClass() {
		return EMailNotification.class;
	}

	/**
	 * The event registry to use for retrieving data about events that triggered
	 * the status change resulting in this notification.
	 */
	private EventRegistry eventRegistry = null;

	/**
	 * This method returns the event registry to use for retrieving data about
	 * events that triggered the status change resulting in this notification.
	 * 
	 * @return the registry
	 */
	public EventRegistry getEventRegistry() {
		return this.eventRegistry;
	}

	/**
	 * The constructor sets up the configuration of an email notification
	 * channel. This simply is the SMTP connection to the SMTP server via which
	 * email notifications are to be sent.
	 * 
	 * @param protocol
	 *            the SMTP protocol variant to use, either <code>smtp</code> or
	 *            <code>smtps</code>
	 * @param smtpServer
	 *            the SMTP server to use for sending mail messages
	 * @param serverPort
	 *            the port on which the SMTP server is listening, 25 is normal
	 * @param user
	 *            the user with whom to authenticate to the SMTP server, or
	 *            <code>null</code> in case authentication is not necessary.
	 * @param password
	 *            the password of the user or <code>null</code> if no password
	 *            is required.
	 * @param senderEmailAddress
	 *            the identifier to use for the sender of the email
	 * @param eventRegistry
	 *            the event registry to use for retrieving data about events
	 *            that triggered the status change resulting in this
	 *            notification.
	 */
	public EMailNotificationChannelConfiguration(String protocol, String smtpServer, int serverPort, String user,
			String password, String senderEmailAddress, EventRegistry eventRegistry) {

		super(protocol, smtpServer, serverPort, user, password, senderEmailAddress);

		this.eventRegistry = eventRegistry;
	}
}
