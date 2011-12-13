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
package com.mercatis.lighthouse3.status.ui.views.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;

public class StatusHistoryTableContentProvider implements IStructuredContentProvider {

	private List<StatusChange> statusChangeList;
	private TableViewer tableViewer;
	private StatusChange lastChange = null;

	public StatusHistoryTableContentProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		statusChangeList = new LinkedList<StatusChange>();
	}

	public Object[] getElements(Object inputElement) {
		List<StatusChange> dest = new ArrayList<StatusChange>(statusChangeList);
		Collections.reverse(dest);
		return dest.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}

	public void setStatus(Status status) {
		statusChangeList = status.getChangeHistory();
		tableViewer.refresh();
	}

	public StatusChange getLastChange() {
		return lastChange;
	}
}
