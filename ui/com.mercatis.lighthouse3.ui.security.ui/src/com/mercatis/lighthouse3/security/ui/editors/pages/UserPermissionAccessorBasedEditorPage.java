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

import java.util.LinkedList;
import java.util.List;
import org.eclipse.ui.forms.editor.FormEditor;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.model.UserAccessor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This editor page is used to edit user permissions.
 * It's divided into three sections:
 * <ol>
 * <li>User selection</li>
 * <li>Context selection</li>
 * <li>Role selection</li>
 * </ol>
 * 
 * @see AbstractAccessorBasedPermissionEditorPage
 */
public class UserPermissionAccessorBasedEditorPage extends AbstractAccessorBasedPermissionEditorPage<UserAccessor> {

	public static final String ID = UserPermissionAccessorBasedEditorPage.class.getName();
	public static final String USER_CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.security.ui.permissioneditor.user.plainedit.contextmenu";

	/**
	 * @param editor
	 * @param title
	 */
	public UserPermissionAccessorBasedEditorPage(FormEditor editor, String title) {
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
	 * AbstractUserBasedPermissionEditorPage#getAllAccessors()
	 */
	@Override
	protected List<UserAccessor> loadAccessors() {
		List<UserAccessor> accessors = new LinkedList<UserAccessor>();
		for (User user : Security.getService().findAllUsers(lighthouseDomain.getProject())) {
			accessors.add(new UserAccessor(lighthouseDomain, user));
		}
		return accessors;
	}
	
	/**
	 * {@inheritDoc}
	 * <br />Converts the user into an {@link UserAccessor} and adds it to the accessor viewer.
	 */
	public void userCreated(User user) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(user.getLighthouseDomain());
		if (lighthouseDomain.equals(this.lighthouseDomain)) {
			UserAccessor accessor = new UserAccessor(lighthouseDomain, user);
			getAccessorViewer().add(accessor);
			getAccessors().add(accessor);
		}
	}

	/**
	 * {@inheritDoc}
	 * <br />Do nothing, users are edited.
	 */
	public void groupCreated(Group group) {}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getAccessorContextMenuId()
	 */
	@Override
	protected String getAccessorContextMenuId() {
		return USER_CONTEXT_MENU_ID;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getEditedAccessorClass()
	 */
	@Override
	public Class<?> getEditedAccessorClass() {
		return UserAccessor.class;
	}
}
