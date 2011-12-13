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
package com.mercatis.lighthouse3.base.ui.editors;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * Derivates of this class can be created with an EditorPageFactory.
 * If you implementation does not allow to use this as a superclass, you may override getPage() and
 * return a specific implementation of IEditorPart.
 * 
 */
public abstract class AbstractLighthouseEditorPage extends FormPage {
	
	/**
	 * When adding a page to an editor, the page is callable in the editor via its index number.
	 */
	private int pageIndex;
	
	/**
	 * The constructor passes everything up to FormPage.
	 * 
	 * @param editor
	 * @param id
	 * @param title
	 */
	public AbstractLighthouseEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	/**
	 * Returning false causes the selectionprovider to call getSelection() on itself
	 * when no selection provider is set.
	 * (loops until stack-overflow)
	 */
	@Override
	public boolean isEditor() {
		return true;
	}
	
	/**
	 * Set the pages index in the enclosing editor.
	 * 
	 * @param pageIndex index in the editor
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	
	/**
	 * Return where in the enclosing editor this pages is added.
	 * 
	 * @return index in the enclosing editor
	 */
	public int getPageIndex() {
		return pageIndex;
	}
	
	/**
	 * Overide this method if you want to wrap a page when you are not able to derivate your editor page
	 * from this one.
	 * 
	 * @return the wrapped page
	 */
	public IEditorPart getPage() {
		return this;
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (active && getEditor() instanceof AbstractExtendableFormEditor) {
			((AbstractExtendableFormEditor)getEditor()).setSelectionProvider(refreshSelectionProvider());
		}
	}

	/**
	 * Replacing the selection provider during the lifetime of the part is not properly supported by the workbench. [IBM]
	 * So we do a workaround and the AbstractExtendableFormEditor asks for a new ISelectionProvider each time the page is activated.
	 * @return an ISelectionProvider - may be null
	 */
	abstract public ISelectionProvider refreshSelectionProvider();
}
