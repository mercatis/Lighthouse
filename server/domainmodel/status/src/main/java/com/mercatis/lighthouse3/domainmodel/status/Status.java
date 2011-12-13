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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.UnitOfWork;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;

/**
 * A status is a kind of traffic light-like control that can be attached to a
 * status carrier, namely: deployments, environments, and process / tasks.
 * <p/>
 * The state of the traffic light is changed by events that occur within the
 * context of the status carrier. Those are given by event templates. A timeout
 * interval can be given to indicate staleness when no events relevant for the
 * traffic light have been occurring for a while.
 * <p/>
 * A status may have one of four states:
 * <ul>
 * <li> <code>OK</code>: to indicate that everything is fine (green light)
 * <li> <code>ERROR</code>: to indicate an error (red light)
 * <li> <code>STALE</code>: no events relevant for the status have occurred
 * within a given timeout interval (yellow light).
 * <li> <code>NONE</code>: the status has not yet moved into state (no light)
 * </ul>
 * <p/>
 * Status support two modes with regard to clearing error states.
 * <ul>
 * <li> <code>AUTO</code>: the status automatically switches back to
 * <code>OK</code> as soon a suitable event for the OK template arrives.
 * <li> <code>MANUAL</code>: the status remains in state <code>ERROR</code>
 * unless manually cleared by a user.
 * </ul>
 * <p/>
 * Status keep a history of their state. Whenever a status changes, registered
 * status change notifiers can be invoked to notify interested parties, e.g., by
 * e-mail. Moreover, there is a callback mechanism to hook into.
 * <p/>
 * When an event occurs that keeps the status in its current state (e.g., a
 * green-switching event on an already green traffic light), no new status
 * change entry is inserted into the history. However, the number of such status
 * reinforcing events are recorded in the newest status change history entry.
 */
public class Status extends CodedDomainModelEntity {

	protected Logger log = Logger.getLogger(this.getClass());

	private static final long serialVersionUID = 4750618283098027769L;

	/**
	 * This is the indicator for auto clearance of status errors.
	 */
	final static public int AUTO_CLEARANCE = 0;

	/**
	 * This is the indicator for manual clearance of status errors.
	 */
	final static public int MANUAL_CLEARANCE = 1;

	/**
	 * This indicates an OK status.
	 */
	final static public int OK = 1;

	/**
	 * This indicates a stale status.
	 */
	final static public int STALE = 2;

	/**
	 * This indicates an ERROR status
	 */
	final static public int ERROR = 3;

	/**
	 * This indicates no status.
	 */
	final static public int NONE = 0;

	/**
	 * The carrier to which the status is attached.
	 */
	private StatusCarrier context = null;

	private boolean enabled = true;

	/*
	 * version property for concurrent modification (hibernate)
	 */
	@SuppressWarnings("unused")
	private long version = 0l;

	/**
	 * Only enabled status are used for status aggregation.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Only enabled status are used for status aggregation.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * This method returns the context of the present status. E.g., a
	 * deployment, an environment, or a process task.
	 * 
	 * @return the context of the status.
	 */
	public StatusCarrier getContext() {
		return this.context;
	}

	/**
	 * This method sets the context of the given status. E.g., a deployment, an
	 * environment, or a process task.
	 * 
	 * @param context
	 *            the context of the status.
	 */
	public void setContext(StatusCarrier context) {
		this.context = context;
	}

	private class StatusCarrierSoftwareComponentRegistryWrapper implements SoftwareComponentRegistry {

		public void register(SoftwareComponent componentToRegister) {
		}

		public void reRegister(SoftwareComponent componentToReRegister) {
		}

		public void unregister(SoftwareComponent componentToUnregister) {
		}

		public List<String> findAllTopLevelComponentCodes() {
			return null;
		}

		public List<Long> findAllTopLevelComponentIds() {
			return null;
		}

		public SoftwareComponent findByCode(String code) {
			for (Deployment deployment : this.statusCarrier.getAssociatedDeployments()) {
				if (deployment.getDeployedComponent().getCode().equals(code)) {
					return deployment.getDeployedComponent();
				}
			}

			return null;
		}

		public void delete(SoftwareComponent entityToDelete) {
		}

		public SoftwareComponent find(long id) {
			return null;
		}

		public List<SoftwareComponent> findByTemplate(SoftwareComponent template) {
			return null;
		}

		public void persist(SoftwareComponent entityToPersist) {
		}

		public void update(SoftwareComponent entityToUpdate) {
		}

		public Class<SoftwareComponent> getManagedType() {
			return SoftwareComponent.class;
		}

		public UnitOfWork getUnitOfWork() {
			return null;
		}

		private StatusCarrier statusCarrier = null;

		public StatusCarrierSoftwareComponentRegistryWrapper(StatusCarrier statusCarrier) {
			this.statusCarrier = statusCarrier;
		}

		public boolean alreadyPersisted(SoftwareComponent entity) {
			return false;
		}

		public List<SoftwareComponent> findAll() {
			return null;
		}

		public String getLighthouseDomain() {
			return null;
		}
	}

	private class StatusCarrierDeploymentRegistryWrapper implements DeploymentRegistry {

		public Deployment deploy(SoftwareComponent component, String location, String description, String contact, String contactEmail) {
			return null;
		}

		public List<String> findAllLocations() {
			return null;
		}

		public List<Deployment> findAtLocation(String location) {
			return null;
		}

		public List<Deployment> findByComponent(SoftwareComponent component) {
			return null;
		}

		public Deployment findByComponentAndLocation(SoftwareComponent component, String location) {
			return findByComponentCodeAndLocation(component.getCode(), location);
		}

		public Deployment findByComponentCodeAndLocation(String componentCode, String location) {
			for (Deployment deployment : this.statusCarrier.getAssociatedDeployments()) {
				if (deployment.getLocation().equals(location) && deployment.getDeployedComponent().getCode().equals(componentCode)) {
					return deployment;
				}
			}

			return null;
		}

		public void undeploy(Deployment deployment) {
		}

		public void delete(Deployment entityToDelete) {
		}

		public Deployment find(long id) {
			return null;
		}

		public List<Deployment> findByTemplate(Deployment template) {
			return null;
		}

		public Class<Deployment> getManagedType() {
			return Deployment.class;
		}

		public UnitOfWork getUnitOfWork() {
			return null;
		}

		public void persist(Deployment entityToPersist) {
		}

		public void update(Deployment entityToUpdate) {
		}

