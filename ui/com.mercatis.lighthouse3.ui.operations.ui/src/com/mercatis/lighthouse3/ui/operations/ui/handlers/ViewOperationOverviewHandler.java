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
package com.mercatis.lighthouse3.ui.operations.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.ui.OperationsUI;
import com.mercatis.lighthouse3.ui.operations.ui.views.OperationsOverView;


public class ViewOperationOverviewHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof LighthouseDomain) {
			final LighthouseDomain lighthouseDomain = (LighthouseDomain) element;
			try {
				final IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OperationsOverView.ID, OperationsOverView.ID + lighthouseDomain.getProject().getName(), IWorkbenchPage.VIEW_CREATE);
				Runnable runnable = new Runnable() {
					public void run() {
						((OperationsOverView) view).loadData(lighthouseDomain);
					}
				};
				BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), runnable);
			} catch (Exception e) {
				OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}
}
