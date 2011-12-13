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
package com.mercatis.lighthouse3.security.ui.editors.pages;

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

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.users.Group;


public class GroupPropertiesEditorPage extends FormPage {

	public static final String ID = GroupPropertiesEditorPage.class.getName();
	private Group group;
	private Text longNameText;
	private Text descriptionText;
	private Text contactText;
	private Text contactEmailText;
	
	/**
	 * @param editor
	 * @param title
	 * @param group
	 */
	public GroupPropertiesEditorPage(FormEditor editor, String title, Group group) {
		super(editor, ID, title);
		this.group = group;
	}
	
	@Override
	public boolean isDirty() {
		if (longNameText == null || descriptionText == null || contactText == null || contactEmailText == null)
			return false;
		return !longNameText.getText().equals(getNullChecked(group.getLongName()))
		|| !descriptionText.getText().equals(getNullChecked(group.getDescription()))
		|| !contactText.getText().equals(getNullChecked(group.getContact()))
		|| !contactEmailText.getText().equals(getNullChecked(group.getContactEmail()));
	}
	
	private String getNullChecked(String string) {
		return string == null ? "" : string;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(group));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new GridLayout(2, false));

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
		Text codeText = toolkit.createText(client, group.getCode());
		codeText.setEditable(false);
		codeText.setEnabled(false);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Long Name:");
		longNameText = toolkit.createText(client, group.getLongName());
		longNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		longNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Description:");
		descriptionText = toolkit.createText(client, group.getDescription());
		descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionText.addModifyListener(modifyListener);
		
		toolkit.createLabel(client, "Contact:");
		contactText = toolkit.createText(client, group.getContact());
		contactText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact Email:");
		contactEmailText = toolkit.createText(client, group.getContactEmail());
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);
		
		section.setClient(client);
}

	public void updateModel() {
		group.setContact(contactText.getText());
		group.setContactEmail(contactEmailText.getText());
		group.setDescription(descriptionText.getText());
		group.setLongName(longNameText.getText());
		getEditor().editorDirtyStateChanged();
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};
}
