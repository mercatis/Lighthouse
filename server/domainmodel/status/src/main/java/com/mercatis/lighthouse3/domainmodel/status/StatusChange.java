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
import java.io.StringWriter;
import java.util.Date;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This class keeps track of status changes. An instance of it represents one
 * change of a status.
 */
public abstract class StatusChange implements Cloneable {
	
	private long id = 0L;
	
	public long getId() {
		return id;
	}
	
	/**
	 * References the change previous to this one
	 */
	private StatusChange previousStatusChange = null;
	
	/**
	 * Returns the StatusChange previous to this one
	 * 
	 * @return
	 */
	public StatusChange getPreviousStatusChange() {
		return previousStatusChange;
	}
	
	/**
	 * Set the StatusChange previous to this one
	 * 
	 * @param previousStatusChange
	 */
	public void setPreviousStatusChange(StatusChange previousStatusChange) {
		this.previousStatusChange = previousStatusChange;
	}
	
	/**
	 * References the change following this one
	 */
	private StatusChange nextStatusChange = null;

	/**
	 * Returns the StatusChange following this one
	 * 
	 * @return
	 */
	public StatusChange getNextStatusChange() {
		return nextStatusChange;
	}
	
	/**
	 * Set the StatusChange following this one
	 * 
	 * @param nextStatusChange
	 */
	public void setNextStatusChange(StatusChange nextStatusChange) {
		this.nextStatusChange = nextStatusChange;
	}
	
	/**
	 * The status to which the status change refers.
	 */
	private Status status = null;

	/**
	 * This method returns the status to which the change refers.
	 * 
	 * @return the status to which the change refers.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * This method sets the status to which the change refers.s
	 * 
	 * @param status
	 *            the status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * This property keeps the new status.
	 */
	private int newStatus = Status.NONE;

	/**
	 * This method returns the new status.
	 * 
	 * @return the new status.
	 */
	public int getNewStatus() {
		return this.newStatus;
	}

	/**
	 * This method sets the new status. This also increments the appropriate
	 * ERROR, OK, or STALE counter.
	 * 
	 * @param newStatus
	 *            the new status.
	 */
	protected void setNewStatus(int newStatus) {
		this.newStatus = newStatus;

		if (newStatus == Status.OK)
			this.incrementOkCounter();
		if (newStatus == Status.ERROR)
			this.incrementErrorCounter();
		if (newStatus == Status.STALE)
			this.incrementStaleCounter();
	}

	/**
	 * This property keeps the time stamp of the status change. The default
	 * value is now.
	 */
	private Date dateOfChange = new Date();

	/**
	 * This method returns the date when the status changed. The default value
	 * is now.
	 * 
	 * @return the date
	 */
	public Date getDateOfChange() {
		return this.dateOfChange;
	}

	/**
	 * This method sets the date when the status changed.
	 * 
	 * @param changedWhen
	 *            the date when the status changed.
	 */
	protected void setChangedWhen(Date changedWhen) {
		this.dateOfChange = changedWhen;
	}

	/**
	 * The number of <code>OK</code> events encountered while in the present
	 * status.
	 */
	private int okCounter = 0;

	/**
	 * This method returns the number of <code>OK</code> events encountered
	 * while in the present status.
	 * 
	 * @return the number
	 */
	public int getOkCounter() {
		return this.okCounter;
	}

	/**
	 * This method increments the number of <code>OK</code> events encountered
	 * while in the present status.
	 */
	public void incrementOkCounter() {
		this.okCounter++;
	}

	/**
	 * The number of <code>ERROR</code> events encountered while in the present
	 * status.
	 */
	private int errorCounter = 0;

	/**
	 * This method returns the number of <code>ERROR</code> events encountered
	 * while in the present status.
	 * 
	 * @return the number
	 */
	public int getErrorCounter() {
		return this.errorCounter;
	}

