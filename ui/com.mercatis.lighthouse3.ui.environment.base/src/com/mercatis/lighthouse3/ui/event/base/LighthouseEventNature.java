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
package com.mercatis.lighthouse3.ui.event.base;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class LighthouseEventNature implements IProjectNature {

	private IProject project;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
