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
package com.mercatis.lighthouse3.ui.swimlaneeditor;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.editor.FormEditor;

import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;


public class PageWrapper extends AbstractLighthouseEditorPage {

	private ProcessTaskSwimlaneEditor wrappedPage;
	
	public PageWrapper(FormEditor editor, String id, String title) {
		super(editor, id, title);
		wrappedPage = new ProcessTaskSwimlaneEditor(editor);
	}

	@Override
	public IEditorPart getPage() {
		return wrappedPage;
	}

	@Override
	public ISelectionProvider refreshSelectionProvider() {
		return null; //this wrapped page is special and should care for this
	}
}
