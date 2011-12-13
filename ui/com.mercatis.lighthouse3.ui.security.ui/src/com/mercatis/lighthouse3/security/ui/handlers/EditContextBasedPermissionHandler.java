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
package com.mercatis.lighthouse3.security.ui.handlers;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractEditorHandler;
import com.mercatis.lighthouse3.security.ui.editors.ContextBasedPermissionEditor;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class EditContextBasedPermissionHandler extends AbstractEditorHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractEditorHandler#getEditorID()
	 */
	@Override
	protected String getEditorID() {
		return ContextBasedPermissionEditor.ID;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractEditorHandler#getEditorInput(java.lang.Object)
	 */
	@Override
	protected GenericEditorInput<?> getEditorInput(Object element) {
		GenericEditorInput<LighthouseDomain> input = null;
		if (element instanceof LighthouseDomain) {
			LighthouseDomain lighthouseDomain = (LighthouseDomain) element;
			input = new GenericEditorInput<LighthouseDomain>(lighthouseDomain, lighthouseDomain);
			input.setPayload(getEditorID());
		}
		return input;
	}
}
