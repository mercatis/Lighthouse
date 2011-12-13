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
package com.mercatis.lighthouse3.base.ui.wizards;

import java.lang.reflect.ParameterizedType;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

public abstract class AbstractWizardParentSelectionPage<ParentType extends CodedDomainModelEntity> extends WizardPage {

	private Button browseParentButton;

	private Button browseProjectButton;

	private LighthouseDomain lighthouseDomain;

	private ParentType parentEntity;

	private Class<ParentType> parentEntityClass;

	private Text parentEntityText;

	private Text projectText;

	@SuppressWarnings("unchecked")
	public AbstractWizardParentSelectionPage(String name) {
		super(name);
		parentEntityClass = (Class<ParentType>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout(3, false));

		initParentUI(composite);
		initUI(composite);
		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	protected abstract ITreeContentProvider getContentProvider();

	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}

	public ParentType getParentEntity() {
		return parentEntity;
	}

	private void initParentUI(final Composite parent) {
		Label projectLabel = new Label(parent, SWT.NONE);
		projectLabel.setText("Domain:");
		projectLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		projectText = new Text(parent, SWT.BORDER);
		projectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		projectText.setEnabled(false);
		if (lighthouseDomain != null)
			projectText.setText(new WorkbenchLabelProvider().getText(lighthouseDomain));

		browseProjectButton = new Button(parent, SWT.PUSH);
		browseProjectButton.setText("Browse...");
		browseProjectButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		browseProjectButton.addListener(SWT.MouseUp, new Listener() {

			@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {
				ElementTreeSelectionDialog browseProjectDialog = new ElementTreeSelectionDialog(parent.getShell(),
						new WorkbenchLabelProvider(), getContentProvider());
				browseProjectDialog.setTitle("Project Selection");
				browseProjectDialog.setMessage("Choose domain:");
				browseProjectDialog.setBlockOnOpen(true);
				browseProjectDialog.setAllowMultiple(false);
				browseProjectDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());

				if (lighthouseDomain != null)
					browseProjectDialog.setInitialSelection(lighthouseDomain);

				if (browseProjectDialog.open() == Window.OK) {
					Object result = browseProjectDialog.getFirstResult();
					if (result instanceof LighthouseDomain) {
						lighthouseDomain = (LighthouseDomain) result;
						projectText.setText(new WorkbenchLabelProvider().getText(lighthouseDomain));
					}

					if (parentEntityClass.isInstance(result)) {
						parentEntity = (ParentType) result;
						parentEntityText.setText(new WorkbenchLabelProvider().getText(parentEntity));
						lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(result);
						projectText.setText(new WorkbenchLabelProvider().getText(lighthouseDomain));
					}
				}

				browseParentButton.setEnabled(lighthouseDomain != null);
				setPageComplete(validatePage());
			}
		});

		Label parentEntityLabel = new Label(parent, SWT.NONE);
		parentEntityLabel.setText("Parent Entity:");
		parentEntityLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		parentEntityText = new Text(parent, SWT.BORDER);
		parentEntityText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		parentEntityText.setEnabled(false);
		if (parentEntity != null)
			parentEntityText.setText(new WorkbenchLabelProvider().getText(parentEntity));

		browseParentButton = new Button(parent, SWT.PUSH);
		browseParentButton.setText("Browse...");
		browseParentButton.setEnabled(lighthouseDomain != null);
		browseParentButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		browseParentButton.addListener(SWT.MouseUp, new Listener() {

			@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {
				ElementTreeSelectionDialog browseParentDialog = new ElementTreeSelectionDialog(parent.getShell(),
						new WorkbenchLabelProvider(), getContentProvider());
				browseParentDialog.setTitle("Parent Selection");
				browseParentDialog.setMessage("Choose a parent:");
				browseParentDialog.setBlockOnOpen(true);
				browseParentDialog.setAllowMultiple(false);

				browseParentDialog.setInput(lighthouseDomain);
				if (parentEntity != null)
					browseParentDialog.setInitialSelections(new Object[] { parentEntity });

				if (browseParentDialog.open() == Window.OK) {
					Object result = browseParentDialog.getFirstResult();
					if (parentEntityClass.isInstance(result)) {
						parentEntity = (ParentType) result;
						parentEntityText.setText(new WorkbenchLabelProvider().getText(parentEntity));
					}
				}
				setPageComplete(validatePage());
			}
		});

		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
	}

	protected abstract void initUI(Composite parent);

	public void setLighthouseDomain(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	public void setParentEntity(ParentType parentEntity) {
		this.parentEntity = parentEntity;
	}

	protected boolean validatePage() {
		if (lighthouseDomain == null) {
			setMessage("Choose domain.", ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Use this method to prefill the code to provide duplicates
	 * @param parentEntity
	 * @return suffix for the new code
	 */
	public static String getPrefilledCode(CodedDomainModelEntity parentEntity) {
		if (parentEntity == null)
			return "";
		return parentEntity.getCode() + "-";
	}
}