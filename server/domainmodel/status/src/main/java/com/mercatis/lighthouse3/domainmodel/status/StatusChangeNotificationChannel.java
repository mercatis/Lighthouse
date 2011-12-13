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

import java.io.IOException;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;

/**
 * This abstract class subsumes notification mechanisms that inform users of
 * status changes. Currently, e-mail notification is supported. But the idea is
 * that future mechanisms can and will be provided.
 * 
 * This class deals with the structure of notification messages, whereas the
 * companion interface <code>StatusChangeNotificationChannel</code> deals with
 * the peculiarities of sending messages.
 */
public abstract class StatusChangeNotificationChannel<Channel extends StatusChangeNotificationChannel<Channel, ChannelConfig>, ChannelConfig extends ChannelConfiguration<Channel>> {

	/**
	 * Represents the unique surrogate id of the status change notification
	 * channel in the database.
	 */
	private long id = 0L;

	/**
	 * Returns the unique surrogate id of the domain model entity in the
	 * database.
	 * 
	 * @return the unique id
	 */
	public long getId() {
		return id;
	}

	/**
	 * This method sets the unique surrogate id of the domain model entity in
	 * the database.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * The status to which the status change notification channel change refers.
	 */
	private Status status = null;

	/**
	 * This method returns the status to which the status change notification
	 * channel refers.
	 * 
	 * @return the status to which the change refers.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * This method sets the status to which the status change notification
	 * channel refers.
	 * 
	 * @param status
	 *            the status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Implement this method to send a status change notification via a given
	 * notification channel channel.
	 * 
	 * @param channelConfiguration
	 *            the configuration of the channel via which to perform the
	 *            notification.
	 * @param statusChange
	 *            the status change resulting in the notification.
	 * @throws StatusChangeNotificationChannelException
	 *             when a problem occurs while sending.
	 */
	abstract public void statusChanged(ChannelConfig channelConfiguration, StatusChange statusChange);

	/**
	 * Override this method in status change notification channel subclasses to
	 * write out additional elements to an XML writer
	 * 
	 * @param xml
	 *            the XML writer to write to.
	 * @throws IOException
	 *             in case of an IO error
	 */
	protected void fillStatusChangeNotificationChannelElement(XmlWriter xml) throws IOException {
		xml.writeEntityWithText("notificationChannelType", this.getClass().getSimpleName());
	}

	/**
	 * Write the current status change notification channel out to an XML
	 * writer.
	 * 
	 * @param xml
	 *            the XML writer to write to.
	 * @throws IOException
	 *             in case of an IO error
	 */
	public void writeToXmlWriter(XmlWriter xml) throws IOException {
		xml.writeEntity("notificationChannel");

		this.fillStatusChangeNotificationChannelElement(xml);

		xml.endEntity();
	}

	/**
	 * This method parses an XML chunk and returns an appropriate status change
	 * notification channel.
	 * 
	 * @param aChannel
	 *            the XML chunk with the status change notification channel XML
	 *            in it.
	 * @return the parsed status change notification channel.
	 */
	@SuppressWarnings("rawtypes")
	static public StatusChangeNotificationChannel parseStatusChangeNotificationChannel(XmlMuncher aChannel) {
		String notificationChannelType = aChannel.readValueFromXml("/*/:notificationChannelType");
		if ("EMailNotification".equals(notificationChannelType)) {
			EMailNotification emailNotification = new EMailNotification();

			String bodyMimeType = aChannel.readValueFromXml("/*/:bodyMimeType");
			if (bodyMimeType != null)
				emailNotification.setBodyMimeType(bodyMimeType);
			emailNotification.setRecipients(aChannel.readValueFromXml("/*/:recipients"));
			emailNotification.setTitleTemplate(aChannel.readValueFromXml("/*/:titleTemplate"));
			emailNotification.setBodyTemplate(aChannel.readValueFromXml("/*/:bodyTemplate"));

			return emailNotification;
		} else

			return null;
	}
}
