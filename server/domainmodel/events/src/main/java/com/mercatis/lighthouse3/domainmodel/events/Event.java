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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.IntervalRange;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.ConstraintViolationException;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.UnknownContextException;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;

/**
 * This class captures the various events managed by Lighthouse 3. An event is
 * always created within the context of a deployment.
 * 
 * An event has the following characteristics:
 * <ul>
 * <li>a unique database ID that is assigned automatically by the persistence
 * mechanism on which the <code>EventRegistry</code> is based.
 * <li>the deployment of the software component that created the event;
 * <li>an event code, identifying the type of event;
 * <li>an event level, identifying the severity of the event;
 * <li>a creation date (timestamp);
 * <li>the machine of origin;
 * <li>an optional descriptive text message associated with the event describing
 * it in more detail;
 * <li>an optional stack trace providing more insights if the event signifies an
 * error;
 * <li>a set of transaction IDs which denote one or more logical business
 * transactions or business process instances in the context of which the event
 * occurred.
 * <li>a set of tags that may be defined by lighthouse users in order to group
 * or mark events.
 * <li>a bunch of user defined fields (UDFs), which define key value pairs of
 * basic types: boolean, integer, long, float, double, date, binary, string.
 * </ul>
 */
public class Event extends DomainModelEntity {
	private static final long serialVersionUID = -8994129275017611362L;

	/**
	 * The enumeration of possible event levels.
	 */
	final public static String FATAL = "FATAL";

	final public static String ERROR = "ERROR";

	final public static String WARNING = "WARNING";

	final public static String INFO = "INFO";

	final public static String DETAIL = "DETAIL";

	final public static String DEBUG = "DEBUG";

	/**
	 * This property keeps the filter from which the event originates.This value
	 * is set by the event filter API.
	 * 
	 */
	transient private String filterOfOrigin = null;

	/**
	 * This method returns filter from which the present event originates. This
	 * value is set by the event filter API.
	 * 
	 * @return the event filter UUID
	 */
	public String getFilterOfOrigin() {
		return this.filterOfOrigin;
	}

	/**
	 * This method sets the UUID of the event filter delivering the event. This
	 * value is set by the event filter API.
	 * 
	 * @param filterUUID
	 *            the event filter UUID
	 */
	public void setFilterOfOrigin(String filterUUID) {
		this.filterOfOrigin = filterUUID;
	}

	/**
	 * The context in which the event occurred.
	 */
	private Deployment context = null;

	/**
	 * This method returns the context in which the event occurred
	 * 
	 * @return the deployment context
	 */
	public Deployment getContext() {
		return context;
	}

	/**
	 * This sets the context of the event.
	 * 
	 * @param context
	 *            the deployment which produced the event.
	 * @return the event itself for setter chaining
	 */
	public void setContext(Deployment context) {
		this.context = context;
	}

	/**
	 * The code characterizing the type of event. The default is
	 * <code>Log</code>.
	 */
	private String code = "Log";

	/**
	 * This method retrieves the code of the event.
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * This sets the the code of the event.
	 * 
	 * @param code
	 *            the event code
	 * @return the event itself for setter chaining
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * This property maintains the severity level of the event. The default is
	 * <code>INFO</code>.
	 */
	private String level = INFO;

	/**
	 * This method returns the severity level of the event. The default is
	 * <code>INFO</code>.
	 * 
	 * @return
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * This method set the severity level of the event. The default is
	 * <code>INFO</code>.
	 * 
	 * @param level
	 *            the level to set. One of: <code>FATAL</code>,
	 *            <code>ERROR</code>, <code>WARNING</code>, <code>INFO</code>,
	 *            <code>DETAIL</code>, <code>DEBUG</code>, or <code>null</code>.
	 *            If the level is set to something else, nothing happens.
	 * 
	 * @return the event itself for setter chaining
	 * 
	 */
	public void setLevel(String level) {
		if ((level == null) || Ranger.isEnumerationRange(level)
				|| FATAL.equals(level) || ERROR.equals(level)
				|| WARNING.equals(level) || INFO.equals(level)
				|| DETAIL.equals(level) || DEBUG.equals(level))
			this.level = level;
	}

	/**
	 * The date of occurrence of the event. Defaults to the time of event object
	 * creation.
	 */
	private Date dateOfOccurrence = new Date();

	/**
	 * This method returns the date of occurrence of the present event. Defaults
	 * to the timestamp of event object creation.
	 * 
	 * @return the date of event occurrence.
	 */
	public Date getDateOfOccurrence() {
		return dateOfOccurrence;
	}

	/**
	 * This method sets the date of occurrence of the event.
	 * 
	 * @param dateOfOccurrence
	 *            the date of occurrence
	 * 
	 * @return the event itself for setter chaining
	 */
	public void setDateOfOccurrence(Date creationDate) {
		this.dateOfOccurrence = creationDate;
	}

	/**
	 * The machine where the event occurred.
	 */
	private String machineOfOrigin = null;

	/**
	 * This method returns the machine from which the event originates.
	 * 
	 * @return the machine of origin.
	 */
	public String getMachineOfOrigin() {
		return this.machineOfOrigin;
	}

