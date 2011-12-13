package com.mercatis.lighthouse3.security.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.mercatis.lighthouse3.security.ui.editors.pages.AbstractPermissionEditorPage;

public class SelectNonePermissionsHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFormPage formPage = getActiveEditorFormPage();
		if (formPage instanceof AbstractPermissionEditorPage<?>) {
			((AbstractPermissionEditorPage<?>)formPage).setAllPermissionsChecked(false);
		}
		return null;
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
}
