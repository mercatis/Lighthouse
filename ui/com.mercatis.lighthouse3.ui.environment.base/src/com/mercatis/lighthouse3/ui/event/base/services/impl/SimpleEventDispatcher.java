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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.mercatis.lighthouse3.api.eventfiltering.EventFilteringService;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener;

/**
 * A basic implementation of <code>EventDispatcher</code>
 * 
 * This class is not supposed to be instantiated by clients. Use
 * <code>EventDispatcherFactory</code> for <code>EventDispatcher</code> creation
 * unless you know what you are doing.
 */
public class SimpleEventDispatcher implements EventDispatcher {
	
	private final ReentrantLock lock = new ReentrantLock();
	private long subscriptionRefreshInterval = 590000;
	private EventFilteringService filteringService = null;
	private Map<String, LighthouseEventListener> listenerRegistry = new HashMap<String, LighthouseEventListener>();
	
	private Timer refreshTimer = null;
	
	/**
	 * @param filteringService
	 * @param subscriptionRefreshInterval
	 * @param lighthouseDomain
	 */
	public void init(EventFilteringService filteringService, long subscriptionRefreshInterval, LighthouseDomain lighthouseDomain) {
		this.filteringService = filteringService;
		this.subscriptionRefreshInterval = subscriptionRefreshInterval;
		
		this.refreshTimer = new Timer();
		this.refreshTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (String subscriptionId : listenerRegistry.keySet()) {
					refreshEventFilter(subscriptionId);
				}
			}
		}, Calendar.getInstance().getTime(), this.subscriptionRefreshInterval);
	}
	
	/**
	 * @param subscriptionId
	 */
	private void refreshEventFilter(String subscriptionId) {
		filteringService.refreshEventFilter(subscriptionId);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.impl.EventDispatcher#addEventListener(com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void addEventListener(LighthouseEventListener listener, Event template, int limit) {
		lock.lock();
		try {
				String subscriptionId = filteringService.registerEventFilter(template, limit);
				System.out.println("Registered EventListener : SubscriptionId: " + subscriptionId);
				template.setFilterOfOrigin(subscriptionId);
				listenerRegistry.put(subscriptionId, listener);
		} finally {
			lock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.impl.EventDispatcher#removeEventListener(com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener, com.mercatis.lighthouse3.domainmodel.events.Event)
	 */
	public void removeEventListener(LighthouseEventListener listener, Event template) {
		System.out.println("removing listener for template with subscription: " + template.getFilterOfOrigin());
		lock.lock();
		try {
			filteringService.deregisterEventFilter(template.getFilterOfOrigin());
			listenerRegistry.remove(template.getFilterOfOrigin());
		} finally {
			lock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.impl.EventDispatcher#removeEventListener(com.mercatis.lighthouse3.ui.event.base.services.LighthouseEventListener)
	 */
	public void removeEventListener(LighthouseEventListener listener) {
		lock.lock();
		try {
			for (String subscriptionId : listenerRegistry.keySet()) {
				if (listenerRegistry.get(subscriptionId).equals(listener)) {
					filteringService.deregisterEventFilter(subscriptionId);
					listenerRegistry.remove(subscriptionId);
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.api.eventfiltering.EventListener#onEvents(java.util.List)
	 */
	public void onEvents(List<Event> events) {
		dispatchEvents(events);
	}
	
	/**
	 * @param events
	 */
	private void dispatchEvents(List<Event> events) {
		lock.lock();
		try {
			if(events.size() > 0) {
				String subscriptionId = events.get(0).getFilterOfOrigin();
				if(listenerRegistry.containsKey(subscriptionId)) {
					listenerRegistry.get(subscriptionId).onEvents(events);
				}
			}
			
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.event.base.services.impl.EventDispatcher#disconnectFromJMSServer()
	 */
	public void disconnectFromJMSServer() {
		filteringService.stopEventListening();
	}
}
