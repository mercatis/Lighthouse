/**
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
package com.mercatis.lighthouse3.domainmodel.events.aggregation;

import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This interface provides basic grouping functionality over a list of
 * <code>Event</code>s to be used in an iteration. It combines the events into
 * groups using a property of <code>Event</code>. The list of events for each
 * iteration step is provided by an <code>IntervalGrouper</code>.
 */
public interface GroupByGrouper extends Iterator<Group<Group<Event>>>, Iterable<Group<Group<Event>>> {

	/**
	 * This initializes the GroupByGrouper and its <code>Iterator</code>
	 * 
	 * @param grouper
	 *            the <code>IntervalGrouper</code> that provides a group of
	 *            events in each iteration step
	 */
	public void init(IntervalGrouper grouper);
}
