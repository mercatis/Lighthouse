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
import com.mercatis.lighthouse3.domainmodel.users.User;
import com.mercatis.lighthouse3.security.ui.editors.pages.UserPropertiesEditorPage;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;


public class UserEditor extends AbstractExtendableFormEditor {

	public static final String ID = UserEditor.class.getName();
	
	private UserPropertiesEditorPage propertiesEditorPage;
	private LighthouseDomain lighthouseDomain;
	private User user;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		lighthouseDomain = ((GenericEditorInput<User>)input).getDomain();
		user = ((GenericEditorInput<User>)input).getEntity();
		setPartName(LabelConverter.getLabel(user));
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
		Security.getService().updateUser(lighthouseDomain.getProject(), user);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		propertiesEditorPage = new UserPropertiesEditorPage(this, "Properties", user);
		try {
			int pageIndex = addPage(propertiesEditorPage);
			setPageText(pageIndex, "Properties");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
