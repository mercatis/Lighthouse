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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.editors.DeploymentEditor;

public class EditDeploymentHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		Deployment deployment = (Deployment) element;
		LighthouseDomain lighthouseDomain = domainService.getLighthouseDomainByEntity(deployment);

		GenericEditorInput<Deployment> input = new GenericEditorInput<Deployment>(lighthouseDomain, deployment);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, input, DeploymentEditor.class.getName());
		} catch (PartInitException ex) {
			throw new ExecutionException(ex.getMessage(), ex);
		}
	}

}
