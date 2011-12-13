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
package com.mercatis.lighthouse3.ui.event.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.mercatis.lighthouse3.ui.event.providers.EventTable;

public class AddColumnAsFilterCriteriaAction extends Action implements ISelectionListener, IWorkbenchAction {

	private IWorkbenchWindow window;
	private IStructuredSelection selection;
	private EventTable table;
	
	public AddColumnAsFilterCriteriaAction(IWorkbenchWindow window, EventTable table) {
		setText("Add Attribute as Filter criteria");
		setId("lighthouse.events.actions.filterCriteria");
		this.window = window;
		this.window.getSelectionService().addSelectionListener(this);
		this.table = table;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		table.addSelectedColumnAsFilterCriteria();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (incoming instanceof IStructuredSelection) {
		    selection = (IStructuredSelection) incoming;
		    setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof com.mercatis.lighthouse3.domainmodel.events.Event);
		  } else {
		    setEnabled(false);
		  }
	}
	
	/**
	 * 
	 */
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

}