	/**
	 * This method increments the number of <code>ERROR</code> events
	 * encountered while in the present status.
	 */
	public void incrementErrorCounter() {
		this.errorCounter++;
	}

	/**
	 * The number of <code>STALE</code> ness changes encountered while in the
	 * present status.
	 */
	private int staleCounter = 0;

	/**
	 * The number of <code>STALE</code> ness changes encountered while in the
	 * present status.
	 * 
	 * @return the number
	 */
	public int getStaleCounter() {
		return this.staleCounter;
	}

	/**
	 * This method increments the number of <code>STALE</code> ness changes
	 * encountered while in the present status.
	 */
	public void incrementStaleCounter() {
		this.staleCounter++;
	}

	protected void setOkCounter(int okCounter) {
		this.okCounter = okCounter;
	}

	protected void setErrorCounter(int errorCounter) {
		this.errorCounter = errorCounter;
	}

	protected void setStaleCounter(int staleCounter) {
		this.staleCounter = staleCounter;
	}

	/**
	 * Override this method in status change subclasses to write out additional
	 * elements to an XML writer
	 * 
	 * @param xml
	 *            the XML writer to write to.
	 * @throws IOException
	 *             in case of an IO error
	 */
	protected void fillStatusChangeElement(XmlWriter xml) throws IOException {
	}

	/**
	 * Write the current status change out to an XML writer.
	 * 
	 * @param xml
	 *            the root element name to use for the status XML
	 * @param xml
	 *            the XML writer to write to.
	 * @throws IOException
	 *             in case of an IO error
	 */
	public void writeToXmlWriter(String rootElement, XmlWriter xml)
			throws IOException {
		xml.writeEntity(rootElement);
		if (this.getId() != 0L) {
			xml.writeEntityWithText("id", this.getId());
		}
		xml.writeEntityWithText("statusChangeType", this.getClass()
				.getSimpleName());

		if (this.getStatus() != null)
			this.getStatus().writeEntityReference("status", xml);

		if (this.getDateOfChange() != null)
			xml.writeEntityWithText("dateOfChange", XmlMuncher
					.javaDateToXmlDateTime(this.getDateOfChange()));

		if (this.getNewStatus() == Status.NONE)
			xml.writeEntityWithText("newStatus", "none");
		if (this.getNewStatus() == Status.STALE)
			xml.writeEntityWithText("newStatus", "stale");
		if (this.getNewStatus() == Status.OK)
			xml.writeEntityWithText("newStatus", "ok");
		if (this.getNewStatus() == Status.ERROR)
			xml.writeEntityWithText("newStatus", "error");

		xml.writeEntityWithText("okCounter", this.getOkCounter());
		xml.writeEntityWithText("errorCounter", this.getErrorCounter());
		xml.writeEntityWithText("staleCounter", this.getStaleCounter());

		this.fillStatusChangeElement(xml);

		xml.endEntity();
	}

	/**
	 * This method returns an XML representation of the current status change.
	 * 
	 * @return the XML representation
	 */
	public String toXml() {
		StringWriter notification = new StringWriter();

		try {
			XmlWriter xml = new XmlEncXmlWriter(notification);
			this.writeToXmlWriter("change", xml);
		} catch (IOException e) {
			return null;
		}

		return notification.toString();
	}

