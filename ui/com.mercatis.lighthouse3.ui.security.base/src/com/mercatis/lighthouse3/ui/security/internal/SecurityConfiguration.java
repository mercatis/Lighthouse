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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.security.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;


public class SecurityConfiguration {
	
	private static final String PROPERTY_SERVER_URL = "SERVER_URL";
	private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
	
	private String pluginId;
	private IProject iProject;
	private IEclipsePreferences preferences;
	
	/**
	 * @param pluginId
	 * @param iProject
	 */
	public SecurityConfiguration(String pluginId, IProject iProject) {
		this.pluginId = pluginId;
		this.iProject = iProject;
		initConfiguration();
	}
	
	protected void initConfiguration() {
		boolean wasOpen = iProject.isOpen(); 
		if(!wasOpen) {
			try {
				iProject.open(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		IScopeContext projectScope = new ProjectScope(iProject);
		this.preferences = projectScope.getNode(this.pluginId);
		try {
			this.preferences.sync();
		} catch (BackingStoreException ex) {
			ex.printStackTrace();
		}
		if(!wasOpen) {
			try {
				iProject.close(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getServerUrl() {
		return this.getConfigurationProperty(PROPERTY_SERVER_URL);
	}
	
	public void setServerUrl(String url) {
		this.setConfigurationProperty(PROPERTY_SERVER_URL, url);
	}
	
	public String getDefaultGroup() {
		return this.getConfigurationProperty(DEFAULT_GROUP);
	}
	
	public void setDefaultGroup(String defaultGroup) {
		this.setConfigurationProperty(DEFAULT_GROUP, defaultGroup);
	}

	/**
	 * @param key
	 * @param def
	 * @return
	 */
	private String getConfigurationProperty(String key, String def) {
		return this.preferences.get(key, def) != null ? this.preferences.get(key, def).trim() : null;
	}
	
	/**
	 * @param key
	 * @return
	 */
	private String getConfigurationProperty(String key) {
		return getConfigurationProperty(key, null);
	}
	
	/**
	 * @param key
	 * @param value
	 */
	private void setConfigurationProperty(String key, String value) {
		this.preferences.put(key, value);
		boolean wasOpen = iProject.isOpen(); 
		if(!wasOpen) {
			try {
				iProject.open(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		try {
			this.preferences.flush();
		} catch (BackingStoreException ex) {
			throw new RuntimeException(ex);
		}
		if(!wasOpen) {
			try {
				iProject.close(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
