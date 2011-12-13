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
package com.mercatis.lighthouse3.ui.environment.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractDynamicWizard;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;

public class NewLighthouseDomainWizard extends AbstractDynamicWizard implements INewWizard {

	private IProject newProject;
	public NewLighthouseDomainWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New Lighthouse Domain");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	private IProject createProject(List<AbstractWizardPage> pages) {
		if (newProject != null) {
			return newProject;
		}
		
		
		IProject newProjectHandle = null;
		URI location = null;
		for (AbstractWizardPage page : pages) {
			if (page.getPage() instanceof WizardNewProjectCreationPage) {
				// get a project handle
				WizardNewProjectCreationPage p = (WizardNewProjectCreationPage)page.getPage();
				// get a project descriptor
				newProjectHandle = p.getProjectHandle();
				if (!p.useDefaults()) {
					location = p.getLocationURI();
				}
				break;
			}
		}
		if (newProjectHandle == null) {
			return null;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		// create the new project operation
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CreateProjectOperation op = new CreateProjectOperation(description, "New Lighthouse Domain");
				try {
					PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (ExecutionException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException ex) {
			return null;
		} catch (InvocationTargetException ex) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			return null;
		}

		newProject = newProjectHandle;
		DomainService domainService = CommonBaseActivator.getPlugin().getDomainService();
		LighthouseDomain lighthouseDomain = domainService.createLighthouseDomain(newProject);
		DomainConfiguration domainConfiguration = domainService.getDomainConfiguration(lighthouseDomain);
		domainService.addLighthouseDomainNature(lighthouseDomain);
		
		//delegate objects to pages - they know what to do - hopefully
		manipulateObject(domainConfiguration);
		manipulateObject(lighthouseDomain);
		try {
			newProject.close(new NullProgressMonitor());
		} catch (CoreException ex) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
		}
		CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(ResourcesPlugin.getWorkspace().getRoot());
		return newProject;
	}
	
	@Override
	protected boolean performFinish(List<AbstractWizardPage> pages) {
		createProject(pages);

		if (newProject == null) {
			return false;
		}

		return true;
	}
}
