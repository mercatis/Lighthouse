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
package com.mercatis.lighthouse3.security.ui.editors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.security.ui.editors.pages.GroupMemberEditorPage;
import com.mercatis.lighthouse3.security.ui.editors.pages.GroupPropertiesEditorPage;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class GroupEditor extends AbstractExtendableFormEditor {

	public static final String ID = GroupEditor.class.getName();
	
	private GroupPropertiesEditorPage propertiesEditorPage;
	private GroupMemberEditorPage membersPage;
	private LighthouseDomain lighthouseDomain;
	private Group group;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		lighthouseDomain = ((GenericEditorInput<Group>)input).getDomain();
		group = ((GenericEditorInput<Group>)input).getEntity();
		setPartName(LabelConverter.getLabel(group));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor#getFactoryExtensionPoint()
	 */
	@Override
	public String getFactoryExtensionPoint() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor#save()
	 */
	@Override
	public void save() {
		propertiesEditorPage.updateModel();
		membersPage.updateModel();
		Security.getService().updateGroup(lighthouseDomain.getProject(), group);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		propertiesEditorPage = new GroupPropertiesEditorPage(this, "Properties", group);
		membersPage = new GroupMemberEditorPage(this, "Members", group);
		try {
			int pageIndex = addPage(propertiesEditorPage);
			setPageText(pageIndex, "Properties");
			
			pageIndex = addPage(membersPage);
			setPageText(pageIndex, "Members");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
