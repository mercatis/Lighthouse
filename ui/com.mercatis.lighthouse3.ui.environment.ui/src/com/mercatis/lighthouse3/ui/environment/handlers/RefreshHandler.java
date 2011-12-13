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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EagerRegistry;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.JobRegistryFactoryService;
import com.mercatis.lighthouse3.services.OperationInstallationRegistryFactoryService;
import com.mercatis.lighthouse3.services.OperationRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class RefreshHandler extends AbstractHandler implements IHandler {

	private class RefreshJob extends Job {
		
		private LighthouseDomain domain;
		
		public RefreshJob(LighthouseDomain domain) {
			super("Refresh");
			setUser(true);

			this.domain = domain;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Refresh " + domain.getProject().getName(), 11);
			try {
				monitor.subTask("Refreshing Software Components..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(SoftwareComponentRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Deployments..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(DeploymentRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Environments..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(EnvironmentRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Process Tasks..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(ProcessTaskRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Status Information..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(StatusRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Event Information..");
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Process Instance Definitions..");
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Process Instance Informations..");
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Operations..");
				try {
					((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(OperationRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				} catch (RuntimeException ex) {
					// operation server not available
				}
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Operation Installations..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(OperationInstallationRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Refreshing Jobs..");
				((EagerRegistry) RegistryFactoryServiceUtil.getRegistryFactoryService(JobRegistryFactoryService.class, domain.getProject(), this)).invalidate();
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			} finally {
				monitor.done();
			}

			return Status.OK_STATUS;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object activePart = HandlerUtil.getActivePart(event);
		if (!(activePart instanceof CommonNavigator))
			return null;
		
		final CommonNavigator navigator = (CommonNavigator) activePart;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection == null || !(selection instanceof IStructuredSelection))
			return null;
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Iterator<?> it = structuredSelection.iterator();
		while(it.hasNext()) {
			Object obj = it.next();
			
			DomainBoundEntityAdapter adapter = (DomainBoundEntityAdapter) Platform.getAdapterManager().getAdapter(obj, DomainBoundEntityAdapter.class);
			LighthouseDomain domain = adapter.getLighthouseDomain(obj);
			
			RefreshJob job = new RefreshJob(domain);
			job.addJobChangeListener(new JobChangeAdapter() {
				
				@Override
				public void done(IJobChangeEvent event) {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						public void run() {
							navigator.getCommonViewer().refresh();
						}
					});
				}
				
			});
			
			job.schedule();
		}
		
		return null;
	}

}
