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
package com.mercatis.lighthouse3.api.statuschange;

import java.util.List;

import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;

/**
 * A simple container class for keeping the parsed contents of a status change
 * notification message.
 */
public class StatusChangeNotification {

	/**
	 * The path identifier of the status to which the status change notification
	 * message refers.
	 */
	private String statusPath = null;

	/**
	 * Returns the path identifier of the status to which the status change
	 * notification message refers.
	 * 
	 * @return the path identifier
	 */
	public String getStatusPath() {
		return this.statusPath;
	}

	/**
	 * The code of the status to which the status change notification message
	 * refers.
	 */
	private String statusCode = null;

	/**
	 * Returns the code of the status to which the status change notification
	 * message refers.
	 * 
	 * @return the status code
	 */
	public String getStatusCode() {
		return this.statusCode;
	}

	/**
	 * The parsed status change to which the status change notification message
	 * refers.
	 */
	private StatusChange statusChange = null;

	/**
	 * Returns the parsed status change to which the status change notification
	 * message refers.
	 * 
	 * @return the status change
	 */
	public StatusChange getStatusChange() {
		return this.statusChange;
	}

	/**
	 * The last status change that occurred before the one to which the status
	 * change notification message refers.
	 */
	private StatusChange priorChange = null;

	/**
	 * Returns last status change that occurred before the one to which the
	 * status change notification message refers.
	 * 
	 * @return the last status change
	 */
	public StatusChange getPriorChange() {
		return this.priorChange;
	}

	/**
	 * This static method parses a status change notification message into a
	 * status change notification
	 * 
	 * @param statusChangeMessage
	 *            the message to parse
	 * @return the notification
	 * @throws XMLSerializationException
	 *             in case of parsing errors.
	 */
	@SuppressWarnings("rawtypes")
	static public StatusChangeNotification parseStatusChangeNotificationMessage(String statusChangeMessage, DomainModelEntityDAO... resolversForEntityReferences) {
		XmlMuncher messageMuncher = new XmlMuncher(statusChangeMessage);

		StatusChangeNotification notification = new StatusChangeNotification();

		notification.statusPath = messageMuncher.readValueFromXml("/*/:statusPath");
		notification.statusCode = messageMuncher.readValueFromXml("/*/:change/:status/:code");

		if (notification.getStatusPath() == null || notification.getStatusCode() == null)
			throw new XMLSerializationException("Could not find status path and status code in status change message",
					null);

		List<XmlMuncher> changes = messageMuncher.getSubMunchersForContext("/*/:change");

		if (changes.isEmpty())
			throw new XMLSerializationException("Could not find change in status change message", null);

		notification.statusChange = StatusChange.parseStatusChange(changes.get(0), resolversForEntityReferences);

		List<XmlMuncher> priorChanges = messageMuncher.getSubMunchersForContext("/*/:priorChange");

		if (!priorChanges.isEmpty()) {
			notification.priorChange = StatusChange.parseStatusChange(priorChanges.get(0), resolversForEntityReferences);
		}

		if (notification.getStatusChange() == null)
			throw new XMLSerializationException("Could not find change in status change message", null);

		return notification;
	}
}
