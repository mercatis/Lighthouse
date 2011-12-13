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
 * This interface represents interval ranges of comparables.
 */
public interface IntervalRange<V extends Comparable<V>> extends Range<V> {

	/**
	 * This method returns the lower bound of the interval.
	 * 
	 * @return the lower bound. <code>null</code> means there is no lower bound,
	 *         i.e., this is an open interval.
	 */
	public V getLowerBound();

	/**
	 * This method returns the upper bound of the interval.
	 * 
	 * @return the upper bound. <code>null</code> means there is no upper bound,
	 *         i.e., this is an open interval.
	 */
	public V getUpperBound();

}