		private StatusCarrier statusCarrier = null;

		public StatusCarrierDeploymentRegistryWrapper(StatusCarrier statusCarrier) {
			this.statusCarrier = statusCarrier;
		}

		public boolean alreadyPersisted(Deployment entity) {
			return false;
		}

		public List<Deployment> findAll() {
			return null;
		}

		public String getLighthouseDomain() {
			return null;
		}
	}

	/**
	 * This property keeps the template for those events that switch the status
	 * to <code>OK</code> in XML representation for persistence needs.
	 */
	private String okTemplateXml = null;

	/**
	 * This property caches the OK template.
	 */
	private Event okTemplate = null;

	/**
	 * This method returns the template for those events that switch the status
	 * to <code>OK</code>.
	 * 
	 * @return the OK event template
	 * @throws ConstraintViolationException
	 *             in case the context of the event template does not refer to
	 *             the a deployment associated with the status carrier.
	 */
	public Event getOkTemplate() {
		if (this.okTemplate != null) {
			return this.okTemplate;
		}

		Event template = null;

		try {
			DeploymentRegistry deploymentRegistry = new StatusCarrierDeploymentRegistryWrapper(this.getContext());
			SoftwareComponentRegistry softwareComponentRegistry = new StatusCarrierSoftwareComponentRegistryWrapper(this.getContext());
			template = EventBuilder.template().done();
			template.fromXml(this.okTemplateXml, deploymentRegistry, softwareComponentRegistry);
			this.okTemplate = template;
		} catch (Exception e) {
			throw new ConstraintViolationException("OK event template of status may only refer to deployments related to the status carrier.", e);
		}

		return template;
	}

	/**
	 * This method sets the template for those events that switch the status to
	 * <code>OK</code>. The context of the template may only refer to
	 * deployments that are associated with the carrier of the status.
	 * 
	 * @param okTemplate
	 *            the OK event template
	 * @throws ConstraintViolationException
	 *             in case the context of the event template does not refer to
	 *             the a deployment associated with the status carrier.
	 */
	public void setOkTemplate(Event okTemplate) {
		if ((okTemplate.getContext() != null) && (this.getContext() != null)) {
			Set<Deployment> associatedDeployments = this.getContext().getAssociatedDeployments();

			if (Ranger.isEnumerationRange(okTemplate.getContext())) {
				for (Deployment deployment : Ranger.castToEnumerationRange(okTemplate.getContext()).getEnumeration()) {
					if (!associatedDeployments.contains(deployment)) {
						throw new ConstraintViolationException("OK event template of status may only refer to deployments related to the status carrier.", null);
					}
				}
			} else {
				if (!associatedDeployments.contains(okTemplate.getContext())) {
					throw new ConstraintViolationException("OK event template of status may only refer to deployments related to the status carrier.", null);
				}
			}
		}
		this.okTemplate = null;
		this.okTemplateXml = okTemplate.toXml();
	}

	/**
	 * This property keeps the template for those events that switch the status
	 * to <code>ERROR</code>.
	 */
	private String errorTemplateXml = null;

	/**
	 * This property caches the ERROR template.
	 */
	private Event errorTemplate = null;

	/**
	 * This method returns the template for those events that switch the status
	 * to <code>ERROR</code>.
	 * 
	 * @return the ERROR event template
	 * @throws ConstraintViolationException
	 *             in case the context of the event template does not refer to
	 *             the a deployment associated with the status carrier.
	 */
	public Event getErrorTemplate() {
		if (this.errorTemplate != null) {
			return this.errorTemplate;
		}

		Event template = null;

		try {
			DeploymentRegistry deploymentRegistry = new StatusCarrierDeploymentRegistryWrapper(this.getContext());
			SoftwareComponentRegistry softwareComponentRegistry = new StatusCarrierSoftwareComponentRegistryWrapper(this.getContext());
			template = EventBuilder.template().done();
			template.fromXml(this.errorTemplateXml, deploymentRegistry, softwareComponentRegistry);
			this.errorTemplate = template;
		} catch (Exception e) {
			throw new ConstraintViolationException("ERROR event template of status may only refer to deployments related to the status carrier.", e);
		}

		return template;
	}

	/**
	 * This method sets the template for those events that switch the status to
	 * <code>ERROR</code>.
	 * 
	 * @param errorTemplate
	 *            the ERROR event template
	 * @throws ConstraintViolationException
	 *             in case the context of the event template does not refer to
	 *             the a deployment associated with the status carrier.
	 */
	public void setErrorTemplate(Event errorTemplate) {
		if ((errorTemplate.getContext() != null) && (this.getContext() != null)) {
			Set<Deployment> associatedDeployments = this.getContext().getAssociatedDeployments();

			if (Ranger.isEnumerationRange(errorTemplate.getContext())) {
				for (Deployment deployment : Ranger.castToEnumerationRange(errorTemplate.getContext()).getEnumeration()) {
					if (!associatedDeployments.contains(deployment)) {
						throw new ConstraintViolationException("ERROR event template of status may only refer to deployments related to the status carrier.",
								null);
					}
				}
			} else {
				if (!associatedDeployments.contains(errorTemplate.getContext())) {
					throw new ConstraintViolationException("ERROR event template of status may only refer to deployments related to the status carrier.", null);
				}
			}
		}
		this.errorTemplate = null;
		this.errorTemplateXml = errorTemplate.toXml();
	}

	/**
	 * This method checks whether a given event could produce a new status by
	 * matching it against the context of the status and the OK and ERROR
	 * templates.
	 * 
	 * @param event
	 *            the event to check
	 * @return <code>OK</code>/<code>ERROR</code> when (a) the event is from a
	 *         deployment associated with the status context and (b) the event
	 *         either matches the OK or ERROR template, respectively.
	 *         <code>NONE</code> otherwise.
	 */
	protected int updatesStatus(Event event) {
		boolean matchesContext = false;

		for (Deployment deployment : this.context.getAssociatedDeployments()) {
			if (deployment.equals(event.getContext())) {
				matchesContext = true;
				break;
			}
		}

		if (!matchesContext) {
			return NONE;
		}

		if (event.matches(this.getOkTemplate())) {
			processEventUdfToMetadata(event);
			return OK;
		}

		if (event.matches(this.getErrorTemplate())) {
			processEventUdfToMetadata(event);
			return ERROR;
		}

		return NONE;
	}

	private void processEventUdfToMetadata(final Event pEvent) {
		for (Entry<String, Object> tEntry : pEvent.getUdfs().entrySet()) {
			setMetadate(tEntry.getKey(), tEntry.getValue());
		}
	}

