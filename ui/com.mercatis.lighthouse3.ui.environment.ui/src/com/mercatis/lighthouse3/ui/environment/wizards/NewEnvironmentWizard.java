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
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.wizards.pages.WizardNewEnvironmentPage;

public class NewEnvironmentWizard extends Wizard implements INewWizard {

	private WizardNewEnvironmentPage page;

	private Environment newEnvironment;

	private LighthouseDomain lighthouseDomain;

	private Environment parentEnvironment;

	public NewEnvironmentWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New Environment");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();

		if (obj instanceof LighthouseDomain) {
			lighthouseDomain = (LighthouseDomain) obj;
		}

		if (obj instanceof EnvironmentContainer) {
			lighthouseDomain = ((EnvironmentContainer) obj).getLighthouseDomain();
		}

		if (obj instanceof Environment) {
			parentEnvironment = (Environment) obj;
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(parentEnvironment);
		}

	}

	public void addPages() {
		page = new WizardNewEnvironmentPage("EnvironmentCreationPage");
		page.setTitle("Environment");
		page.setDescription("Create a new Environment.");

		page.setLighthouseDomain(lighthouseDomain);
		page.setParentEntity(parentEnvironment);

		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (newEnvironment != null)
			return true;

		if (createEnvironment()) {
			if (page.getParentEntity() != null) {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getParentEntity());
			} else {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getLighthouseDomain().getEnvironmentContainer());
			}
			return true;
		}

		return false;
	}

	protected boolean createEnvironment() {
		Environment environment = new Environment();
		environment.setCode(page.getNewEnvironmentCode());
		environment.setLongName(page.getNewEnvironmentDescription());

		try {
			parentEnvironment = page.getParentEntity();
			if (parentEnvironment != null) {
				parentEnvironment.addSubEntity(environment);
			}
			CommonBaseActivator.getPlugin().getDomainService().persistEnvironment(lighthouseDomain, environment);

			newEnvironment = environment;
			return true;
		} catch (Exception ex) {
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			String message = ex.getMessage();
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
