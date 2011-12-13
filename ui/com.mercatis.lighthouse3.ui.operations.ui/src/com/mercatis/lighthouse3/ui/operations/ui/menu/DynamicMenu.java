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
package com.mercatis.lighthouse3.ui.operations.ui.menu;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.OperationCallWizard;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class DynamicMenu extends ContributionItem {

	public DynamicMenu() {
	}

	/**
	 * @param id
	 */
	public DynamicMenu(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets
	 * .Menu, int)
	 */
	public void fill(Menu menu, int index) {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof Deployment) {
				Deployment selectedDeployment = (Deployment) ((StructuredSelection) selection).getFirstElement();
				List<OperationInstallation> availableOps = OperationBase.getOperationInstallationService().findAtDeployment(selectedDeployment);

				// remove all operation installations that the current user must not execute
				for (Iterator<OperationInstallation> it = availableOps.iterator(); it.hasNext();) {
					OperationInstallation installation = it.next();
					if (!CodeGuard.hasRole(Role.OPERATION_EXECUTE, installation))
						it.remove();
				}
				
				MenuItem subMenuItem = new MenuItem(menu, SWT.CASCADE, index);
				subMenuItem.setText("Execute");
				subMenuItem.setImage(ImageDescriptor.createFromURL(getClass().getResource("/icons/operationcall.png"))
						.createImage());

				Menu submenu = new Menu(menu);
				subMenuItem.setMenu(submenu);

				if (!availableOps.isEmpty()) {
					for (OperationInstallation operationInstallation : availableOps) {
						final OperationInstallationWrapper loader = new OperationInstallationWrapper(operationInstallation);

						MenuItem item = new MenuItem(submenu, SWT.PUSH);
						item.setEnabled(loader.getOperation() != null);
						item.setText(LabelConverter.getLabel(loader));
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(org.eclipse.swt.widgets.Event event) {
								OperationCallWizard wizard = new OperationCallWizard(loader);
								WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(), wizard);
								dialog.open();
							}
						});
					}
				}
				subMenuItem.setEnabled(!availableOps.isEmpty());
			}
		}
	}
}
