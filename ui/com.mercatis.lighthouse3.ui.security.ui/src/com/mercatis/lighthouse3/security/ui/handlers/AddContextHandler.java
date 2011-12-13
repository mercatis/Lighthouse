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
package com.mercatis.lighthouse3.security.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.mercatis.lighthouse3.security.ui.editors.pages.AbstractAccessorBasedPermissionEditorPage;


public class AddContextHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFormPage formPage = getActiveEditorFormPage();
		if (formPage instanceof AbstractAccessorBasedPermissionEditorPage<?>) {
			if (((AbstractAccessorBasedPermissionEditorPage<?>)formPage).getSelectedAccessor() != null) {
				((AbstractAccessorBasedPermissionEditorPage<?>)formPage).addContext();
			} else {
//				popup?
			}
		}
		return null;
	}
	
	private IFormPage getActiveEditorFormPage() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof FormEditor) {
			FormEditor fe = (FormEditor) editor;
			IFormPage page = fe.getActivePageInstance();
			return page;
		}
		return null;
	}
}
