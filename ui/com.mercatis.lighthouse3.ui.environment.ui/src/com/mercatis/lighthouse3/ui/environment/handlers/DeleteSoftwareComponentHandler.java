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
package com.mercatis.lighthouse3.ui.environment.handlers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractDeleteHandler;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.security.internal.Security;

public class DeleteSoftwareComponentHandler extends AbstractDeleteHandler {

	private Set<SoftwareComponentContainer> containers;

	@Override
	protected void execute(Object element) throws ExecutionException {
		SoftwareComponent softwareComponent = (SoftwareComponent) element;
		LighthouseDomain lighthouseDomain = domainService.getLighthouseDomainByEntity(softwareComponent);

		Security.getService().deleteAssignmentsByEntity(lighthouseDomain.getProject(), softwareComponent);
		for (SoftwareComponent component : softwareComponent.getSubEntities()) {
			Security.getService().deleteAssignmentsByEntity(lighthouseDomain.getProject(), component);
		}
		
		SoftwareComponent parentEntity = softwareComponent.getParentEntity();
		if (parentEntity != null) {
			parentEntity.removeSubEntity(softwareComponent);
			domainService.updateSoftwareComponent(parentEntity);
		}
		domainService.deleteSoftwareComponent(softwareComponent);
		
		containers.add(lighthouseDomain.getSoftwareComponentContainer());
	}

	@Override
	protected void postExecution(ExecutionEvent event) throws ExecutionException {
		for (SoftwareComponentContainer container : containers) {
			CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(container);
		}
	}

	@Override
	protected void preExecution(ExecutionEvent event) throws ExecutionException {
		containers = new HashSet<SoftwareComponentContainer>();
	}
}
