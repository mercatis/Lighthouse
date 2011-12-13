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
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents a certain time interval result of an aggregation. The interval
 * result holds several groups. Each group may contain either an Integer,
 * Double, Float, Long, Date or HashMap<String, Integer>. HashMap as group
 * result can only occur when using the count command on target != EVENTS in the
 * aggregate method.
 * 
 * It provides iteration functionality to use in a for each loop f.e.
 */
public class AggregationIntervalResult implements Iterator<Object>, Iterable<Object> {

	/**
	 * The name of this time interval
	 */
	private String intervalName;

	/**
	 * A <code>HashMap</code> holding the groups of this time interval
	 */
	private HashMap<String, Object> groups;

	/**
	 * Holds a list of keys of the groups to use in the iteration.
	 */
	private ArrayList<String> keys;

	/**
	 * Holds the current index of the list of keys for the iteration.
	 */
	private int counter;

	/**
	 * Constructor that takes the interval name and a HashMap as arguments
	 * 
	 * @param intervalName
	 *            the name of the time interval
	 * @param groups
	 *            the groups that are being held by this interval result.
	 */
	public AggregationIntervalResult(String intervalName, HashMap<String, Object> interval) {
		this.groups = interval;
		this.intervalName = intervalName;
	}

	/**
	 * Returns the name of this time interval
	 * 
	 * @return the name of this time interval
	 */
	public String getIntervalName() {
		return intervalName;
	}

	/**
	 * Returns all groups of this time interval as an instance of
	 * <code>HashMap</code>
	 * 
	 * @return all groups as <code>HashMap</code>
	 */
	public HashMap<String, Object> getGroupsAsMap() {
		return groups;
	}

	/**
	 * Returns a list of group names of this time interval
	 * 
	 * @return a list of group names
	 */
	public ArrayList<String> getGroupOverview() {
		return new ArrayList<String>(groups.keySet());
	}

	/**
	 * Returns the aggregation result for a certain group. The aggregation
	 * result can be of following types: Integer, Double, Float, Long, Date,
	 * HashMap<String, Integer>
	 * 
	 * @param group
	 *            the group to return the result for
	 * @return the aggregation result for that group
	 */
	public Object getGroupResult(String group) {
		return groups.get(group);
	}

	/**
	 * Checks if a group exists by using the group name
	 * 
	 * @param group
	 *            the group name
	 * @return true, if the group exists, false otherwise
	 */
	public boolean groupExists(String group) {
		return groups.containsKey(group);
	}

	/**
	 * Initializes the Iterator by saving all keys of the group
	 * <code>HashMap</code>.
	 */
	private void initIterator() {
		this.keys = new ArrayList<String>(this.groups.keySet());
		this.counter = 0;
	}

	/**
	 * Checks whether there is one more iteration step or not
	 * 
	 * @return true if there is one more step, false otherwise
	 */
	public boolean hasNext() {
		return counter < keys.size();
	}

	/**
	 * Returns the next aggregation result of a group in an iteration. The
	 * aggregation result can be of following types: Integer, Double, Float,
	 * Long, Date, HashMap<String, Integer>
	 * 
	 * @return the next aggregation result.
	 */
	public Object next() {
		return this.groups.get(this.keys.get(this.counter++));
	}

	/**
	 * remove is not implemented
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented.");
	}

	/**
	 * Returns the Iterator for iteration. Necessary for the use of for each
	 * loops.
	 * 
	 * @return the iterator for iteration
	 */
	public Iterator<Object> iterator() {
		initIterator();
		return this;
	}
}
