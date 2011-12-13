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

import java.util.ArrayList;
import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This iterator combines events to groups, by using the code property, in every
 * iteration step. The group of events, which will be divided into subgroups,
 * for each iteration step is provided by an <code>IntervalGrouper</code>.
 */
public class GroupByGrouperCode implements GroupByGrouper {

	/**
	 * The <code>IntervalGrouper</code> which delivers events grouped to a
	 * certain time interval in every iteration step.
	 */
	IntervalGrouper grouper;

	/**
	 * Returns the Iterator for iteration. Necessary for the use of for each
	 * loops.
	 * 
	 * @return the iterator for iteration
	 */
	public Iterator<Group<Group<Event>>> iterator() {
		return this;
	}

	/**
	 * Checks whether there is one more iteration step or not
	 * 
	 * @return true if there is one more step, false otherwise
	 */
	public boolean hasNext() {
		return grouper.hasNext();
	}

	/**
	 * Delivers a time interval group containing the grouped events every
	 * iteration step.
	 */
	public Group<Group<Event>> next() {
		ArrayList<Group<Event>> list = new ArrayList<Group<Event>>();
		Group<Event> group = grouper.next();

		for (Event e : group.getList())
			addToGroupDistinctly(list, e);

		return new Group<Group<Event>>(list, group.getIdentifier());
	}

	/**
	 * Puts an <code>Event</code> to it's group in a list of groups. If the
	 * necessary group does not exist, yet, it we will be created.
	 * 
	 * @param list
	 *            the list to be filled
	 * @param e
	 *            the event to be put in a group
	 */
	private void addToGroupDistinctly(ArrayList<Group<Event>> list, Event e) {
		boolean found = false;

		for (Group<Event> group : list)
			if (group.getIdentifier().equals(e.getCode())) {
				group.getList().add(e);
				found = true;
			}

		if (!found) {
			ArrayList<Event> newList = new ArrayList<Event>();
			newList.add(e);
			list.add(new Group<Event>(newList, e.getCode()));
		}
	}

	/**
	 * remove is not implemented
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented by any GroupByGrouper");
	}

	/**
	 * This initializes the GroupByGrouper and its Iterator
	 * 
	 * @param grouper
	 *            The <code>IntervalGrouper</code> to be used for retrieving the
	 *            groups in each iteration step.
	 */
	public void init(IntervalGrouper grouper) {
		this.grouper = grouper;
	}

}