	/**
	 * This method sets the machine of origin.
	 * 
	 * @param machineOfOrigin
	 *            the machine of origin
	 * @return the event itself for setter chaining.
	 */
	public void setMachineOfOrigin(String machineOfOrigin) {
		this.machineOfOrigin = machineOfOrigin;
	}

	/**
	 * An optional descriptive message describing the event in more detail.
	 */
	private String message = null;

	/**
	 * Returns an optional descriptive message describing the event.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets an optional descriptive message describing the event.
	 * 
	 * @param message
	 *            the message to set.
	 * @return the event itself for setter chaining
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * An optional text dump describing a problem situation signified by the
	 * present event in more detail.
	 */
	private String stackTrace = null;

	/**
	 * Returns an optional text dump describing a problem situation signified by
	 * the present event in more detail.
	 * 
	 * @return the stack trace
	 */
	public String getStackTrace() {
		return stackTrace;
	}

	/**
	 * Sets an optional text dump describing a problem situation signified by
	 * the present event in more detail.
	 * 
	 * @param stackTrace
	 *            the stack trace to set.
	 * 
	 */
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	/**
	 * A set of transaction IDs which denote one or more logical business
	 * transactions or business process instances in the context of which the
	 * event occurred.
	 */
	private Set<String> transactionIds = new HashSet<String>();

	/**
	 * Sets a transaction ID denoting a logical business transaction or business
	 * process instances in the context of which the event occurred. An event
	 * may be assigned more than one ID.
	 * 
	 * @param transactionId
	 *            the transaction Id to set. .
	 */
	public void addTransactionId(String transactionId) {
		this.transactionIds.add(transactionId);
	}

	/**
	 * Returns the set of transaction IDs associated with the present event.
	 * These denote one or more logical business transactions or business
	 * process instances in the context of which the event occurred.
	 * 
	 * @return the set of transaction IDs
	 */
	public Set<String> getTransactionIds() {
		return this.transactionIds;
	}

	/**
	 * Sets a set of transaction IDs which denote one or more logical business
	 * transactions or business process instances in the context of which the
	 * event occurred.
	 * 
	 * Useful for event template manipulation by means of introspection.
	 * 
	 * @param transactionIds
	 *            the transaction Ids to set.
	 */
	public void setTransactionIds(Set<String> transactionIds) {
		this.transactionIds = transactionIds;
	}

	/**
	 * Sets a set of Tags.
	 * 
	 * @param tags
	 *            the tags to set.
	 */
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	/**
	 * Sets a map of UDFs.
	 * 
	 * @param udfs
	 *            the udfs to set.
	 */
	public void setUdfs(Map<String, Object> udfs) {
		this.udfs = udfs;
	}

	/**
	 * Removes a transaction ID from the event.
	 * 
	 * @param transactionId
	 *            the transaction Id to remove. .
	 */
	public void removeTransactionId(String transactionId) {
		this.transactionIds.remove(transactionId);
	}

	/**
	 * This predicate decides whether the present event appears in the context
	 * of a given transaction.
	 * 
	 * @param transactionId
	 *            the ID of the transaction of interest
	 * @return <code>true</code> iff the event has been assigned to the given
	 *         transaction ID.
	 */
	public boolean inTransaction(String transactionId) {
		return this.transactionIds.contains(transactionId);
	}

	/**
	 * This predicate decides whether the present event appears in the context
	 * of one of the given transactions.
	 * 
	 * @param transactionIds
	 *            the set of IDs of the transactions of interest
	 * @return <code>true</code> iff the event has been assigned to one of the
	 *         given transaction IDs.
	 */
	public boolean inOneTransaction(Set<String> transactionIds) {
		for (String transactionId : transactionIds)
			if (this.inTransaction(transactionId))
				return true;

		return false;
	}

	/**
	 * A set of tags created by Lighthouse users in order to mark or categorize
	 * events.
	 */
	private Set<String> tags = new HashSet<String>();

	/**
	 * Attaches a tag in order to mark or categorize the event.
	 * 
	 * @param tag
	 *            the tag to attach. .
	 */
	public void tag(String tag) {
		this.tags.add(tag);
	}

	/**
	 * Removes a tag from the event.
	 * 
	 * @param transactionId
	 *            the tag to remove. .
	 */
	public void removeTag(String tag) {
		this.tags.remove(tag);
	}

	/**
	 * Returns the set of tags attached by Lighthouse users to the current
	 * event.
	 * 
	 * @return the set of tags
	 */
	public Set<String> getTags() {
		return this.tags;
	}

	/**
	 * This predicate decides whether the present event has a given tag
	 * attached.
	 * 
	 * @param tag
	 *            the tag to look for
	 * @return <code>true</code> iff the tag has been attached to the present
	 *         event.
	 */
	public boolean hasTag(String tag) {
		return this.tags.contains(tag);
	}

	/**
	 * This dictionary manages a set of arbitrary UDFs attached to the event. As
	 * for permissible value type, these are: boolean, integer, long, float,
	 * double, date, binary, string.
	 */
	private Map<String, Object> udfs = new HashMap<String, Object>();

