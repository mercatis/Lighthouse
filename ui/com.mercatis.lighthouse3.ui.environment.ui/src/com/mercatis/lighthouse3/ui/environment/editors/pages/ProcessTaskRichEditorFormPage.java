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
package com.mercatis.lighthouse3.ui.environment.editors.pages;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

public class ProcessTaskRichEditorFormPage extends AbstractLighthouseEditorPage {

	public static final String ID = ProcessTaskRichEditorFormPage.class.getName();

	private ProcessTask processTask;

	private Text descriptionText;
	private Text contactText;
	private Text contactEmailText;
	private Text versionText;
	private Text longNameText;

	private boolean formFieldsComplete = false;

	public ProcessTaskRichEditorFormPage(FormEditor editor) {
		super(editor, ID, "Properties");
	}

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		processTask = ((GenericEditorInput<ProcessTask>) getEditor().getEditorInput()).getEntity();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(processTask));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

		createHeaderSection(form, toolkit);
		formFieldsComplete = true;
		
		if (!CodeGuard.hasRole(Role.PROCESS_TASK_MODIFY, processTask)) {
			descriptionText.setEditable(false);
			descriptionText.setEnabled(false);
			contactText.setEditable(false);
			contactText.setEnabled(false);
			contactEmailText.setEditable(false);
			contactEmailText.setEnabled(false);
			versionText.setEditable(false);
			versionText.setEnabled(false);
			longNameText.setEditable(false);
			longNameText.setEnabled(false);
		}
	}

	private void createHeaderSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 3;
		section.marginWidth = 3;
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		section.setLayoutData(gd);

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Code:");
		Text codeText = toolkit.createText(client, processTask.getCode());
		codeText.setLayoutData(textData);
		codeText.setEditable(false);
		codeText.setEnabled(false);

		toolkit.createLabel(client, "Version");
		versionText = toolkit.createText(client, processTask.getVersion());
		versionText.setLayoutData(textData);
		versionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Long name:");
		longNameText = toolkit.createText(client, processTask.getLongName());
		longNameText.setLayoutData(textData);
		longNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, processTask.getDescription());
		descriptionText.setLayoutData(textData);
		descriptionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, processTask.getContact());
		contactText.setLayoutData(textData);
		contactText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Email:");
		contactEmailText = toolkit.createText(client, processTask.getContactEmail());
		contactEmailText.setLayoutData(textData);
		contactEmailText.addModifyListener(modifyListener);

		section.setClient(client);
	}

	@Override
	public boolean isDirty() {
		if (!formFieldsComplete)
			return false;
		if (versionText.getText().length() > 0 && processTask.getVersion() == null
				|| !versionText.getText().equals(processTask.getVersion()) && processTask.getVersion() != null)
			return true;
		if (longNameText.getText().length() > 0 && processTask.getLongName() == null
				|| !longNameText.getText().equals(processTask.getLongName()) && processTask.getLongName() != null)
			return true;
		if (descriptionText.getText().length() > 0 && processTask.getDescription() == null
				|| !descriptionText.getText().equals(processTask.getDescription())
				&& processTask.getDescription() != null)
			return true;
		if (contactText.getText().length() > 0 && processTask.getContact() == null
				|| !contactText.getText().equals(processTask.getContact()) && processTask.getContact() != null)
			return true;
		if (contactEmailText.getText().length() > 0 && processTask.getContactEmail() == null
				|| !contactEmailText.getText().equals(processTask.getContactEmail())
				&& processTask.getContactEmail() != null)
			return true;
		return false;
	}

	public void updateModel() {
		processTask.setVersion(versionText.getText());
		processTask.setLongName(longNameText.getText());
		processTask.setDescription(descriptionText.getText());
		processTask.setContact(contactText.getText());
		processTask.setContactEmail(contactEmailText.getText());
		getEditor().editorDirtyStateChanged();
	}

	@Override
	public ISelectionProvider refreshSelectionProvider() {
		return null;
	}
}
