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
package com.mercatis.lighthouse3.ui.status.handler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.status.ui.views.StatusHistoryView;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class ViewStatusHistoryHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof Status || element instanceof StatusEditingObject) {
			final Status status;
			if (element instanceof Status) {
				status = (Status) element;
			} else {
				status = ((StatusEditingObject) element).getStatus();
			}
			try {
				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StatusHistoryView.ID, status.toString(),IWorkbenchPage.VIEW_VISIBLE);
	
				LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getStatusService().getLighthouseDomainForEntity(
						status);
				int pageSize = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
						.getStatusPageSize();
				int pageNo = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
						.getStatusPageNo();
				Status refreshedStatus = CommonBaseActivator.getPlugin().getStatusService().refresh(status, pageSize, pageNo);
				((StatusHistoryView) view).setStatus(refreshedStatus);
			} catch (Exception e) {
				CommonUIActivator.getPlugin().getLog().log(new org.eclipse.core.runtime.Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}
}
