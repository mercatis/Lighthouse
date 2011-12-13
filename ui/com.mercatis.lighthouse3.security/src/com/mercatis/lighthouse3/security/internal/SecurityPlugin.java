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
package com.mercatis.lighthouse3.security.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.mercatis.lighthouse3.security.SecurityBinding;
import com.mercatis.lighthouse3.security.api.AuthenticationModule;
import com.mercatis.lighthouse3.security.api.AuthorizationModule;
import com.mercatis.lighthouse3.security.api.LoginModule;

public class SecurityPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.security";
	
	private static final String EXTENSION_POINT_ID = "com.mercatis.lighthouse3.security";
	
	private static final String ATTRIBUTE_EXECUTABLE_EXTENSION = "class";
	
	private static final String ATTRIBUTE_CONTEXT = "context";

	private static final String ELEMENT_LOGIN_MODULE = "loginModule";

	private static final String ELEMENT_AUTHENTICATION_MODULE = "authenticationModule";

	private static final String ELEMENT_AUTHORIZATION_MODULE = "authorizationModule";

	// The shared instance
	private static SecurityPlugin plugin;
	
	private SecurityBindingImpl binding;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SecurityPlugin getDefault() {
		return plugin;
	}
	
	public SecurityBinding getSecurityBinding() {
		return binding;
	}

	protected IConfigurationElement getSecurityBinding(Context context) {
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement element : configurationElements) {
			String securityBindingContext = element.getAttribute(ATTRIBUTE_CONTEXT);
			if (securityBindingContext != null) {
				Context tmp = new Context(securityBindingContext);
				if (tmp.isParentContext(context)) {
					return element;
				}
			}
		}
		
		return null;
	}
	
	protected LoginModule getLoginModule(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren(ELEMENT_LOGIN_MODULE);
		if (children.length > 0) {
			try {
				return (LoginModule) children[0].createExecutableExtension(ATTRIBUTE_EXECUTABLE_EXTENSION);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected AuthenticationModule getAuthenticationModule(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren(ELEMENT_AUTHENTICATION_MODULE);
		if (children.length > 0) {
			try {
				return (AuthenticationModule) children[0].createExecutableExtension(ATTRIBUTE_EXECUTABLE_EXTENSION);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected AuthorizationModule getAuthorizationModule(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren(ELEMENT_AUTHORIZATION_MODULE);
		if (children.length > 0) {
			try {
				return (AuthorizationModule) children[0].createExecutableExtension(ATTRIBUTE_EXECUTABLE_EXTENSION);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * @param context
	 * @return
	 */
	public boolean login(Context context) {
		IConfigurationElement element = getSecurityBinding(context);
		if (element == null)
			return false;
		
		LoginModule loginModule = getLoginModule(element);
		if (loginModule == null)
			return false;
		SecurityBinding authenticationBinding = loginModule.login(context.toString());
		if (authenticationBinding == null)
			return false;
		
		AuthenticationModule authenticationModule = getAuthenticationModule(element);
		if (!authenticationModule.authenticate(context.toString(), authenticationBinding))
			return false;
		
		AuthorizationModule authorizationModule = getAuthorizationModule(element);
		SecurityBinding authorizedBinding = authorizationModule.authorize(context.toString(), authenticationBinding);
		if (authorizedBinding == null)
			return false;
		
		if (this.binding == null)
			this.binding = (SecurityBindingImpl) authenticationBinding;
		else
			this.binding = (SecurityBindingImpl) this.binding.merge((SecurityBindingImpl) authenticationBinding);
		this.binding = (SecurityBindingImpl) this.binding.merge((SecurityBindingImpl) authorizedBinding);
		
		return true;
	}
	
	/**
	 * @param context
	 * @return
	 */
	public boolean logout(Context context) {
		binding.removeContext(context);
		return false;
	}

	/**
	 * @param context
	 * @return
	 */
	public boolean isAuthenticated(Context context) {
		return binding == null ? false : this.binding.getName(context) != null;
	}
	
}
