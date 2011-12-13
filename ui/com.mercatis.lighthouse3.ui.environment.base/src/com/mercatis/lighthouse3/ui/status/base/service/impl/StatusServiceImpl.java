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
package com.mercatis.lighthouse3.ui.status.base.service.impl;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import com.mercatis.lighthouse3.api.statuschange.BasicStatusChangeMonitor;
import com.mercatis.lighthouse3.api.statuschange.StatusChangeListener;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;
import com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener;
import com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;

/**
 * Provides operations (load, update, persist) for Status.
 * <br>You can register as:
 * <ul>
 * <li><code>com.mercatis.lighthouse3.api.statuschange.StatusChangeListener</code> <i>(fired when the LH3 server reports a change)</i></li>
 * <li><code>com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener</code> <i>(fired after altering a Status)</i></li>
 * </ul>
 * 
 * <br>An instance of this class will listen on domain changes.
 * <br>The status change cache will be cleared an every domain or status model change - subject to be changed.
 */
public class StatusServiceImpl implements StatusService, StatusModelChangedListener, DomainChangeListener {

	/**
	 * Holds a status registry for each LighthouseDomain
	 */
	private Map<LighthouseDomain, StatusRegistry> statusRegistries;
	
	/**
	 * Listeners to be notified on status model changes
	 */
	private List<StatusModelChangedListener> statusModelChangeListeners = new Vector<StatusModelChangedListener>();
	
	/**
	 * Holds the last change of a status mapped by <code>Status.getCode()</code>
	 * <br />Useful for eg LabelDecorators
	 */
	private volatile Map<String, StatusChange> statusChangeCache = new HashMap<String, StatusChange>();
	
	private Map<LighthouseDomain, BasicStatusChangeMonitor> basicChangeMonitors;
	
	private BundleContext context;
	
	/**
	 * Creates a new instance of StatusService.
	 * <br>Registers itself as:
	 * <ul>
	 * <li><code>com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener</code></li>
	 * <li><code>com.mercatis.lighthouse3.ui.status.base.StatusModelChangedListener</code></li>
	 * </ul>
	 */
	public StatusServiceImpl(BundleContext context) {
		this.context = context;
		statusRegistries = new HashMap<LighthouseDomain, StatusRegistry>();
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
		statusModelChangeListeners.add(this);
		basicChangeMonitors = new HashMap<LighthouseDomain, BasicStatusChangeMonitor>(); 
	}

