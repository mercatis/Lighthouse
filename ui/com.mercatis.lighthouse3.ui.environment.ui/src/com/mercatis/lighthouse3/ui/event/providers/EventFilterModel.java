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
package com.mercatis.lighthouse3.ui.event.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel;
import com.mercatis.lighthouse3.base.ui.widgets.eventfilter.InputControlType;
import com.mercatis.lighthouse3.commons.commons.EnumerationRange;
import com.mercatis.lighthouse3.commons.commons.IntervalRange;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class EventFilterModel implements FilterModel {
	
	public static final int CODE = 0;

	public static final int DATE = 1;
	
	public static final int DEPLOYMENT = 2;

	public static final int MESSAGE = 3;
	
	public static final int ORIGIN = 4;
	
	public static final int SEVERITY = 5;
	
	public static final int TAG = 6;
	
	public static final int TRANSACTION_ID = 7;
	
	public static final int UDF = 8;
	
	private List<String> severities;
	
	private Event template;
	
	private LighthouseDomain lighthouseDomain;
	
	private List<Deployment> deployments;
	
	/**
	 * @param lighthouseDomain
	 */
	public EventFilterModel(LighthouseDomain lighthouseDomain) {
		this(lighthouseDomain, EventBuilder.template().done());
	}
	
	/**
	 * @param template
	 */
	public EventFilterModel(LighthouseDomain lighthouseDomain, Event template) {
		this.lighthouseDomain = lighthouseDomain;
		this.template = template;
		
		severities = new ArrayList<String>(6);
		severities.add("DEBUG");
		severities.add("DETAIL");
		severities.add("INFO");
		severities.add("WARNING");
		severities.add("ERROR");
		severities.add("FATAL");
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#canRecur(int)
	 */
	public boolean canRecur(int propertyIndex) {
		switch (propertyIndex) {
		case CODE:
		case DEPLOYMENT:
		case MESSAGE:
		case ORIGIN:
		case SEVERITY:
		case TAG:
		case TRANSACTION_ID:
		case UDF:
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getChoicesFor(int)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getChoicesFor(int propertyIndex) {
		switch (propertyIndex) {
		case SEVERITY:
			return (List<T>) severities;
		case DEPLOYMENT:
			if (deployments == null)
				deployments = CommonBaseActivator.getPlugin().getDomainService().getAllDeployments(lighthouseDomain);
			return (List<T>) deploymentsToStrings(deployments);
		}
		
		return Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getInputControlTypeFor(int)
	 */
	public InputControlType getInputControlTypeFor(int propertyIndex) {
		switch (propertyIndex) {
		case CODE:
		case MESSAGE:
		case ORIGIN:
		case TAG:
		case TRANSACTION_ID:
			return InputControlType.Text;
		case DATE:
			return InputControlType.IntervalDateTime;
		case SEVERITY:
		case DEPLOYMENT:
			return InputControlType.Combo;
		case UDF:
			return InputControlType.UserDefinedField;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getPropertyCount()
	 */
	public int getPropertyCount() {
		return 9;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getPropertyName(int)
	 */
	public String getPropertyName(int propertyIndex) {
		switch (propertyIndex) {
		case CODE:
			return "Code";
		case DATE:
			return "Date";
		case DEPLOYMENT:
			return "Deployment";
		case MESSAGE:
			return "Message";
		case ORIGIN:
			return "Machine";
		case SEVERITY:
			return "Severity";
		case TAG:
			return "Tagged as";
		case TRANSACTION_ID:
			return "Transaction ID";
		case UDF:
			return "User Defined Field";
		}
		
		return "UNKNOWN";
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getTemplate()
	 */
	public Event getTemplate() {
		return template;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#getValuesFor(int)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getValuesFor(int propertyIndex) {
		switch (propertyIndex) {
		case CODE:
			return (List<T>) valueToList(template.getCode());
		case DATE:
			return (List<T>) listOfIntervalsToListOfTuples(valueToList(template.getDateOfOccurrence()));
		case DEPLOYMENT:
			return (List<T>) deploymentsToStrings(valueToList(template.getContext()));
		case MESSAGE:
			return (List<T>) valueToList(template.getMessage());
		case ORIGIN:
			return (List<T>) valueToList(template.getMachineOfOrigin());
		case SEVERITY:
			return (List<T>) valueToList(template.getLevel());
		case TAG:
			return (List<T>) new LinkedList<String>(template.getTags());
		case TRANSACTION_ID:
			return (List<T>) new LinkedList<String>(template.getTransactionIds());
		case UDF:
			return (List<T>) mapToListOfTuples(template.getUdfs());
		}
		
		return Collections.emptyList();
	}

	private List<String> deploymentsToStrings(List<Deployment> values) {
		List<String> result = new ArrayList<String>(values.size());
		for (Deployment deployment : values) {
			result.add(LabelConverter.getLabel(deployment));
		}
		
		Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
		
		return result;
	}
	
	private List<Deployment> stringsToDeployments(List<String> values) {
		List<Deployment> result = new ArrayList<Deployment>(values.size());
		for (String entry : values) {
			for (Deployment deployment : this.deployments) {
				if (LabelConverter.getLabel(deployment).equals(entry)) {
					result.add(deployment);
					break;
				}
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#isMandatory(int)
	 */
	public boolean isMandatory(int propertyIndex) {
		switch (propertyIndex) {
		case DATE:
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> List<Tuple<T,T>> listOfIntervalsToListOfTuples(List<T> list) {
		List<Tuple<T,T>> result = new LinkedList<Tuple<T,T>>();
		for (T entry : list) {
			if (Ranger.isIntervalRange(entry)) {
				IntervalRange range = Ranger.castToIntervalRange((Comparable) entry);
				result.add(new Tuple<T,T>((T) range.getLowerBound(), (T) range.getUpperBound()));
			} else {
				result.add(new Tuple<T,T>(entry, entry));
			}
		}
		
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> List<T> listOfTuplesToListOfIntervals(List<Tuple<T,T>> values) {
		List<T> result = new LinkedList<T>();
		for (Tuple<T, T> tuple : values) {
			T value = (T) Ranger.interval((Comparable) tuple.getA(), (Comparable) tuple.getB());
			result.add(value);
		}
		
		return result;
	}
	
	private <T> Map<String,T> listOfTuplesToMap(List<Tuple<String,T>> values) {
		Map<String,T> result = new HashMap<String,T>();
		for (Tuple<String, T> tuple : values) {
			result.put(tuple.getA(), tuple.getB());
		}
		
		return result;
	}

	private <T> T listToValue(List<T> values) {
		if (values.isEmpty())
			return null;
		
		if (values.size() == 1)
			return values.iterator().next();
		
		return Ranger.enumeration(new HashSet<T>(values));
	}

	private <T> List<Tuple<String,T>> mapToListOfTuples(Map<String,T> map) {
		List<Tuple<String,T>> result = new LinkedList<Tuple<String,T>>();
		for (Map.Entry<String,T> entry : map.entrySet()) {
			result.add(new Tuple<String,T>(entry.getKey(), entry.getValue()));
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#setTemplate(java.lang.Object)
	 */
	public void setTemplate(Event template) {
		this.template = template;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.widgets.eventfilter.FilterModel#setValuesFor(java.util.List, int)
	 */
	@SuppressWarnings("unchecked")
	public <T> void setValuesFor(List<T> values, int propertyIndex) {

		switch (propertyIndex) {
		case CODE:
			template.setCode((String) listToValue(values));
			break;
		case DATE:
			if (values.size() == 0)
				template.setDateOfOccurrence(null);
			else
				template.setDateOfOccurrence((Date) listOfTuplesToListOfIntervals((List<Tuple<T, T>>) values).get(0));
			break;
		case DEPLOYMENT:
			template.setContext((Deployment) listToValue(stringsToDeployments((List<String>) values)));
			break;
		case MESSAGE:
			template.setMessage((String) listToValue(values));
			break;
		case ORIGIN:
			template.setMachineOfOrigin((String) listToValue(values));
			break;
		case SEVERITY:
			template.setLevel((String) listToValue(values));
			break;
		case TAG:
			template.setTags(new HashSet<String>((List<String>) values));
			break;
		case TRANSACTION_ID:
			template.setTransactionIds(new HashSet<String>((List<String>) values));
			break;
		case UDF:
			template.setUdfs((Map<String, Object>) listOfTuplesToMap((List<Tuple<String,T>>) values));
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> valueToList(T value) {
		if (value == null)
			return Collections.emptyList();
		
		if (List.class.isAssignableFrom(value.getClass())) {
			return (List<T>) value;
		}

		List<T> result = new LinkedList<T>();
		if (Ranger.isEnumerationRange(value)) {
			EnumerationRange<T> range = Ranger.castToEnumerationRange(value);
			for (T entry : range.getEnumeration()) {
				result.add(entry);
			}
		} else {
			result.add(value);
		}
		return result;
	}

	// very hacky, this is done to avoid an bundle dependeny requirement 
	@SuppressWarnings("unchecked")
	public <T> void setAllowedDeployments(List<T> deployments) {
		this.deployments = (List<Deployment>) deployments;
	}

}
