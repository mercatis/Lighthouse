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
package com.mercatis.lighthouse3.ui.operations.ui.wizards.pages;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.providers.DeploymentsOnlyContentProvider;


public class InstallOperationWizardDeploymentSelectionPage extends WizardPage {
	
	private LighthouseDomain lighthouseDomain;
	private TreeViewer deploymentsTree;

	public InstallOperationWizardDeploymentSelectionPage(String pageName, LighthouseDomain lighthouseDomain) {
		super(pageName);
		this.lighthouseDomain = lighthouseDomain;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);
	
		composite.setLayout(new GridLayout(2, false));
		
		initUI(composite);
		
		setPageComplete(validatePage());
		setControl(composite);
	}
	
	private void initUI(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		deploymentsTree = new TreeViewer(composite);
		deploymentsTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		deploymentsTree.setContentProvider(new DeploymentsOnlyContentProvider());
		deploymentsTree.setLabelProvider(new WorkbenchLabelProvider());
		deploymentsTree.setInput(lighthouseDomain);
		deploymentsTree.addSelectionChangedListener(listener);
	}

	private boolean validatePage() {
		if (getSelectedDeployments() == null) {
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private ISelectionChangedListener listener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			setPageComplete(validatePage());
		}
	};

	public Set<Deployment> getSelectedDeployments() {
		Object[] selected = getSelection();
		Set<Deployment> selectedOperations = new HashSet<Deployment>();
		for (Object obj : selected) {
			if (obj instanceof Deployment) {
				selectedOperations.add((Deployment) obj);
			} else if (obj instanceof Location) {
				selectedOperations.addAll(((Location)obj).getDeployments());
			}
		}
		return selectedOperations;
	}
	
	private Object[] getSelection() {
		ISelection selection = deploymentsTree.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object[] selected = ((IStructuredSelection)selection).toArray();
			return selected;
		}
		return null;
	}
}
