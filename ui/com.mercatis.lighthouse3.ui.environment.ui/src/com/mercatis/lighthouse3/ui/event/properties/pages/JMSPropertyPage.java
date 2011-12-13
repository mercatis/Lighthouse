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
package com.mercatis.lighthouse3.ui.event.properties.pages;

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

import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.base.services.EventConfiguration;
import com.mercatis.lighthouse3.ui.event.base.services.EventService;


public class JMSPropertyPage extends PropertyPage {

	private Text jmsProviderUrlField;
	private Text jmsProviderField;
	private Text jmsUsernameField;
	private Text jmsPasswordField;

	private LighthouseDomain lighthouseDomain;

	ModifyListener ml = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setValid(isValid());
		}
	};
	
	@Override
	protected Control createContents(Composite parent) {
		lighthouseDomain = (LighthouseDomain) getElement();
		Composite client = new Composite(parent, SWT.NONE);
		client.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);

		Label jmsProviderUrlLabel = new Label(client, SWT.NONE);
		jmsProviderUrlLabel.setText("Server URL");
		jmsProviderUrlLabel.setFont(client.getFont());

		jmsProviderUrlField = new Text(client, SWT.BORDER);
		jmsProviderUrlField.setLayoutData(data);
		jmsProviderUrlField.setFont(client.getFont());
		jmsProviderUrlField.addModifyListener(ml);

		Label jmsProviderLabel = new Label(client, SWT.NONE);
		jmsProviderLabel.setText("Provider Class");
		jmsProviderLabel.setFont(client.getFont());

		jmsProviderField = new Text(client, SWT.BORDER);
		jmsProviderField.setLayoutData(data);
		jmsProviderField.setFont(client.getFont());
		jmsProviderField.addModifyListener(ml);

		Label usernameLabel = new Label(client, SWT.NONE);
		usernameLabel.setText("Username");
		usernameLabel.setFont(client.getFont());

		jmsUsernameField = new Text(client, SWT.BORDER);
		jmsUsernameField.setLayoutData(data);
		jmsUsernameField.setFont(client.getFont());
		jmsUsernameField.addModifyListener(ml);

		Label passwordLabel = new Label(client, SWT.NONE);
		passwordLabel.setText("Password");
		passwordLabel.setFont(client.getFont());

		jmsPasswordField = new Text(client, SWT.BORDER | SWT.PASSWORD);
		jmsPasswordField.setLayoutData(data);
		jmsPasswordField.setFont(client.getFont());
		jmsPasswordField.addModifyListener(ml);

		setValues();
		return client;
	}

	private void setValues() {
		if (lighthouseDomain != null) {
			jmsProviderUrlField.setText(CommonBaseActivator.getPlugin().getEventService()
					.getEventConfiguration(lighthouseDomain).getJmsBrokerUrl());
			jmsProviderField.setText(CommonBaseActivator.getPlugin().getEventService()
					.getEventConfiguration(lighthouseDomain).getJmsProviderClass());
			jmsUsernameField.setText(CommonBaseActivator.getPlugin().getEventService()
					.getEventConfiguration(lighthouseDomain).getJmsUser());
			jmsPasswordField.setText(CommonBaseActivator.getPlugin().getEventService()
					.getEventConfiguration(lighthouseDomain).getJmsPassword());
		}
	}

	protected void performDefaults() {
		setValues();
	}

	public boolean performOk() {
		updateProject();
		return true;
	}

	private void updateProject() {
		setProjectSettings();
	}

	private void setProjectSettings() {
		EventService eventService = CommonBaseActivator.getPlugin().getEventService();
		EventConfiguration eventConfiguration = eventService.getEventConfiguration(lighthouseDomain);
		eventConfiguration.setJmsProviderClass(jmsProviderField.getText());
		eventConfiguration.setJmsBrokerUrl(jmsProviderUrlField.getText());
		eventConfiguration.setJmsUser(jmsUsernameField.getText());
		eventConfiguration.setJmsPassword(jmsPasswordField.getText());
	}

	@Override
	public boolean isValid() {
		if (jmsProviderUrlField.getText().equals("")) {
			setErrorMessage("Server URL must not be empty");
			return false;
		}
		if (jmsProviderField.getText().equals("")) {
			setErrorMessage("Provider Class must not be empty");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}
