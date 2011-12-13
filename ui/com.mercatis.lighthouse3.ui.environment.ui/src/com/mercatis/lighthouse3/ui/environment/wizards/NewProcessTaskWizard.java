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

import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.status.ui.LighthouseStatusDecorator;
import com.mercatis.lighthouse3.status.ui.wizards.StatusWizard;
import com.mercatis.lighthouse3.status.ui.wizards.pages.StatusWizardMainPage;
import com.mercatis.lighthouse3.status.ui.wizards.pages.StatusWizardNotificationPage;
import com.mercatis.lighthouse3.status.ui.wizards.pages.WizardEventTemplatePage;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;
import com.mercatis.lighthouse3.ui.environment.wizards.pages.WizardNewProcessTaskPage;
import com.mercatis.lighthouse3.ui.environment.wizards.pages.WizardProcessTaskAddDeploymentsPage;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPart;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;

public class NewProcessTaskWizard extends Wizard implements INewWizard {

	private WizardNewProcessTaskPage page;

	private ProcessTask newProcessTask;
	private ProcessTask parentProcessTask;
	private LighthouseDomain lighthouseDomain;

	private StatusWizardMainPage statusMainPage;
	private WizardEventTemplatePage statusOkPage;
	private WizardEventTemplatePage statusErrorPage;
	private StatusWizardNotificationPage statusNotificationPage;

	private WizardProcessTaskAddDeploymentsPage deploymentsPage;

	public NewProcessTaskWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New ProcessTask");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof LighthouseDomain) {
			lighthouseDomain = (LighthouseDomain) obj;
		} else if (obj instanceof ProcessTaskContainer) {
			lighthouseDomain = ((ProcessTaskContainer) obj).getLighthouseDomain();
		} else if (obj instanceof ProcessTask) {
			parentProcessTask = (ProcessTask) obj;
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(obj);
		} else if (obj instanceof ProcessTaskModelEditPart) {
			ProcessTaskModelEditPart part = (ProcessTaskModelEditPart) obj;
			parentProcessTask = part.getProcessTask();
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(parentProcessTask);
		} else if (obj instanceof ProcessTaskEditPart) {
			ProcessTaskEditPart part = (ProcessTaskEditPart) obj;
			parentProcessTask = part.getProcessTask();
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(parentProcessTask);
		}
	}

	public void addPages() {
		page = new WizardNewProcessTaskPage("ProjectCreationPage");

		page.setLighthouseDomain(lighthouseDomain);
		page.setParentEntity(parentProcessTask);
		
		List<Deployment> deps = CommonBaseActivator.getPlugin().getDomainService().getAllDeployments(lighthouseDomain);

		deploymentsPage = new WizardProcessTaskAddDeploymentsPage("AddDeploymentsPage", deps);
		
		addPage(page);
		addPage(deploymentsPage);
		
		statusMainPage = new StatusWizardMainPage("Status");
		statusMainPage.setTitle("Status");
		statusMainPage.setDescription("Edit details of the Status.");
		
		try {
			statusOkPage = new WizardEventTemplatePage("OK Template", lighthouseDomain, WizardEventTemplatePage.EventType.OK);
			statusOkPage.setTitle("OK Template");
			statusOkPage.setDescription("Define details of OK Template");
			
			statusErrorPage = new WizardEventTemplatePage("Error Template", lighthouseDomain, WizardEventTemplatePage.EventType.ERROR);
			statusErrorPage.setTitle("Error Template");
			statusErrorPage.setDescription("Define details of Error Template");

			statusNotificationPage = new StatusWizardNotificationPage("Notification Channel", lighthouseDomain);
			statusNotificationPage.setTitle("Notification Channel");
			statusNotificationPage.setDescription("Edit the template for email notification");
			
			addPage(statusMainPage);
			addPage(statusOkPage);
			addPage(statusErrorPage);
			addPage(statusNotificationPage);
		}
		catch (Exception e) {
			new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "New Status Wizard", null, e	.getMessage(), MessageDialog.WARNING, new String[] { "OK" }, 0).open();
			//This RuntimeException will be catched by the UI framework and prevents this wizard from opening
			throw new RuntimeException("wizardkillerexception", null);
		}
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (getCurrentPageIndex()==1) {
			statusOkPage.setDeployments(deploymentsPage.getDeploymentSet());
			statusErrorPage.setDeployments(deploymentsPage.getDeploymentSet());
		}
		return super.getNextPage(page);
	}
	
	private int getCurrentPageIndex() {
		IWizardPage current = getContainer().getCurrentPage();
		if (current == page)
			return 0;
		if (current == deploymentsPage)
			return 1;
		if (current == statusMainPage)
			return 2;
		if (current == statusOkPage)
			return 3;
		if (current == statusErrorPage)
			return 4;
		if (current == statusNotificationPage)
			return 5;
		return -1;
	}
	
	@Override
	public boolean canFinish() {
		switch (getCurrentPageIndex()) {
			case 0:
				return page.isPageComplete();
			case 1:
				return deploymentsPage.isPageComplete();
			default:
				return statusMainPage.isPageComplete();
		}
	}

	@Override
	public boolean performFinish() {
		int pageIdx = getCurrentPageIndex(); 
		try {
			ProcessTask processTask = createProcessTask();
			
			DomainService ds = CommonBaseActivator.getPlugin().getDomainService();
			ds.persistProcessTask(lighthouseDomain, processTask);
			newProcessTask = processTask;
			
			if (pageIdx>0) {
				for (Deployment d : deploymentsPage.getDeploymentList())
					processTask.attachDeployment(d);
				
				ds.updateProcessTask(processTask);
				
				if (pageIdx>1) {
					com.mercatis.lighthouse3.domainmodel.status.Status status = StatusWizard.createStatus(processTask, statusMainPage, statusOkPage, statusErrorPage, statusNotificationPage);
		
					status.setCode(statusMainPage.getCode());
					status.setClearanceType(statusMainPage.getClearanceType());
					status.setStalenessIntervalInMsecs(statusMainPage.getStalenessInterval());
		
					CommonBaseActivator.getPlugin().getStatusService().persistStatus(status);
					PlatformUI.getWorkbench().getDecoratorManager().update(LighthouseStatusDecorator.id);
				}
			}
			if (page.getParentEntity() != null) {
				// update the parent component
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getParentEntity());
			} else {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getLighthouseDomain().getProcessTaskContainer());
			}
			return true;
		} catch (Exception ex) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			String message = ex.getMessage();
			if (message.contains("404")) {
				message = "Resource not found";
			} else if (message.contains("401")) {
				message = "Access to resource denied";
			}
			page.setErrorMessage(message);
		}
		return false;
	}

	protected ProcessTask createProcessTask() {
		ProcessTask processTask = new ProcessTask();
		processTask.setCode(page.getNewProcessTaskCode());
		processTask.setLongName(page.getNewProcessTaskDescription());

		ProcessTask parent = page.getParentEntity();
		if (parent != null) {
			parent.addSubEntity(processTask);
		}
		return processTask;
	}

	public ProcessTask getProcessTask() {
		return newProcessTask;
	}
}
