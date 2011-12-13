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
package com.mercatis.lighthouse3.ui.environment.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.commons.commons.EnumerationRange;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.environment.base.model.AbstractContainer;

public class RemoveDeploymentHandler extends AbstractStructuredSelectionHandler {

	private List<AbstractContainer> containers;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		preExecution(event);
		
		boolean selectedDeploymentPartOfStatus = false;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof TreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) selection;
			List<TreePath> paths = new ArrayList<TreePath>(Arrays.asList(treeSelection.getPaths()));
			
			// remove all paths that do not end in a deployment..
			for (Iterator<TreePath> it = paths.iterator(); it.hasNext();) {
				if (!(it.next().getLastSegment() instanceof Deployment)) {
					it.remove();
				}
			}
			
			// remove all paths where the selected deployment is part of a status..
			for (Iterator<TreePath> it = paths.iterator(); it.hasNext();) {
				TreePath path = it.next();
				Deployment deployment = (Deployment) path.getLastSegment();
				path = path.getParentPath();
				while (path != null) {
					Object element = path.getLastSegment();
					if (element instanceof StatusCarrier) {
						StatusCarrier carrier = (StatusCarrier) element;
						StatusRegistry registry = RegistryFactoryServiceUtil.getRegistryFactoryService(StatusRegistryFactoryService.class, deployment.getLighthouseDomain(), this);
						List<Status> statuus = registry.getStatusForCarrier(carrier);
						for (Status status : statuus) {
							if (contextContainsDeployment(status.getOkTemplate(), deployment)) {
								selectedDeploymentPartOfStatus = true;
								it.remove();
								break;
							}
							
							if (contextContainsDeployment(status.getErrorTemplate(), deployment)) {
								selectedDeploymentPartOfStatus = true;
								it.remove();
								break;
							}
						}
					}
					path = path.getParentPath();
				}
			}
			
			// detach remaining deployments..
			for (Iterator<TreePath> it = paths.iterator(); it.hasNext();) {
				TreePath path = it.next();
				Deployment deployment = (Deployment) path.getLastSegment();
				DeploymentCarryingDomainModelEntity<?> carrier = (DeploymentCarryingDomainModelEntity<?>) path.getParentPath().getLastSegment();
				carrier.detachDeployment(deployment);
				
				if (carrier instanceof Environment) {
					EnvironmentRegistry registry = RegistryFactoryServiceUtil.getRegistryFactoryService(EnvironmentRegistryFactoryService.class, carrier.getLighthouseDomain(), this);
					registry.update((Environment) carrier);
				}
				if (carrier instanceof ProcessTask) {
					ProcessTaskRegistry registry = RegistryFactoryServiceUtil.getRegistryFactoryService(ProcessTaskRegistryFactoryService.class, carrier.getLighthouseDomain(), this);
					registry.update((ProcessTask) carrier);
				}
				containers.add(domainService.getLighthouseDomainByEntity(carrier).getContainerFor(carrier));
			}
			
			// notify user on deployments that were not detached..
			if (selectedDeploymentPartOfStatus) {
				Shell shell = HandlerUtil.getActiveShell(event);
				MessageDialog.openWarning(shell, "Deployments not detached", "One or more deployments were not detached because they are part of a status.");
			}
		}

		postExecution(event);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean contextContainsDeployment(Event template, Deployment deployment) {
		Deployment context = template.getContext();
		if (context instanceof EnumerationRange<?>) {
			EnumerationRange<Deployment> deployments = (EnumerationRange<Deployment>) context;
			return deployments.contains(deployment);
		}
		
		return context.equals(deployment);
	}

	@Override
	protected void execute(Object element) throws ExecutionException {
	}

	@Override
	protected void postExecution(ExecutionEvent event) throws ExecutionException {
		for (AbstractContainer container : containers) {
			domainService.notifyDomainChange(container);
		}
	}

	@Override
	protected void preExecution(ExecutionEvent event) throws ExecutionException {
		containers = new ArrayList<AbstractContainer>();
	}
}
