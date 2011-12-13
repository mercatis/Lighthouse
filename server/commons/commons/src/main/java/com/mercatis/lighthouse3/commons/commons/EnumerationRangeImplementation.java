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

import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a straight forward implementation of the
 * <code>EnumerationRange</code> interface.
 */
public class EnumerationRangeImplementation<V> implements EnumerationRange<V> {

	/**
	 * This property maintains the enumeration.
	 */
	private Set<V> enumeration = new HashSet<V>();

	public Set<V> getEnumeration() {
		return this.enumeration;
	}

	public boolean contains(V value) {
		return this.getEnumeration().contains(value);
	}

	public boolean overlaps(Range<V> otherValueRange) {
		if (!(otherValueRange instanceof EnumerationRange))
			return false;

		EnumerationRange<V> otherEnumerationRange = (EnumerationRange<V>) otherValueRange;

		for (V enumerator : this.getEnumeration()) {
			if (otherEnumerationRange.contains(enumerator))
				return true;
		}

		return false;
	}

	public String asString() {
		StringBuilder result = new StringBuilder();

		boolean first = true;

		for (V enumerator : this.getEnumeration()) {
			if (!first)
				result.append(XmlMuncher.VALUE_ENUMERATION_SEPARATOR);
			else
				first = false;
			result.append(enumerator.toString());
		}

		return result.toString();
	}

	/**
	 * Public constructor for enumeration ranges.
	 * 
	 * @param enumeration
	 *            the enumeration values making up the range
	 */
	public EnumerationRangeImplementation(Set<V> enumeration) {
		this.getEnumeration().addAll(enumeration);
	}
}
