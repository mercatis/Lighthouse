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
package com.mercatis.lighthouse3.security.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.editors.pages.AbstractContextBasedPermissionEditorPage;
import com.mercatis.lighthouse3.security.ui.model.GroupAccessor;
import com.mercatis.lighthouse3.security.ui.model.UserAccessor;
import com.mercatis.lighthouse3.security.ui.wizards.NewGroupWizard;
import com.mercatis.lighthouse3.security.ui.wizards.NewUserWizard;


public class AddCreateAccessorHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractContextBasedPermissionEditorPage<?> page = getActiveEditorFormPage();
		if (page != null) {
			Class<?> accessorClass = page.getEditedAccessorClass();
			if (accessorClass.equals(UserAccessor.class)) {
				NewUserWizard wizard = new NewUserWizard();
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				if (dialog.open() == Dialog.OK) {
					User user = wizard.getUser();
					UserAccessor userAccessor = new UserAccessor(page.getLighthouseDomain(), user);
					page.addAccessor(userAccessor);
				}
			} else if (accessorClass.equals(GroupAccessor.class)) {
				NewGroupWizard wizard = new NewGroupWizard();
				WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
				if (dialog.open() == Dialog.OK) {
					Group group = wizard.getGroup();
					GroupAccessor groupAccessor = new GroupAccessor(page.getLighthouseDomain(), group);
					page.addAccessor(groupAccessor);
				}
			}
		}
		return null;
	}
	
	private AbstractContextBasedPermissionEditorPage<?> getActiveEditorFormPage() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof FormEditor) {
			FormEditor fe = (FormEditor) editor;
			IFormPage page = fe.getActivePageInstance();
			if (page instanceof AbstractContextBasedPermissionEditorPage<?>) {
				return (AbstractContextBasedPermissionEditorPage<?>) page;
			}
		}
		return null;
	}
}
