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
package com.mercatis.lighthouse3.status.ui.wizards.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.wizards.AbstractWizardPage;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.service.StatusConfiguration;


public class WizardNewLighthouseDomainStatusPage extends AbstractWizardPage {

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private Text jmsTopic;
	private String jmsTopicValue;
	
	private Text notificationTemplateFolder;
	private String notificationTemplateFolderValue;
	
	private String historyPageSizeValue;
	private String historyPageNoValue;

	@Override
	public void prefillProperty(Object key, Object value) {
		if (key != null && value != null) {
			if (key.equals("DOMAIN_STATUS_PUBLICATION_TOPIC")) {
				jmsTopicValue = (String) value;
			} else if (key.equals("DOMAIN_STAUTS_NOTIFICATION_TEMPLATE_FOLDER")) {
				notificationTemplateFolderValue = (String) value;
			} else if (key.equals("DOMAIN_STATUS_HISTORY_PAGE_SIZE")) {
				historyPageSizeValue = (String) value;
			} else if (key.equals("DOMAIN_STATUS_HISTORY_PAGE_NO")) {
				historyPageNoValue = (String) value;
			}
		}
	}

	/**
	 * @param pageName
	 */
	public WizardNewLighthouseDomainStatusPage() {
		super("Status Page");
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.wizards.pages.AbstractWizardPage#setPropertiesFor(java.lang.Object)
	 */
	@Override
	public void setPropertiesFor(Object o) {
		if (o instanceof LighthouseDomain) {
			LighthouseDomain lighthouseDomain = (LighthouseDomain) o;
			StatusConfiguration statusConfiguration = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain);
			statusConfiguration.setStatusPublicationTopic(jmsTopic.getText());
			statusConfiguration.setStatusNotificationTemplateFolder(notificationTemplateFolder.getText());
			if (historyPageNoValue != null) {
				statusConfiguration.setStatusPageNo(Integer.parseInt(historyPageNoValue));
			} else {
				statusConfiguration.setStatusPageNo(1);
			}
			if (historyPageSizeValue != null) {
				statusConfiguration.setStatusPageSize(Integer.parseInt(historyPageSizeValue));
			} else {
				statusConfiguration.setStatusPageSize(10);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;

		Group jmsGroup = new Group(composite, SWT.NONE);
		jmsGroup.setText("JMS settings");
		GridLayout jmsLayout = new GridLayout();
		jmsLayout.numColumns = 2;
		jmsGroup.setLayout(jmsLayout);
		jmsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label jmsTopicLabel = new Label(jmsGroup, SWT.NONE);
		jmsTopicLabel.setText("Status Change Topic");
		jmsTopicLabel.setFont(parent.getFont());

		jmsTopic = new Text(jmsGroup, SWT.BORDER);
		jmsTopic.setLayoutData(data);
		jmsTopic.setFont(parent.getFont());
		jmsTopic.setText(jmsTopicValue!=null ? jmsTopicValue : "com.mercatis.lighthouse3.service.statusmonitor.statuschanges");
		jmsTopic.addListener(SWT.Modify, modifyListener);
		jmsTopic.setFocus();
		
		Group notificationGroup = new Group(composite, SWT.NONE);
		notificationGroup.setText("Status template settings");
		GridLayout templateLayout = new GridLayout();
		templateLayout.numColumns = 3;
		notificationGroup.setLayout(templateLayout);
		notificationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label statusTemplateLabel = new Label(notificationGroup, SWT.NONE);
		statusTemplateLabel.setText("EMail Notification template folder");
		statusTemplateLabel.setFont(parent.getFont());

		notificationTemplateFolder = new Text(notificationGroup, SWT.BORDER);
		notificationTemplateFolder.setLayoutData(data);
		notificationTemplateFolder.setFont(parent.getFont());
		notificationTemplateFolder.setText(notificationTemplateFolderValue!=null ? notificationTemplateFolderValue : CommonBaseActivator.getPlugin().getDefaultTemplateDir().getAbsolutePath());
		notificationTemplateFolder.addListener(SWT.Modify, modifyListener);

		Button selectDirectory = new Button(notificationGroup, SWT.PUSH);
		selectDirectory.setText("Browse...");
		selectDirectory.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
				directoryDialog.setMessage("Please select a directory for templates.");
				directoryDialog.setText("Select directory");
				directoryDialog.setFilterPath(notificationTemplateFolder.getText());
				String directory = directoryDialog.open();
				if (directory != null) {
					notificationTemplateFolder.setText(directory);
				}
			}
		});
		
		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	private boolean validatePage() {
		if (jmsTopic.getText().length() == 0) {
			setErrorMessage(null);
			setMessage("Please enter a JMS topic.");
			return false;
		}
		if (notificationTemplateFolder.getText().length() == 0) {
			setErrorMessage(null);
			setMessage("Please select a folder for notification templates.");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};
}