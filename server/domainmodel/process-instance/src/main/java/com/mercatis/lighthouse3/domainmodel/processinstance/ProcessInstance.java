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
package com.mercatis.lighthouse3.domainmodel.processinstance;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;

public class ProcessInstance extends DomainModelEntity {

	private static final long serialVersionUID = 4793602849132574705L;

	private Set<Event> events = new HashSet<Event>();
	
	private Date startDate = null;
	
	private Date endDate = null;
	
	private boolean erroneous = false;
	
	private boolean closed = false;
	
	private ProcessInstanceDefinition processInstanceDefinition;

	public ProcessInstanceDefinition getProcessInstanceDefinition() {
		return processInstanceDefinition;
	}

	public void setProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		this.processInstanceDefinition = processInstanceDefinition;
	}
	
	public void addEvent(Event event) {
		if (this.closed == true) {
			throw new ConstraintViolationException("Events must not be added to closed ProcessInstances.", null);
		}
		events.add(event);
	}

	public Set<Event> getEvents() {
		return events;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public void setEvents(Set<Event> events) {
		this.events = events;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public boolean isErroneous() {
		return erroneous;
	}

	public void setErroneous(boolean erroneous) {
		this.erroneous = erroneous;
	}
	
	public boolean isClosed() {
		return this.closed;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);
		this.processInstanceDefinition.writeEntityReference("processInstanceDefinition", xml);
		xml.writeEntityWithText("startDateTime", XmlMuncher.javaDateToXmlDateTime(this.getStartDate()));
		if (this.getEndDate() != null)
			xml.writeEntityWithText("endDateTime", XmlMuncher.javaDateToXmlDateTime(this.getEndDate()));
		
		xml.writeEntityWithText("isErroneous", Boolean.toString(this.isErroneous()));
		xml.writeEntityWithText("isClosed", Boolean.toString(this.isClosed()));
		
		// events
		xml.writeEntity("events");
		for (Event event : this.events) {
			event.writeEntityReference("event", xml);
		}
		xml.endEntity();
	}
	
	@Override
	public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
		if (this.getId() != 0L) {
			xml.writeEntity(referenceTagName);
			xml.writeEntityWithText("id", this.getId());
			xml.endEntity();
		}
	}
	
	@Override
	protected void readPropertiesFromXml(XmlMuncher xml) {
		super.readPropertiesFromXml(xml);
		
		this.startDate = XmlMuncher.xmlDateTimeToJavaDate(xml.readValueFromXml("/*/:startDateTime"));
		String endDateValue = xml.readValueFromXml("/*/:endDateTime");
		if (endDateValue != null)
			this.endDate = XmlMuncher.xmlDateTimeToJavaDate(endDateValue);
		this.setErroneous(Boolean.parseBoolean(xml.readValueFromXml("/*/:isErroneous")));
		this.setClosed(Boolean.parseBoolean(xml.readValueFromXml("/*/:isClosed")));
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xml, DomainModelEntityDAO... resolversForEntityReferences) {
		if (resolversForEntityReferences.length == 0 || !resolversForEntityReferences[0].getManagedType().equals(ProcessInstanceDefinition.class)) {
			throw new XMLSerializationException(
					"XML deserialization of ProcessInstance requires reference to a ProcessInstanceDefinitionRegistry as 1st resolverForEntityReferences.",
					null);
		}
		ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry = (ProcessInstanceDefinitionRegistry) resolversForEntityReferences[0];
		
		if (resolversForEntityReferences.length <= 1 || !resolversForEntityReferences[1].getManagedType().equals(Event.class)) {
			throw new XMLSerializationException(
					"XML deserialization of ProcessInstance requires reference to an EventRegistry as 2nd resolverForEntityReferences.",
					null);
		}
		EventRegistry eventRegistry = (EventRegistry) resolversForEntityReferences[1];
		
		super.resolveEntityReferencesFromXml(xml, resolversForEntityReferences);
		
		String processInstanceDefinitionCode = xml.readValueFromXml("/*/:processInstanceDefinition/:code");
		ProcessInstanceDefinition processInstanceDefinition = processInstanceDefinitionRegistry.findByCode(processInstanceDefinitionCode);
		this.setProcessInstanceDefinition(processInstanceDefinition);
		
		List<String> eventIds = xml.readValuesFromXml("/*/:events/:event/:id");
		for (String eventId : eventIds) {
			Event event = eventRegistry.find(Long.parseLong(eventId));
			this.events.add(event);
		}
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getProcessInstanceDefinition() != null) {
			parameters.put("processInstanceDefinitionCode", this.getProcessInstanceDefinition().getCode());
		}
		
		if (this.getStartDate() != null) {
			parameters.put("startDate", XmlMuncher.javaDateToXmlDateTime(this.getStartDate()));
		}
		
		if (this.getEndDate() != null) {
			parameters.put("endDate", XmlMuncher.javaDateToXmlDateTime(this.getEndDate()));
		}
		
		if (this.isClosed()) {
			parameters.put("isClosed", Boolean.toString(true));
		}
		
		if (this.isErroneous()) {
			parameters.put("isErroneous", Boolean.toString(true));
		}
		
		return parameters;
	}
	
	public void fromQueryParameters(Map<String, String> queryParameters, ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry) {
		// resolve ProcessInstanceDefinition
		String processInstanceDefinitionCode = queryParameters.get("processInstanceDefinitionCode");
		if (processInstanceDefinitionCode != null) {
			this.setProcessInstanceDefinition(processInstanceDefinitionRegistry.findByCode(processInstanceDefinitionCode));
		}
		
		String isErroneous = queryParameters.get("isErroneous");
		if (isErroneous != null) {
			this.erroneous = Boolean.parseBoolean(isErroneous);
		}
		
		String isClosed = queryParameters.get("isClosed");
		if (isClosed != null) {
			this.closed = Boolean.parseBoolean(isClosed);
		}
		
		this.fromQueryParameters(queryParameters);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (closed ? 1231 : 1237);
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (erroneous ? 1231 : 1237);
		result = prime
				* result
				+ ((processInstanceDefinition == null) ? 0
						: processInstanceDefinition.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
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
		ProcessInstance other = (ProcessInstance) obj;
		if (closed != other.closed)
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (erroneous != other.erroneous)
			return false;
		if (processInstanceDefinition == null) {
			if (other.processInstanceDefinition != null)
				return false;
		} else if (!processInstanceDefinition
				.equals(other.processInstanceDefinition))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

}
