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
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.internal.Security;

public class DeleteEnvironmentHandler extends AbstractDeleteHandler {

	private Set<EnvironmentContainer> containers;

	protected void execute(Object element) throws ExecutionException {
			Environment environment = (Environment) element;
			LighthouseDomain lighthouseDomain = domainService.getLighthouseDomainByEntity(environment);

			Security.getService().deleteAssignmentsByEntity(lighthouseDomain.getProject(), environment);
			for (Environment env : environment.getSubEntities()) {
				Security.getService().deleteAssignmentsByEntity(lighthouseDomain.getProject(), env);
			}
			
			Environment parentEntity = environment.getParentEntity();
			if (parentEntity != null) {
				parentEntity.removeSubEntity(environment);
				domainService.updateEnvironment(parentEntity);
			}
			domainService.deleteEnvironment(environment);
			
			containers.add(lighthouseDomain.getEnvironmentContainer());
	}

	@Override
	protected void postExecution(ExecutionEvent event) throws ExecutionException {
			for (EnvironmentContainer container : containers) {
				CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(container);
			}
	}

	@Override
	protected void preExecution(ExecutionEvent event) throws ExecutionException {
		containers = new HashSet<EnvironmentContainer>();
	}

}
