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

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.api.eventfiltering.EventFilteringException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.services.EventRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.base.LighthouseEventNature;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;
import com.mercatis.lighthouse3.ui.event.base.services.EventService;
import com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener;

public class EventServiceImpl implements EventService {

	protected Map<LighthouseDomain,EventRegistry> eventRegistries = new HashMap<LighthouseDomain, EventRegistry>();
	
	protected EventDispatcherFactory eventDispatcherFactory;
	
	private BundleContext context;
	
	public EventServiceImpl(BundleContext context) {
		this.context = context;
		eventDispatcherFactory = new EventDispatcherFactory(context);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#addEventNature(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void addEventNature(LighthouseDomain lighthouseDomain) {
		CommonBaseActivator.getPlugin().getNatureService().addNature(lighthouseDomain, LighthouseEventNature.class.getName());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#getEventConfiguration(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public EventConfiguration getEventConfiguration(LighthouseDomain lighthouseDomain) {
		return new EventConfigurationImpl(lighthouseDomain);
	}


	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#addEventListener(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain, com.mercatis.lighthouse3.ui.event.base.services.WidgetEventListener, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void addEventListener(LighthouseDomain lighthouseDomain, LighthouseEventListener listener, Event template, int limit) throws EventFilteringException {
		eventDispatcherFactory.getEventDispatcher(lighthouseDomain).addEventListener(listener, template, limit);
	}


	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#removeEventListener(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain, com.mercatis.lighthouse3.ui.event.base.services.WidgetEventListener, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void removeEventListener(LighthouseDomain lighthouseDomain, LighthouseEventListener listener, Event template) {
		eventDispatcherFactory.getEventDispatcher(lighthouseDomain).removeEventListener(listener, template);
	}


	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#removeEventListener(com.mercatis.lighthouse3.ui.event.base.services.WidgetEventListener)
	 */
	public void removeEventListener(LighthouseEventListener listener) {
		for (EventDispatcher dispatcher : eventDispatcherFactory.getAllEventDispatchers()) {
			dispatcher.removeEventListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#updateEvent(com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void updateEvent(Event event) {
		Deployment deployment = event.getContext();
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(deployment);
		getEventRegistry(lighthouseDomain).update(event);
	}
	
	protected EventRegistry getEventRegistry(LighthouseDomain lighthouseDomain) {
		EventRegistry eventRegistry = eventRegistries.get(lighthouseDomain);
		if (eventRegistry == null) {
			ServiceReference<?> ref = context.getServiceReference(EventRegistryFactoryService.class.getName());
			if (ref != null) {
				eventRegistry = ((EventRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
				eventRegistries.put(lighthouseDomain, eventRegistry);
			}
		}
		
		return eventRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#getEventByID(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain, long)
	 */
	public Event getEventByID(LighthouseDomain lighthouseDomain, long eventId) {
		EventRegistry eventRegistry = getEventRegistry(lighthouseDomain);
		if(eventRegistry != null) {
			return eventRegistry.find(eventId);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#closeConnectionToServer(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeConnectionToServer(LighthouseDomain domain) {
		EventDispatcher dispatcher = eventDispatcherFactory.getEventDispatcher(domain);
		dispatcher.disconnectFromJMSServer();
		eventDispatcherFactory.removeEventDispatcher(domain);
		dispatcher = null;
		eventRegistries.remove(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.EventService#closeAllConnectionsToServer()
	 */
	public void closeAllConnectionsToServer() {
		eventDispatcherFactory.removeAllEventDispatchers();
		eventRegistries.clear();
	}
}
