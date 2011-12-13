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
package com.mercatis.lighthouse3.ui.swimlaneeditor.actions;

import java.util.Comparator;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.actions.DropDownAction;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;
import com.mercatis.lighthouse3.ui.swimlaneeditor.providers.ProcessInstanceLabelProvider;
import com.mercatis.lighthouse3.ui.swimlaneeditor.views.ProcessInstanceTreeTableView;


public class InstanceNavigationAction extends DropDownAction<ProcessInstance> {

	private IInstanceView instanceView;
	
	public InstanceNavigationAction(IInstanceView instanceView) {
		this.instanceView = instanceView;
		this.setLabelProvider(new ProcessInstanceLabelProvider());
		this.setComparator(itemComparator);
	}
	
	@Override
	public void run() {
		DropDownItem firstMenuItem = getFirstMenuItem();
		if (firstMenuItem != null) {
			setProcessInstanceOnEditor(firstMenuItem.getData());
		}
	}

	@Override
	public void runFromMenuItem(String name, ProcessInstance data) {
		setProcessInstanceOnEditor(data);
	}
	
	private void setProcessInstanceOnEditor(ProcessInstance processInstance) {
		instanceView.setActiveProcessInstance(processInstance);
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProcessInstanceTreeTableView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
			((ProcessInstanceTreeTableView) viewPart).setProcessInstance(instanceView.getActiveProcessInstance());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private Comparator<DropDownItem> itemComparator = new Comparator<DropDownItem>() {
		public int compare(DropDownItem o1, DropDownItem o2) {
			int result = 0;
			if (sortingMode != SortingMode.NONE) {
				if (o1.getData().getStartDate().equals(o2.getData().getStartDate())) {
					result = Long.valueOf(o1.getData().getId())
					.compareTo(Long.valueOf(o2.getData().getId()));
				} else {
					result = o1.getData().getStartDate().compareTo(o2.getData().getStartDate());
				}
				if (sortingMode == SortingMode.DESCENDING) {
					result *= -1;
				}
			}
			return result;
		}
	};
}
