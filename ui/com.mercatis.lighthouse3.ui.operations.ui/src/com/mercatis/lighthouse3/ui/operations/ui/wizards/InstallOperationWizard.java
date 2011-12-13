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
package com.mercatis.lighthouse3.ui.operations.ui.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.model.Category;
import com.mercatis.lighthouse3.ui.operations.ui.OperationsUI;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.pages.InstallOperationWizardDeploymentSelectionPage;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.pages.InstallOperationWizardOperationSelectionPage;

public class InstallOperationWizard extends Wizard implements INewWizard {

	private LighthouseDomain lighthouseDomain;
	private Set<Deployment> selectedDeployments;
	private Set<Operation> selectedOperations;
	private InstallOperationWizardOperationSelectionPage listOperationsPage;
	private InstallOperationWizardDeploymentSelectionPage listDeploymentsPage;
	
	public InstallOperationWizard() {
		setWindowTitle("Install Operation");
	}

	@Override
	public boolean performFinish() {
		try {
			if (selectedDeployments == null) {
				selectedDeployments = listDeploymentsPage.getSelectedDeployments();
			}
			if (selectedOperations == null) {
				selectedOperations = listOperationsPage.getSelectedOperations();
			}
			for (Deployment parentDeployment : selectedDeployments) {
				for (Operation selectedOperation : selectedOperations) {
					OperationInstallation installation = new OperationInstallation();
					installation.setInstallationLocation(parentDeployment);
					installation.setInstalledOperation(selectedOperation);
					OperationBase.getOperationInstallationService().persist(installation);
				}
			}
		}
		catch (Exception e) {
			new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Install Operation", null, e.getMessage(), MessageDialog.ERROR, new String[] { "OK" }, 0).open();
			return false;
		}
		return true;
	}
	
	public void addOperation(Operation op) {
		if (selectedOperations == null) {
			selectedOperations = new HashSet<Operation>();
		}
		selectedOperations.add(op);
	}
	
	public void setLighthouseDomain(LighthouseDomain domain) {
		lighthouseDomain = domain;
	}
	
	public void addDeployment(Deployment dep) {
		if (selectedDeployments == null) {
			selectedDeployments = new HashSet<Deployment>();
		}
		selectedDeployments.add(dep);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection instanceof TreeSelection) {
			try {
				TreeSelection treeSelection = (TreeSelection) selection;
				for (TreePath path : treeSelection.getPaths()) {
					for (int i = 0; i < path.getSegmentCount(); i++) {
						Object obj = path.getSegment(i);
						if (obj instanceof Category<?>) {
							setLighthouseDomain(((Category<?>)obj).getLighthouseDomain());
						}
						if (obj instanceof Operation) {
							addOperation((Operation) obj);
						}
						if (obj instanceof LighthouseDomain) {
							setLighthouseDomain((LighthouseDomain) obj);
						}
						if (obj instanceof Deployment) {
							addDeployment((Deployment) obj);
						}
					}
				}
				if (treeSelection.getPaths().length == 0 || (selectedDeployments == null && selectedOperations == null)) {
					tryToInitFromActiveEditor();
				}
			}
			catch (Exception e) {
				OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
			}
		} else {
			tryToInitFromActiveEditor();
		}
	}
	
	private void tryToInitFromActiveEditor() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof FormEditor) {
			FormEditor fe = (FormEditor) editor;
			IFormPage page = fe.getActivePageInstance();
			if (page.getEditorInput() instanceof GenericEditorInput<?>) {
				GenericEditorInput<?> editorInput = (GenericEditorInput<?>)page.getEditorInput();
				Object obj = editorInput.getEntity();
				if (obj instanceof Deployment) {
					addDeployment((Deployment) obj);
				}
				setLighthouseDomain(editorInput.getDomain());
			}
		}
	}

	@Override
	public void addPages() {
		if (selectedOperations == null) {
			listOperationsPage = new InstallOperationWizardOperationSelectionPage("Select Operatrion", lighthouseDomain);
			listOperationsPage.setTitle("Install Operation");
			listOperationsPage.setDescription("Select Operation to install on Deployment" + (selectedDeployments.size() > 1 ? "s" : ""));
			addPage(listOperationsPage);
		}
		if (selectedDeployments == null) {
			listDeploymentsPage = new InstallOperationWizardDeploymentSelectionPage("Select Deployment", lighthouseDomain);
			listDeploymentsPage.setTitle("Install Operation");
			listDeploymentsPage.setDescription("Select Deployment on wich the Operation" + (selectedOperations.size() > 1 ? "s" : "") + " should be installed");
			addPage(listDeploymentsPage);
		}
	}
}