	/**
	 * This property indicates how error states are cleared: manually or
	 * automatically. The latter is the default.
	 */
	private int clearanceType = AUTO_CLEARANCE;

	/**
	 * This method returns the clearance type of the present status. This
	 * indicates how error states are cleared: manually or automatically. The
	 * latter is the default.
	 * 
	 * @return the clearance type.
	 */
	public int getClearanceType() {
		return this.clearanceType;
	}

	/**
	 * This method is used to set the clearance type of the present status.This
	 * indicates how error states are cleared: manually or automatically. The
	 * latter is the default.
	 * 
	 * @param clearanceType
	 *            the clearance type.
	 * @throws ConstraintViolationException
	 *             in case an invalid clearance type is set.
	 */
	public void setClearanceType(int clearanceType) {
		if ((clearanceType != AUTO_CLEARANCE) && (clearanceType != MANUAL_CLEARANCE)) {
			throw new ConstraintViolationException("Attempt to set invalid clearance type for status.", null);
		}

		this.clearanceType = clearanceType;
	}

	/**
	 * This predicate indicates, whether the given status is automatically
	 * cleared of error states.
	 * 
	 * @return <code>true<code> iff the present status is cleared automatically.
	 */
	public boolean isAutomaticallyCleared() {
		return this.clearanceType == AUTO_CLEARANCE;
	}

	/**
	 * This predicate indicates, whether the given status is manual cleared of
	 * error states.
	 * 
	 * @return <code>true<code> iff the present status is cleared automatically.
	 */
	public boolean isManullayCleared() {
		return this.clearanceType == MANUAL_CLEARANCE;
	}

	/**
	 * This is an optional, human readable name of the status.
	 */
	private String longName = null;

	/**
	 * This method returns an optional, human readable name of the status.
	 * 
	 * @return the name of the status
	 */
	public String getLongName() {
		return this.longName;
	}

	/**
	 * This method can be used to set a new status name.
	 * 
	 * @param name
	 *            the new status name.
	 */
	public void setLongName(String name) {
		this.longName = name;
	}

	/**
	 * This property keeps an optional textual description of the purpose of the
	 * status.
	 */
	private String description = null;

	/**
	 * Call this method to obtain a textual description of the status.
	 * 
	 * @return a description of the present status.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * This method can be used to provide an optional description for the
	 * present status.
	 * 
	 * @param description
	 *            the description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * An optional contact responsible for the status. E.g., the user who
	 * created the status or support.
	 */
	private String contact = null;

	/**
	 * This method returns a contact responsible for the status. E.g., the user
	 * who created the status or support.
	 * 
	 * @return the contact
	 */
	public String getContact() {
		return this.contact;
	}

	/**
	 * This method sets contact information for the status. E.g., the user who
	 * created the status or support.
	 * 
	 * @param contact
	 *            the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * An optional email address for contacting someone responsible.
	 */
	private String contactEmail = null;

	/**
	 * This method obtains an optional email address to contact someone
	 * responsible for the status.
	 * 
	 * @return the contact email.
	 */
	public String getContactEmail() {
		return this.contactEmail;
	}

	/**
	 * This method can be used to set an optional email address for contacting
	 * someone responsible for the status.
	 * 
	 * @param contactEmail
	 *            the contact email.
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * This property keeps the staleness interval in msecs indicating when a
	 * status should change to stale. The default is infinite - i.e., a status
	 * never goes stale.
	 */
	private long stalenessIntervalInMsecs = 0l;

	/**
	 * This method returns the staleness interval in msecs indicating when a
	 * status should change to stale. The default is infinite - i.e., a status
	 * never goes stale.
	 * 
	 * @return the staleness interval
	 */
	public long getStalenessIntervalInMsecs() {
		return this.stalenessIntervalInMsecs;
	}

	/**
	 * Use this method to set the staleness interval in msecs to indicate when a
	 * status should change to stale.
	 * 
	 * @param stalenessIntervalInMsecs
	 *            the staleness interval in milliseconds.
	 */
	public void setStalenessIntervalInMsecs(long stalenessIntervalInMsecs) {
		this.stalenessIntervalInMsecs = stalenessIntervalInMsecs;
	}

	/**
	 * This property keeps a timestamp indicating when the status has changed
	 * for the last time. This is used to determine staleness
	 */
	private Date lastStatusUpdate = new Date();

	/**
	 * This dictionary manages a set of metadate attached to the Status. As for
	 * permissible value type, these are: boolean, integer, long, float, double,
	 * date, binary, string.
	 */
	private Map<String, Object> metadata = new HashMap<String, Object>();

	private transient Map<String, String> metadataKeys = new HashMap<String, String>();

	/**
	 * This method sets a metadate on the Status. Permissible value types are
	 * boolean, integer, long, float, double, date, binary, string.
	 * 
	 * @param fieldName
	 *            the name of metadate to set.
	 * @param value
	 *            the value of the metadate to set.
	 * @throws ConstraintViolationException
	 *             in case that one tries to set an UDF with an unsupported
	 *             value type.
	 */
	public void setMetadate(String fieldName, Object value) {
		if (!((value instanceof String) || (value instanceof Float) || (value instanceof Double) || (value instanceof Integer) || (value instanceof Long)
				|| (value instanceof Date) || (value instanceof Boolean) || (value instanceof byte[]) || (value instanceof char[]))) {
			throw new ConstraintViolationException("UDF value of unsupported type. Must be one of boolean, integer, long, float, double, date, binary, clob, string",
					null);
		}

		if (this.metadataKeys.isEmpty()) {
			for (Iterator<String> it = this.metadata.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				this.metadataKeys.put(key.toLowerCase(), key);
			}
		}

		String oldKey = this.metadataKeys.get(fieldName.toLowerCase());
		if (oldKey != null && oldKey.compareTo(fieldName) != 0) {
			this.metadata.remove(oldKey);
			this.metadataKeys.put(fieldName.toLowerCase(), fieldName);
		}

		this.metadata.put(fieldName, value);
	}

