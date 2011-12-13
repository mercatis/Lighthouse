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
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.editors.pages.DeploymentRichEditorFormPage;

public class DeploymentEditor extends AbstractExtendableFormEditor {

	public static final String ID = DeploymentEditor.class.getName();

	private DeploymentRichEditorFormPage richFormPage;

	@Override
	protected void addPages() {
		try {
			richFormPage = new DeploymentRichEditorFormPage(this);
			int idx = addPage(richFormPage);
			setPageText(idx, "Properties");
		} catch (PartInitException e) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);

		Deployment deployment = ((GenericEditorInput<Deployment>) input).getEntity();
		setPartName(LabelConverter.getLabel(deployment));
	}

	@Override
	public String getFactoryExtensionPoint() {
		return "com.mercatis.lighthouse3.ui.editors.deploymenteditor.pagefactories";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save() {
		if (richFormPage.isDirty())
			richFormPage.updateModel();

		Deployment deployment = ((GenericEditorInput<Deployment>) getEditorInput()).getEntity();

		// save changes
		CommonBaseActivator.getPlugin().getDomainService().updateDeployment(deployment);

		// notify property change listeners
		try {
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(deployment);
		} catch (Throwable t) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, t.getMessage(), t));
		}
	}
}
