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

import org.eclipse.swt.widgets.Shell;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class EnvironmentContainerDropTargetAdapter implements DropTargetAdapter<EnvironmentContainer> {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#handleDrop(int, java.lang.Object, java.lang.Object[], org.eclipse.swt.widgets.Shell)
	 */
	public boolean handleDrop(int operation, EnvironmentContainer target, Object[] elements, Shell shell) {
		for (Object element : elements) {
			Environment dropped = (Environment) element;
			Environment formerParent = dropped.getParentEntity();
			
			if (formerParent != null) {
				for (Deployment deployment : dropped.getAssociatedDeployments()) {
					if (CommonBaseActivator.getPlugin().getDomainService().isDeploymentPartOfStatusTemplate(dropped, deployment)) {
						return false;
					}
				}
				formerParent.removeSubEntity(dropped);
				CommonBaseActivator.getPlugin().getDomainService().updateEnvironment((Environment) formerParent);
			
				dropped.setParentEntity(null);
				CommonBaseActivator.getPlugin().getDomainService().updateEnvironment(dropped);
				
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(target);
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.common.DropTargetAdapter#validateDrop(int, java.lang.Object, java.lang.Object[])
	 */
	public boolean validateDrop(int operation, EnvironmentContainer target, Object[] elements) {
		for (Object element : elements) {
			if (!CommonBaseActivator.getPlugin().getDomainService().inSameDomain(target, element))
				return false;

			if (!(element instanceof Environment))
				return false;
			
			if (!CodeGuard.hasRole(Role.ENVIRONMENT_DROP, target))
				return false;
			
			if (!CodeGuard.hasRole(Role.ENVIRONMENT_DRAG, element))
				return false;
		}
		return true;
	}
}
