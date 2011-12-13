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
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This class captures status changes in the status change history that were
 * triggered automatically by the occurrence of an event. The eligible status of
 * such a change is either <code>OK</code> or <code>ERROR</code>.
 */
public class EventTriggeredStatusChange extends StatusChange {

	/**
	 * This property refers to the ID of the triggering event of the status
	 * change.
	 */
	private Event triggeringEvent = null;

	/**
	 * Return the ID of the event that triggered the status change.
	 * 
	 * @return the triggering event
	 */
	public Event getTriggeringEvent() {
		return this.triggeringEvent;
	}

	protected void setTriggeringEvent(Event triggeringEvent) {
		this.triggeringEvent = triggeringEvent;
	}
	
	

	/**
	 * This is the constructor for event triggered status changes.
	 * 
	 * @param newStatus
	 *            the new status to change into.
	 * @param trigger
	 * @throws ConstraintViolationException
	 *             if the new status is neither <code>ERROR</code> nor
	 *             <code>OK</code>.
	 */
	public EventTriggeredStatusChange(int newStatus, Event trigger) {
		if ((newStatus != Status.ERROR) && (newStatus != Status.OK))
			throw new ConstraintViolationException("New status of event triggered status change must bei OK or ERROR",
					null);

		this.triggeringEvent = trigger;
		this.setNewStatus(newStatus);
		this.setChangedWhen(trigger.getDateOfOccurrence());
	}

	protected EventTriggeredStatusChange() {
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		EventTriggeredStatusChange clone = (EventTriggeredStatusChange) super.clone();
		clone.triggeringEvent = this.triggeringEvent;
		
		return clone;
	}

	@Override
	protected void fillStatusChangeElement(XmlWriter xml) throws IOException {
		super.fillStatusChangeElement(xml);
		if (this.getTriggeringEvent() != null) {
			xml.writeEntity("triggeringEvent");
			this.getTriggeringEvent().toXml(xml);
			xml.endEntity();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((triggeringEvent == null) ? 0 : triggeringEvent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventTriggeredStatusChange other = (EventTriggeredStatusChange) obj;
		if (triggeringEvent == null) {
			if (other.triggeringEvent != null)
				return false;
		} else if (!triggeringEvent.equals(other.triggeringEvent))
			return false;
		return true;
	}

}
