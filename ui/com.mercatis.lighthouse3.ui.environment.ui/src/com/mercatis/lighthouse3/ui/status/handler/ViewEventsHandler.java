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

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.editors.EventEditor;


public class ViewEventsHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		
		if(element instanceof EventTriggeredStatusChange) {
			EventTriggeredStatusChange statusChange = (EventTriggeredStatusChange) element;
			
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(((EventTriggeredStatusChange)element).getStatus().getContext());
			Event event = ((EventTriggeredStatusChange) element).getTriggeringEvent();
			
			String title = LabelConverter.getLabel(statusChange.getStatus()); 
			
			EventEditorInput input = null;
			
			Calendar from = new GregorianCalendar();
			from.setTime(event.getDateOfOccurrence());
			from.add(Calendar.MINUTE, -1);
			
			Calendar to = new GregorianCalendar();
			to.setTime(event.getDateOfOccurrence());
			to.add(Calendar.MINUTE, 1);
			
			//create template with time from: dateOfOccurence - 1Min to: dateOfOccurence + 1Min
			Event template = EventBuilder.template().setDateOfOccurrence(Ranger.interval(from.getTime(),to.getTime())).done();

			//set deployments
			template.setContext(Ranger.enumeration(statusChange.getStatus().getContext().getAssociatedDeployments()));
			
			input = new EventEditorInput(lighthouseDomain, template, title);
			input.setMaximumEventsForDisplay(200);
			
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IEditorPart editor = IDE.openEditor(page, input, "lighthouse3.events.editor.event");
				if(editor instanceof EventEditor) {
					((EventEditor) editor).startEventReceipt();
				}
			} catch (PartInitException ex) {
				throw new ExecutionException(ex.getMessage(), ex);
			}
		}
	}


}
