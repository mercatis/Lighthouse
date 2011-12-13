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
package com.mercatis.lighthouse3.service.eventlogger;

import java.rmi.server.UID;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This class captures an event filter that has been registered by clients with
 * the event logger service.
 */
public class EventFilter {

	/**
	 * This property contains a universal unique id of the event filter.
	 */
	private String uuid = null;

	/**
	 * This method returns the universal unique id of the present event filter.
	 * 
	 * @return the UUID of the filter
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * This property contains the ID of the client registering the filter.
	 */
	private String clientId = null;

	/**
	 * This property returns the client ID of the client registering the filter.
	 * 
	 * @return the client ID
	 */
	public String getClientId() {
		return this.clientId;
	}

	/**
	 * This property maintains the last timestamp when the filter was refreshed.
	 */
	private Date lastTimeRefreshed = null;

	/**
	 * This method performs a match of the event filter against an event.
	 * 
	 * @param eventMatchFilterHandler
	 *            the event match filter handler to be called in case of a
	 *            match.
	 * @param event
	 *            the event to match
	 * @returns <code>true</code> iff the match was successful.
	 */
	public boolean match(EventFilterMatchHandler eventMatchFilterHandler, Event event) {
		if (event.matches(this.template)) {
			if (eventMatchFilterHandler != null)
				eventMatchFilterHandler.handleMatch(this, event);
			return true;
		} else
			return false;
	}

	/**
	 * The predicate returns true iff the filter has expired, i.e., the last
	 * time the filter has been refreshed was before the passed amount of
	 * milliseconds
	 * 
	 * @param expiryIntervalInMsecs
	 *            the expiry interval in millisecs.
	 * @return <code>true</code> iff the filter has expired.
	 */
	public boolean hasExpired(long expiryIntervalInMsecs) {
		Calendar lastRefreshed = new GregorianCalendar();
		lastRefreshed.setTime(this.lastTimeRefreshed);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return (now.getTimeInMillis() - lastRefreshed.getTimeInMillis()) > expiryIntervalInMsecs;
	}

	/**
	 * This method refreshes the current filter postponing expiry.
	 */
	public void refresh() {
		this.lastTimeRefreshed = new Date();
	}

	/**
	 * This property manages the event template used for event filtering.
	 */
	private Event template = null;

	/**
	 * Returns the event template used for filtering.
	 * 
	 * @return the event template used for filtering.
	 */
	public Event getTemplate() {
		return this.template;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventFilter other = (EventFilter) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	/**
	 * This is the constructor for event filters to register with the event
	 * logger service.
	 * 
	 * @param clientId
	 *            the id of the client registering the filter.
	 * @param template
	 *            the template acting as the filter
	 */
	public EventFilter(String clientId, Event template) {
		this.uuid = new UID().toString();
		this.clientId = clientId;
		this.refresh();
		this.template = template;
	}

}
