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
import com.mercatis.lighthouse3.security.ui.model.GroupAccessor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * This editor page is used to edit group permissions.
 * It's divided into three sections:
 * <ol>
 * <li>Group selection</li>
 * <li>Context selection</li>
 * <li>Role selection</li>
 * </ol>
 * 
 * @see AbstractAccessorBasedPermissionEditorPage
 */
public class GroupPermissionAccessorBasedEditorPage extends AbstractAccessorBasedPermissionEditorPage<GroupAccessor> {

	public static final String ID = GroupPermissionAccessorBasedEditorPage.class.getName();
	public static final String GROUP_CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.security.ui.permissioneditor.group.plainedit.contextmenu";

	/**
	 * @param editor
	 * @param title
	 */
	public GroupPermissionAccessorBasedEditorPage(FormEditor editor, String title) {
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
		return "Groups";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.mercatis.lighthouse3.security.ui.editors.pages.
	 * AbstractUserBasedPermissionEditorPage#getAllAccessors()
	 */
	@Override
	protected List<GroupAccessor> loadAccessors() {
		List<GroupAccessor> accessors = new LinkedList<GroupAccessor>();
		for (Group group : Security.getService().findAllGroups(lighthouseDomain.getProject())) {
			accessors.add(new GroupAccessor(lighthouseDomain, group));
		}
		return accessors;
	}
	
	/**
	 * {@inheritDoc}
	 * <br />Converts the group into a {@link GroupAccessor} and adds it to the accessor viewer.
	 */
	public void groupCreated(Group group) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(group.getLighthouseDomain());
		if (lighthouseDomain.equals(this.lighthouseDomain)) {
			GroupAccessor accessor = new GroupAccessor(lighthouseDomain, group);
			getAccessorViewer().add(accessor);
			getAccessors().add(accessor);
		}
	}

	/**
	 * {@inheritDoc}
	 * <br />Do nothing, groups are edited.
	 */
	public void userCreated(User user) {}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getAccessorContextMenuId()
	 */
	@Override
	protected String getAccessorContextMenuId() {
		return GROUP_CONTEXT_MENU_ID;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage#getEditedAccessorClass()
	 */
	@Override
	public Class<?> getEditedAccessorClass() {
		return GroupAccessor.class;
	}
}
