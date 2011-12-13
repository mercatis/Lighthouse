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
package com.mercatis.lighthouse3.status.ui.properties.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration;


public class StatusPropertyPage extends PropertyPage {

	private LighthouseDomain lighthouseDomain;
	private Text jmsUpdateStatusTopicField;
	private Text notifiactionTemplateFolder;
	private Text statusHistoryPageSize;

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

		Label statusHistoryPageSizeLabel = new Label(client, SWT.NONE);
		statusHistoryPageSizeLabel.setText("Status History Page Size");
		statusHistoryPageSizeLabel.setFont(parent.getFont());

		statusHistoryPageSize = new Text(client, SWT.BORDER);
		statusHistoryPageSize.setLayoutData(data);
		statusHistoryPageSize.setFont(parent.getFont());
		statusHistoryPageSize.addModifyListener(ml);

		Label jmsEventUpdateTopicLabel = new Label(client, SWT.NONE);
		jmsEventUpdateTopicLabel.setText("Status Change Topic");
		jmsEventUpdateTopicLabel.setFont(parent.getFont());

		jmsUpdateStatusTopicField = new Text(client, SWT.BORDER);
		jmsUpdateStatusTopicField.setLayoutData(data);
		jmsUpdateStatusTopicField.setFont(parent.getFont());
		jmsUpdateStatusTopicField.addModifyListener(ml);

		Label notificationTemplateLabel = new Label(client, SWT.NONE);
		notificationTemplateLabel.setText("Template folder");
		notificationTemplateLabel.setFont(parent.getFont());

		notifiactionTemplateFolder = new Text(client, SWT.BORDER);
		notifiactionTemplateFolder.setLayoutData(data);
		notifiactionTemplateFolder.setFont(parent.getFont());
		notifiactionTemplateFolder.addModifyListener(ml);

		Button selectDirectory = new Button(client, SWT.PUSH);
		selectDirectory.setText("Browse...");
		selectDirectory.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay()
						.getActiveShell());
				directoryDialog.setMessage("Please selecte a directory for templates.");
				directoryDialog.setText("Select directory");
				directoryDialog.setFilterPath(notifiactionTemplateFolder.getText());
				String directory = directoryDialog.open();
				if (directory != null) {
					notifiactionTemplateFolder.setText(directory);
				}
			}
		});

		setValues();
		return client;
	}

	private void setValues() {
		if (lighthouseDomain != null) {
			jmsUpdateStatusTopicField.setText(CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
					.getStatusPublicationTopic());
			notifiactionTemplateFolder.setText(CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
					.getStatusNotificationTemplateFolder());
			statusHistoryPageSize.setText(Integer.toString(CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(
					lighthouseDomain).getStatusPageSize()));
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
		StatusConfiguration statusConfiguration = CommonBaseActivator.getPlugin().getStatusService()
				.getStatusConfiguration(lighthouseDomain);
		statusConfiguration.setStatusPublicationTopic(jmsUpdateStatusTopicField.getText());
		statusConfiguration.setStatusNotificationTemplateFolder(notifiactionTemplateFolder.getText());
		statusConfiguration.setStatusPageSize(Integer.parseInt(statusHistoryPageSize.getText()));
	}

	@Override
	public boolean isValid() {
		if (jmsUpdateStatusTopicField.getText().equals("")) {
			setErrorMessage("Update Topic must not be empty");
			return false;
		}
		if (notifiactionTemplateFolder.getText().equals("")) {
			setErrorMessage("Template folder must not be empty");
			return false;
		}
		try {
			Integer.parseInt(statusHistoryPageSize.getText());
		}
		catch (NumberFormatException e) {
			setErrorMessage("Cannot parse page size \"" + statusHistoryPageSize.getText() + "\" to integer");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}
