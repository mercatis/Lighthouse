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
package com.mercatis.lighthouse3.ui.environment.wizards.pages;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.providers.DeploymentsOnlyContentProvider;
import com.mercatis.lighthouse3.ui.environment.providers.GenericLabelComparator;
import com.mercatis.lighthouse3.ui.environment.providers.SoftwareComponentsOnlyContentProvider;

public class WizardNewDeploymentPage extends WizardPage {

	private Button browseProjectButton;

	private ElementTreeSelectionDialog browseProjectDialog;

	private Button browseSoftwareComponentButton;

	private ElementListSelectionDialog browseSoftwareComponentDialog;

	private WorkbenchLabelProvider labelProvider;

	private LighthouseDomain lighthouseDomain;

	private Location location;

	private Combo locationCombo;

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			setPageComplete(validatePage());
		}
	};

	private Text projectText;

	private SoftwareComponent softwareComponent;

	private Text softwareComponentText;

	public WizardNewDeploymentPage(String pageName) {
		super(pageName);
		labelProvider = new WorkbenchLabelProvider();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		initUI(composite);
		updateUI();

		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}

	public String getLocationText() {
		return locationCombo.getText();
	}

	public SoftwareComponent getSoftwareComponent() {
		return softwareComponent;
	}

	private void initUI(final Composite parent) {
		parent.setLayout(new FillLayout());
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		Label projectLabel = new Label(composite, SWT.NONE);
		projectLabel.setText("Domain:");

		projectText = new Text(composite, SWT.BORDER);
		projectText.setEditable(false);
		projectText.setEnabled(false);
		projectText.setLayoutData(gd);
		projectText.addModifyListener(modifyListener);

		browseProjectButton = new Button(composite, SWT.PUSH);
		browseProjectButton.setText("Browse...");
		browseProjectButton.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				prepareBrowseProjectDialog(parent);
				browseProjectDialog.setInitialSelection(lighthouseDomain);
				if (browseProjectDialog.open() == Window.OK) {
					Object result = browseProjectDialog.getFirstResult();
					if (result instanceof LighthouseDomain) {
						lighthouseDomain = (LighthouseDomain) result;
					}

					if (result instanceof Location) {
						location = (Location) result;
						lighthouseDomain = location.getDeploymentContainer().getLighthouseDomain();
					}

					updateUI();
				}
			}
		});

		Label locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText("Location:");

		locationCombo = new Combo(composite, SWT.BORDER);
		locationCombo.addModifyListener(modifyListener);
		locationCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		// Fills blank space
		new Label(composite, SWT.NONE);

		Label componentLabel = new Label(composite, SWT.NONE);
		componentLabel.setText("Component:");

		softwareComponentText = new Text(composite, SWT.BORDER);
		softwareComponentText.setEditable(false);
		softwareComponentText.setEnabled(false);
		softwareComponentText.setLayoutData(gd);
		softwareComponentText.addModifyListener(modifyListener);

		browseSoftwareComponentButton = new Button(composite, SWT.PUSH);
		browseSoftwareComponentButton.setText("Browse...");
		browseSoftwareComponentButton.addListener(SWT.MouseUp, new Listener() {

			public void handleEvent(Event event) {
				ElementTreeSelectionDialog browseParentDialog = new ElementTreeSelectionDialog(parent.getShell(),
						new WorkbenchLabelProvider(), getContentProvider());
				browseParentDialog.setTitle("Component Selection");
				browseParentDialog.setMessage("Choose a component:");
				browseParentDialog.setBlockOnOpen(true);
				browseParentDialog.setAllowMultiple(false);

				browseParentDialog.setInput(lighthouseDomain);
				if (softwareComponent != null)
					browseParentDialog.setInitialSelections(new Object[] { softwareComponent });

				if (browseParentDialog.open() == Window.OK) {
					Object result = browseParentDialog.getFirstResult();
					if (result != null && result instanceof SoftwareComponent) {
						softwareComponent = (SoftwareComponent) result;
						softwareComponentText.setText(new WorkbenchLabelProvider().getText(softwareComponent));
					}
				}
				setPageComplete(validatePage());
			}
		});
	}

	protected void prepareBrowseProjectDialog(Composite parent) {
		browseProjectDialog = new ElementTreeSelectionDialog(parent.getShell(), new WorkbenchLabelProvider(),
				new DeploymentsOnlyContentProvider());
		browseProjectDialog.setTitle("Select domain");
		browseProjectDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		browseProjectDialog.setBlockOnOpen(true);
		browseProjectDialog.setAllowMultiple(false);
		browseProjectDialog.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof Deployment)
					return false;

				return true;
			}
		});
	}

	protected void prepareBrowseSoftwareComponentDialog(Composite parent) {
		browseSoftwareComponentDialog = new ElementListSelectionDialog(parent.getShell(), new WorkbenchLabelProvider());
		browseSoftwareComponentDialog.setTitle("Select a Software Component");
		browseSoftwareComponentDialog.setBlockOnOpen(true);
	}

	public void setLighthouseDomain(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setSoftwareComponent(SoftwareComponent softwareComponent) {
		this.softwareComponent = softwareComponent;
	}

	protected void updateUI() {
		projectText.setText("");
		softwareComponentText.setText("");
		locationCombo.removeAll();
		browseSoftwareComponentButton.setEnabled(false);

		if (lighthouseDomain == null)
			return;

		// update project
		projectText.setText(labelProvider.getText(lighthouseDomain));

		// update locations
		List<Location> locations = lighthouseDomain.getDeploymentContainer().getLocations();
		Collections.sort(locations, GenericLabelComparator.getInstance());
		
		for (Location location : locations) {
			locationCombo.add(labelProvider.getText(location));
		}
		if (location != null) {
			locationCombo.setText(labelProvider.getText(location));
		}

		// update components
		browseSoftwareComponentButton.setEnabled(true);
		if (softwareComponent != null) {
			softwareComponentText.setText(labelProvider.getText(softwareComponent));
		}
	}

	private boolean validatePage() {
		if (lighthouseDomain == null) {
			setMessage("Please select a project.");
			return false;
		}

		if (locationCombo.getText().length() == 0) {
			setMessage("Please select or enter a location.");
			return false;
		}

		if (softwareComponent == null) {
			setMessage("Please select a software component.");
			return false;
		}

		setMessage(null);
		return true;
	}

	protected ITreeContentProvider getContentProvider() {
		return new SoftwareComponentsOnlyContentProvider();
	}		
	
}
