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

import java.util.List;

/**
 * This class represents a group. It uses an identifying string and maps a list
 * of elements of the specified Type(<T>) to it. Generic is used here to provide
 * groups that contain other groups.
 * 
 * @param <T>
 *            the type of the list.
 */
public class Group<T> {

	/**
	 * The list that this group contains.
	 */
	private List<T> list;

	/**
	 * The name of this group.
	 */
	private String identifier;

	/**
	 * A constructor that takes a list of certain elements and an identifying
	 * string as group name as attributes.
	 * 
	 * @param list
	 *            the list to be held by this group
	 * @param identifier
	 *            the name of this group
	 */
	public Group(List<T> list, String identifier) {
		this.list = list;
		this.identifier = identifier;
	}

	/**
	 * Returns the list of this group
	 * 
	 * @return the list
	 */
	public List<T> getList() {
		return list;
	}

	/**
	 * Returns the name of this group
	 * 
	 * @return the group name
	 */
	public String getIdentifier() {
		return identifier;
	}
}