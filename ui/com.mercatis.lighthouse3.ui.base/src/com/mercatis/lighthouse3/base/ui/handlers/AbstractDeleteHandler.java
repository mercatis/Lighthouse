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
package com.mercatis.lighthouse3.base.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;


/**
 * Differentation of the AbstractStructuredSelectionHandler, used to delete entities.
 * The execution will only be commited, if the user interacts with the dialog. It's possible
 * to delete a multiselection.
 * 
 */
public abstract class AbstractDeleteHandler extends AbstractStructuredSelectionHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		preExecution(event);

		activePart = HandlerUtil.getActivePart(event);
		currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection == null || !(currentSelection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
		Object[] elements = structuredSelection.toArray();

		if (showDialog(elements)) {
			for (Object element : elements) {
				execute(element);
			}
		}
		else {
			return null;
		}

		postExecution(event);
		return null;
	}
	
	protected boolean showDialog(Object[] elements) {
		String message = "";
		if (elements.length == 1) {
			message = "Do you really want to delete " + LabelConverter.getLabel(elements[0]) + "?";
		} else if (elements.length > 1) {
			message = "Do you really want to delete these " + elements.length + " elements?";
		} else {
			throw new RuntimeException("No elements in selection!");
		}
		MessageDialog md = new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				"Delete", null, message,
				MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
		int button = md.open();
		return button == 0;
	}
}
