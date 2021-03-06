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
package com.mercatis.lighthouse3.ui.environment.base.services.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.LighthouseNatureService;

public class LighthouseNatureServiceImpl implements LighthouseNatureService {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.LighthouseNatureService#addNature(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain, java.lang.String)
	 */
	public void addNature(LighthouseDomain lighthouseDomain, String natureId) {
		try {
			IProject project = lighthouseDomain.getProject();
			IProjectDescription description = project.getDescription();
			String[] oldNatureIds = description.getNatureIds();
			String[] newNatureIds = new String[oldNatureIds.length + 1];
			System.arraycopy(oldNatureIds, 0, newNatureIds, 0, oldNatureIds.length);
			newNatureIds[oldNatureIds.length] = natureId;
			description.setNatureIds(newNatureIds);
			project.setDescription(description, null);
		} catch (CoreException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
			throw new RuntimeException(ex);
		}
	}

}
