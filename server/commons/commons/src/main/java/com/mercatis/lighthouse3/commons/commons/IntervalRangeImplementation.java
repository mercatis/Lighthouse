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
package com.mercatis.lighthouse3.commons.commons;

/**
 * This class provides a straightforward implementation of the
 * <code>IntervalRange</code> interface.
 */
public class IntervalRangeImplementation<V extends Comparable<V>> implements IntervalRange<V> {

	/**
	 * This property maintains the lower bound.
	 */
	private V lowerBound = null;

	public V getLowerBound() {
		return this.lowerBound;
	}

	/**
	 * This property maintains the upper bound.
	 */
	private V upperBound = null;

	public V getUpperBound() {
		return this.upperBound;
	}

	public boolean contains(V value) {
		if ((this.getLowerBound() == null) && (this.getUpperBound() == null))
			return true;
		else if ((this.getLowerBound() != null) && (this.getUpperBound() != null))
			return (this.getLowerBound().compareTo(value) <= 0) && (this.getUpperBound().compareTo(value) >= 0);
		else if (this.getLowerBound() != null)
			return this.getLowerBound().compareTo(value) <= 0;
		else
			return this.getUpperBound().compareTo(value) >= 0;
	}

	public boolean overlaps(Range<V> otherValueRange) {
		if (!(otherValueRange instanceof IntervalRange))
			return false;

		IntervalRange<V> otherIntervalRange = (IntervalRange<V>) otherValueRange;

		if ((otherIntervalRange.getLowerBound() == null) && (otherIntervalRange.getUpperBound() == null))
			return true;
		else if (otherIntervalRange.getLowerBound() == null)
			return this.contains(otherIntervalRange.getUpperBound());
		else if (otherIntervalRange.getUpperBound() == null)
			return this.contains(otherIntervalRange.getLowerBound());
		else
			return this.contains(otherIntervalRange.getUpperBound())
					|| this.contains(otherIntervalRange.getLowerBound());
	}

	public String asString() {
		StringBuilder result = new StringBuilder();

		if (this.getLowerBound() != null)
			result.append(this.getLowerBound().toString());

		result.append(XmlMuncher.VALUE_INTERVAL_SEPARATOR);

		if (this.getUpperBound() != null)
			result.append(this.getUpperBound().toString());

		return result.toString();
	}

	/**
	 * Public constructor for ranges.
	 * 
	 * @param lowerBound
	 *            the lower bound of the range
	 * @param upperBound
	 *            the upper bound of the range
	 */
	public IntervalRangeImplementation(V lowerBound, V upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
}
