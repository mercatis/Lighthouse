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
package com.mercatis.lighthouse3.base.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;

/**
 * Differentation of the AbstractStructuredSelectionHandler, used to open editors.
 * It provides a busy indication, so that the ui looks still responsive,
 * even if the editor needs some more time to load everything.
 * 
 */
public abstract class AbstractEditorHandler extends AbstractStructuredSelectionHandler {

	/**
	 * Display that message in the job
	 */
	private String busyMessage = "Loading data for editor";
	
	@Override
	protected void execute(Object element) throws ExecutionException {
		new OpenEditorJob(element, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()).schedule();
	}
	
	/**
	 * Override the default message
	 * 
	 * @param message
	 */
	public void setBusyMessage(String message) {
		this.busyMessage = message;
	}
	
	/**
	 * Implement and return the id of the editor to be opened.
	 * 
	 * @return editorId
	 */
	protected abstract String getEditorID();
	
	/**
	 * Provide the input for the editor
	 * 
	 * @param element
	 * @return
	 */
	protected abstract GenericEditorInput<?> getEditorInput(Object element);
	
	/**
	 * This job will be instanciated when opening the editor.
	 */
	private class OpenEditorJob extends Job {

		private IWorkbenchPage page;
		private Object element;
		private IStatus status = Status.OK_STATUS;
		private Display display;

		public OpenEditorJob(Object element, IWorkbenchPage page) {
			super("Open editor");
			this.display = Display.getCurrent();
			this.element = element;
			this.page = page;
			setUser(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(busyMessage, IProgressMonitor.UNKNOWN);
			display.syncExec(new Runnable() {
				public void run() {
					try {
						IDE.openEditor(page, getEditorInput(element), getEditorID());
					} catch (PartInitException ex) {
						status = UIJob.errorStatus(ex);
						ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Could not open editor", "Failed to open an editor for " + element.getClass().getSimpleName(), status);
					}
				}});
			return status;
		}
	}
}
