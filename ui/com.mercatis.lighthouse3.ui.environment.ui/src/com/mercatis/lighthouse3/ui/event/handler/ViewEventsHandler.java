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
package com.mercatis.lighthouse3.ui.event.handler;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;

public class ViewEventsHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {

		String title = "";
		
		EventEditorInput input = null;
		Event template = EventBuilder.template().setDateOfOccurrence(Ranger.interval(new Date(System.currentTimeMillis() - 5 * 60 * 1000), null)).done();
		
		LighthouseDomain lighthouseDomain = null;
		Set<Deployment> deployments = new HashSet<Deployment>();

		if (element instanceof Location) {
			Location location = (Location) element;
			title = LabelConverter.getLabel(location);
			lighthouseDomain = (LighthouseDomain) getLighthouseDomain(location);
			deployments.addAll(location.getDeployments());
		}

		if (element instanceof Deployment) {
			Deployment deployment = (Deployment) element;
			title = LabelConverter.getLabel(deployment);
			lighthouseDomain = (LighthouseDomain) getLighthouseDomain(deployment);
			deployments.add(deployment);
		}

		if (element instanceof Environment) {
			Environment environment = (Environment) element;
			title = LabelConverter.getLabel(environment);
			lighthouseDomain = (LighthouseDomain) getLighthouseDomain(environment);
			deployments.addAll(environment.getAllDeployments());
		}

		if (element instanceof ProcessTask) {
			ProcessTask processTask = (ProcessTask) element;
			title = LabelConverter.getLabel(processTask);
			lighthouseDomain = (LighthouseDomain) getLighthouseDomain(processTask);
			deployments.addAll(processTask.getAllDeployments());
		}
		if (element instanceof ProcessTaskModelEditPart) {
			ProcessTask processTask = ((ProcessTaskModelEditPart) element).getProcessTask();
			title = LabelConverter.getLabel(processTask);
			lighthouseDomain = (LighthouseDomain) getLighthouseDomain(processTask);
			deployments.addAll(processTask.getAllDeployments());
		}

		if (deployments.size() == 1)
			template.setContext(deployments.iterator().next());
		else
			template.setContext(Ranger.enumeration(deployments));
		
		input = new EventEditorInput(lighthouseDomain, template, title);
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, input, "lighthouse3.events.editor.event");
		} catch (PartInitException ex) {
			throw new ExecutionException(ex.getMessage(), ex);
		}
	}
	
	private LighthouseDomain getLighthouseDomain(Object object) {
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(object);
	}

}
