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

import java.util.Dictionary;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.Bundle;

public class LighthouseApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private static String version;

	public LighthouseApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new LighthouseApplicationActionBarAdvisor(configurer);
	}
	
	@Override
	public void preWindowOpen() {
		version = getVersion();
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1000, 700));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle(version);
	}
	
	public static String getVersion() {
		Bundle bundle = Platform.getBundle("com.mercatis.lighthouse3.ui.application");
		Dictionary<String, String> headers = bundle.getHeaders();
		String bundleVer = headers.get("Bundle-Version");
		int endIdx = bundleVer.lastIndexOf('.');
		String ver = bundleVer.substring(0, endIdx);
		return "Lighthouse " + ver;
	}

	@Override
	public void postWindowCreate() {
		super.postWindowCreate();
		
		//Remove some toolbar entries provides by the platform but not needed
		String[] menuItemsToRemove = new String[] {
				"org.eclipse.ui.edit.text.actionSet.navigation",
				"org.eclipse.ui.edit.text.actionSet.annotationNavigation",
				"org.eclipse.search.searchActionSet"
		};
		
		ICoolBarManager coolBarManager = getWindowConfigurer().getActionBarConfigurer().getCoolBarManager(); 
		for (String menuItem : menuItemsToRemove) {
			coolBarManager.remove(menuItem);
		}
		coolBarManager.update(true);
		
		// close all projects on startup
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			try {
				project.close(new NullProgressMonitor());
			} catch (CoreException e) {
			}
		}
	}
}
