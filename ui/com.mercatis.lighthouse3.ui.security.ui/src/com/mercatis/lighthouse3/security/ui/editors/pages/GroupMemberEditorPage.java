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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.base.ui.widgets.ListConfrontationWidget;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.editors.UserEditor;
import com.mercatis.lighthouse3.security.ui.providers.UserLabelProvider;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class GroupMemberEditorPage extends FormPage implements ModifyListener {

	public static final String ID = GroupMemberEditorPage.class.getName();
	private LighthouseDomain lighthouseDomain;
	private Group group;
	private ListConfrontationWidget<User> listConfrontation;
	
	/**
	 * @param editor
	 * @param title
	 * @param group
	 */
	public GroupMemberEditorPage(FormEditor editor, String title, Group group) {
		super(editor, ID, title);
		this.group = group;
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		lighthouseDomain = ((GenericEditorInput<?>) getEditor().getEditorInput()).getDomain();

		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		form.setText("Members for: " + LabelConverter.getLabel(group));
		toolkit.decorateFormHeading(form.getForm());
		form.getBody().setLayout(new FillLayout());

		createHeaderSection(form, toolkit);
		refresh();
	}
	
	private void createHeaderSection(ScrolledForm form, FormToolkit toolkit) {
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		section.clientVerticalSpacing = 5;
		section.marginHeight = 8;
		section.marginWidth = 8;

		Composite client = toolkit.createComposite(section);
		toolkit.paintBordersFor(client);

		listConfrontation = new ListConfrontationWidget<User>(client, new UserLabelProvider());
		listConfrontation.setAttachedEditor(lighthouseDomain, UserEditor.class.getName());
		listConfrontation.addSelectionChangedListener(this);

		section.setClient(client);
	}
	
	private void refresh() {
		List<User> available = Security.getService().findAllUsers(lighthouseDomain.getProject());
		available.removeAll(group.getMembers());

		try {
			listConfrontation.setElements(available, new ArrayList<User>(group.getMembers()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isDirty() {
		if (listConfrontation == null)
			return false;
		if (group.getMembers().size() != listConfrontation.getSelectedElements().size()
				|| !group.getMembers().containsAll(listConfrontation.getSelectedElements()))
			return true;
		return false;
	}
	
	public void updateModel() {
		if (listConfrontation != null) { //page not shown, no updates.
			group.setMembers(listConfrontation.getSelectedElements());
			getEditor().editorDirtyStateChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		getEditor().editorDirtyStateChanged();
	}
}
