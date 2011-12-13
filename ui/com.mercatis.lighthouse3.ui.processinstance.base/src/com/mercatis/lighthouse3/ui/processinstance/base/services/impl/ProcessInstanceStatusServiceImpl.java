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
package com.mercatis.lighthouse3.ui.processinstance.base.services.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener;
import com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;

/**
 * This implementation is used for ProcessInstanceView. The functionality is limited to operations needed by the view.
 * It holds StatusHistograms related to a specific ProcessInstanceDefinition.
 * 
 */
public class ProcessInstanceStatusServiceImpl implements StatusService {

	/**
	 * Maps a Histogram to a StatusCarrier
	 */
	private Map<StatusCarrier, StatusHistogram> statusHistograms = new HashMap<StatusCarrier, StatusHistogram>();
	
	/**
	 * Registers the carrier for a status
	 */
	private Map<Status, StatusCarrier> statusCarriers = new HashMap<Status, StatusCarrier>();
	
	/**
	 * Initialize the service and pass the definition it is used for.
	 * All Statuses that are found in the definitions ProcessTasks childs are stored in a map.
	 * For each found carrier, a new StatusHistogram is created.
	 * 
	 * @param definition
	 */
	public ProcessInstanceStatusServiceImpl(ProcessInstanceDefinition definition) {
		for (StatusCarrier carrier : definition.getProcessTask().getSubCarriers()) {
			List<Status> statuses = CommonBaseActivator.getPlugin().getStatusService().getPagedStatusesForCarrier(carrier, 1, 1);
			for (Status status : statuses) {
				statusCarriers.put(status, carrier);
			}
			statusHistograms.put(carrier, new StatusHistogram());
		}
	}
	
	/**
	 * Set the current ProcessInstance of interest, get its events in update all StatusHistograms with these Events.
	 * Now each Histogram show a snapshot of the Statuses status in the end of the given instances lifetime.
	 * 
	 * @param newInstance
	 */
	public void setProcessInstance(ProcessInstance newInstance) {
		statusHistograms.clear();
		if (newInstance == null) {
			System.err.println("ProcessInstanceStatusServiceImpl#setProcessInstance: Process Instance == null.");
			return;
		}
		List<Event> events = new LinkedList<Event>();
		events.addAll(newInstance.getEvents());
		Collections.sort(events, eventComparator);
		for (Event event : events) {
			for (Status status : statusCarriers.keySet()) {
				StatusHistogram histogram = statusHistograms.get(statusCarriers.get(status));
				if (histogram == null) {
					histogram = new StatusHistogram();
					statusHistograms.put(statusCarriers.get(status), histogram);
				}
				if (event.matches(status.getOkTemplate())) {
					histogram.incOk();
				} else if (event.matches(status.getErrorTemplate())) {
					histogram.incError();
				} else {
					histogram.incNone();
				}
			}
		}
		fireChange();
	}
	
	public StatusHistogram getStatusHistogramForObject(LighthouseDomain lighthouseDomain, Object object) {
		StatusHistogram histogram = statusHistograms.get(object);
		if (histogram == null)
			histogram = new StatusHistogram();
		return histogram;
	}
	
	private void fireChange() {
		Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
		eventProperties.put("type", "instanceAggregationChanged");
		Services.postEvent(new org.osgi.service.event.Event("com/mercatis/lighthouse3/event/instanceViewChange", eventProperties));
	}
	
	private Comparator<Event> eventComparator = new Comparator<Event>() {
		public int compare(Event o1, Event o2) {
			return o1.getDateOfOccurrence().compareTo(o2.getDateOfOccurrence());
		}
	};

	public void addStatusModelChangedListener(StatusModelChangedListener listener) {}
	public void clearStatusManually(Status status, String reason, String clearer) {}
	public void closeAllLighthouseDomains() {}
	public void closeLighthouseDomain(LighthouseDomain lighthouseDomain) {}
	public void deleteStatus(Status status) {}
	public Status findStatusByCode(LighthouseDomain lighthouseDomain, String code) {return null;}
	public StatusChange getLastChangeForStatus(Status status) {return null;}
	public LighthouseDomain getLighthouseDomainForEntity(Object entity) {return null;}
	public List<Status> getPagedStatusesForCarrier(StatusCarrier statusCarrier, int pageSize, int pageNo) {return null;}
	public StatusConfiguration getStatusConfiguration(LighthouseDomain lighthouseDomain) {return null;}
	public void openLighthouseDomain(LighthouseDomain lighthouseDomain) {}
	public void persistStatus(Status status) {}
	public Status refresh(Status status, int pageSize, int pageNo) {return null;}
	public void removeStatusModelChangedListener(StatusModelChangedListener listener) {}
	public void updateStatus(Status status) {}
}
