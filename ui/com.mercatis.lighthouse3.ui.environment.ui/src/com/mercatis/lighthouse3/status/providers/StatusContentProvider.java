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
package com.mercatis.lighthouse3.status.providers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusContentProvider implements IStructuredContentProvider {

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		final List tStatuses = (List) inputElement;
		Collections.sort(tStatuses, new StatusComparator());
		return tStatuses.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	private static class StatusComparator implements Comparator<Object>{

		public int compare(Object o1, Object o2) {
			if(o1!=null && o2!=null){
				final String tCodeOne = getCode(o1);
				final String tCodeOther = getCode(o2);
				if(o1!=null && o2!=null){
					return tCodeOne.compareTo(tCodeOther);
				}
			}
			return 0;
		}

		private String getCode(final Object pObject){
			if(pObject instanceof StatusEditingObject){
				return ((StatusEditingObject)pObject).getCode();
			}
			else if(pObject instanceof Status){
				return ((Status)pObject).getCode();
			}
			return null;
		}
	}
	
}