	/**
	 * Provides a registry for a LighthouseDomain and creates one if it did not exist.
	 * 
	 * @param lighthouseDomain
	 * @return registry
	 */
	private StatusRegistry getStatusRegistry(final LighthouseDomain lighthouseDomain) {
		StatusRegistry registry = statusRegistries.get(lighthouseDomain);
		if (registry == null) {
			ServiceReference<?> ref = context.getServiceReference(StatusRegistryFactoryService.class.getName());
			if (ref != null) {
				registry = ((StatusRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
				statusRegistries.put(lighthouseDomain, registry);
				BasicStatusChangeMonitor bscm = getBasicStatusChangeMonitor(lighthouseDomain);
				bscm.registerListener(
						new StatusChangeListener() {
							public void statusChanged(String statusPath, String statusCode, StatusChange statusChange, StatusChange priorChange) {
								StatusServiceImpl.this.statusChanged(lighthouseDomain, statusPath, statusCode, statusChange, priorChange);
							}
						});
				bscm.start();
			}
		}
		return registry;
	}

	/**
	 * Provides a StatusRegistry for a StatusCarrier via <code>CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(Object entity)</code>
	 * @param statusCarrier
	 * @return registry
	 */
	private StatusRegistry getStatusRegistry(StatusCarrier statusCarrier) {
		return getStatusRegistry(CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(statusCarrier));
	}

	/**
	 * Provides a StatusRegistry for a Status via <code>Status.getContext()</code> and passing to <code>getStatusRegistry(StatusCarrier statusCarrier)</code>
	 * @param status
	 * @return registry
	 */
	private StatusRegistry getStatusRegistry(Status status) {
		return getStatusRegistry(status.getContext());
	}

	/**
	 * Returns the configuration for this LighthouseDomain.
	 * @param lighthouseDomain
	 * @return StatusConfiguration
	 */
	public StatusConfiguration getStatusConfiguration(LighthouseDomain lighthouseDomain) {
		return new StatusConfigurationImpl(lighthouseDomain);
	}

	/**
	 * Returns a <code>List&lt;Status&gt;</code> with limited amount of StatusChanges.
	 * <br>Use it like the commonly known ItemsPerPage thingie.
	 * @param statusCarrier
	 * @param pageSize Amount of Changes to be loaded
	 * @param pageNo The page you want to see <b>(1 based!)</b> <i>(eg: pageSize = 10 && pageNo = 2 shows changes 11-20)</i>
	 * @return A paged Status
	 */
	public List<Status> getPagedStatusesForCarrier(StatusCarrier statusCarrier, int pageSize, int pageNo) {
		StatusRegistry registry = getStatusRegistry(statusCarrier);
		return registry.getStatusForCarrier(statusCarrier, pageSize, pageNo);
	}

	/**
	 * Gets or creates a {@link StatusHistogram} for a given object.
	 */
	public StatusHistogram getStatusHistogramForObject(LighthouseDomain lighthouseDomain, Object object) {
		return getStatusRegistry(lighthouseDomain).getAggregatedCurrentStatusForCarrier((StatusCarrier) object, true);
	}

	public Status refresh(Status status, int pageSize, int pageNo) {
		StatusRegistry registry = getStatusRegistry(status);
		return registry.findByCode(status.getCode(), pageSize, pageNo);
	}

	public void persistStatus(Status status) {
		StatusRegistry registry = getStatusRegistry(status);
		registry.persist(status);
		status = refresh(status, 1, 1);

		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(
				status.getContext());
		fireStatusModelChanged(lighthouseDomain, status, null, null, status);
	}

	public void updateStatus(Status status) {
		StatusRegistry registry = getStatusRegistry(status);
		registry.update(status);

		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(
				status.getContext());
		fireStatusModelChanged(lighthouseDomain, status, null, status, status);
		markStatusAsDirtyForAggregation(lighthouseDomain, status);
	}

	public void deleteStatus(Status status) {
		StatusRegistry registry = getStatusRegistry(status);
		registry.delete(status);

		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(
				status.getContext());
		fireStatusModelChanged(lighthouseDomain, status, null, status, null);
		markStatusAsDirtyForAggregation(lighthouseDomain, status);
	}

	public void clearStatusManually(Status status, String reason, String clearer) {
		StatusRegistry registry = getStatusRegistry(status);
		registry.clearStatusManually(status.getCode(), clearer, reason);
	}

	public StatusChange getLastChangeForStatus(Status status) {
		StatusChange statusChange = statusChangeCache.get(status.getCode());
		if (statusChange == null) {
			statusChange = getStatusRegistry(status).findByCode(status.getCode(), 1, 1).getCurrent();
		}
		return statusChange;
	}
	
	private void markStatusAsDirtyForAggregation(LighthouseDomain lighthouseDomain, Status status) {
		Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
		eventProperties.put("code", status.getCode());
		eventProperties.put("serverDomainKey", lighthouseDomain.getServerDomainKey());
		eventProperties.put("statusPath", status.getPath());
		String topic = getTopicFromStatusPath(status.getPath(), lighthouseDomain);
		
		//send event to aggregators - blocking
		eventProperties.put("type", "statusDirty");
		Services.sendEvent(new Event(topic, eventProperties));
		
		//post event, aggregation finished (UI may update labels...) - non blocking
		eventProperties.put("type", "statusAggregationChanged");
		Services.postEvent(new Event(topic, eventProperties));
	}

	private void statusChanged(LighthouseDomain lighthouseDomain, String statusPath, String statusCode,
			StatusChange statusChange, StatusChange priorChange) {

		statusChangeCache.put(statusCode, statusChange);

		//Assemble the event properties
		Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
		eventProperties.put("code", statusCode);
		eventProperties.put("serverDomainKey", lighthouseDomain.getServerDomainKey());
		eventProperties.put("statusPath", statusPath);
		eventProperties.put("statusChange", statusChange);
		if (priorChange != null)
			eventProperties.put("priorChange", priorChange);

		String topic = getTopicFromStatusPath(statusPath, lighthouseDomain);
		
		if (statusChange.isNewStatusChange()) {
			//send event to aggregators - blocking
			eventProperties.put("type", "statusChanged");
			Services.sendEvent(new Event(topic, eventProperties));
			
			//post event, aggregation finished (UI may update labels...) - non blocking
			eventProperties.put("type", "statusAggregationChanged");
			Services.postEvent(new Event(topic, eventProperties));
		} else {
			//update only views showing the counters
			eventProperties.put("type", "statusCounterChanged");
			Services.postEvent(new Event(topic, eventProperties));
		}
	}
	
	private String getTopicFromStatusPath(String statusPath, LighthouseDomain lighthouseDomain) {
		statusPath = statusPath.replaceFirst(":/", "");
		String topic = "com/mercatis/lighthouse3/event/"
			+ lighthouseDomain.getServerDomainKey().hashCode() //add the serverDomainKey
			+ "/" + statusPath.substring(0, statusPath.indexOf("/")); //add the statuscarrier type
		StringBuilder topicBuilder = new StringBuilder(topic);
		StringTokenizer tok = new StringTokenizer(statusPath.substring(statusPath.indexOf("/") + 1), "/");
		while (tok.hasMoreTokens()) {
			topicBuilder.append("/")
			.append(tok.nextToken().hashCode());
		}
		topic = topicBuilder.toString();
		return topic;
	}

	public LighthouseDomain getLighthouseDomainForEntity(Object entity) {
		if (entity instanceof Status) {
			entity = ((Status) entity).getContext();
		}
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(entity);
	}

	public Status findStatusByCode(LighthouseDomain lighthouseDomain, String code) {
		return getStatusRegistry(lighthouseDomain).findByCode(code, 1, 1);
	}

	public void addStatusModelChangedListener(StatusModelChangedListener listener) {
		statusModelChangeListeners.add(listener);
	}

	public void removeStatusModelChangedListener(StatusModelChangedListener listener) {
		statusModelChangeListeners.remove(listener);
	}

	private void fireStatusModelChanged(LighthouseDomain lighthouseDomain, Object source, String property,
			Object oldValue, Object newValue) {
		for (StatusModelChangedListener listener : statusModelChangeListeners
				.toArray(new StatusModelChangedListener[statusModelChangeListeners.size()])) {
			listener.statusModelChanged(lighthouseDomain, source, property, oldValue, newValue);
		}
	}

	public void statusModelChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue,
			Object newValue) {
		clearCache();
	}

	public void domainChange(DomainChangeEvent event) {
		clearCache();
	}

	@SuppressWarnings("unchecked")
	private BasicStatusChangeMonitor getBasicStatusChangeMonitor(LighthouseDomain lighthouseDomain) {
		BasicStatusChangeMonitor bscm = basicChangeMonitors.get(lighthouseDomain);
		if (bscm == null) {
			EventConfiguration eventConfiguration = CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain);
			StatusConfiguration statusConfiguration = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain);

			Class<? extends JmsProvider> jmsProviderClass = null;
			try {
				jmsProviderClass = (Class<? extends JmsProvider>) Class.forName(eventConfiguration.getJmsProviderClass());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("JMS Provider Class " + eventConfiguration.getJmsProviderClass()	+ " not present.");
			}

			SoftwareComponentRegistry softwareComponentRegistry = RegistryFactoryServiceUtil.getRegistryFactoryService(SoftwareComponentRegistryFactoryService.class, lighthouseDomain.getServerDomainKey(), this);
			DeploymentRegistry deploymentRegistry = RegistryFactoryServiceUtil.getRegistryFactoryService(DeploymentRegistryFactoryService.class, lighthouseDomain.getServerDomainKey(), this);
			bscm = new BasicStatusChangeMonitor(jmsProviderClass, eventConfiguration.getJmsBrokerUrl(), eventConfiguration.getJmsUser(), eventConfiguration.getJmsPassword(), statusConfiguration.getStatusPublicationTopic(), deploymentRegistry, softwareComponentRegistry);
			basicChangeMonitors.put(lighthouseDomain, bscm);
		}
		return bscm;
	}
	
	
	/**
	 * @param lighthouseDomain
	 */
	private void clearRegistryForDomain(LighthouseDomain lighthouseDomain) {
		StatusRegistry registry = statusRegistries.get(lighthouseDomain);
		if(registry != null) {
			statusRegistries.remove(lighthouseDomain);
			registry = null;
		}
	}
	
	/**
	 * @param lighthouseDomain
	 */
	private void clearChangeMonitorForDomain(LighthouseDomain lighthouseDomain) {
		BasicStatusChangeMonitor bscm = basicChangeMonitors.get(lighthouseDomain);
		if(bscm != null) {
			bscm.stop();
			basicChangeMonitors.remove(lighthouseDomain);
			bscm = null;
		}
	}
	
	/**
	 * This method invalidates all caches for status changes.
	 */
	private void clearCache() {
		statusChangeCache.clear();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusService#closeAllLighthouseDomains()
	 */
	public synchronized void closeAllLighthouseDomains() {
		for (LighthouseDomain domain : statusRegistries.keySet()) {
			clearRegistryForDomain(domain);
		}
		for (LighthouseDomain domain : basicChangeMonitors.keySet()) {
			clearChangeMonitorForDomain(domain);
		}
		clearCache();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusService#closeLighthouseDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeLighthouseDomain(LighthouseDomain lighthouseDomain) {
		clearRegistryForDomain(lighthouseDomain);
		clearChangeMonitorForDomain(lighthouseDomain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.status.base.service.StatusService#openLighthouseDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openLighthouseDomain(LighthouseDomain lighthouseDomain) {
	}
}
