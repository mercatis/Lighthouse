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
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

public class SoftwareComponentRichEditorFormPage extends FormPage {

	public static final String ID = SoftwareComponentRichEditorFormPage.class.getName();

	private SoftwareComponent softwareComponent;

	private Text versionText;
	private Text longNameText;
	private Text descriptionText;
	private Text copyrightText;
	private Text contactText;
	private Text contactEmailText;

	private ModifyListener modifyListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};

	@Override
	public boolean isDirty() {
		if (versionText.getText().length() > 0 && softwareComponent.getVersion() == null
				|| !versionText.getText().equals(softwareComponent.getVersion())
				&& softwareComponent.getVersion() != null)
			return true;
		if (longNameText.getText().length() > 0 && softwareComponent.getLongName() == null
				|| !longNameText.getText().equals(softwareComponent.getLongName())
				&& softwareComponent.getLongName() != null)
			return true;
		if (descriptionText.getText().length() > 0 && softwareComponent.getDescription() == null
				|| !descriptionText.getText().equals(softwareComponent.getDescription())
				&& softwareComponent.getDescription() != null)
			return true;
		if (copyrightText.getText().length() > 0 && softwareComponent.getCopyright() == null
				|| !copyrightText.getText().equals(softwareComponent.getCopyright())
				&& softwareComponent.getCopyright() != null)
			return true;
		if (contactText.getText().length() > 0 && softwareComponent.getContact() == null
				|| !contactText.getText().equals(softwareComponent.getContact())
				&& softwareComponent.getContact() != null)
			return true;
		if (contactEmailText.getText().length() > 0 && softwareComponent.getContactEmail() == null
				|| !contactEmailText.getText().equals(softwareComponent.getContactEmail())
				&& softwareComponent.getContactEmail() != null)
			return true;
		return false;
	}

	public void updateModel() {
		softwareComponent.setVersion(versionText.getText());
		softwareComponent.setLongName(longNameText.getText());
		softwareComponent.setDescription(descriptionText.getText());
		softwareComponent.setCopyright(copyrightText.getText());
		softwareComponent.setContact(contactText.getText());
		softwareComponent.setContactEmail(contactEmailText.getText());
		getEditor().editorDirtyStateChanged();
	}

	public SoftwareComponentRichEditorFormPage(FormEditor editor) {
		super(editor, ID, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		softwareComponent = ((GenericEditorInput<SoftwareComponent>) getEditor().getEditorInput()).getEntity();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(softwareComponent));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

		createHeaderSection(form, toolkit);
		
		if (!CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_MODIFY, softwareComponent)) {
			descriptionText.setEditable(false);
			descriptionText.setEnabled(false);
			contactText.setEditable(false);
			contactText.setEnabled(false);
			contactEmailText.setEditable(false);
			contactEmailText.setEnabled(false);
			copyrightText.setEditable(false);
			copyrightText.setEnabled(false);
			longNameText.setEditable(false);
			longNameText.setEnabled(false);
			versionText.setEditable(false);
			versionText.setEnabled(false);
		}
	}

	private void createHeaderSection(final ScrolledForm form, FormToolkit toolkit) {
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
		Text codeText = toolkit.createText(client, softwareComponent.getCode());
		codeText.setEditable(false);
		codeText.setEnabled(false);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Version:");
		versionText = toolkit.createText(client, softwareComponent.getVersion());
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		versionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Long name:");
		longNameText = toolkit.createText(client, softwareComponent.getLongName());
		longNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		longNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, softwareComponent.getDescription());
		descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Copyright:");
		copyrightText = toolkit.createText(client, softwareComponent.getCopyright());
		copyrightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyrightText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, softwareComponent.getContact());
		contactText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Email:");
		contactEmailText = toolkit.createText(client, softwareComponent.getContactEmail());
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);

		section.setClient(client);
	}
}