	/**
	 * This method sets a user defined field on the event. Permissible value
	 * types are boolean, integer, long, float, double, date, binary, string.
	 * 
	 * @param udf
	 *            the udf to set.
	 * @param value
	 *            the value of the udf to set. .
	 * @throws ConstraintViolationException
	 *             in case that one tries to set an UDF with an unsupported
	 *             value type.
	 */
	public void setUdf(String udf, Object value) {
		if (!((value instanceof String) || (value instanceof Float)
				|| (value instanceof Double) || (value instanceof Integer)
				|| (value instanceof Long) || (value instanceof Date)
				|| (value instanceof Boolean) || (value instanceof byte[]) || (value instanceof char[])))
			throw new ConstraintViolationException(
					"UDF value of unsupported type. Must be one of boolean, integer, long, float, double, date, binary, string, clob",
					null);

		if (value instanceof String && ((String) value).length() > 255) {
			value = ((String) value).toCharArray();
		}
		this.udfs.put(udf, value);
	}

	/**
	 * This method removes a user defined field from the event. P
	 * 
	 * @param udf
	 *            the udf to clear.
	 */
	public void clearUdf(String udf) {
		this.udfs.remove(udf);
	}

	/**
	 * This method returns the value of a given UDF assigned to the present
	 * event.
	 * 
	 * @param udf
	 *            the UDF containing the value of interest
	 * @return the value of the UDF or <code>null</code>, if the UDF does not
	 *         exist.
	 */
	public Object getUdf(String udf) {
		Object value = this.udfs.get(udf);

		return value;
	}

	/**
	 * This method returns a map with all udf key value pairs assigned to the
	 * present event.
	 * 
	 * @return the udf map
	 */
	public Map<String, Object> getUdfs() {
		return this.udfs;
	}

	/**
	 * This predicate tests whether the present event has a given UDF assigned.
	 * 
	 * @param udf
	 *            the UDF to look for
	 * @return <code>true</code> iff the present event features a UDF of the
	 *         given name
	 */
	public boolean hasUdf(String udf) {
		return this.getUdf(udf) != null;
	}

	/**
	 * This predicate compares the present event with a given event template for
	 * equality. The template is just another event object describing the
	 * structure of the event objects of interest.
	 * 
	 * In general, if a property in the template is set to <code>null</code>, it
	 * is not of interest for the match. If it is not <code>null</code> a
	 * matching event must have the property set to an equal value.
	 * 
	 * The following event properties receive special treatment:
	 * <ul>
	 * <li><code>context</code>: the template may use an enumeration range in
	 * order to look for events from different deployments at the same time.
	 * <li><code>code</code>: the template may use an enumeration range in order
	 * to look for events with different codes at the same time.
	 * <li><code>level</code>: the template may use an enumeration range in
	 * order to look for events with different levels at the same time.
	 * <li><code>dateOfOccurrence</code>: the template may use an interval range
	 * in order to look for events within a given time interval.
	 * <li><code>transactionIds</code>: if the template does not set a
	 * transaction Id, transaction IDs are ignored for the matching. Otherwise,
	 * one of the transaction IDs of the template must be found with each
	 * matching event.
	 * <li><code>message</code>: if the template does not set a message, the
	 * message is ignored for the matching. Otherwise, the message of the
	 * template must be contained in the message of any matching event.
	 * <li><code>stackTrace</code>: if the template does not set a stack trace,
	 * the trace is ignored for the matching. Otherwise, the trace of the
	 * template must be contained in the stack trace of any matching event.
	 * <li><code>tags</code>: if the template does not set any tags, tags are
	 * ignored for the matching. Otherwise, all tags of the template must be
	 * found with each matching event.
	 * <li><code>udfs</code>: if the template does not set any UDFs, UDFs are
	 * ignored for the matching. Otherwise, all UDFs of the template must be
	 * found with each matching event and carry the same value.
	 * </ul>
	 * 
	 * @param template
	 * @return
	 */
	public boolean matches(Event template) {
		if ((template.getId() != 0L) && (this.getId() != template.getId()))
			return false;

		if (template.getContext() != null) {
			if (this.getContext() == null)
				return false;
			if (Ranger.isEnumerationRange(template.getContext())
					&& !Ranger.castToEnumerationRange(template.getContext())
							.contains(this.getContext()))
				return false;
			if (!Ranger.isEnumerationRange(template.getContext())
					&& !this.getContext().equals(template.getContext()))
				return false;
		}

		if (template.getCode() != null) {
			if (this.getCode() == null)
				return false;
			if (Ranger.isEnumerationRange(template.getCode())
					&& !Ranger.castToEnumerationRange(template.getCode())
							.contains(this.getCode()))
				return false;
			if (!Ranger.isEnumerationRange(template.getCode())
					&& !this.getCode().equals(template.getCode()))
				return false;
		}

		if (template.getLevel() != null) {
			if (this.getLevel() == null)
				return false;
			else if (Ranger.isEnumerationRange(template.getLevel())) {
				if (!Ranger.castToEnumerationRange(template.getLevel())
						.contains(this.getLevel()))
					return false;
			} else if (!this.getLevel().equals(template.getLevel()))
				return false;
		}

		if (template.getDateOfOccurrence() != null) {
			if (this.getDateOfOccurrence() == null)
				return false;
			if (Ranger.isIntervalRange(template.getDateOfOccurrence())
					&& !Ranger.castToIntervalRange(
							template.getDateOfOccurrence()).contains(
							this.getDateOfOccurrence()))
				return false;
			if (!Ranger.isIntervalRange(template.getDateOfOccurrence())
					&& !this.getDateOfOccurrence().equals(
							template.getDateOfOccurrence()))
				return false;
		}

		if ((template.getMachineOfOrigin() != null)) {
			if (this.getMachineOfOrigin() == null)
				return false;
			else if (!this.getMachineOfOrigin().equals(
					template.getMachineOfOrigin()))
				return false;
		}

		if ((template.getMessage() != null)) {
			if (this.getMessage() == null)
				return false;
			else if (!this.getMessage().contains(template.getMessage()))
				return false;
		}

		if ((template.getStackTrace() != null)) {
			if (this.getStackTrace() == null)
				return false;
			else if (!this.getStackTrace().contains(template.getStackTrace()))
				return false;
		}

		if (template.getTags().size() > 0) {
			for (String tag : template.getTags()) {
				if (!this.hasTag(tag))
					return false;
			}
		}

		if (template.getTransactionIds().size() > 0) {
			boolean templateFound = false;

			Iterator<String> transactionIds = template.getTransactionIds()
					.iterator();
			while (!templateFound && transactionIds.hasNext()) {
				String transactionId = transactionIds.next();
				if (this.inTransaction(transactionId))
					templateFound = true;
			}

			if (!templateFound)
				return false;
		}

		if (template.getUdfs().size() > 0) {
			for (String udf : template.getUdfs().keySet()) {
				if (!template.getUdf(udf).equals(this.getUdf(udf)))
					return false;
			}
		}

		return true;
	}

