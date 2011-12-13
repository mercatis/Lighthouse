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
import com.mercatis.lighthouse3.domainmodel.users.User;


public class UserPropertiesEditorPage extends FormPage {

	public static final String ID = UserPropertiesEditorPage.class.getName();
	private User user;
	private Text givenNameText;
	private Text surNameText;
	private Text contactEmailText;

	/**
	 * @param editor
	 * @param title
	 * @param user
	 */
	public UserPropertiesEditorPage(FormEditor editor, String title, User user) {
		super(editor, ID, title);
		this.user = user;
	}
	
	@Override
	public boolean isDirty() {
		if (givenNameText == null || surNameText == null || contactEmailText == null)
			return false;
		return !givenNameText.getText().equals(getNullChecked(user.getGivenName()))
		|| !surNameText.getText().equals(getNullChecked(user.getSurName()))
		|| !contactEmailText.getText().equals(getNullChecked(user.getContactEmail()));
	}
	
	private String getNullChecked(String string) {
		return string == null ? "" : string;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Properties for: " + LabelConverter.getLabel(user));
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
		Text codeText = toolkit.createText(client, user.getCode());
		codeText.setEditable(false);
		codeText.setEnabled(false);
		codeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(client, "Given Name:");
		givenNameText = toolkit.createText(client, user.getGivenName());
		givenNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		givenNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Surname:");
		surNameText = toolkit.createText(client, user.getSurName());
		surNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		surNameText.addModifyListener(modifyListener);

		toolkit.createLabel(client, "Contact Email:");
		contactEmailText = toolkit.createText(client, user.getContactEmail());
		contactEmailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contactEmailText.addModifyListener(modifyListener);
		
		section.setClient(client);
}

	public void updateModel() {
		user.setGivenName(givenNameText.getText());
		user.setSurName(surNameText.getText());
		user.setContactEmail(contactEmailText.getText());
		getEditor().editorDirtyStateChanged();
	}

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			getEditor().editorDirtyStateChanged();
		}
	};
}
