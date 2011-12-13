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
package com.mercatis.lighthouse3.domainmodel.events;

import java.util.Date;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;

/**
 * A builder class that enables the user to construct new Event objects by means
 * of setter chaining.
 */
public class EventBuilder {

	private Event event = new Event();

	private EventBuilder() {
	}

	/**
	 * This method returns an Event Builder instance that enables the user to
	 * construct new Event Objects by means of setter chaining.
	 * 
	 * @return an EventBuilder.
	 */
	public static EventBuilder template() {
		return new EventBuilder().setCode(null).setContext(null).setDateOfOccurrence(null).setLevel(null);
	}

	public static EventBuilder newEvent() {
		return new EventBuilder();
	}

	/**
	 * @return the configured Event.
	 */
	public Event done() {
		Event tmp = this.event;
		this.event = null;
		return tmp;
	}

	/**
	 * @param transactionId
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#addTransactionId(java.lang.String)
	 */
	public EventBuilder addTransactionId(String transactionId) {
		event.addTransactionId(transactionId);
		return this;
	}

	/**
	 * @param code
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setCode(java.lang.String)
	 */
	public EventBuilder setCode(String code) {
		event.setCode(code);
		return this;
	}

	/**
	 * @param context
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setContext(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public EventBuilder setContext(Deployment context) {
		event.setContext(context);
		return this;
	}

	/**
	 * @param creationDate
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setDateOfOccurrence(java.util.Date)
	 */
	public EventBuilder setDateOfOccurrence(Date creationDate) {
		event.setDateOfOccurrence(creationDate);
		return this;
	}

	/**
	 * @param level
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setLevel(java.lang.String)
	 */
	public EventBuilder setLevel(String level) {
		event.setLevel(level);
		return this;
	}

	/**
	 * @param machineOfOrigin
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setMachineOfOrigin(java.lang.String)
	 */
	public EventBuilder setMachineOfOrigin(String machineOfOrigin) {
		event.setMachineOfOrigin(machineOfOrigin);
		return this;
	}

	/**
	 * @param message
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setMessage(java.lang.String)
	 */
	public EventBuilder setMessage(String message) {
		event.setMessage(message);
		return this;
	}

	/**
	 * @param stackTrace
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setStackTrace(java.lang.String)
	 */
	public EventBuilder setStackTrace(String stackTrace) {
		event.setStackTrace(stackTrace);
		return this;
	}

	/**
	 * @param udf
	 * @param value
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#setUdf(java.lang.String,
	 *      java.lang.Object)
	 */
	public EventBuilder setUdf(String udf, Object value) {
		event.setUdf(udf, value);
		return this;
	}

	/**
	 * @param tag
	 * @see com.mercatis.lighthouse3.domainmodel.events.Event#tag(java.lang.String)
	 */
	public EventBuilder tag(String tag) {
		event.tag(tag);
		return this;
	}
}
