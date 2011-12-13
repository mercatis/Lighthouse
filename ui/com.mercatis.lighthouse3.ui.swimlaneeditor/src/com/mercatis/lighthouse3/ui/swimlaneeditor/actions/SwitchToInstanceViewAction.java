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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;
import com.mercatis.lighthouse3.ui.swimlaneeditor.views.ProcessInstanceTreeTableView;

public class SwitchToInstanceViewAction extends Action {

	private IInstanceView instanceView;
	
	public SwitchToInstanceViewAction(IInstanceView instanceView) {
		super(null, AS_CHECK_BOX);
		this.instanceView = instanceView;
		this.setImageDescriptor(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/dates.gif")));
		this.setText("Enable Instance View");
	}

	public void run() {
		instanceView.setInstanceViewEnabled(isChecked());
		if (isChecked()) {
			try {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProcessInstanceTreeTableView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				((ProcessInstanceTreeTableView) viewPart).setProcessInstance(instanceView.getActiveProcessInstance());
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
