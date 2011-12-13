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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.forms.editor.IFormPage;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.security.ui.editors.pages.AbstractAccessorBasedPermissionEditorPage;


public class RemoveContextHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		IFormPage formPage = getActiveEditorFormPage();
		if (formPage instanceof AbstractAccessorBasedPermissionEditorPage<?> && element instanceof String) {
			((AbstractAccessorBasedPermissionEditorPage<?>)formPage).removeContext((String)element);
		}
	}
}
