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
package com.mercatis.lighthouse3.api.eventfiltering;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This interface has to be implemented by listeners who want to be notified
 * about events that match the registered filters by means of <code>Event</code>
 * objects instead of the JMS messages carrying the events.
 */
public interface EventListener {
	/**
	 * The method being called for events of interest.
	 * 
	 * @param events
	 *            a list of events of interest.
	 */
	public void onEvents(List<Event> events);
}
