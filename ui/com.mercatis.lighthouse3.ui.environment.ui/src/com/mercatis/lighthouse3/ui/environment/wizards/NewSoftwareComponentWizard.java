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
package com.mercatis.lighthouse3.ui.environment.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.environment.wizards.pages.WizardNewSoftwareComponentPage;

public class NewSoftwareComponentWizard extends Wizard implements INewWizard {

	private WizardNewSoftwareComponentPage page;

	private SoftwareComponent newSoftwareComponent;

	private LighthouseDomain lighthouseDomain;

	private SoftwareComponent parentSoftwareComponent;

	public NewSoftwareComponentWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("New Software Component");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();

		if (obj instanceof SoftwareComponent) {
			parentSoftwareComponent = (SoftwareComponent) obj;
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(parentSoftwareComponent);
		}

		if (obj instanceof LighthouseDomain) {
			lighthouseDomain = (LighthouseDomain) obj;
		}

		if (obj instanceof SoftwareComponentContainer) {
			lighthouseDomain = ((SoftwareComponentContainer) obj).getLighthouseDomain();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		page = new WizardNewSoftwareComponentPage("ProjectCreationPage");
		page.setTitle("Software Component");
		page.setDescription("Create a new Software Component.");
		page.setLighthouseDomain(lighthouseDomain);
		page.setParentEntity(parentSoftwareComponent);

		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (newSoftwareComponent != null)
			return true;

		if (createSoftwareComponent()) {
			if (page.getParentEntity() != null) {
				// update the parent component
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getParentEntity());
			} else {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getLighthouseDomain().getSoftwareComponentContainer());
			}
			return true;
		}
		return false;
	}

	protected boolean createSoftwareComponent() {
		LighthouseDomain lighthouseDomain = page.getLighthouseDomain();
		SoftwareComponent parentComponent = page.getParentEntity();
		String code = page.getNewSoftwareComponentCode();

		SoftwareComponent component = new SoftwareComponent();
		component.setCode(code);
		component.setLongName(page.getNewSoftwareComponentDescription());

		try {
			if (parentComponent != null) {
				parentComponent.addSubEntity(component);
			}
			CommonBaseActivator.getPlugin().getDomainService().persistSoftwareComponent(lighthouseDomain, component);

			newSoftwareComponent = component;
			return true;
		} catch (Exception e) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
			String message = e.getMessage();
			if (message.contains("404")) {
				message = "Resource not found";
			} else if (message.contains("401")) {
				message = "Access to resource denied";
			}
			page.setErrorMessage(message);
			return false;
		}
	}

}
