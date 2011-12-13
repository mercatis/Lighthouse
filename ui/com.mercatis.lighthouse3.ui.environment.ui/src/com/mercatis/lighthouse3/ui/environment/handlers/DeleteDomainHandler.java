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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.navigator.CommonNavigator;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractDeleteHandler;
import com.mercatis.lighthouse3.ui.common.CommonUIActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class DeleteDomainHandler extends AbstractDeleteHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler#execute(java.lang.Object)
	 */
	@Override
	protected void execute(Object element) throws ExecutionException {
		CommonNavigator navigator = (CommonNavigator)getActivePart();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (element instanceof LighthouseDomain) {
			IResource resources[] = { ((LighthouseDomain) element).getProject() };
			try {
				workspace.delete(resources, true, new NullProgressMonitor());
			} catch (CoreException e) {
				CommonUIActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonUIActivator.PLUGIN_ID, e.getMessage(), e));
			}
		}
		navigator.getCommonViewer().refresh(null, true);
	}
}
