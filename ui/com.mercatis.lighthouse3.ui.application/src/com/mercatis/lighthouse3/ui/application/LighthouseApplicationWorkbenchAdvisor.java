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
package com.mercatis.lighthouse3.ui.application;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;

public class LighthouseApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String INITIAL_PERSPECTIVE_ID = "com.mercatis.lighthouse3.ui.environment.EnvironmentPerspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new LighthouseApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
	}

	@Override
	public IAdaptable getDefaultPageInput() {
		return org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
	}
	
	@Override
	public boolean preShutdown() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IProgressMonitor pm = new NullProgressMonitor();
		boolean result = true;
		for (IProject ip : allProjects)
			try {
				ip.close(pm);
			} catch (CoreException e) {
				e.printStackTrace();
				result = false;
			}
		return result;
	}
	
	//Necessary for saveAndRestore to work with CommonNavigator
	@Override
	public void preStartup() {
		IDE.registerAdapters();
	}
	
	@Override
	public String getInitialWindowPerspectiveId() {
		return INITIAL_PERSPECTIVE_ID;
	}

}