	/**
	 * This method parses an XML chunk and returns an appropriate status change.
	 * 
	 * @param aChange
	 *            the XML chunk with the status change XML in it.
	 * @return the parsed status change.
	 */
	@SuppressWarnings("rawtypes")
	static public StatusChange parseStatusChange(XmlMuncher aChange, DomainModelEntityDAO... resolversForEntityReferences) {
		String id = aChange.readValueFromXml("/*/:id");
				
		String statusChangeType = aChange.readValueFromXml("/*/:statusChangeType");

		Date dateOfChange = XmlMuncher.xmlDateTimeToJavaDate(aChange.readValueFromXml("/*/:dateOfChange"));

		String newStatusString = aChange.readValueFromXml("/*/:newStatus");
		int newStatus = Status.NONE;

		if ("stale".equals(newStatusString))
			newStatus = Status.STALE;
		if ("error".equals(newStatusString))
			newStatus = Status.ERROR;
		if ("ok".equals(newStatusString))
			newStatus = Status.OK;

		int okCounter = Integer.parseInt(aChange
				.readValueFromXml("/*/:okCounter"));
		int errorCounter = Integer.parseInt(aChange
				.readValueFromXml("/*/:errorCounter"));
		int staleCounter = Integer.parseInt(aChange
				.readValueFromXml("/*/:staleCounter"));

		String clearer = aChange.readValueFromXml("/*/:clearer");
		String reason = aChange.readValueFromXml("/*/:reason");

		if (statusChangeType.equals("ManualStatusClearance")) {
			ManualStatusClearance manualStatusClearance = new ManualStatusClearance();
			if (id != null) {
				((StatusChange) manualStatusClearance).id = (Long.parseLong(id));
			}
			manualStatusClearance.setClearer(clearer);
			manualStatusClearance.setReason(reason);
			manualStatusClearance.setChangedWhen(dateOfChange);
			manualStatusClearance.setOkCounter(okCounter);
			manualStatusClearance.setErrorCounter(errorCounter);
			manualStatusClearance.setStaleCounter(staleCounter);

			return manualStatusClearance;
		}

		if (statusChangeType.equals("StalenessChange")) {
			StalenessChange stalenessChange = new StalenessChange();
			if (id != null) {
				((StatusChange) stalenessChange).id = (Long.parseLong(id));
			}
			stalenessChange.setChangedWhen(dateOfChange);
			stalenessChange.setOkCounter(okCounter);
			stalenessChange.setErrorCounter(errorCounter);
			stalenessChange.setStaleCounter(staleCounter);

			return stalenessChange;
		}

		if (statusChangeType.equals("EventTriggeredStatusChange")) {
			Event event = new Event();
			XmlMuncher eventXml = aChange.getSubMunchersForContext("/*/:triggeringEvent/:*").get(0);
			event.fromXml(eventXml, resolversForEntityReferences);
			
			EventTriggeredStatusChange eventChange = new EventTriggeredStatusChange(newStatus, event);
			if (id != null) {
				((StatusChange) eventChange).id = (Long.parseLong(id));
			}
			eventChange.setChangedWhen(dateOfChange);
			eventChange.setOkCounter(okCounter);
			eventChange.setErrorCounter(errorCounter);
			eventChange.setStaleCounter(staleCounter);

			return eventChange;
		}
		return null;
	}

	/**
	 * This method returns whether a given status change is really a new one and
	 * not just one with incremented counters;
	 * 
	 * @return <code>true</code> iff this is a new status change.
	 */
	public boolean isNewStatusChange() {
		return (this.errorCounter + this.okCounter + this.staleCounter) <= 1;
	}
	
	public StatusChange() {
		super();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		StatusChange clone = (StatusChange) super.clone();
		clone.dateOfChange = this.dateOfChange;
		clone.okCounter = this.okCounter;
		clone.errorCounter = this.errorCounter;
		clone.staleCounter = this.staleCounter;
		clone.newStatus = this.newStatus;
		
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateOfChange == null) ? 0 : dateOfChange.hashCode());
		result = prime * result + errorCounter;
		result = prime * result + newStatus;
		result = prime * result + okCounter;
		result = prime * result + staleCounter;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		StatusChange other = (StatusChange) obj;
		if (dateOfChange == null) {
			if (other.dateOfChange != null)
				return false;
		} else if (!dateOfChange.equals(other.dateOfChange))
			return false;
		if (errorCounter != other.errorCounter)
			return false;
		if (newStatus != other.newStatus)
			return false;
		if (okCounter != other.okCounter)
			return false;
		if (staleCounter != other.staleCounter)
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}
}
