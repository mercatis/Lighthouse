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

/**
 * The aggregation target defines on which attribute the aggregation should be
 * used.
 */
public class AggregationTarget {

	/**
	 * The target type defines on which type of attribute of the
	 * <code>Event</code> class the aggregation should be used
	 */
	public enum Type {
		EVENTS, DATE, LEVEL, UDF, MACHINE_OF_ORIGIN
	}

	/**
	 * Provides additional information on the attribute e.g. the UDF name
	 */
	private String identification = null;

	/**
	 * Returns additional information on the attribute#
	 * 
	 * @return the additional information string
	 */
	public String getIdentification() {
		return this.identification;
	}

	/**
	 * Sets an additional information on the attribute for the Aggregation e.g.
	 * the UDF name
	 * 
	 * @param identification
	 *            additional information of an attribute
	 * @return the aggregation target itself for setter chaining
	 */
	public AggregationTarget setIdentification(String identification) {
		this.identification = identification;
		return this;
	}

	/**
	 * The target type defines on which type of attribute of the
	 * <code>Event</code> class the aggregation should be used
	 */
	private Type type = null;

	/**
	 * Returns the attribute on which the aggregation should be used on
	 * 
	 * @return the target type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Sets the target type of the aggregation, on which the aggregation should
	 * be used on
	 * 
	 * @param type
	 *            Defines the type of attribute of the <code>Event</code> class
	 * @return the aggregation target itself for setter chaining
	 */
	public AggregationTarget setType(Type type) {
		this.type = type;
		return this;
	}
}
