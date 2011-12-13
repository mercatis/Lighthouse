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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EventRegistryFactoryService;
import com.mercatis.lighthouse3.services.JobRegistryFactoryService;
import com.mercatis.lighthouse3.services.OperationInstallationRegistryFactoryService;
import com.mercatis.lighthouse3.services.OperationRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessInstanceDefinitionRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessInstanceRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class OpenLighthouseDomainHandler extends AbstractHandler implements IHandler {
	
	private class ConnectJob extends Job {
		
		private LighthouseDomain domain;
		
		public ConnectJob(LighthouseDomain domain) {
			super("Connect to Lighthouse Domain");
			setUser(true);

			this.domain = domain;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Connect to " + domain.getProject().getName(), 11);
			try {
				monitor.subTask("Loading Software Components..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(SoftwareComponentRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Deployments..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(DeploymentRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Environments..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(EnvironmentRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Process Tasks..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(ProcessTaskRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Status Information..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(StatusRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Event Information..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(EventRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Process Instance Definitions..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(ProcessInstanceDefinitionRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Process Instance Informations..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(ProcessInstanceRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Operations..");
				try {
					RegistryFactoryServiceUtil.getRegistryFactoryService(OperationRegistryFactoryService.class, domain.getProject(), this);
				} catch (RuntimeException ex) {
					// operation server not available
				}
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				monitor.subTask("Loading Operation Installations..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(OperationInstallationRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				monitor.subTask("Loading Jobs..");
				RegistryFactoryServiceUtil.getRegistryFactoryService(JobRegistryFactoryService.class, domain.getProject(), this);
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				monitor.done();
			}

			return Status.OK_STATUS;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection == null || !(selection instanceof IStructuredSelection))
			return null;
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		
		if(structuredSelection.getFirstElement() instanceof LighthouseDomain) {
			final LighthouseDomain domain = ((LighthouseDomain) structuredSelection.getFirstElement());
			final String context = ((ContextAdapter) domain.getAdapter(ContextAdapter.class)).toContext(domain);
			final CommonNavigator navigator = (CommonNavigator) HandlerUtil.getActivePart(event);
			
			if (domain.getProject().isOpen())
				return null;
			
			try {
				if (Security.login(context)) {
					ConnectJob job = new ConnectJob(domain);
					job.addJobChangeListener(new JobChangeAdapter() {
						
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult() != Status.OK_STATUS) {
								System.err.println("Job did not finish with OK_STATUS.");
								try {
									domain.getProject().close(new NullProgressMonitor());
								} catch (CoreException e) { /* ignore */}
								CommonBaseActivator.getPlugin().getLighthouseDomainBroadCaster().notifyDomainClosed(domain);
							} else {
								CommonBaseActivator.getPlugin().getLighthouseDomainBroadCaster().notifyDomainOpened(domain);
							}
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								public void run() {
									navigator.getCommonViewer().refresh();
								}
							});
						}
						
					});
					
					// open the domain prior to connecting the registries, because property (like host) access fails on a closed domain.
					domain.getProject().open(new NullProgressMonitor());
					
					job.schedule();
					
				} else {
					domain.getProject().close(new NullProgressMonitor());
					Status status = new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Not authenticated");
					ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Authentication failure", "Domain closed", status);
				}
			} catch (PersistenceException pe) {
				try {
					domain.getProject().close(new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				String message = pe.getMessage();
				if (message.contains("Error 401")) {
					message = "Unauthorized";
				}
				Status status = new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, message);
				ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Authentication failure", "Domain closed", status);
			} catch (CoreException ex) {
				ex.printStackTrace();
				try {
					domain.getProject().close(new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				Status status = new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, "Not authenticated", ex);
				ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Authentication failure", "Domain closed", status);
			}
		}
		
		return null;
	}
}
