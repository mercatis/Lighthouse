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
import java.util.HashMap;
import java.util.Iterator;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.aggregation.AggregationTarget.Type;

/**
 * Iterator that delivers the numbered property of <code>Event</code> in a group
 * of events in each iteration step. For all targets except for EVENTS this
 * delivers grouped results instead of one single result.
 */
public class AggregatorCount implements Aggregator {

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
	 * Used to provide group names in a certain date format
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

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

		if (target.getType().equals(Type.EVENTS)) {
			for (Group<Event> groups : groupResult.getList()) {
				map.put(groups.getIdentifier(), groups.getList().size());
			}

		} else if (target.getType().equals(Type.LEVEL)) {
			for (Group<Event> groups : groupResult.getList()) {
				map.put(groups.getIdentifier(), addByLevel(groups));
			}

		} else if (target.getType().equals(Type.DATE)) {
			for (Group<Event> groups : groupResult.getList()) {
				map.put(groups.getIdentifier(), addByDate(groups));
			}

		} else if (target.getType().equals(Type.MACHINE_OF_ORIGIN)) {
			for (Group<Event> groups : groupResult.getList()) {
				map.put(groups.getIdentifier(), addByOrigin(groups));
			}

		} else if (target.getType().equals(Type.UDF)) {
			if (target.getIdentification() != null) {
				for (Group<Event> groups : groupResult.getList()) {
					map.put(groups.getIdentifier(), addByUDF(groups, target.getIdentification()));
				}
			} else {
				throw new AggregationException("No target identification assigned.", null);
			}
		}

		return new AggregationIntervalResult(groupResult.getIdentifier(), map);
	}

	/**
	 * Returns a list of numbered values of the machine of origin in a group of
	 * events. Counts all events of the same machine.
	 * 
	 * @param group
	 *            the group of events
	 * @return a map of numbered values and identifiers
	 */
	private HashMap<String, Integer> addByOrigin(Group<Event> group) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for (Event e : group.getList()) {

			String id = e.getMachineOfOrigin();
			
			if(id == null)
				id = "No Entry";

			if (!map.containsKey(id))
				map.put(id, 1);
			else
				map.put(id, map.get(id) + 1);
		}

		return map;
	}

	/**
	 * Returns a list of numbered values of the level in a group of events.
	 * Counts all events of the same level.
	 * 
	 * @param group
	 *            the group of events
	 * @return a map of numbered values and identifiers
	 */
	private HashMap<String, Integer> addByLevel(Group<Event> group) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for (Event e : group.getList()) {

			String id = e.getLevel();

			if (!map.containsKey(id))
				map.put(id, 1);
			else
				map.put(id, map.get(id) + 1);
		}

		return map;
	}

	/**
	 * Returns a list of numbered values of the date in a group of events.
	 * Counts all events of the same second.
	 * 
	 * @param group
	 *            the group of events
	 * @return a map of numbered values and identifiers
	 */
	private HashMap<String, Integer> addByDate(Group<Event> group) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for (Event e : group.getList()) {

			String id = dateFormat.format(e.getDateOfOccurrence());

			if (!map.containsKey(id))
				map.put(id, 1);
			else
				map.put(id, map.get(id) + 1);
		}

		return map;
	}

	/**
	 * Returns a list of numbered values of a certain UDF in a group of events.
	 * Counts all events that have the same UDF value
	 * 
	 * @param group
	 *            the group of events
	 * @param udfName
	 *            the identification of the UDF
	 * @return a map of numbered values and identifiers
	 */
	private HashMap<String, Integer> addByUDF(Group<Event> group, String udfName) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for (Event e : group.getList()) {

			String id = "";

			if (e.hasUdf(udfName)) {
				id = String.valueOf(e.getUdf(udfName));
			} else {
				throw new AggregationException("Target does not exist.", null);
			}

			if (!map.containsKey(id))
				map.put(id, 1);
			else
				map.put(id, map.get(id) + 1);
		}

		return map;
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
