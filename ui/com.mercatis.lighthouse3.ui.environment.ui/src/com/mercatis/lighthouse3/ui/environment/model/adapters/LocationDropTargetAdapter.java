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
package com.mercatis.lighthouse3.ui.environment.model.adapters;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.wizards.NewDeploymentWizard;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class LocationDropTargetAdapter implements DropTargetAdapter<Location> {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#handleDrop(int, java.lang.Object, java.lang.Object[], org.eclipse.swt.widgets.Shell)
	 */
	public boolean handleDrop(int operation, Location target, Object[] elements, Shell shell) {
		LighthouseDomain targetLighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(target);
		SoftwareComponent component = (SoftwareComponent) elements[0];

		NewDeploymentWizard wizard = new NewDeploymentWizard();
		wizard.setLighthouseDomain(targetLighthouseDomain);
		wizard.setLocation(target);
		wizard.setSoftwareComponent(component);

		WizardDialog dialog = new WizardDialog(shell, wizard);
		boolean result = (dialog.open() == Window.OK);
		
		if (result) {
			Deployment deployment = wizard.getDeployment();
			Location location = (Location) CommonBaseActivator.getPlugin().getDomainService().getParentForEntity(deployment);
			DeploymentContainer container = location.getDeploymentContainer();
		
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(container);
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(deployment);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#validateDrop(int, java.lang.Object, java.lang.Object[])
	 */
	public boolean validateDrop(int operation, Location target, Object[] elements) {
		if (elements.length > 1)
			return false;
		
		for (Object element : elements) {
			if (!CommonBaseActivator.getPlugin().getDomainService().inSameDomain(target, element))
				return false;

			if (!(element instanceof SoftwareComponent))
				return false;
			
			if (!CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_DEPLOY, element))
				return false;
		}
		return true;
	}

}
