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
package com.mercatis.lighthouse3.status.ui.wizards;

import java.io.File;
import java.lang.reflect.Field;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.domainmodel.status.EMailNotification;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusChangeNotificationChannel;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusNotificationEditor extends Wizard {

	private LighthouseDomain lighthouseDomain;
	private Status status;
	private EMailNotification emailNotification;
	private Label receiverLabel;
	private Text receiver;
	private Label bodyMimeTypeLabel;
	private Text bodyMimeType;
	private Label subjectTemplateLabel;
	private Text subjectTemplateText;
	private Button loadSubjectTemplateButton;
	private Label bodyTemplateLabel;
	private Text bodyTemplateText;
	private Button loadBodyTemplateButton;
	private Button enableNotification;
	private String bodyTemplate, titleTemplate;

	public StatusNotificationEditor(LighthouseDomain lighthouseDomain, Status status) {
		this.lighthouseDomain = lighthouseDomain;
		this.status = status;
		this.emailNotification = getFirstEMailnotificationFromStatus(status);
	}

	@Override
	public boolean performFinish() {
		if (enableNotification.getSelection()) {
			if (emailNotification == null) {
				emailNotification = new EMailNotification();
			}
			emailNotification.setRecipients(receiver.getText());
			if (titleTemplate != null && titleTemplate.length() > 0 && !titleTemplate.startsWith("<")) {
				emailNotification.setTitleTemplate(titleTemplate);
			}
			if (bodyTemplate != null && bodyTemplate.length() > 0 && !bodyTemplate.startsWith("<")) {
				emailNotification.setBodyTemplate(bodyTemplate);
			}
			emailNotification.setBodyMimeType(bodyMimeType.getText());
		} else {
			emailNotification = null;
		}
		
		EMailNotification current = getFirstEMailnotificationFromStatus(status);
		if (emailNotification != null) {
			if (current == null && emailNotification != null) {
				current = new EMailNotification();
				for (StatusChangeNotificationChannel<?, ?> channel : status.getChangeNotificationChannels()) {
					if (channel instanceof EMailNotification)
						status.detachChangeNotificationChannel(channel);
				}
				status.attachChangeNotificationChannel(current);
			}
			current.setRecipients(emailNotification.getRecipients());
			current.setTitleTemplate(emailNotification.getTitleTemplate());
			current.setBodyTemplate(emailNotification.getBodyTemplate());
			current.setBodyMimeType(emailNotification.getBodyMimeType());
		}
		else if (current != null) {
			status.getChangeNotificationChannels().clear();
		}
		return true;
	}

	public EMailNotification getEMailNotification() {
		return this.emailNotification;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		pageContainer.setLayout(new GridLayout(3, false));
		createNotificationSection(pageContainer);

		try {
			Field finishButton = getContainer().getClass().getDeclaredField("finishButton");
			finishButton.setAccessible(true);
			((Button) finishButton.get(getContainer())).setText("&Save");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	private void createNotificationSection(Composite parent) {
		GridData labelGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);

		parent.setLayout(new GridLayout(3, false));

		enableNotification = new Button(parent, SWT.CHECK);
		enableNotification.setText("Enable Notification");
		GridData notifyGridData = new GridData();
		notifyGridData.horizontalSpan = 3;
		enableNotification.setLayoutData(notifyGridData);
		enableNotification.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				setNotificationFieldsEnabled(enableNotification.getSelection());
			}
		});

		receiverLabel = new Label(parent, SWT.NONE);
		receiverLabel.setText("Receiver:");
		receiverLabel.setLayoutData(labelGridData);
		GridData inputGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		inputGridData.horizontalSpan = 2;
		receiver = new Text(parent, SWT.BORDER);
		receiver.setLayoutData(inputGridData);

		bodyMimeTypeLabel = new Label(parent, SWT.NONE);
		bodyMimeTypeLabel.setText("Body mime-type:");
		bodyMimeTypeLabel.setLayoutData(labelGridData);
		bodyMimeType = new Text(parent, SWT.BORDER);
		bodyMimeType.setLayoutData(inputGridData);

		subjectTemplateLabel = new Label(parent, SWT.NONE);
		subjectTemplateLabel.setText("Subject Template:");
		subjectTemplateLabel.setLayoutData(labelGridData);

		subjectTemplateText = new Text(parent, SWT.BORDER);
		subjectTemplateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		loadSubjectTemplateButton = new Button(parent, SWT.PUSH);
		loadSubjectTemplateButton.setText("...");
		loadSubjectTemplateButton.setToolTipText("Load template from file...");
		loadSubjectTemplateButton.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));
		loadSubjectTemplateButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				File file = openFileDialog();
				if (file != null) {
					titleTemplate = StatusEditingObject.readTemplateIntoString(file);
					subjectTemplateText.setText(file.getName());
				}
			}
		});

		bodyTemplateLabel = new Label(parent, SWT.NONE);
		bodyTemplateLabel.setText("Body Template:");
		GridData bodyTemplateLabelGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		bodyTemplateLabel.setLayoutData(bodyTemplateLabelGridData);

		bodyTemplateText = new Text(parent, SWT.BORDER);
		bodyTemplateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		loadBodyTemplateButton = new Button(parent, SWT.PUSH);
		loadBodyTemplateButton.setText("...");
		loadBodyTemplateButton.setToolTipText("Load template from file...");
		loadBodyTemplateButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		loadBodyTemplateButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				File file = openFileDialog();
				if (file != null) {
					bodyTemplate = StatusEditingObject.readTemplateIntoString(file);
					bodyTemplateText.setText(file.getName());
				}
			}
		});
		readNotifiaction();
	}

	private void readNotifiaction() {
		if (emailNotification != null) {
			enableNotification.setSelection(true);
			receiver.setText(getNullchecked(emailNotification.getRecipients()));
			bodyMimeType.setText(getNullchecked(emailNotification.getBodyMimeType()));
			String header = emailNotification.getTitleTemplate();
			if (header!=null && header.length()>0)
				subjectTemplateText.setText("<configured>");
			String body = emailNotification.getBodyTemplate();
			if (body!=null && body.length()>0)
				bodyTemplateText.setText("<configured>");
		} else {
			enableNotification.setSelection(false);
			receiver.setText("");
			subjectTemplateText.setText("");
			bodyTemplateText.setText("");
			bodyMimeType.setText(new EMailNotification().getBodyMimeType());
		}
		setNotificationFieldsEnabled(emailNotification != null);
	}

	private void setNotificationFieldsEnabled(boolean enabled) {
		receiverLabel.setEnabled(enabled);
		receiver.setEnabled(enabled);
		subjectTemplateLabel.setEnabled(enabled);
		subjectTemplateText.setEnabled(false);
		bodyTemplateLabel.setEnabled(enabled);
		bodyTemplateText.setEnabled(false);
		loadBodyTemplateButton.setEnabled(enabled);
		loadSubjectTemplateButton.setEnabled(enabled);
		bodyMimeTypeLabel.setEnabled(enabled);
		bodyMimeType.setEnabled(enabled);
	}

	private File openFileDialog() {
		String dir = CommonBaseActivator.getPlugin().getStatusService().getStatusConfiguration(lighthouseDomain)
				.getStatusNotificationTemplateFolder();
		FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		fileDialog.setFilterPath(dir);
		fileDialog.setFilterExtensions(new String[] { "*.vm" });
		String filename = fileDialog.open();
		return filename != null ? new File(filename) : null;
	}

	private String getNullchecked(String string) {
		return string == null ? "" : string;
	}
	
	private EMailNotification getFirstEMailnotificationFromStatus(Status status) {
		if (status.getChangeNotificationChannels() != null) {
			for (StatusChangeNotificationChannel<?, ?> channel : status.getChangeNotificationChannels()) {
				if (channel instanceof EMailNotification)
					return (EMailNotification) channel;
			}
		}
		return null;
	}
}
