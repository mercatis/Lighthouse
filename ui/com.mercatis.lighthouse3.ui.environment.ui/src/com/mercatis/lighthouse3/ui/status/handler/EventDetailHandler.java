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
import org.eclipse.ui.views.IViewDescriptor;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.event.views.EventDetailView;


public class EventDetailHandler extends AbstractStructuredSelectionHandler { 

	@Override
	protected void execute(Object element) throws ExecutionException {
		if(element instanceof EventTriggeredStatusChange) {
			try {
				IViewDescriptor viewDesc = PlatformUI.getWorkbench().getViewRegistry().find(EventDetailView.ID);
				IViewPart view;
				
				if(viewDesc == null) {
					view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(EventDetailView.ID, "",IWorkbenchPage.VIEW_VISIBLE);
				}
				else {
					view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(EventDetailView.ID);
				}
				if(view instanceof EventDetailView) {
					EventDetailView detailView = (EventDetailView) view;
					Event event = ((EventTriggeredStatusChange) element).getTriggeringEvent();
					
					detailView.showEvent(event);
				}
				
			}catch (Exception e) {
				CommonUIActivator.getPlugin().getLog().log(new org.eclipse.core.runtime.Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
			}
			
		}
	}
}
