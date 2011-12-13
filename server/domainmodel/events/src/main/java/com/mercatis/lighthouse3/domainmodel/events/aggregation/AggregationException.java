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

import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;

/**
 * An Exception used by the aggregate method of the <code>EventRegistry</code>
 * and it's corresponding classes.
 */
public class AggregationException extends PersistenceException {

	private static final long serialVersionUID = -534083944741138824L;

	public AggregationException(String message, Exception cause) {
		super(message, cause);
	}

}
