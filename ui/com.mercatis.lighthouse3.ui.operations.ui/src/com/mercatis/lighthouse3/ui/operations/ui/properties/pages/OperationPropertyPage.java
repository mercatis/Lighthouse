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
package com.mercatis.lighthouse3.ui.operations.ui.properties.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration;


public class OperationPropertyPage extends PropertyPage {

	private LighthouseDomain lighthouseDomain;
	private Text urlText;

	ModifyListener ml = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setValid(isValid());
			updateApplyButton();
			if(getContainer() != null) {
				getContainer().updateButtons();
			}
		}
	};

	@Override
	protected Control createContents(Composite parent) {
		lighthouseDomain = (LighthouseDomain) getElement();
		Composite client = new Composite(parent, SWT.NONE);
		client.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);

		Label urlLabel = new Label(client, SWT.NONE);
		urlLabel.setText("Operation URL");
		urlLabel.setFont(parent.getFont());

		urlText = new Text(client, SWT.BORDER);
		urlText.setLayoutData(data);
		urlText.setFont(parent.getFont());
		urlText.addModifyListener(ml);

		setValues();
		return client;
	}

	/**
	 * 
	 */
	private void setValues() {
		if (lighthouseDomain != null) {
			OperationConfiguration operationConfiguration = OperationBase.getOperationService().getOperationConfiguration(lighthouseDomain);
			if(operationConfiguration.existsOperationUrl()) {
				urlText.setText(operationConfiguration.getOperationUrl());
				setValid(false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		updateProject();
		return true;
	}

	/**
	 * 
	 */
	private void updateProject() {
		setProjectSettings();
	}

	/**
	 * 
	 */
	private void setProjectSettings() {
		OperationConfiguration operationConfiguration = OperationBase.getOperationService().getOperationConfiguration(lighthouseDomain);
		operationConfiguration.setOperationUrl(urlText.getText());
	}

	@Override
	public boolean isValid() {
		if (urlText.getText().equals("")) {
			setErrorMessage("URL must not be empty");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}
