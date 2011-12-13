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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.handlers.HandlerUtil;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;


public abstract class AbstractStructuredSelectionHandler extends AbstractHandler {

	protected IAdapterManager adapterManager = Platform.getAdapterManager();

	protected DomainService domainService = CommonBaseActivator.getPlugin().getDomainService();

	protected IWorkbenchPart activePart;
	
	protected ISelection currentSelection;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		preExecution(event);

		activePart = HandlerUtil.getActivePart(event);
		currentSelection = HandlerUtil.getCurrentSelection(event);
		
		if (currentSelection == null || !(currentSelection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
		Object[] elements = structuredSelection.toArray();

		for (Object element : elements) {
			execute(element);
		}

		postExecution(event);
		return null;
	}

	protected abstract void execute(Object element) throws ExecutionException;

	protected void postExecution(ExecutionEvent event) throws ExecutionException {
	}

	protected void preExecution(ExecutionEvent event) throws ExecutionException {
	}
	
	protected IEditorPart getActiveEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}
	
	protected IFormPage getActiveEditorFormPage() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof FormEditor) {
			FormEditor fe = (FormEditor) editor;
			IFormPage page = fe.getActivePageInstance();
			return page;
		}
		return null;
	}
	
	protected IWorkbenchPart getActivePart() {
		return activePart;
	}
	
	protected ISelection getCurrentSelection() {
		return currentSelection;
	}
}