	public Object getMetadateValueForStringAndType(String typeString, String metadateValue) {
		Object value = null;
		MetadateType type = MetadateType.fromTypeString(typeString);
		switch (type) {
		case STRING:
			value = metadateValue;
			break;
		case BOOLEAN:
			value = Boolean.valueOf(metadateValue);
			break;
		case INTEGER:
			value = new Integer(metadateValue);
			break;
		case LONG:
			value = new Long(metadateValue);
			break;
		case DOUBLE:
			value = new Double(metadateValue);
			break;
		case FLOAT:
			value = new Float(metadateValue);
			break;
		case BINARY:
			value = XmlMuncher.xmlBinaryToByteArray(metadateValue);
			break;
		case CLOB:
			value = new String(metadateValue);
			break;
		case DATE_TIME:
			value = XmlMuncher.xmlDateTimeToJavaDate(metadateValue);
			break;
		}
		return value;
	}

	/**
	 * This method removes a metadate from the Status.
	 * 
	 * @param fieldName
	 *            of the metadate to remove.
	 */
	public void clearMetadate(String fieldName) {
		this.metadata.remove(fieldName);
	}

	/**
	 * This method returns the value of a given metadate attached tp the Status
	 * 
	 * @param fieldName
	 *            the metadate containing the value of interest
	 * @return the value of the metadate or <code>null</code>, if the metadate
	 *         does not exist.
	 */
	public Object getMetadate(String fieldName) {
		return this.metadata.get(fieldName);
	}

	/**
	 * This method returns a map with all metadata key value pairs assigned to
	 * the present event.
	 * 
	 * @return the metadata map
	 */
	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	/**
	 * This predicate tests whether the present event has a given metadate
	 * assigned.
	 * 
	 * @param fieldName
	 *            the name of the metadate to look for
	 * @return <code>true</code> iff the present event features a metadate of
	 *         the given name
	 */
	public boolean hasMetadate(String fieldName) {
		return this.getMetadate(fieldName) != null;
	}

	/**
	 * Sets a map of metadate.
	 * 
	 * @param metadata
	 *            the metadatas to set.
	 */
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	/**
	 * This method receives an event and changes the current status accordingly.
	 * 
	 * @param event
	 *            the event to process
	 */
	synchronized public void processEvent(Event event) {
		if (this.isAutomaticallyCleared()) {
			this.processAutoClearanceMode(event);
		} else {
			this.processManualClearanceMode(event);
		}
	}

