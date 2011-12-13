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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.users.Group;
import com.mercatis.lighthouse3.security.ui.editors.GroupEditor;
import com.mercatis.lighthouse3.security.ui.model.GroupAccessor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class EditGroupHandler extends AbstractStructuredSelectionHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler#execute(java.lang.Object)
	 */
	@Override
	protected void execute(Object element) throws ExecutionException {
		Group group = null;
		
		if (element instanceof GroupAccessor) {
			group = ((GroupAccessor)element).getGroup();
		} else if (element instanceof Group) {
			group = (Group) element;
		}
		
		if (group != null) {
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(group.getLighthouseDomain());

			GenericEditorInput<Group> input = new GenericEditorInput<Group>(lighthouseDomain, group);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, input, GroupEditor.ID);
			} catch (PartInitException ex) {
				throw new ExecutionException(ex.getMessage(), ex);
			}
		}
	}
}
