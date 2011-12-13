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
package com.mercatis.lighthouse3.ui.event.base.services;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

public interface EventService {

	/**
	 * @param lighthouseDomain
	 */
	public void addEventNature(LighthouseDomain lighthouseDomain);
	
	/**
	 * @param lighthouseDomain
	 * @return
	 */
	public EventConfiguration getEventConfiguration(LighthouseDomain lighthouseDomain);
	
	/**
	 * Registers a lister to receive all events, which match the given template, for the given domain
	 * 
	 * @param lighthouseDomain
	 * @param listener
	 * @param template
	 */
	public void addEventListener(LighthouseDomain lighthouseDomain, LighthouseEventListener listener, Event template, int limit);

	/**
	 * Removes a listener for the given template, for the given domain
	 * 
	 * @param lighthouseDomain
	 * @param listener
	 * @param template
	 */
	public void removeEventListener(LighthouseDomain lighthouseDomain, LighthouseEventListener listener, Event template);

	/***
	 * Removes a listener for all templates, for all domains
	 * 
	 * @param listener
	 * @param template
	 */
	public void removeEventListener(LighthouseEventListener listener);
	
	/**Updates the given event
	 * @param event
	 */
	public void updateEvent(Event event);
	
	/**Retrieves the event for the given Id
	 * @param lighthouseDomain
	 * @param eventId
	 * @return
	 */
	public Event getEventByID(LighthouseDomain lighthouseDomain, long eventId);
	
	/**
	 * 
	 */
	public void closeAllConnectionsToServer();
	
	/**
	 * @param domain
	 */
	public void closeConnectionToServer(LighthouseDomain domain);
}
