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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.commons.commons.Ranger;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;


public class ViewInstanceEventsAction extends Action {
	
	IInstanceView instanceView;
	
	public ViewInstanceEventsAction(IInstanceView instanceView) {
		super();
		this.instanceView = instanceView;
		this.setText("View Instance Events");
	}

	@Override
	public void run() {
		ProcessInstance instance = instanceView.getActiveProcessInstance();
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(instance.getProcessInstanceDefinition().getProcessTask());

		EventEditorInput input = null;
		Event template = EventBuilder.template().setDateOfOccurrence(Ranger.interval(instance.getStartDate(), instance.getEndDate())).done();
		
		Set<Deployment> deployments = new HashSet<Deployment>();
		String title = LabelConverter.getLabel(instance);
		deployments.addAll(instance.getProcessInstanceDefinition().getProcessTask().getAllDeployments());

		if (deployments.size() == 1)
			template.setContext(deployments.iterator().next());
		else
			template.setContext(Ranger.enumeration(deployments));
		
		input = new EventEditorInput(lighthouseDomain, template, instance.getEvents(), title);
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, input, "lighthouse3.events.editor.event");
		} catch (PartInitException ex) {
			ex.printStackTrace();
		}
	}
}
