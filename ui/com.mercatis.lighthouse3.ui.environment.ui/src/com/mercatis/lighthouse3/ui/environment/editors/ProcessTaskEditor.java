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
package com.mercatis.lighthouse3.ui.environment.editors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.editors.pages.AttachedDeploymentsEditorFormPage;
import com.mercatis.lighthouse3.ui.environment.editors.pages.ProcessTaskChildEditorFormPage;
import com.mercatis.lighthouse3.ui.environment.editors.pages.ProcessTaskRichEditorFormPage;

public class ProcessTaskEditor extends AbstractExtendableFormEditor {

	public static final String ID = ProcessTaskEditor.class.getName();
	private ProcessTaskRichEditorFormPage richFormPage;
	private ProcessTaskChildEditorFormPage childEditor;
	private AttachedDeploymentsEditorFormPage deploymentsPage;

	@Override
	protected void createPages() {
		richFormPage = new ProcessTaskRichEditorFormPage(this);
		childEditor = new ProcessTaskChildEditorFormPage(this);
		deploymentsPage = new AttachedDeploymentsEditorFormPage(this);
		super.createPages();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addPages() {
		int requestedPage = -1;
		GenericEditorInput<ProcessTask> editorInput = (GenericEditorInput<ProcessTask>) getEditorInput();
		try {
			int idx;
			idx = addPage(richFormPage);
			setPageText(idx, "Properties");
			if (editorInput.getPayload() != null && editorInput.getPayload().equals(richFormPage.getClass()))
				requestedPage = idx;

			idx = addPage(childEditor);
			setPageText(idx, "Children");
			if (editorInput.getPayload() != null && editorInput.getPayload().equals(childEditor.getClass()))
				requestedPage = idx;

			idx = addPage(deploymentsPage);
			setPageText(idx, "Deployments");
			if (editorInput.getPayload() != null && editorInput.getPayload().equals(deploymentsPage.getClass()))
				requestedPage = idx;

		} catch (PartInitException e) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
		}

		if (requestedPage != -1)
			setActivePage(requestedPage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);

		ProcessTask processTask = ((GenericEditorInput<ProcessTask>) input).getEntity();
		setPartName(LabelConverter.getLabel(processTask));
	}

	@Override
	public String getFactoryExtensionPoint() {
		return "com.mercatis.lighthouse3.ui.editors.processtaskeditor.pagefactories";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save() {
		ProcessTask task = ((GenericEditorInput<ProcessTask>) getEditorInput()).getEntity();

		if (richFormPage.isDirty()) {
			richFormPage.updateModel();
		}
		if (childEditor.isDirty()) {
			childEditor.updateModel();
		}
		if (deploymentsPage.isDirty()) {
			deploymentsPage.updateModel();
		}

		// save changes
		CommonBaseActivator.getPlugin().getDomainService().updateProcessTask(task);

		// notify property change listeners
		CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(task);
	}
}
