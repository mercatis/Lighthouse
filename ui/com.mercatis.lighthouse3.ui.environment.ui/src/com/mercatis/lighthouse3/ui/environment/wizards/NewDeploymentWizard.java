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
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.wizards.pages.WizardNewDeploymentPage;

public class NewDeploymentWizard extends Wizard implements INewWizard {

	private Deployment newDeployment;

	private LighthouseDomain lighthouseDomain;

	private Location location;

	private SoftwareComponent softwareComponent;

	private WizardNewDeploymentPage page;

	public NewDeploymentWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("New Deployment");
	}

	@Override
	public boolean performFinish() {
		if (newDeployment != null)
			return true;

		if (createDeployment()) {
			try {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(page.getLighthouseDomain().getDeploymentContainer());
			}
			catch (Throwable t) {
				CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, t.getMessage(), t));
			}
			return true;
		}

		return false;
	}

	protected boolean createDeployment() {
		Deployment deployment = new Deployment();
		deployment.setLocation(page.getLocationText());
		deployment.setDeployedComponent(page.getSoftwareComponent());

		try {
			CommonBaseActivator.getPlugin().getDomainService().persistDeployment(lighthouseDomain, deployment);

			newDeployment = deployment;
			return true;
		} catch (Exception ex) {
			String message = ex.getMessage();
			if (message.contains("404")) {
				message = "Resource not found";
			} else if (message.contains("401")) {
				message = "Access to resource denied";
			}
			page.setErrorMessage(message);
			CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.WARNING, CommonUIActivator.PLUGIN_ID, ex.getMessage(), ex));
			return false;
		}
	}

	public void setLighthouseDomain(LighthouseDomain lighthouseDomain) {
		this.lighthouseDomain = lighthouseDomain;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setSoftwareComponent(SoftwareComponent softwareComponent) {
		this.softwareComponent = softwareComponent;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof LighthouseDomain) {
			lighthouseDomain = (LighthouseDomain) obj;
		} else if (obj instanceof DeploymentContainer) {
			lighthouseDomain = ((DeploymentContainer) obj).getLighthouseDomain();
		} else if (obj instanceof Location) {
			location = (Location) obj;
			lighthouseDomain = location.getDeploymentContainer().getLighthouseDomain();
		} else if (obj instanceof Deployment) {
			location = CommonBaseActivator.getPlugin().getDomainService().getLocation((Deployment) obj);
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(obj);
		} else if (obj instanceof SoftwareComponent) {
			softwareComponent = (SoftwareComponent) obj;
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(obj);
		}
	}

	@Override
	public void addPages() {
		page = new WizardNewDeploymentPage("New Deployment");
		page.setLighthouseDomain(lighthouseDomain);
		page.setLocation(location);
		page.setSoftwareComponent(softwareComponent);
		page.setTitle("Deployment");
		page.setDescription("Deploy a software component.");
		addPage(page);
	}

	public Deployment getDeployment() {
		return newDeployment;
	}
}
