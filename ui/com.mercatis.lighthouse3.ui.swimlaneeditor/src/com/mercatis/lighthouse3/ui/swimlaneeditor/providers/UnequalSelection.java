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
package com.mercatis.lighthouse3.ui.swimlaneeditor.providers;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * An IStructuredSelection that never equals another one.
 * 
 */
public class UnequalSelection implements IStructuredSelection {

	private Object selection;
	
	public UnequalSelection(Object selection) {
		this.selection = selection;
	}
	
	@Override
	public boolean equals(Object obj) {
		return false;
	}

	public Object getFirstElement() {
		return selection;
	}

	@SuppressWarnings("rawtypes")
	public Iterator iterator() {
		return toList().iterator();
	}

	public int size() {
		return selection == null ? 0 : 1;
	}

	public Object[] toArray() {
		return selection == null ? new Object[0] : new Object[] {selection};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List toList() {
		List list;
		if (selection == null) {
			list = Collections.EMPTY_LIST;
		} else {
			list = new LinkedList();
			list.add(selection);
		}
		return list;
	}

	public boolean isEmpty() {
		return selection == null;
	}
}
