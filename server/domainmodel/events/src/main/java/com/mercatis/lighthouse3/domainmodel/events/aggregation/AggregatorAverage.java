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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationTarget.Type;

/**
 * Iterator that delivers the average value of a property of <code>Event</code>
 * in a group of events in each iteration step.
 */
public class AggregatorAverage implements Aggregator {

	/**
	 * The GroupByGrouper that provides the groups of events for each iteration
	 * step.
	 */
	private GroupByGrouper grouper;

	/**
	 * The aggregation target to compute the average value from.
	 */
	private AggregationTarget target;

	/**
	 * This initializes the Aggregator and its Iterator
	 * 
	 * @param grouper
	 *            The <code>GroupByGrouper</code> to be used for retrieving the
	 *            groups in each iteration step.
	 * @param target
	 *            The aggregation target property of an <code>Event</code>.
	 */
	public void init(GroupByGrouper grouper, AggregationTarget target) {
		this.grouper = grouper;
		this.target = target;
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
	 * Delivers an aggregation result in each iteration step.
	 */
	public AggregationIntervalResult next() {

		HashMap<String, Object> map = new HashMap<String, Object>();
		Group<Group<Event>> groupResult = grouper.next();

		if (target.getType().equals(Type.EVENTS) || target.getType().equals(Type.LEVEL)
				|| target.getType().equals(Type.MACHINE_OF_ORIGIN)) {
			throw new AggregationException("Not a countable type.", null);

		} else if (target.getType().equals(Type.UDF)) {
			if (target.getIdentification() != null) {
				for (Group<Event> groups : groupResult.getList()) {
					map.put(groups.getIdentifier(), addByUDF(groups, target.getIdentification()));
				}
			} else {
				throw new AggregationException("No target identification assigned.", null);
			}
		} else if (target.getType().equals(Type.DATE)) {
			for (Group<Event> groups : groupResult.getList()) {
				map.put(groups.getIdentifier(), addByDate(groups));
			}
		}

		return new AggregationIntervalResult(groupResult.getIdentifier(), map);
	}

	/**
	 * Returns the average value of a certain UDF in a group of events.
	 * 
	 * @param group
	 *            the group of events
	 * @param udfName
	 *            the identification of the UDF
	 * @return the average value
	 */
	private Object addByUDF(Group<Event> group, String udfName) {
		int integerResult = 0;
		double doubleResult = 0.0;
		float floatResult = 0.0F;
		long longResult = 0L;

		int counter = 0;

		for (Event e : group.getList()) {

			Object o = new Object();

			if (e.hasUdf(udfName)) {
				o = e.getUdf(udfName);
			} else {
				throw new AggregationException("Target does not exist.", null);
			}

			if (o instanceof Integer) {
				integerResult += Integer.parseInt(String.valueOf(o));
				counter++;
			} else if (o instanceof Long) {
				longResult += Long.parseLong(String.valueOf(o));
				counter++;
			} else if (o instanceof Double) {
				doubleResult += Double.parseDouble(String.valueOf(o));
				counter++;
			} else if (o instanceof Float) {
				floatResult += Float.parseFloat(String.valueOf(o));
				counter++;
			} else {
				throw new AggregationException("Target is not of type Integer, Long, Double or Float.", null);
			}
		}

		// Prevent division by zero
		if (counter == 0)
			counter = 1;

		if (doubleResult != 0.0) {
			return doubleResult / (double) counter;
		} else if (floatResult != 0.0F) {
			return floatResult / (float) counter;
		} else if (longResult != 0L) {
			return longResult / (long) counter;
		} else {
			return integerResult / counter;
		}
	}

	/**
	 * Returns the average date of occurrence in a group of events.
	 * 
	 * @param group
	 *            the group of events
	 * @return the average date
	 */
	private Date addByDate(Group<Event> group) {
		long result = 0L;
		long counter = 0L;

		for (Event e : group.getList()) {
			result += e.getDateOfOccurrence().getTime();
			counter++;
		}

		// Prevent division by zero
		if (counter == 0L)
			counter = 1L;

		// Average of Sum
		result /= counter;

		return new Date(result);
	}

	/**
	 * remove is not implemented
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented by any Aggregator");
	}

	/**
	 * Returns the Iterator for iteration. Necessary for the use of for each
	 * loops.
	 * 
	 * @return the iterator for iteration
	 */
	public Iterator<AggregationIntervalResult> iterator() {
		return this;
	}
}
