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

import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.security.ui.Activator;
import com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage;
import com.mercatis.lighthouse3.security.ui.editors.pages.GroupPermissionContextBasedEditorPage;
import com.mercatis.lighthouse3.security.ui.editors.pages.UserPermissionContextBasedEditorPage;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class ContextBasedPermissionEditor extends AbstractExtendableFormEditor {
	
	public static final String ID = ContextBasedPermissionEditor.class.getName();
	private UserPermissionContextBasedEditorPage userPermissionContextBasedEditorPage;
	private GroupPermissionContextBasedEditorPage groupPermissionContextBasedEditorPage;
	private LighthouseDomain lighthouseDomain;

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
		if (userPermissionContextBasedEditorPage != null)
			userPermissionContextBasedEditorPage.save();
		if (groupPermissionContextBasedEditorPage != null)
			groupPermissionContextBasedEditorPage.save();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		lighthouseDomain = ((GenericEditorInput<LighthouseDomain>)input).getDomain();
		setPartName("User permissions on " + LabelConverter.getLabel(lighthouseDomain));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		userPermissionContextBasedEditorPage = new UserPermissionContextBasedEditorPage(this, "Users");
		groupPermissionContextBasedEditorPage = new GroupPermissionContextBasedEditorPage(this, "Groups");
		try {
			int pageIndex = this.addPage(userPermissionContextBasedEditorPage);
			setPageImage(pageIndex, ImageDescriptor.createFromURL(getClass().getResource("/icons/user.png")).createImage());

			pageIndex = this.addPage(groupPermissionContextBasedEditorPage);
			setPageImage(pageIndex, ImageDescriptor.createFromURL(getClass().getResource("/icons/group.png")).createImage());
		} catch (PartInitException ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dispose() {
		for (AbstractPermissionEditorPage<?> page : (Vector<AbstractPermissionEditorPage<?>>)pages) {
			if (page != null)
				page.dispose();
		}
		super.dispose();
	}
}
