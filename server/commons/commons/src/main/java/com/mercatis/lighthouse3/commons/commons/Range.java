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
 * This interface represents ranges of values.
 */
public interface Range<V> {

	/**
	 * This method checks whether the current range overlaps with a different
	 * one.
	 * 
	 * @param otherValueRange
	 *            the other range
	 * @return <code>true</code> iff the ranges overlap.
	 */
	public abstract boolean overlaps(Range<V> otherValueRange);

	/**
	 * This predicate checks whether a given value is contained in the range.
	 * 
	 * @param value
	 *            the value to check for containment
	 * @return <code>true</code> iff the value is contained in the range.
	 */
	public abstract boolean contains(V value);

	/**
	 * Transforms the given range into a string representation.
	 * 
	 * @return the string representation of the range
	 */
	public abstract String asString();

}