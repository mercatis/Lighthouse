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
package com.mercatis.lighthouse3.ui.event.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.mercatis.lighthouse3.domainmodel.events.Event;

public class EventSelectionListener implements ISelectionListener {

	EventDetailView eventDetailView = null;

	public EventSelectionListener(EventDetailView eventDetailView) {
		super();
		this.eventDetailView = eventDetailView;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof Event) {
				showEventDetails((Event) structuredSelection.getFirstElement());
			}
		}
	}

	private void showEventDetails(Event event) {
		eventDetailView.showEvent(event);
	}

}
