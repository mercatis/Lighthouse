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
package com.mercatis.lighthouse3.security.ui;

import org.eclipse.core.resources.IProject;
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
import com.mercatis.lighthouse3.ui.security.internal.Security;
import com.mercatis.lighthouse3.ui.security.internal.SecurityConfiguration;


public class SecurityPropertyPage extends PropertyPage {

	private IProject iProject;
	private Text urlText;
	private Text defaultGroupText;
	
	@Override
	protected Control createContents(Composite parent) {
		LighthouseDomain lighthouseDomain = (LighthouseDomain)getElement();
		iProject = lighthouseDomain.getProject();
		
		Composite client = new Composite(parent, SWT.NONE);
		client.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);

		Label urlLabel = new Label(client, SWT.NONE);
		urlLabel.setText("Security Server URL");
		urlLabel.setFont(parent.getFont());

		urlText = new Text(client, SWT.BORDER);
		urlText.setLayoutData(data);
		urlText.setFont(parent.getFont());
		urlText.addModifyListener(modifyListener);
		
		Label defaultGroupLabel = new Label(client, SWT.NONE);
		defaultGroupLabel.setText("Default user group");
		defaultGroupLabel.setFont(parent.getFont());
		
		defaultGroupText = new Text(client, SWT.BORDER);
		defaultGroupText.setLayoutData(data);
		defaultGroupText.setFont(parent.getFont());
		defaultGroupText.addModifyListener(modifyListener);

		setValues();
		return client;
	}
	
	private void setValues() {
		String serverUrl = Security.getDefault().getSecurityConfiguration(iProject).getServerUrl();
		if (serverUrl == null)
			serverUrl = "";
		
		if (urlText != null)
			urlText.setText(serverUrl);
		
		String defaultGroup = Security.getDefault().getSecurityConfiguration(iProject).getDefaultGroup();
		if (defaultGroup == null)
			defaultGroup = "";
		
		if (defaultGroupText != null)
			defaultGroupText.setText(defaultGroup);
	}
	
	ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setValid(isValid());
			updateApplyButton();
			if(getContainer() != null) {
				getContainer().updateButtons();
			}
		}
	};

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

	private void updateProject() {
		setProjectSettings();
	}

	private void setProjectSettings() {
		SecurityConfiguration securityConfiguration = Security.getDefault().getSecurityConfiguration(iProject);
		securityConfiguration.setServerUrl(urlText.getText());
		securityConfiguration.setDefaultGroup(defaultGroupText.getText());
	}

	@Override
	public boolean isValid() {
		if (urlText != null && urlText.getText().equals("")) {
			setErrorMessage("URL must not be empty");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}
