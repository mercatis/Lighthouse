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
package com.mercatis.lighthouse3.status.ui.wizards.pages;

import java.io.File;
import java.io.FilenameFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotification;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusWizardNotificationPage extends WizardPage {

	private File[] availableTemplates;

	private EMailNotification statusChangeNotificationChannel = new EMailNotification();
	private Text   receiver;
	private Combo  subjectTemplate;
	private Text   bodyMimeType;
	private Combo  bodyTemplate;
	private Button enableNotification;

	private Label receiverLabel;
	private Label subjectTemplateLabel;
	private Label bodyMimeTypeLabel;
	private Label bodyTemplateLabel;
	
	private boolean templatesAvailable;

	public StatusWizardNotificationPage(String pageName, LighthouseDomain lighthouseDomain) {
		super(pageName);
		this.availableTemplates = listTemplatesInTemplateDir(lighthouseDomain);
		if (availableTemplates == null || availableTemplates.length == 0) {
			templatesAvailable = false;
			setErrorMessage("No notification templates available.");
		}
		else {
			templatesAvailable = true;
		}
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(new GridLayout(2, false));

		enableNotification = new Button(composite, SWT.CHECK);
		enableNotification.setText("Enable Notification");
		enableNotification.setEnabled(templatesAvailable);
		GridData notyData = new GridData();
		notyData.horizontalSpan = 2;
		enableNotification.setLayoutData(notyData);
		enableNotification.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				setPageComplete(validatePage());
			}
		});

		receiverLabel = new Label(composite, SWT.NONE);
		receiverLabel.setText("Receiver:");
		receiver = new Text(composite, SWT.BORDER);
		receiver.setLayoutData(gd);
		receiver.addListener(SWT.Modify, modifyListener);

		subjectTemplateLabel = new Label(composite, SWT.NONE);
		subjectTemplateLabel.setText("Subject Template:");
		subjectTemplate = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN);
		subjectTemplate.setLayoutData(gd);

		bodyTemplateLabel = new Label(composite, SWT.NONE);
		bodyTemplateLabel.setText("Body Template:");
		bodyTemplate = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN);
		bodyTemplate.setLayoutData(gd);

		if (templatesAvailable) {
			for (File template : availableTemplates) {
				String name = template.getName();
				subjectTemplate.setData(name, template);
				bodyTemplate.setData(name, template);
				subjectTemplate.add(name);
				bodyTemplate.add(name);
			}
		}
		subjectTemplate.addListener(SWT.Modify, modifyListener);
		bodyTemplate.addListener(SWT.Modify, modifyListener);
		
		subjectTemplate.addListener(SWT.Selection, modifyListener);
		bodyTemplate.addListener(SWT.Selection, modifyListener);

		bodyMimeTypeLabel = new Label(composite, SWT.NONE);
		bodyMimeTypeLabel.setText("Body mime-type:");
		bodyMimeType = new Text(composite, SWT.BORDER);
		bodyMimeType.setText(statusChangeNotificationChannel.getBodyMimeType());
		bodyMimeType.setLayoutData(gd);
		bodyMimeType.addListener(SWT.Modify, modifyListener);

		setControl(composite);
		validatePage();
		setErrorMessage(null);
		if (templatesAvailable)
			setMessage(null);
		else
			setMessage("No notification templates available.");
	}

	public boolean validatePage() {
		setFieldsEnabled(isNotificationEnabled());
		if (isNotificationEnabled()) {
			if (receiver.getText().equals("")) {
				setMessage("Receiver is not specified.");
				return false;
			}
			if (subjectTemplate.getText().equals("")) {
				setMessage("Subject Template is not specified");
				return false;
			}
			if (bodyTemplate.getText().equals("")) {
				setMessage("Body Template is not specified");
				return false;
			}
			if (bodyMimeType.getText().equals("")) {
				setMessage("Body mime-type is not specified");
				return false;
			}
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private Listener modifyListener = new Listener() {
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			setPageComplete(validatePage());
		}
	};

	public StatusChangeNotificationChannel<?, ?> getNotificationChannel() {
		statusChangeNotificationChannel.setRecipients(receiver.getText());
		statusChangeNotificationChannel.setBodyMimeType(bodyMimeType.getText());
		statusChangeNotificationChannel.setTitleTemplate(StatusEditingObject.readTemplateIntoString((File) subjectTemplate.getData(subjectTemplate.getText())));
		statusChangeNotificationChannel.setBodyTemplate(StatusEditingObject.readTemplateIntoString((File) bodyTemplate.getData(bodyTemplate.getText())));
		return statusChangeNotificationChannel;
	}

	public boolean isNotificationEnabled() {
		return enableNotification.getSelection();
	}

	private void setFieldsEnabled(boolean enable) {
		if (receiverLabel != null && receiver != null && subjectTemplateLabel != null && subjectTemplate != null
				&& bodyTemplateLabel != null && bodyTemplate != null && bodyMimeType != null && bodyMimeTypeLabel != null) {
			receiverLabel.setEnabled(enable);
			receiver.setEnabled(enable);
			subjectTemplateLabel.setEnabled(enable);
			subjectTemplate.setEnabled(enable);
			bodyTemplateLabel.setEnabled(enable);
			bodyTemplate.setEnabled(enable);
			bodyMimeTypeLabel.setEnabled(enable);
			bodyMimeType.setEnabled(enable);
		}
	}

	private File[] listTemplatesInTemplateDir(LighthouseDomain lighthouseDomain) {
		String dir = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
				.getStatusNotificationTemplateFolder();
		File f = new File(dir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.contains(".") && name.substring(name.lastIndexOf(".")).equals(".vm"))
					return true;
				return false;
			}
		};
		return f.listFiles(filter);
	}
}
