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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

public class EnvironmentRichEditorFormPage extends FormPage {

	public static final String ID = EnvironmentRichEditorFormPage.class.getName();

	private Environment environment;

	private Text longNameText;
	private Text descriptionText;
	private Text contactText;
	private Text contactEmailText;

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};

	public EnvironmentRichEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		environment = ((GenericEditorInput<Environment>) getEditor().getEditorInput()).getEntity();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(environment));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

		createHeaderSection(form, toolkit);
		
		if (!CodeGuard.hasRole(Role.ENVIRONMENT_MODIFY, environment)) {
			descriptionText.setEditable(false);
			descriptionText.setEnabled(false);
			contactText.setEditable(false);
			contactText.setEnabled(false);
			contactEmailText.setEditable(false);
			contactEmailText.setEnabled(false);
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
		section.setLayoutData(gd);

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);
		client.setLayout(new GridLayout(2, false));

		toolkit.createLabel(client, "Code:");
		Text codeText = toolkit.createText(client, environment.getCode());
		codeText.setEditable(false);
		codeText.setEnabled(false);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Name:");
		longNameText = toolkit.createText(client, environment.getLongName());
		longNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		longNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, environment.getDescription());
		descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, environment.getContact());
		contactText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Email:");
		contactEmailText = toolkit.createText(client, environment.getContactEmail());
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);

		section.setClient(client);
	}

	@Override
	public boolean isDirty() {
		if (longNameText == null)
			return false;
		if (longNameText.getText().length() > 0 && environment.getLongName() == null
				|| !longNameText.getText().equals(environment.getLongName()) && environment.getLongName() != null)
			return true;
		if (descriptionText.getText().length() > 0 && environment.getDescription() == null
				|| !descriptionText.getText().equals(environment.getDescription())
				&& environment.getDescription() != null)
			return true;
		if (contactText.getText().length() > 0 && environment.getContact() == null
				|| !contactText.getText().equals(environment.getContact()) && environment.getContact() != null)
			return true;
		if (contactEmailText.getText().length() > 0 && environment.getContactEmail() == null
				|| !contactEmailText.getText().equals(environment.getContactEmail())
				&& environment.getContactEmail() != null)
			return true;
		return false;
	}

	public void updateModel() {
		environment.setLongName(longNameText.getText());
		environment.setDescription(descriptionText.getText());
		environment.setContact(contactText.getText());
		environment.setContactEmail(contactEmailText.getText());
		getEditor().editorDirtyStateChanged();
	}
}
