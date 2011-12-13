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
import java.util.Calendar;
import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This iterator combines events to groups, by using the time granulation
 * 'years', in every iteration step. The list of events is retrieved by an
 * iterator over <code>Event</code>s.
 */
public class IntervalGrouperYears implements IntervalGrouper {

	/**
	 * The iterator that holds the events, which need to be grouped.
	 */
	private Iterator<Event> iterator = null;

	/**
	 * Saves the year of the last group.
	 */
	private int lastYear = Integer.MAX_VALUE;

	/**
	 * Checks whether there is one more iteration step or not
	 * 
	 * @return true if there is one more step, false otherwise
	 */
	public boolean hasNext() {
		if (iterator.hasNext())
			return true;

		return false;
	}

	/**
	 * This initializes the IntervalGrouper and its Iterator
	 * 
	 * @param iterator
	 *            The <code>Iterator</code> to be used for retrieving the events
	 *            in each iteration step.
	 */
	public void init(Iterator<Event> iterator) {
		this.iterator = iterator;
	}

	/**
	 * Delivers a group containing the events every iteration step.
	 */
	public Group<Event> next() {
		ArrayList<Event> events = new ArrayList<Event>();
		ArrayList<Event> remaining = new ArrayList<Event>();
		Calendar cal = Calendar.getInstance();

		int thisYear = -1;

		while (iterator.hasNext()) {
			Event e = iterator.next();
			cal.setTime(e.getDateOfOccurrence());
			int time = cal.get(Calendar.YEAR);
			if (thisYear == -1)
				if (time < lastYear)
					thisYear = time;
			if (time == thisYear)
				events.add(e);
			else
				remaining.add(e);
		}

		if (remaining.size() != 0)
			this.iterator = remaining.iterator();

		lastYear = thisYear;

		return new Group<Event>(events, "" + thisYear);
	}

	/**
	 * remove is not implemented
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented by any IntervalGrouper");
	}

	/**
	 * Returns the Iterator for iteration. Necessary for the use of for each
	 * loops.
	 * 
	 * @return the iterator for iteration
	 */
	public Iterator<Group<Event>> iterator() {
		return this;
	}

}