	private void processAutoClearanceMode(Event event) {
		int statusChange = this.updatesStatus(event);
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] StatusChange Mode: " + statusChange);
			log.debug("[" + this.getCode() + "] Before Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
		if (statusChange == NONE) {
			return;
		}

		if (this.isNone() || this.isStale()) {

			if (statusChange == OK) {
				this.change(new EventTriggeredStatusChange(OK, event));
			} else {
				this.change(new EventTriggeredStatusChange(ERROR, event));
			}

		} else if (this.isError()) {

			if (statusChange == ERROR) {
				this.getCurrent().incrementErrorCounter();
				this.lastStatusUpdate = new Date();
			} else {
				this.change(new EventTriggeredStatusChange(OK, event));
			}

		} else if (this.isOk()) {

			if (statusChange == OK) {
				this.getCurrent().incrementOkCounter();
				this.lastStatusUpdate = new Date();
			} else {
				this.change(new EventTriggeredStatusChange(ERROR, event));
			}

		}
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] After Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
	}

	private void processManualClearanceMode(Event event) {
		int statusChange = this.updatesStatus(event);
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] StatusChange Mode: " + statusChange);
			log.debug("[" + this.getCode() + "] Before Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
		if (statusChange == NONE) {
			return;
		}

		if (this.isNone() || isStale()) {

			if (statusChange == OK) {
				this.change(new EventTriggeredStatusChange(OK, event));
			} else {
				this.change(new EventTriggeredStatusChange(ERROR, event));
			}

		} else if (this.isError()) {

			if (statusChange == ERROR) {
				this.getCurrent().incrementErrorCounter();
				this.lastStatusUpdate = new Date();
			} else {
				this.getCurrent().incrementOkCounter();
				this.lastStatusUpdate = new Date();
			}

		} else if (this.isOk()) {

			if (statusChange == OK) {
				this.getCurrent().incrementOkCounter();
				this.lastStatusUpdate = new Date();
			} else {
				this.change(new EventTriggeredStatusChange(ERROR, event));
			}

		}
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] After Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
	}

	public void processStalenessChange(StalenessChange change) {
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] Before Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
		if (isStale() || (isManullayCleared() && isError())) {
			getCurrent().incrementStaleCounter();
			lastStatusUpdate = change.getDateOfChange();
		} else if (isAutomaticallyCleared()) {
			change(change);
			lastStatusUpdate = change.getDateOfChange();
		} else if (isManullayCleared() && !isError()) {
			change(change);
			lastStatusUpdate = change.getDateOfChange();
		}
		if (log.isDebugEnabled()) {
			log.debug("[" + this.getCode() + "] After Change: okCounter: " + this.getCurrent().getOkCounter() + ", errorCounter: "
					+ this.getCurrent().getErrorCounter() + ", staleCounter: " + this.getCurrent().getStaleCounter());
		}
	}

	/**
	 * This property keeps the set of status change notification channels
	 * attached to the current status.
	 */
	private Set<StatusChangeNotificationChannel<?, ?>> statusChangeNotificationChannels = new HashSet<StatusChangeNotificationChannel<?, ?>>();

	/**
	 * This method is used to attach an additional status change notification
	 * channel to the present status.
	 * 
	 * @param statusChangeNotificationChannel
	 *            the channel to attach
	 */
	public void attachChangeNotificationChannel(StatusChangeNotificationChannel<?, ?> statusChangeNotificationChannel) {
		this.statusChangeNotificationChannels.add(statusChangeNotificationChannel);
		statusChangeNotificationChannel.setStatus(this);
	}

	/**
	 * This method returns all status change notification channels attached to
	 * the current status.
	 * 
	 * @return the channels attached to the current status.
	 */
	public Set<StatusChangeNotificationChannel<?, ?>> getChangeNotificationChannels() {
		return this.statusChangeNotificationChannels;
	}

	/**
	 * This method removes a formerly attached status change notification
	 * channel from the present status.
	 * 
	 * @param statusChangeNotificationChannel
	 *            the channel to remove.
	 */
	public void detachChangeNotificationChannel(StatusChangeNotificationChannel<?, ?> statusChangeNotificationChannel) {
		this.statusChangeNotificationChannels.remove(statusChangeNotificationChannel);
		statusChangeNotificationChannel.setStatus(null);
	}

	/**
	 * This property keeps a list of listeners interested in changes of the
	 * current status.
	 */
	private List<StatusChangeListener> statusChangeListeners = new ArrayList<StatusChangeListener>();

	/**
	 * Call this method to register a status change listener with the current
	 * status.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	synchronized public void registerStatusChangeListener(StatusChangeListener listener) {
		if (!this.statusChangeListeners.contains(listener)) {
			this.statusChangeListeners.add(listener);
		}
	}

	/**
	 * Call this method to deregister a status change listener from the current
	 * status.
	 * 
	 * @param listener
	 *            the listener to deregister. If not registered, nothing happens
	 */
	synchronized public void deregisterStatusChangeListener(StatusChangeListener listener) {
		this.statusChangeListeners.remove(listener);
	}

	/**
	 * This method notifies all listeners about a change in the current status.
	 */
	synchronized public void notifyStatusChangeListeners() {
		if (log.isDebugEnabled()) {
			log.debug("Notifying StatusChangeListeners on change for Status: '" + this.getCode() + "'.");
		}

		for (StatusChangeListener listener : new ArrayList<StatusChangeListener>(this.statusChangeListeners)) {
			if (log.isDebugEnabled()) {
				log.debug("Notifying StatusChangeListeners: '" + listener + "'.");
			}
			listener.statusChanged(this.getCurrent());
		}
	}

	/**
	 * This keeps the latest status change.
	 */
	private StatusChange latestStatusChange = null;

	/**
	 * This method adds a change the status change history. Registered listeners
	 * are notified about this change.
	 * 
	 * @param newStatus
	 *            the status to add
	 */
	public void change(StatusChange newStatus) {
		this.addChangeToHistory(newStatus);
		this.notifyStatusChangeListeners();
	}

	/**
	 * This method adds a change to the status change history. Registered
	 * listeners are not(!) notified about this change.
	 * 
	 * @param statusChange
	 *            the statusChange to add
	 */
	synchronized public void addChangeToHistory(StatusChange statusChange) {
		statusChange.setStatus(this);
		statusChange.setNextStatusChange(null);
		statusChange.setPreviousStatusChange(null);
		this.lastStatusUpdate = statusChange.getDateOfChange();

		if (this.latestStatusChange != null) {
			statusChange.setPreviousStatusChange(this.latestStatusChange);
			this.latestStatusChange.setNextStatusChange(statusChange);
		}
		this.latestStatusChange = statusChange;
	}

	public Date getLastStatusUpdate() {
		return this.lastStatusUpdate;
	}

	/**
	 * This method returns the complete status change history (ascending, from
	 * oldest to newest change).
	 * 
	 * @return the status change history
	 * @deprecated
	 */
	synchronized public List<StatusChange> getChangeHistory() {
		List<StatusChange> history = new LinkedList<StatusChange>();
		StatusChange change = this.latestStatusChange;
		while (change != null) {
			history.add(change);
			change = change.getPreviousStatusChange();
		}
		Collections.reverse(history);

		return Collections.unmodifiableList(history);
	}

	/**
	 * This method returns the current status, i.e., the first change in the
	 * status change history.
	 * 
	 * @return the last / current status change.
	 */
	synchronized public StatusChange getCurrent() {
		return this.latestStatusChange;
	}

	/**
	 * This method returns the previous status, i.e., the second change in the
	 * status change history.
	 * 
	 * @return the previous status change. <code>null</code> is returned in case
	 *         that there has not yet been a status change before.
	 */
	public StatusChange getPrevious() {
		if (this.latestStatusChange == null) {
			return null;
		}

		return this.latestStatusChange.getPreviousStatusChange();
	}

	/**
	 * This predicate returns <code>true</code>, iff the current status is
	 * <code>OK</code>.
	 * 
	 * @return <code>true</code>, iff the current status is <code>OK</code>.
	 */
	public boolean isOk() {
		return this.getCurrent().getNewStatus() == OK;
	}

	/**
	 * This predicate returns <code>true</code>, iff the current status is
	 * <code>ERROR</code>.
	 * 
	 * @return <code>true</code>, iff the current status is <code>ERROR</code>.
	 */
	public boolean isError() {
		return this.getCurrent().getNewStatus() == ERROR;
	}

	/**
	 * This predicate returns <code>true</code>, iff the current status is
	 * <code>STALE</code>.
	 * 
	 * @return <code>true</code>, iff the current status is <code>STALE</code>.
	 */
	public boolean isStale() {
		return this.getCurrent().getNewStatus() == STALE;
	}

	/**
	 * This predicate returns <code>true</code>, iff the current status is
	 * <code>NONE</code>.
	 * 
	 * @return <code>true</code>, iff the current status is <code>NONE</code>.
	 */
	public boolean isNone() {
		return this.getCurrent().getNewStatus() == NONE;
	}

	/**
	 * This method creates a clone of the present status. However, the status
	 * history is paginated. For pagination, you specify the page size in terms
	 * of status changes and the number of the page you are interested in.
	 * <p/>
	 * Note that the latest status change (i.e., the first status change in the
	 * history) is always returned. The pages are counted from this latest
	 * change on. I.e.,requesting the x-th page of size ten always returns 11
	 * status changes in the history, with the latest change being the first in
	 * the history.
	 * 
	 * @param pageSize
	 *            the size of the pages
	 * @param pageNo
	 *            the number of the page of interest
	 * @return a paginated copy of the present status.
	 */
	public Status createPaginatedStatus(int pageSize, int pageNo) {
		Status paginatedStatus = new Status();

		paginatedStatus.setId(this.getId());
		paginatedStatus.setContext(this.getContext());
		paginatedStatus.setStalenessIntervalInMsecs(this.getStalenessIntervalInMsecs());
		paginatedStatus.setErrorTemplate(this.getErrorTemplate());
		paginatedStatus.setOkTemplate(this.getOkTemplate());
		paginatedStatus.setCode(this.getCode());
		paginatedStatus.setLongName(this.getLongName());
		paginatedStatus.setDescription(this.getDescription());
		paginatedStatus.setContact(this.getContact());
		paginatedStatus.setContactEmail(this.getContactEmail());
		paginatedStatus.setLighthouseDomain(this.getLighthouseDomain());
		paginatedStatus.setClearanceType(this.getClearanceType());
		paginatedStatus.setEnabled(this.isEnabled());
		paginatedStatus.statusChangeNotificationChannels = new HashSet<StatusChangeNotificationChannel<?, ?>>(this.statusChangeNotificationChannels);
		paginatedStatus.latestStatusChange = null;
		paginatedStatus.metadata = new HashMap<String, Object>(this.metadata);

		int paginationStart = Math.max(0, pageSize * (pageNo - 1) + 1);
		int paginationStop = Math.max(0, pageSize * pageNo);

		// iterate forward until paginationStop reached or out of changes
		int currentPosition = 0;
		StatusChange change = this.latestStatusChange;
		while (change.getPreviousStatusChange() != null && currentPosition < paginationStop) {
			change = change.getPreviousStatusChange();
			currentPosition++;
		}

		// iterate backwards until paginationStart reached
		do {
			// in order to protect _this_ object and its status changes from the
			// following re-chaining of status changes, we need to copy the
			// status changes first
			StatusChange copy = null;
			try {
				copy = (StatusChange) change.clone();
			} catch (CloneNotSupportedException e) {
				log.error("Could not clone status change.", e);
			}

			paginatedStatus.addChangeToHistory(copy);
			change = change.getNextStatusChange();
			currentPosition--;
		} while (currentPosition >= paginationStart);

		// to comply with the method description, we must ensure that the
		// latestStatusChange is always the latestStatusChange
		if (paginatedStatus.latestStatusChange != this.latestStatusChange) {
			try {
				paginatedStatus.addChangeToHistory((StatusChange) this.latestStatusChange.clone());
			} catch (CloneNotSupportedException e) {
				log.error("Could not clone status change.", e);
			}
		}

		return paginatedStatus;
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.isAutomaticallyCleared()) {
			xml.writeEntityWithText("clearance", "auto");
		} else {
			xml.writeEntityWithText("clearance", "manual");
		}

		xml.writeEntityWithText("stalenessInterval", this.getStalenessIntervalInMsecs());

		if (this.getContext() != null) {
			xml.writeEntity("context");
			String name = this.getContext().getRootElementName();
			this.getContext().writeEntityReference(name, xml);
			xml.endEntity();
		}

		if (this.getOkTemplate() != null) {
			xml.writeEntity("okTemplate");
			xml.writeEntity("Event");
			this.getOkTemplate().fillRootElement(xml);
			xml.endEntity();
			xml.endEntity();
		}

		if (this.getErrorTemplate() != null) {
			xml.writeEntity("errorTemplate");
			xml.writeEntity("Event");
			this.getErrorTemplate().fillRootElement(xml);
			xml.endEntity();
			xml.endEntity();
		}

		if (this.getLongName() != null) {
			xml.writeEntityWithText("longName", this.getLongName());
		}

		if (this.getDescription() != null) {
			xml.writeEntityWithText("description", this.getDescription());
		}

		if (this.getContact() != null) {
			xml.writeEntityWithText("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			xml.writeEntityWithText("contactEmail", this.getContactEmail());
		}

		xml.writeEntityWithText("enabled", Boolean.toString(this.isEnabled()));

		xml.writeEntity("changeHistory");

		for (StatusChange change : this.getChangeHistory()) {
			change.writeToXmlWriter("change", xml);
		}

		xml.endEntity();

		xml.writeEntity("notificationChannels");

		for (StatusChangeNotificationChannel<?, ?> changeNotificationChannel : this.getChangeNotificationChannels()) {
			changeNotificationChannel.writeToXmlWriter(xml);
		}

		xml.endEntity();

		if (this.getMetadata().size() > 0) {
			xml.writeEntity("metadata");
			for (String metadate : this.getMetadata().keySet()) {
				xml.writeEntity("metadate");
				xml.writeEntityWithText("key", metadate);

				Object value = this.getMetadate(metadate);

				if (value instanceof Date) {
					xml.writeEntityWithText("type", "dateTime");
					xml.writeEntityWithText("value", XmlMuncher.javaDateToXmlDateTime((Date) value));
				} else if (value instanceof byte[]) {
					xml.writeEntityWithText("type", "binary");
					xml.writeEntityWithText("value", XmlMuncher.byteArrayToXmlBinary((byte[]) value));
				} else if (value instanceof char[]) {
						xml.writeEntityWithText("type", "clob");
						xml.writeEntityWithText("value", XmlMuncher.charArrayToXmlBinary((char[]) value));
				} else {
					xml.writeEntityWithText("type", value.getClass().getSimpleName().toLowerCase());
					xml.writeEntityWithText("value", value.toString());
				}
				xml.endEntity();

			}
			xml.endEntity();
		}
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		String clearance = xmlDocument.readValueFromXml("/*/:clearance");

		if ("manual".equals(clearance)) {
			this.setClearanceType(Status.MANUAL_CLEARANCE);
		} else {
			this.setClearanceType(Status.AUTO_CLEARANCE);
		}

		this.setStalenessIntervalInMsecs(Long.parseLong(xmlDocument.readValueFromXml("/*/:stalenessInterval")));
		this.setLongName(xmlDocument.readValueFromXml("/*/:longName"));
		this.setDescription(xmlDocument.readValueFromXml("/*/:description"));
		this.setContact(xmlDocument.readValueFromXml("/*/:contact"));
		this.setContactEmail(xmlDocument.readValueFromXml("/*/:contactEmail"));
		this.setEnabled(Boolean.valueOf(xmlDocument.readValueFromXml("/*/:enabled")));
		this.readNotificationChannels(xmlDocument);
		this.readMetadata(xmlDocument);
	}

	private void readMetadata(final XmlMuncher xmlDocument) {
		this.metadata = new HashMap<String, Object>();
		List<String> metadata = xmlDocument.readValuesFromXml("/*/:metadata/:metadate/:key");
		List<String> metadataTypes = xmlDocument.readValuesFromXml("/*/:metadata/:metadate/:type");
		List<String> metadataValues = xmlDocument.readValuesFromXml("/*/:metadata/:metadate/:value");

		for (int u = 0; u < metadata.size(); u++) {
			String metadate = metadata.get(u);

			Object value = getMetadateValueForStringAndType(metadataTypes.get(u), metadataValues.get(u));

			if (value != null) {
				this.setMetadate(metadate, value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void readStatusChangeHistory(XmlMuncher xmlDocument, DomainModelEntityDAO... resolversForEntityReferences) {
		// if this was loaded from db, we do not need to update the change history from xml
		if (this.latestStatusChange != null && this.latestStatusChange.getId() != 0) {
			return;
		}
		
		List<XmlMuncher> changes = xmlDocument.getSubMunchersForContext("/*/:changeHistory/:change");
		
		StatusChange deserializedChange = null;
		try {
			for (XmlMuncher aChange : changes) {
				StatusChange statusChange = StatusChange.parseStatusChange(aChange, resolversForEntityReferences);
				statusChange.setStatus(this);
					
				if (deserializedChange != null) {
					statusChange.setPreviousStatusChange(deserializedChange);
					deserializedChange.setNextStatusChange(statusChange);
				}
				deserializedChange = statusChange;
			}
		} catch (Exception e) {
			throw new XMLSerializationException("XML deserialization of status change history failed, ", e);
		}

		if (this.latestStatusChange == null || !this.latestStatusChange.equals(deserializedChange)) {
			this.latestStatusChange = deserializedChange;
		}
	}

	private void readNotificationChannels(XmlMuncher xmlDocument) {
		Set<StatusChangeNotificationChannel<?, ?>> deserializedChannels = new HashSet<StatusChangeNotificationChannel<?,?>>();
		try {
			for (XmlMuncher aChannel : xmlDocument.getSubMunchersForContext("/*/:notificationChannels/:notificationChannel")) {
				StatusChangeNotificationChannel<?, ?> channel = StatusChangeNotificationChannel.parseStatusChangeNotificationChannel(aChannel);
				deserializedChannels.add(channel);
			}
		} catch (Exception e) {
			throw new XMLSerializationException("XML deserialization of status change notification channels failed, ", e);
		}
		
		if (!deserializedChannels.equals(this.statusChangeNotificationChannels)) {
			
			for (StatusChangeNotificationChannel<?, ?> channel : this.statusChangeNotificationChannels) {
				this.detachChangeNotificationChannel(channel);
			}
			
			for (StatusChangeNotificationChannel<?, ?> channel : deserializedChannels) {
				this.attachChangeNotificationChannel(channel);
			}
		}
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument, DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length == 0) || !resolversForEntityReferences[0].getManagedType().equals(Environment.class)) {
			throw new XMLSerializationException(
					"XML deserialization of status requires reference to a EnvironmentRegistry as 1st resolverForEntityReferences.", null);
		}

		if ((resolversForEntityReferences.length <= 1) || !resolversForEntityReferences[1].getManagedType().equals(ProcessTask.class)) {
			throw new XMLSerializationException(
					"XML deserialization of status requires reference to a ProcessTaskRegistry as 2nd resolverForEntityReferences.", null);
		}

		if ((resolversForEntityReferences.length <= 2) || !resolversForEntityReferences[2].getManagedType().equals(Deployment.class)) {
			throw new XMLSerializationException("XML deserialization of status requires reference to a DeploymentRegistry as 3rd resolverForEntityReferences.",
					null);
		}

		if ((resolversForEntityReferences.length <= 3) || !resolversForEntityReferences[3].getManagedType().equals(SoftwareComponent.class)) {
			throw new XMLSerializationException(
					"XML deserialization of status requires reference to a SoftwareComponentRegistry as 4th resolverForEntityReferences.", null);
		}

		EnvironmentRegistry environmentRegistry = (EnvironmentRegistry) resolversForEntityReferences[0];
		ProcessTaskRegistry processTaskRegistry = (ProcessTaskRegistry) resolversForEntityReferences[1];
		DeploymentRegistry deploymentRegistry = (DeploymentRegistry) resolversForEntityReferences[2];
		SoftwareComponentRegistry softwareComponentRegistry = (SoftwareComponentRegistry) resolversForEntityReferences[3];

		String environmentCode = xmlDocument.readValueFromXml("/*/:context/:Environment/:code");

		if (environmentCode != null) {

			Environment environment = environmentRegistry.findByCode(environmentCode);

			if (environment == null) {
				throw new XMLSerializationException("XML serialization of status references unknown environment with code " + environmentCode, null);
			}
			this.setContext(environment);

		} else {

			String processCode = xmlDocument.readValueFromXml("/*/:context/:ProcessTask/:code");

			if (processCode != null) {

				ProcessTask processTask = processTaskRegistry.findByCode(processCode);

				if (processTask == null) {
					throw new XMLSerializationException("XML serialization of status references unknown process / task with code " + processCode, null);
				}
				this.setContext(processTask);

			} else {

				String deploymentLocation = xmlDocument.readValueFromXml("/*/:context/:Deployment/:deploymentLocation");
				String deployedComponentCode = xmlDocument.readValueFromXml("/*/:context/:Deployment/:deployedComponentCode");

				if (((deployedComponentCode != null) && (deploymentLocation == null)) || ((deployedComponentCode == null) && (deploymentLocation != null))
						|| ((deployedComponentCode == null) && (deploymentLocation == null))) {
					throw new XMLSerializationException("XML serialization of status features incomplete reference to deployment: code or location missing",
							null);
				}

				this.setContext(this.findDeploymentByLocationAndCode(softwareComponentRegistry, deploymentRegistry, deploymentLocation, deployedComponentCode));

			}
		}

		try {
			XmlMuncher okTemplateXml = xmlDocument.getSubMunchersForContext("/*/:okTemplate/:Event").get(0);
			XmlMuncher errorTemplateXml = xmlDocument.getSubMunchersForContext("/*/:errorTemplate/:Event").get(0);

			Event okTemplate = EventBuilder.template().done();
			okTemplate.fromXml(okTemplateXml, deploymentRegistry, softwareComponentRegistry);
			this.setOkTemplate(okTemplate);

			Event errorTemplate = EventBuilder.template().done();
			errorTemplate.fromXml(errorTemplateXml, deploymentRegistry, softwareComponentRegistry);
			this.setErrorTemplate(errorTemplate);
		} catch (Exception e) {
			throw new XMLSerializationException("XML deserialization status ok or error template failed, ", e);
		}

		this.readStatusChangeHistory(xmlDocument, deploymentRegistry, softwareComponentRegistry);

	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> queryParameters = super.toQueryParameters();

		if (this.getLongName() != null) {
			queryParameters.put("longName", this.getLongName());
		}

		if (this.getDescription() != null) {
			queryParameters.put("description", this.getDescription());
		}

		if (this.getContact() != null) {
			queryParameters.put("contact", this.getContact());
		}

		if (this.getContactEmail() != null) {
			queryParameters.put("contactEmail", this.getContactEmail());
		}

		if (this.isAutomaticallyCleared()) {
			queryParameters.put("clearance", "auto");
		} else {
			queryParameters.put("clearance", "manual");
		}

		if (!this.getMetadata().isEmpty()) {
			StringBuilder metadataBuilder = new StringBuilder();
			StringBuilder metadataValuesBuilder = new StringBuilder();
			StringBuilder metadataTypesBuilder = new StringBuilder();

			boolean firstRun = true;

			for (String metadate : this.getMetadata().keySet()) {
				if (!firstRun) {
					metadataBuilder.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
					metadataValuesBuilder.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
					metadataTypesBuilder.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
				}

				metadataBuilder.append(metadate);
				Object metadateValue = this.getMetadate(metadate);

				if (metadateValue instanceof Date) {
					metadataValuesBuilder.append(XmlMuncher.javaDateToXmlDateTime((Date) metadateValue));
					metadataTypesBuilder.append("dateTime");
				}

				if (metadateValue instanceof Integer) {
					metadataValuesBuilder.append(metadateValue.toString());
					metadataTypesBuilder.append("integer");
				}

				if (metadateValue instanceof Long) {
					metadataValuesBuilder.append(metadateValue.toString());
					metadataTypesBuilder.append("long");
				}

				if (metadateValue instanceof Float) {
					metadataValuesBuilder.append(metadateValue.toString());
					metadataTypesBuilder.append("float");
				}

				if (metadateValue instanceof Double) {
					metadataValuesBuilder.append(metadateValue.toString());
					metadataTypesBuilder.append("double");
				}

				if (metadateValue instanceof byte[]) {
					metadataValuesBuilder.append(XmlMuncher.byteArrayToXmlBinary((byte[]) metadateValue));
					metadataTypesBuilder.append("binary");
				}
				
				if (metadateValue instanceof char[]) {
					metadataValuesBuilder.append(XmlMuncher.charArrayToXmlBinary((char[]) metadateValue));
					metadataTypesBuilder.append("clob");
				}

				if (metadateValue instanceof String) {
					metadataValuesBuilder.append(metadateValue);
					metadataTypesBuilder.append("string");
				}

				firstRun = false;
			}

			queryParameters.put("metadata", metadataBuilder.toString());
			queryParameters.put("metadataValues", metadataValuesBuilder.toString());
			queryParameters.put("metadataTypes", metadataTypesBuilder.toString());
		}

		return queryParameters;
	}

	@Override
	public void fromQueryParameters(Map<String, String> queryParameters) {

		this.setLongName(queryParameters.get("longName"));
		this.setDescription(queryParameters.get("description"));
		this.setContact(queryParameters.get("contact"));
		this.setContactEmail(queryParameters.get("contactEmail"));

		if (queryParameters.containsKey("clearance") && queryParameters.get("clearance").equals("auto")) {
			this.setClearanceType(Status.AUTO_CLEARANCE);
		} else if (queryParameters.containsKey("clearance") && queryParameters.get("clearance").equals("manual")) {
			this.setClearanceType(Status.MANUAL_CLEARANCE);
		}

		if (queryParameters.containsKey("metadata") && queryParameters.containsKey("metadataValues") && queryParameters.containsKey("metadataTypes")) {

			List<String> metadata = XmlMuncher.getValueEnumerationElements(queryParameters.get("metadata"));
			List<String> metadataValues = XmlMuncher.getValueEnumerationElements(queryParameters.get("metadataValues"));
			List<String> metadataTypes = XmlMuncher.getValueEnumerationElements(queryParameters.get("metadataTypes"));

			if ((metadata.size() != metadataValues.size()) || (metadataValues.size() != metadataTypes.size()) || (metadata.size() != metadataTypes.size())) {
				throw new ConstraintViolationException("Invalid enumeration in metadata", null);
			}

			for (int i = 0; i < metadata.size(); i++) {
				String metadate = metadata.get(i);
				String metadataValue = metadataValues.get(i);
				String metadataType = metadataTypes.get(i);
				Object value = getMetadateValueForStringAndType(metadataType, metadataValue);

				if (value != null) {
					this.setMetadate(metadate, value);
				}
			}

			queryParameters.remove("metadata");
			queryParameters.remove("metadataValues");
			queryParameters.remove("metadataTypes");
		}

		super.fromQueryParameters(queryParameters);
	}

	/**
	 * This method returns a path identifier for the given status. Its URL-like
	 * form is constructed by taking the path of the context of the status and
	 * appending the status code. The path identifier is build by appending the
	 * string "status" to the identifier of the context path.
	 * 
	 * @return the path for the status.
	 */
	@Override
	public String getPath() {
		return (this.getContext().getPath() + "/" + this.getCode()).replaceFirst(":", "status:");
	}

	/**
	 * This is the public constructor for status. Calling it moves the current
	 * status to <code>NONE</code>.
	 */
	public Status() {
		ManualStatusClearance initialStatus = new ManualStatusClearance();
		this.addChangeToHistory(initialStatus);
	}

	/**
	 * Given a software component and deployment registry, this method retrieves
	 * a deployment identified by deployed software component code and
	 * deployment location.
	 * 
	 * @param softwareComponentRegistry
	 *            the software component registry to use
	 * @param deploymentRegistry
	 *            the deployment registry to use
	 * @param location
	 *            the deployment location
	 * @param code
	 *            the deployed component's code
	 * @return the deployment
	 * @throws ConstraintViolationException
	 *             in case a component deployment cannot be found at the given
	 *             location.
	 */
	private Deployment findDeploymentByLocationAndCode(SoftwareComponentRegistry softwareComponentRegistry, DeploymentRegistry deploymentRegistry,
			String location, String code) {
		SoftwareComponent softwareComponent = softwareComponentRegistry.findByCode(code);

		if (softwareComponent == null) {
			throw new ConstraintViolationException("Cannot find software component with code " + code + " for status context", null);
		}

		Deployment context = deploymentRegistry.findByComponentAndLocation(softwareComponent, location);

		if (context == null) {
			throw new ConstraintViolationException("Cannot find deployment of software component with code " + code + " at location " + location
					+ " for status context", null);
		}
		return context;
	}

	public static enum MetadateType {
		STRING("string"), BOOLEAN("boolean"), INTEGER("integer"), LONG("long"), DOUBLE("double"), FLOAT("float"), BINARY("binary"), CLOB("clob"), DATE_TIME("dateTime");
		private final String type;
		private static Map<String, MetadateType> types = new HashMap<String, MetadateType>();
		static {
			for (MetadateType mt : values()) {
				types.put(mt.type, mt);
			}
		}

		MetadateType(String type) {
			this.type = type;
		}

		public static MetadateType fromTypeString(String type) {
			return types.get(type);
		}
	}
}
