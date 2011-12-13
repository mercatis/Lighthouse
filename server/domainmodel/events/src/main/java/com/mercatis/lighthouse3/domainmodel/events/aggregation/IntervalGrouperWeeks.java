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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;

/**
 * This iterator combines events to groups, by using the time granulation
 * 'weeks', in every iteration step. The list of events is retrieved by an
 * iterator over <code>Event</code>s.
 */
public class IntervalGrouperWeeks implements IntervalGrouper {

	/**
	 * The iterator that holds the events, which need to be grouped.
	 */
	private Iterator<Event> iterator = null;

	/**
	 * Saves the week of the last group.
	 */
	private int lastWeek = Integer.MAX_VALUE;

	/**
	 * Saves the year of the last group.
	 */
	private int lastYear = Integer.MAX_VALUE;

	/**
	 * Used for retrieving a group identification from a date.
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("ww'. week of' yyyy");

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
	 * Delivers a group containing the events every iteration step.
	 */
	public Group<Event> next() {
		ArrayList<Event> events = new ArrayList<Event>();
		ArrayList<Event> remaining = new ArrayList<Event>();
		Calendar cal = Calendar.getInstance();

		int thisYear = 0;
		int thisWeek = -1;

		String groupName = "";

		while (iterator.hasNext()) {
			Event e = iterator.next();
			Date curDate = e.getDateOfOccurrence();
			cal.setTime(curDate);
			int year = cal.get(Calendar.YEAR);
			int week = cal.get(Calendar.WEEK_OF_YEAR);

			if (thisWeek == -1)
				if (isBeforeLastDate(year, week)) {
					thisYear = year;
					thisWeek = week;
				}
			if (year == thisYear && week == thisWeek) {
				events.add(e);
				groupName = dateFormat.format(curDate);
			} else {
				remaining.add(e);
			}
		}

		if (remaining.size() != 0)
			this.iterator = remaining.iterator();

		lastYear = thisYear;
		lastWeek = thisWeek;

		return new Group<Event>(events, groupName);
	}

	/**
	 * Checks if a certain date is older than the saved last groups date.
	 * 
	 * @param year
	 *            the year of the date to be checked
	 * @param week
	 *            the week of the date to be checked
	 * @return true if it is older, false otherwise
	 */
	private boolean isBeforeLastDate(int year, int week) {
		return (year < lastYear) || (year == lastYear && week < lastWeek);
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
