package com.mercatis.lighthouse3.ui.security.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.mercatis.lighthouse3.ui.service.SecurityService;
import com.mercatis.lighthouse3.ui.service.impl.SecurityServiceImplementation;

/**
 * The activator class controls the plug-in life cycle
 */
public class Security extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.ui.security";

	// The shared instance
	private static Security plugin;
	
	private ContextAdapterFactory contextAdapterFactory;
	
	private Map<IProject, SecurityConfiguration> securityConfigurations = new HashMap<IProject, SecurityConfiguration>();
	private SecurityService securityService;
	
	protected void registerAdapters() {
		contextAdapterFactory = new ContextAdapterFactory();
		Platform.getAdapterManager().registerAdapters(contextAdapterFactory, String.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		securityService = new SecurityServiceImplementation();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		securityService = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Security getDefault() {
		return plugin;
	}

	protected void unregisterAdapters() {
		Platform.getAdapterManager().unregisterAdapters(contextAdapterFactory);
		contextAdapterFactory = null;
	}
	
	public SecurityConfiguration getSecurityConfiguration(IProject iProject) {
		SecurityConfiguration securityConfiguration = this.securityConfigurations.get(iProject);
		if (securityConfiguration == null) {
			securityConfiguration = new SecurityConfiguration(Security.PLUGIN_ID, iProject);
			securityConfigurations.put(iProject, securityConfiguration);
		}
		return securityConfiguration;
	}
	
	public static SecurityService getService() {
		return getDefault().securityService;
	}
}
