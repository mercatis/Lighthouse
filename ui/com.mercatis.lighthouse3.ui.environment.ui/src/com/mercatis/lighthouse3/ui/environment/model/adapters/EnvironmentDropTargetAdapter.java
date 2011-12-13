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

import java.util.List;
import org.eclipse.swt.widgets.Shell;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class EnvironmentDropTargetAdapter implements DropTargetAdapter<Environment> {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#handleDrop(int, java.lang.Object, java.lang.Object[], org.eclipse.swt.widgets.Shell)
	 */
	public boolean handleDrop(int operation, Environment target, Object[] elements, Shell shell) {
		for (Object element : elements) {
			if (element instanceof Environment) {
				Environment dropped = (Environment) element;
				Environment formerParent = dropped.getParentEntity();
				if (formerParent != null) {
					for (Deployment deployment : dropped.getAssociatedDeployments()) {
						if (CommonBaseActivator.getPlugin().getDomainService().isDeploymentPartOfStatusTemplate(dropped, deployment)) {
							return false;
						}
					}
					formerParent.removeSubEntity(dropped);
					CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(formerParent);
					CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(formerParent);
				}
			
				target.addSubEntity(dropped);
				CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(dropped);
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(target);
			}
			
			if (element instanceof Deployment) {
				Deployment deployment = (Deployment) element;
				target.attachDeployment(deployment);
				CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(target);
			}
			
			if (element instanceof Location) {
				Location location = (Location) element;
				List<Deployment> deployments = location.getDeployments();
				for (Deployment deployment : deployments) {
					target.attachDeployment(deployment);
				}
				CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(target);
			}
			
			CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(target);
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(target);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#validateDrop(int, java.lang.Object, java.lang.Object[])
	 */
	public boolean validateDrop(int operation, Environment target, Object[] elements) {
		for (Object element : elements) {
			if (!CommonBaseActivator.getPlugin().getDomainService().inSameDomain(target, element))
				return false;

			if (!(element instanceof Environment || element instanceof Deployment || element instanceof Location))
				return false;
			 
			if (element instanceof Environment) {
				if(!(CodeGuard.hasRole(Role.ENVIRONMENT_DRAG, element) && CodeGuard.hasRole(Role.ENVIRONMENT_DROP, target)))
					return false;
			}
				
			
			if (element instanceof Deployment && !CodeGuard.hasRole(Role.DEPLOYMENT_INSTALL, element))
				return false;
			
			if (element instanceof Location) {
				List<Deployment> deployments = ((Location) element).getDeployments();
				for (Deployment deployment : deployments) {
					if (!CodeGuard.hasRole(Role.DEPLOYMENT_INSTALL, deployment)) {
						return false;
					}
				}
			}
		}

		return true;
	}

}
