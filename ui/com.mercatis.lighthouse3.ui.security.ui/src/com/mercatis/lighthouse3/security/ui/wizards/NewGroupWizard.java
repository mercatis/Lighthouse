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
/**
 * 
 */
package com.mercatis.lighthouse3.security.ui.wizards;

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
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.security.ui.wizards.pages.NewGroupWizardPropertiesPage;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class NewGroupWizard extends Wizard implements INewWizard {

	private LighthouseDomain lighthouseDomain;
	private NewGroupWizardPropertiesPage propertiesPage;
	private Group group;
	
	public NewGroupWizard() {
		setWindowTitle("Create Group");
	}
	
	public Group getGroup() {
		return group;
	}

	@Override
	public boolean performFinish() {
		try {
			group = new Group();
			group.setCode(propertiesPage.getCode());
			group.setLongName(propertiesPage.getLongName());
			group.setContact(propertiesPage.getContact());
			group.setContactEmail(propertiesPage.getContactEmail());
			group.setDescription(propertiesPage.getDescription());
			
			Security.getService().persistGroup(lighthouseDomain.getProject(), group);
		}
		catch (Exception e) {
			new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Create Group", null, e.getMessage(), MessageDialog.ERROR, new String[] { "OK" }, 0).open();
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof FormEditor) {
			FormEditor fe = (FormEditor) editor;
			IFormPage page = fe.getActivePageInstance();
			if (page.getEditorInput() instanceof GenericEditorInput<?>) {
				GenericEditorInput<?> editorInput = (GenericEditorInput<?>)page.getEditorInput();
				lighthouseDomain = editorInput.getDomain();
			}
		}
	}

	@Override
	public void addPages() {
		propertiesPage = new NewGroupWizardPropertiesPage();
		addPage(propertiesPage);
	}
}