	@Override
	public void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getContext() != null) {
			if (!Ranger.isEnumerationRange(this.getContext()))
				this.getContext().writeEntityReference("context", xml);
			else {
				xml.writeEntity("context");
				xml.writeEntity("enumerationRange");

				for (Deployment enumeratedContext : Ranger
						.castToEnumerationRange(this.getContext())
						.getEnumeration())
					enumeratedContext.writeEntityReference("enumeration", xml);

				xml.endEntity();
				xml.endEntity();
			}
		}

		if (this.getCode() != null) {
			if (!Ranger.isEnumerationRange(this.getCode()))
				xml.writeEntityWithText("code", this.getCode());
			else {
				xml.writeEntity("code");
				xml.writeEntity("enumerationRange");

				for (String enumeratedCode : Ranger.castToEnumerationRange(
						this.getCode()).getEnumeration())
					xml.writeEntityWithText("enumeration", enumeratedCode);

				xml.endEntity();
				xml.endEntity();
			}

		}

		if (this.getDateOfOccurrence() != null) {
			if (!Ranger.isIntervalRange(this.getDateOfOccurrence()))
				xml.writeEntityWithText("dateOfOccurrence", XmlMuncher
						.javaDateToXmlDateTime(this.getDateOfOccurrence()));
			else {
				xml.writeEntity("dateOfOccurrence");
				xml.writeEntity("intervalRange");

				IntervalRange<Date> dateRange = Ranger.castToIntervalRange(this
						.getDateOfOccurrence());

				if (dateRange.getLowerBound() != null)
					xml.writeEntityWithText("lowerBound", XmlMuncher
							.javaDateToXmlDateTime(dateRange.getLowerBound()));

				if (dateRange.getUpperBound() != null)
					xml.writeEntityWithText("upperBound", XmlMuncher
							.javaDateToXmlDateTime(dateRange.getUpperBound()));

				xml.endEntity();
				xml.endEntity();
			}

		}

		if (this.getLevel() != null) {
			if (!Ranger.isEnumerationRange(this.getLevel()))
				xml.writeEntityWithText("level", this.getLevel());
			else {
				xml.writeEntity("level");
				xml.writeEntity("enumerationRange");

				for (String enumeratedLevel : Ranger.castToEnumerationRange(
						this.getLevel()).getEnumeration())
					xml.writeEntityWithText("enumeration", enumeratedLevel);

				xml.endEntity();
				xml.endEntity();
			}
		}

		if (this.getMachineOfOrigin() != null)
			xml.writeEntityWithText("machineOfOrigin", this
					.getMachineOfOrigin());

		if (this.getMessage() != null)
			xml.writeEntityWithText("message", this.getMessage());

		if (this.getStackTrace() != null)
			xml.writeEntityWithText("stackTrace", this.getStackTrace());

		if (this.getTransactionIds().size() > 0) {
			xml.writeEntity("transactions");
			for (String transactionId : this.getTransactionIds())
				xml.writeEntityWithText("transactionId", transactionId);
			xml.endEntity();
		}

		if (this.getUdfs().size() > 0) {
			xml.writeEntity("udfs");
			for (String udf : this.getUdfs().keySet()) {
				xml.writeEntity("udf");
				xml.writeEntityWithText("key", udf);

				Object value = this.getUdf(udf);

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

		if (this.getTags().size() > 0) {
			xml.writeEntity("tags");
			for (String tag : this.getTags())
				xml.writeEntityWithText("tag", tag);
			xml.endEntity();
		}

	}

	@Override
	public void writeEntityReference(String referenceTagName, XmlWriter xml)
			throws IOException {
		if (this.getId() != 0L) {
			xml.writeEntity(referenceTagName);
			xml.writeEntityWithText("id", this.getId());
			xml.endEntity();
		}
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		List<String> codes = xmlDocument
				.readValuesFromXml("/*/:code/:enumerationRange/:enumeration");

		if (!codes.isEmpty())
			this.setCode(Ranger.enumeration(new HashSet<String>(codes)));
		else
			this.setCode(xmlDocument.readValueFromXml("/*/:code"));

		List<String> levels = xmlDocument
				.readValuesFromXml("/*/:level/:enumerationRange/:enumeration");

		if (!levels.isEmpty())
			this.setLevel(Ranger.enumeration(new HashSet<String>(levels)));
		else
			this.setLevel(xmlDocument.readValueFromXml("/*/:level"));

		this.setMachineOfOrigin(xmlDocument
				.readValueFromXml("/*/:machineOfOrigin"));
		this.setMessage(xmlDocument.readValueFromXml("/*/:message"));
		this.setStackTrace(xmlDocument.readValueFromXml("/*/:stackTrace"));

		String dateLowerBound = xmlDocument
				.readValueFromXml("/*/:dateOfOccurrence/:intervalRange/:lowerBound");
		String dateUpperBound = xmlDocument
				.readValueFromXml("/*/:dateOfOccurrence/:intervalRange/:upperBound");

		if ((dateLowerBound != null) || (dateUpperBound != null)) {
			Date lowerBound = null;
			if (dateLowerBound != null)
				lowerBound = XmlMuncher.xmlDateTimeToJavaDate(dateLowerBound);

			Date upperBound = null;
			if (dateUpperBound != null)
				upperBound = XmlMuncher.xmlDateTimeToJavaDate(dateUpperBound);

			this.setDateOfOccurrence(Ranger.interval(lowerBound, upperBound));

		} else if (xmlDocument.readValueFromXml("/*/:dateOfOccurrence") != null)
			this.setDateOfOccurrence(XmlMuncher
					.xmlDateTimeToJavaDate(xmlDocument
							.readValueFromXml("/*/:dateOfOccurrence")));
		else
			this.setDateOfOccurrence(null);

		this.tags = new HashSet<String>();

		for (String tag : xmlDocument.readValuesFromXml("/*/:tags/:tag")) {
			this.tag(tag);
		}

		this.transactionIds = new HashSet<String>();
		for (String transactionId : xmlDocument
				.readValuesFromXml("/*/:transactions/:transactionId")) {
			this.addTransactionId(transactionId);
		}

		this.udfs = new HashMap<String, Object>();
		List<XmlMuncher> udfMunchers = xmlDocument.getSubMunchersForContext("/*/:udfs/:udf");
		for (XmlMuncher udfMuncher : udfMunchers) {
			String udfKey = udfMuncher.readValueFromXml("/:udf/:key");
			String udfType = udfMuncher.readValueFromXml("/:udf/:type");
			String udfValue = udfMuncher.readValueFromXml("/:udf/:value");
                        if (udfValue == null) {
                            continue;
                        }
			
			Object value = null;
			if (udfType.equals("string"))
				value = udfValue;
			else if (udfType.equals("boolean"))
				value = new Boolean(udfValue);
			else if (udfType.equals("integer"))
				value = new Integer(udfValue);
			else if (udfType.equals("long"))
				value = new Long(udfValue);
			else if (udfType.equals("double"))
				value = new Double(udfValue);
			else if (udfType.equals("float"))
				value = new Float(udfValue);
			else if (udfType.equals("binary"))
				value = XmlMuncher.xmlBinaryToByteArray(udfValue);
			else if (udfType.equals("clob"))
				value = XmlMuncher.xmlBinaryToCharArray(udfValue);
			else if (udfType.equals("dateTime"))
				value = XmlMuncher.xmlDateTimeToJavaDate(udfValue);

			if (value != null)
				this.setUdf(udfKey, value);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xmlDocument,
			DomainModelEntityDAO... resolversForEntityReferences) {

		if ((resolversForEntityReferences.length == 0)
				|| !resolversForEntityReferences[0].getManagedType().equals(
						Deployment.class)) {
			throw new XMLSerializationException(
					"XML deserialization of event requires reference to a DeploymentRegistry as 1st resolverForEntityReferences.",
					null);
		}

		if ((resolversForEntityReferences.length <= 1)
				|| !resolversForEntityReferences[1].getManagedType().equals(
						SoftwareComponent.class)) {
			throw new XMLSerializationException(
					"XML deserialization of event requires reference to a SoftwareComponentRegistry as 2nd resolverForEntityReferences.",
					null);
		}

		DeploymentRegistry deploymentRegistry = (DeploymentRegistry) resolversForEntityReferences[0];
		SoftwareComponentRegistry softwareComponentRegistry = (SoftwareComponentRegistry) resolversForEntityReferences[1];

		super.resolveEntityReferencesFromXml(xmlDocument,
				resolversForEntityReferences);

		String deploymentLocation = xmlDocument
				.readValueFromXml("/*/:context/:deploymentLocation");
		String deployedComponentCode = xmlDocument
				.readValueFromXml("/*/:context/:deployedComponentCode");

		if ((deploymentLocation != null) && (deployedComponentCode != null)) {
			this.setContext(this.findDeploymentByLocationAndCode(
					softwareComponentRegistry, deploymentRegistry,
					deploymentLocation, deployedComponentCode));
		} else {
			List<String> deploymentLocations = xmlDocument
					.readValuesFromXml("/*/:context/:enumerationRange/:enumeration/:deploymentLocation");
			List<String> deployedComponentCodes = xmlDocument
					.readValuesFromXml("/*/:context/:enumerationRange/:enumeration/:deployedComponentCode");

			if (!deploymentLocations.isEmpty()
					&& (deploymentLocations.size() == deployedComponentCodes
							.size())) {
				Set<Deployment> contextEnumeration = new HashSet<Deployment>();

				for (int l = 0; l < deploymentLocations.size(); l++)
					contextEnumeration.add(this
							.findDeploymentByLocationAndCode(
									softwareComponentRegistry,
									deploymentRegistry, deploymentLocations
											.get(l), deployedComponentCodes
											.get(l)));

				this.setContext(Ranger.enumeration(contextEnumeration));
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime
				* result
				+ ((dateOfOccurrence == null) ? 0 : dateOfOccurrence.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result
				+ ((machineOfOrigin == null) ? 0 : machineOfOrigin.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((stackTrace == null) ? 0 : stackTrace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (!getClass().isInstance(obj))
			return false;

		Event other = (Event) obj;

		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;

		if (dateOfOccurrence == null) {
			if (other.dateOfOccurrence != null)
				return false;
		} else if (!dateOfOccurrence.equals(other.dateOfOccurrence))
			return false;

		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;

		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;

		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;

		if (machineOfOrigin == null) {
			if (other.machineOfOrigin != null)
				return false;
		} else if (!machineOfOrigin.equals(other.machineOfOrigin))
			return false;

		if (stackTrace == null) {
			if (other.stackTrace != null)
				return false;
		} else if (!stackTrace.equals(other.stackTrace))
			return false;

		for (String transactionId : this.getTransactionIds()) {
			if (!other.inTransaction(transactionId))
				return false;
		}

		if (this.getTransactionIds().size() != other.getTransactionIds().size())
			return false;

		for (String tag : this.getTags()) {
			if (!other.hasTag(tag))
				return false;
		}

		if (this.getTags().size() != other.getTags().size())
			return false;

		for (String udf : this.getUdfs().keySet()) {
			if ((other.getUdf(udf) == null) || !other.getUdf(udf).equals(this.getUdf(udf)))
				return false;
		}

		if (this.getUdfs().keySet().size() != other.getUdfs().keySet().size())
			return false;

		return true;
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> queryParameters = super.toQueryParameters();
		if (this.getContext() != null) {
			if (Ranger.isEnumerationRange(this.getContext())) {
				StringBuilder contextLocation = new StringBuilder();
				StringBuilder contextCode = new StringBuilder();
				boolean firstRun = true;

				for (Deployment deployment : Ranger.castToEnumerationRange(
						this.getContext()).getEnumeration()) {
					if (!firstRun) {
						contextCode.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
						contextLocation.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
					}

					contextCode.append(deployment.getDeployedComponent()
							.getCode());
					contextLocation.append(deployment.getLocation());

					firstRun = false;
				}

				queryParameters.put("contextLocation", contextLocation
						.toString());
				queryParameters.put("contextCode", contextCode.toString());
			} else {
				queryParameters.put("contextLocation", this.getContext()
						.getLocation());
				queryParameters.put("contextCode", this.getContext()
						.getDeployedComponent().getCode());
			}
		}

		if (this.getCode() != null) {
			if (Ranger.isEnumerationRange(this.getCode())) {
				StringBuilder codeBuilder = new StringBuilder();
				boolean firstRun = true;

				for (String code : Ranger
						.castToEnumerationRange(this.getCode())
						.getEnumeration()) {
					if (!firstRun)
						codeBuilder
								.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);

					codeBuilder.append(code);
					firstRun = false;
				}

				queryParameters.put("code", codeBuilder.toString());
			} else {
				queryParameters.put("code", this.getCode());
			}
		}

		if (this.getLevel() != null) {
			if (Ranger.isEnumerationRange(this.getLevel())) {
				StringBuilder levelBuilder = new StringBuilder();
				boolean firstRun = true;

				for (String level : Ranger.castToEnumerationRange(
						this.getLevel()).getEnumeration()) {
					if (!firstRun)
						levelBuilder
								.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);

					levelBuilder.append(level);
					firstRun = false;
				}

				queryParameters.put("level", levelBuilder.toString());
			} else {
				queryParameters.put("level", this.getLevel());
			}
		}

		if (this.getDateOfOccurrence() != null) {
			if (Ranger.isIntervalRange(this.getDateOfOccurrence())) {
				StringBuilder dateBuilder = new StringBuilder();
				IntervalRange<Date> range = Ranger.castToIntervalRange(this
						.getDateOfOccurrence());

				if (range.getLowerBound() != null)
					dateBuilder.append(XmlMuncher.javaDateToXmlDateTime(range
							.getLowerBound()));

				dateBuilder.append(XmlMuncher.VALUE_INTERVAL_SEPARATOR);

				if (range.getUpperBound() != null)
					dateBuilder.append(XmlMuncher.javaDateToXmlDateTime(range
							.getUpperBound()));

				queryParameters.put("dateOfOccurrence", dateBuilder.toString());
			} else {
				queryParameters.put("dateOfOccurrence", XmlMuncher
						.javaDateToXmlDateTime(this.getDateOfOccurrence()));
			}
		}

		if (this.getMachineOfOrigin() != null)
			queryParameters.put("machineOfOrigin", this.getMachineOfOrigin());

		if (this.getMessage() != null)
			queryParameters.put("message", this.getMessage());

		if (this.getStackTrace() != null)
			queryParameters.put("stackTrace", this.getStackTrace());

		if (!this.getTransactionIds().isEmpty()) {
			StringBuilder transactionBuilder = new StringBuilder();
			boolean firstRun = true;

			for (String transactionId : this.getTransactionIds()) {
				if (!firstRun)
					transactionBuilder
							.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);

				transactionBuilder.append(transactionId);
				firstRun = false;
			}

			queryParameters
					.put("transactionIds", transactionBuilder.toString());
		}

		if (!this.getTags().isEmpty()) {
			StringBuilder tagBuilder = new StringBuilder();
			boolean firstRun = true;

			for (String tag : this.getTags()) {
				if (!firstRun)
					tagBuilder.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);

				tagBuilder.append(tag);
				firstRun = false;
			}

			queryParameters.put("tags", tagBuilder.toString());
		}

		if (!this.getUdfs().isEmpty()) {
			StringBuilder udfsBuilder = new StringBuilder();
			StringBuilder udfValuesBuilder = new StringBuilder();
			StringBuilder udfTypesBuilder = new StringBuilder();

			boolean firstRun = true;

			for (String udf : this.getUdfs().keySet()) {
				if (!firstRun) {
					udfsBuilder.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
					udfValuesBuilder
							.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
					udfTypesBuilder
							.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
				}

				udfsBuilder.append(udf);
				Object udfValue = this.getUdf(udf);

				if (udfValue instanceof Date) {
					udfValuesBuilder.append(XmlMuncher
							.javaDateToXmlDateTime((Date) udfValue));
					udfTypesBuilder.append("dateTime");
				}

				if (udfValue instanceof Integer) {
					udfValuesBuilder.append(udfValue.toString());
					udfTypesBuilder.append("integer");
				}

				if (udfValue instanceof Long) {
					udfValuesBuilder.append(udfValue.toString());
					udfTypesBuilder.append("long");
				}

				if (udfValue instanceof Float) {
					udfValuesBuilder.append(udfValue.toString());
					udfTypesBuilder.append("float");
				}

				if (udfValue instanceof Double) {
					udfValuesBuilder.append(udfValue.toString());
					udfTypesBuilder.append("double");
				}

				if (udfValue instanceof byte[]) {
					udfValuesBuilder.append(XmlMuncher
							.byteArrayToXmlBinary((byte[]) udfValue));
					udfTypesBuilder.append("binary");
				}
				
				if (udfValue instanceof char[]) {
					udfValuesBuilder.append(XmlMuncher.charArrayToXmlBinary((char[]) udfValue));
					udfTypesBuilder.append("clob");
				}

				if (udfValue instanceof String) {
					udfValuesBuilder.append(udfValue);
					udfTypesBuilder.append("string");
				}

				firstRun = false;
			}

			queryParameters.put("udfs", udfsBuilder.toString());
			queryParameters.put("udfValues", udfValuesBuilder.toString());
			queryParameters.put("udfTypes", udfTypesBuilder.toString());
		}

		return queryParameters;
	}

	/**
	 * This method fills in the present event from a bunch of CGI query
	 * parameters. It supports the more complex event attributes like udfs,
	 * tags, and transaction IDs as well as ranges for filling in event
	 * templates.
	 * 
	 * @param queryParameters
	 *            the query parameters
	 * @param softwareComponentRegistry
	 *            the software component registry to use for resolving the event
	 *            context
	 * @param deploymentRegistry
	 *            the deployment registry to use for resolvin the event context
	 */
	public void fromQueryParameters(Map<String, String> queryParameters,
			SoftwareComponentRegistry softwareComponentRegistry,
			DeploymentRegistry deploymentRegistry) {
		if (queryParameters.containsKey("contextLocation")
				&& queryParameters.containsKey("contextCode")) {
			String locationValue = queryParameters.get("contextLocation");
			String codeValue = queryParameters.get("contextCode");

			if (locationValue.contains(XmlMuncher.VALUE_ENUMERATION_SEPARATOR)) {
				List<String> locations = XmlMuncher
						.getValueEnumerationElements(locationValue);
				List<String> codes = XmlMuncher
						.getValueEnumerationElements(codeValue);

				if (locations.size() != codes.size())
					throw new ConstraintViolationException(
							"Invalid enumeration in event context", null);

				Set<Deployment> enumeration = new HashSet<Deployment>();

				for (int i = 0; i < locations.size(); i++) {
					enumeration.add(this.findDeploymentByLocationAndCode(
							softwareComponentRegistry, deploymentRegistry,
							locations.get(i), codes.get(i)));
				}

				this.setContext(Ranger.enumeration(enumeration));
			} else {
				this.setContext(this.findDeploymentByLocationAndCode(
						softwareComponentRegistry, deploymentRegistry,
						locationValue, codeValue));
			}

			queryParameters.remove("contextLocation");
			queryParameters.remove("contextCode");
		}

		if (queryParameters.containsKey("code")
				&& queryParameters.get("code").contains(
						XmlMuncher.VALUE_ENUMERATION_SEPARATOR)) {
			List<String> codes = XmlMuncher
					.getValueEnumerationElements(queryParameters.get("code"));

			this.setCode(Ranger.enumeration(new HashSet<String>(codes)));
			queryParameters.remove("code");
		}

		if (queryParameters.containsKey("level")
				&& queryParameters.get("level").contains(
						XmlMuncher.VALUE_ENUMERATION_SEPARATOR)) {
			List<String> levels = XmlMuncher
					.getValueEnumerationElements(queryParameters.get("level"));

			this.setLevel(Ranger.enumeration(new HashSet<String>(levels)));
			queryParameters.remove("level");
		}

		if (queryParameters.containsKey("dateOfOccurrence")
				&& queryParameters.get("dateOfOccurrence").contains(
						XmlMuncher.VALUE_INTERVAL_SEPARATOR)) {
			List<String> dates = XmlMuncher
					.getValueIntervalElements(queryParameters
							.get("dateOfOccurrence"));

			Date lowerBound = null;
			Date upperBound = null;

			if (!queryParameters.get("dateOfOccurrence").startsWith(
					XmlMuncher.VALUE_INTERVAL_SEPARATOR)) {
				lowerBound = XmlMuncher.xmlDateTimeToJavaDate(dates.get(0));
				if (dates.size() == 2)
					upperBound = XmlMuncher.xmlDateTimeToJavaDate(dates.get(1));
			} else if (dates.size() == 1) {
				upperBound = XmlMuncher.xmlDateTimeToJavaDate(dates.get(0));
			}

			this.setDateOfOccurrence(Ranger.interval(lowerBound, upperBound));
			queryParameters.remove("dateOfOccurrence");
		} else if (queryParameters.containsKey("dateOfOccurrence")) {
			this.setDateOfOccurrence(XmlMuncher
					.xmlDateTimeToJavaDate(queryParameters
							.get("dateOfOccurrence")));
			queryParameters.remove("dateOfOccurrence");
		}

		if (queryParameters.containsKey("transactionIds")) {
			List<String> transactionIds = XmlMuncher
					.getValueEnumerationElements(queryParameters
							.get("transactionIds"));

			for (String transactionId : transactionIds)
				this.addTransactionId(transactionId);

			queryParameters.remove("transactionIds");
		}

		if (queryParameters.containsKey("tags")) {
			List<String> tags = XmlMuncher
					.getValueEnumerationElements(queryParameters.get("tags"));

			for (String tag : tags)
				this.tag(tag);

			queryParameters.remove("tags");
		}

		if (queryParameters.containsKey("udfs")
				&& queryParameters.containsKey("udfValues")
				&& queryParameters.containsKey("udfTypes")) {

			List<String> udfs = XmlMuncher
					.getValueEnumerationElements(queryParameters.get("udfs"));
			List<String> udfValues = XmlMuncher
					.getValueEnumerationElements(queryParameters
							.get("udfValues"));
			List<String> udfTypes = XmlMuncher
					.getValueEnumerationElements(queryParameters
							.get("udfTypes"));

			if ((udfs.size() != udfValues.size())
					|| (udfValues.size() != udfTypes.size())
					|| (udfs.size() != udfTypes.size()))
				throw new ConstraintViolationException(
						"Invalid enumeration in udfs", null);

			for (int i = 0; i < udfs.size(); i++) {
				String udf = udfs.get(i);
				String udfValue = udfValues.get(i);
				String udfType = udfTypes.get(i);
				Object value = null;

				if (udfType.equals("string"))
					value = udfValue;
				else if (udfType.equals("boolean"))
					value = new Boolean(udfValue);
				else if (udfType.equals("integer"))
					value = new Integer(udfValue);
				else if (udfType.equals("long"))
					value = new Long(udfValue);
				else if (udfType.equals("double"))
					value = new Double(udfValue);
				else if (udfType.equals("float"))
					value = new Float(udfValue);
				else if (udfType.equals("binary"))
					value = XmlMuncher.xmlBinaryToByteArray(udfValue);
				else if (udfType.equals("clob"))
					value = XmlMuncher.xmlBinaryToCharArray(udfValue);
				else if (udfType.equals("dateTime"))
					value = XmlMuncher.xmlDateTimeToJavaDate(udfValue);

				if (value != null)
					this.setUdf(udf, value);
			}

			queryParameters.remove("udfs");
			queryParameters.remove("udfValues");
			queryParameters.remove("udfTypes");
		}

		this.fromQueryParameters(queryParameters);
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
	private Deployment findDeploymentByLocationAndCode(
			SoftwareComponentRegistry softwareComponentRegistry,
			DeploymentRegistry deploymentRegistry, String location, String code) {

		Deployment context = deploymentRegistry.findByComponentCodeAndLocation(code, location);
		
		if (context == null) {
			throw new UnknownContextException(
					"Cannot find deployment of software component with code "
					+ code + " at location " + location
					+ " for event context", null);
		}
		return context;
	}

}
