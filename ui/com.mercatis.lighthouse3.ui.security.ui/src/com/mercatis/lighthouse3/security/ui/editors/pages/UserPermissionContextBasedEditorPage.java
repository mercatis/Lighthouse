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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.model.AbstractAccessor;
import com.mercatis.lighthouse3.security.ui.model.UserAccessor;
import com.mercatis.lighthouse3.security.ui.providers.UserLabelProvider;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This editor page is used to edit user permissions.
 * It's divided into three sections:
 * <ol>
 * <li>Context selection</li>
 * <li>User selection</li>
 * <li>Role selection</li>
 * </ol>
 * 
 * @see AbstractContextBasedPermissionEditorPage
 */
public class UserPermissionContextBasedEditorPage extends AbstractContextBasedPermissionEditorPage<UserAccessor> {

	public static final String ID = UserPermissionContextBasedEditorPage.class.getName();
	public static final String CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.security.ui.permissioneditor.user.permission.contextmenu";

	/**
	 * @param editor
	 * @param title
	 */
	public UserPermissionContextBasedEditorPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.mercatis.lighthouse3.security.ui.editors.pages.
	 * AbstractPermissionEditorPage#getAccessorSectionName()
	 */
	@Override
	protected String getAccessorSectionName() {
		return "Users";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.mercatis.lighthouse3.security.ui.editors.pages.
	 * AbstractPermissionEditorPage#getAccessorsForContext(java.lang.String)
	 */
	@Override
	protected List<UserAccessor> loadAccessorsForContext(String context) {
		Set<User> users = Security.getService().findUsersWithContext(lighthouseDomain.getProject(), context);
		List<UserAccessor> accessors = new LinkedList<UserAccessor>();
		for (User user : users) {
			accessors.add(new UserAccessor(lighthouseDomain, user));
		}
		return accessors;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractContextBasedPermissionEditorPage#getContextMenuId()
	 */
	@Override
	protected String getAccessorContextMenuId() {
		return CONTEXT_MENU_ID;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractContextBasedPermissionEditorPage#selectAccessor()
	 */
	@Override
	protected AbstractAccessor selectAccessor() {
		SelectionDialog selectUser = new SelectionDialog("Select User");
		if (selectUser.open() == IDialogConstants.OK_ID) {
			User user = (User) selectUser.getSelection().getFirstElement();
			UserAccessor accessor = new UserAccessor(lighthouseDomain, user);
			return accessor;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getSelectionDialogViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected StructuredViewer getSelectionDialogViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.SINGLE);
		viewer.setSorter(new ViewerSorter());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new UserLabelProvider());
		List<User> input = Security.getService().findAllUsers(lighthouseDomain.getProject());
		Collections.sort(input, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return LabelConverter.getLabel(o1).compareTo(LabelConverter.getLabel(o2));
			}
		});
		viewer.setInput(input);
		return viewer;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getEditedAccessorClass()
	 */
	@Override
	public Class<?> getEditedAccessorClass() {
		return UserAccessor.class;
	}
}
