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
package com.mercatis.lighthouse3.ui.environment.base.services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public abstract class AbstractConfiguration {
	
	protected LighthouseDomain lighthouseDomain;
	protected String pluginId;
	protected IEclipsePreferences preferences;
	
	/**
	 * @param lighthouseDomain
	 * @param pluginId
	 */
	public AbstractConfiguration(LighthouseDomain lighthouseDomain, String pluginId) {
		this.lighthouseDomain = lighthouseDomain;
		this.pluginId = pluginId;
		initConfiguration();
	}
	
	/**
	 * 
	 */
	protected void initConfiguration() {
		IProject project = lighthouseDomain.getProject();
		boolean wasOpen = project.isOpen(); 
		//This has to be done in order to be able to edit preferences even if the iproject is closed
		if(!wasOpen) {
			try {
				project.open(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		IScopeContext projectScope = new ProjectScope(project);
		this.preferences = projectScope.getNode(this.pluginId);
		try {
			this.preferences.sync();
		} catch (BackingStoreException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
		}
		if(!wasOpen) {
			try {
				project.close(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	public String getConfigurationProperty(String key, String def) {
		return this.preferences.get(key, def) != null ? this.preferences.get(key, def).trim() : null;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getConfigurationProperty(String key) {
		return getConfigurationProperty(key, null);
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void setConfigurationProperty(String key, String value) {
		if (value == null) {
			return;
		}
		this.preferences.put(key, value);
		IProject project = lighthouseDomain.getProject();
		//This has to be done in order to be able to edit preferences even if the iproject is closed
		boolean wasOpen = project.isOpen(); 
		if(!wasOpen) {
			try {
				project.open(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		try {
			this.preferences.flush();
		} catch (BackingStoreException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
			throw new RuntimeException(ex);
		}
		if(!wasOpen) {
			try {
				project.close(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**Check whether a given property exists
	 * @param key
	 * @param def
	 * @return
	 */
	public boolean existsProperty(String key) {
		return this.preferences.get(key, null) != null ? true : false;
	}
}
