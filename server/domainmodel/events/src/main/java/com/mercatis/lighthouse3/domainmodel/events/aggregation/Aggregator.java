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

import java.util.Iterator;

/**
 * An interface that provides a certain aggregation functionality over a group
 * of <code>Event</code>s by being used as an Iterator. The list of events for
 * each iteration step is provided by a <code>GroupByGrouper</code>.
 */
public interface Aggregator extends Iterator<AggregationIntervalResult>, Iterable<AggregationIntervalResult> {

	/**
	 * This initializes the Aggregator and its Iterator
	 * 
	 * @param grouper
	 *            The <code>GroupByGrouper</code> to be used for retrieving the
	 *            groups in each iteration step.
	 * @param target
	 *            The aggregation target property of an <code>Event</code>.
	 */
	public void init(GroupByGrouper grouper, AggregationTarget target);

}
