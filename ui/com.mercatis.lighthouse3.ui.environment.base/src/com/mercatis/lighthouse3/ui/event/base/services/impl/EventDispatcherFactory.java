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
package com.mercatis.lighthouse3.ui.event.base.services.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.api.eventfiltering.EventFilteringException;
import com.mercatis.lighthouse3.api.eventfiltering.EventFilteringService;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;

/**
 * Factory class for <code>EventDispatcher</code> creation
 */
public class EventDispatcherFactory {

	private Map<LighthouseDomain, EventDispatcher> dispatcherRegistry = new HashMap<LighthouseDomain, EventDispatcher>();

	private BundleContext context;
	
	public EventDispatcherFactory(BundleContext context) {
		this.context = context;
	}
	
	/**
	 * @return
	 */
	public List<EventDispatcher> getAllEventDispatchers() {
		return new LinkedList<EventDispatcher>(dispatcherRegistry.values());
	}

	/**
	 * @param dispatcher
	 */
	public void removeEventDispatcher(LighthouseDomain domain) {
		dispatcherRegistry.remove(domain);
	}
	
	public void removeAllEventDispatchers() {
		dispatcherRegistry.clear();
	}
	
	/**
	 * @param lighthouseDomain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	synchronized public EventDispatcher getEventDispatcher(LighthouseDomain lighthouseDomain) throws EventFilteringException {
		if (this.dispatcherRegistry.containsKey(lighthouseDomain)) {
			return this.dispatcherRegistry.get(lighthouseDomain);
		}

		EventConfiguration eventConfiguration = CommonBaseActivator.getPlugin().getEventService().getEventConfiguration(lighthouseDomain);

		Class<? extends JmsProvider> jmsProviderClass = null;

		try {
			jmsProviderClass = (Class<? extends JmsProvider>) Class.forName(eventConfiguration.getJmsProviderClass());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("JMS Provider Class " + eventConfiguration.getJmsProviderClass()
					+ " not present.");
		}
		String clientId = null;

		try {
			clientId = InetAddress.getLocalHost().getHostAddress() + "#ui-event-base#" + lighthouseDomain.toString()
					+ "#" + System.currentTimeMillis();
		} catch (UnknownHostException e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		}

		DomainConfiguration domainConfiguration = CommonBaseActivator.getPlugin().getDomainService().getDomainConfiguration(lighthouseDomain);

		DeploymentRegistry deploymentRegistry = null;
		ServiceReference<?> ref = context.getServiceReference(DeploymentRegistryFactoryService.class.getName());
		if (ref != null) {
			deploymentRegistry = ((DeploymentRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
		}
		
		SoftwareComponentRegistry softwareComponentRegistry = null;
		ref = context.getServiceReference(SoftwareComponentRegistryFactoryService.class.getName());
		if (ref != null) {
			softwareComponentRegistry = ((SoftwareComponentRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
		}

		SimpleEventDispatcher eventDispatcher = new SimpleEventDispatcher();

		String context = new StringBuilder("//LH3/").append(lighthouseDomain.getServerDomainKey()).toString();
		String user = Security.getLoginName(context);
		String password = new String(Security.getLoginPassword(context));

		EventFilteringService eventFilteringServiceInResolvedMode = new EventFilteringService(domainConfiguration
		.getUrl(), jmsProviderClass, eventConfiguration.getJmsBrokerUrl(), eventConfiguration.getJmsUser(),
		eventConfiguration.getJmsPassword(), eventConfiguration.getEventsPublicationTopic(), clientId,
		eventDispatcher, softwareComponentRegistry, deploymentRegistry, user, password);

		eventDispatcher.init(eventFilteringServiceInResolvedMode, eventConfiguration
				.getEventFilterRegistrationRefreshInterval(), lighthouseDomain);

		dispatcherRegistry.put(lighthouseDomain, eventDispatcher);

		eventFilteringServiceInResolvedMode.startEventListeningWithException();

		return eventDispatcher;
	}
}
