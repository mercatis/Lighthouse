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
package com.mercatis.lighthouse3.ui.swimlaneeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.forms.editor.FormEditor;

import com.mercatis.lighthouse3.base.ui.editors.AbstractEditorPageFactory;
import com.mercatis.lighthouse3.base.ui.editors.AbstractLighthouseEditorPage;


public class SwimlaneEditorPageFactory extends AbstractEditorPageFactory {

	public List<AbstractLighthouseEditorPage> getPages(FormEditor editor) {
		List<AbstractLighthouseEditorPage> pages = new ArrayList<AbstractLighthouseEditorPage>();
		pages.add(new PageWrapper(editor, ProcessTaskSwimlaneEditor.class.getName(), "Graph"));
		return pages;
	}

	@Override
	public FactoryPosition getFactoryPosition() {
		return FactoryPosition.BEFORE_STATIC;
	}
}
