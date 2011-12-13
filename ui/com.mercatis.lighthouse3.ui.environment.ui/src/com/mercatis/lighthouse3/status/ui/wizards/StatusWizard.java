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
package com.mercatis.lighthouse3.status.ui.wizards;

import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotification;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.status.ui.LighthouseStatusDecorator;
import com.mercatis.lighthouse3.status.ui.wizards.pages.StatusWizardMainPage;
import com.mercatis.lighthouse3.status.ui.wizards.pages.StatusWizardNotificationPage;
import com.mercatis.lighthouse3.status.ui.wizards.pages.WizardEventTemplatePage;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;

public class StatusWizard extends Wizard implements INewWizard {

	private StatusCarrier statusCarrier;
	private StatusWizardMainPage mainPage;
	private WizardEventTemplatePage okPage;
	private WizardEventTemplatePage errorPage;
	private StatusWizardNotificationPage notificationPage;

	public StatusWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("New Status");
	}

	public StatusWizard(Status status) {
		setNeedsProgressMonitor(true);
		setWindowTitle("Edit Status");
	}
	
	public StatusWizard(StatusCarrier carrier) {
		this();
		setStatusCarrier(carrier);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj == null) {
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editor instanceof FormEditor) {
				FormEditor fe = (FormEditor) editor;
				IFormPage page = fe.getActivePageInstance();
				if (page.getEditorInput() instanceof GenericEditorInput<?>) {
					obj = ((GenericEditorInput<?>)page.getEditorInput()).getEntity();
				}
			}
		} else
			setStatusCarrier(obj);
	}
	
	private void setStatusCarrier(Object obj) {
		if (obj instanceof StatusCarrier) {
			statusCarrier = (StatusCarrier) obj;
		} else if (obj instanceof StatusEditingObject) {
			statusCarrier = (StatusCarrier)((StatusEditingObject)obj).getStatus().getContext();
		} else {
			throw new RuntimeException("Status cannot be added to selected carrier");
		}
	}

	public void addPages() {
		Set<Deployment> deps = statusCarrier.getAssociatedDeployments();
		if (deps.size()==0) {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "New Status Wizard", "The selected "+statusCarrier.getRootElementName()+" does not have any deployment.\n\nUnable to create a status without a deployment.");
			//This RuntimeException will be catched by the UI framework and prevents this wizard from opening
			throw new RuntimeException("wizardkillerexception", null);
		}
		LighthouseDomain domain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(deps.iterator().next().getLighthouseDomain());
		
		mainPage = new StatusWizardMainPage("Status", statusCarrier);
		mainPage.setTitle("Status");
		mainPage.setDescription("Edit details of the Status.");
		
		okPage = new WizardEventTemplatePage("OK Template", domain, WizardEventTemplatePage.EventType.OK);
		okPage.setDeployments(statusCarrier.getAssociatedDeployments());
		okPage.setTitle("OK Template");
		okPage.setDescription("Define details of OK Template");
		
		errorPage = new WizardEventTemplatePage("Error Template", domain, WizardEventTemplatePage.EventType.ERROR);
		errorPage.setDeployments(statusCarrier.getAssociatedDeployments());
		errorPage.setTitle("Error Template");
		errorPage.setDescription("Define details of Error Template");

		notificationPage = new StatusWizardNotificationPage("Notification Channel", CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(statusCarrier));
		notificationPage.setTitle("Notification Channel");
		notificationPage.setDescription("Edit the template for email notification");
		
		addPage(mainPage);
		addPage(okPage);
		addPage(errorPage);
		addPage(notificationPage);
	}

	@Override
	public boolean performFinish() {
		try {
			Status status = createStatus(statusCarrier, mainPage, okPage, errorPage, notificationPage);

			CommonBaseActivator.getPlugin().getStatusService().persistStatus(status);
			PlatformUI.getWorkbench().getDecoratorManager().update(LighthouseStatusDecorator.id);
			return true;
		}
		catch (Exception e) {
			new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Create Status", null, e.getMessage(), MessageDialog.ERROR, new String[] { "OK" }, 0).open();
			return false;
		}
	}
	
	public static Status createStatus(StatusCarrier sc, StatusWizardMainPage swmp, WizardEventTemplatePage wetpOk, WizardEventTemplatePage wetpErr, StatusWizardNotificationPage swnp) throws Exception {
		Status status = new Status();
		status.setContext(sc);

		status.setCode(swmp.getCode());
		status.setClearanceType(swmp.getClearanceType());
		status.setStalenessIntervalInMsecs(swmp.getStalenessInterval());

		//For better validating the TemplatePages add the templates to the status themselves during validating
		status.setOkTemplate(wetpOk.getTemplate());
		status.setErrorTemplate(wetpErr.getTemplate());
		
		if (status.getChangeNotificationChannels() != null) {
			StatusChangeNotificationChannel<?, ?>[] channels = status.getChangeNotificationChannels().toArray(new StatusChangeNotificationChannel<?, ?>[0]);
			for (StatusChangeNotificationChannel<?, ?> channel : channels) {
				status.detachChangeNotificationChannel(channel);
			}
		}
		
		if (swnp.isNotificationEnabled()) {
			EMailNotification enoty = ((EMailNotification)swnp.getNotificationChannel());
			if (enoty.getRecipients().trim().length() == 0) {
				throw new Exception("No recepients given in notification page.");
			}
			if (enoty.getBodyMimeType().trim().length() == 0) {
				throw new Exception("No mime type given in notification page.");
			}
			if (enoty.getBodyTemplate().trim().length() == 0) {
				throw new Exception("No body template given in notification page.");
			}
			if (enoty.getTitleTemplate().trim().length() == 0) {
				throw new Exception("No title template given in notification page.");
			}
			status.attachChangeNotificationChannel(swnp.getNotificationChannel());
		}
		return status;
	}
}
