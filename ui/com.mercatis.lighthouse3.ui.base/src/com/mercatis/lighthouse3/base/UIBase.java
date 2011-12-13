package com.mercatis.lighthouse3.base;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.security.internal.Security;

/**
 * The activator class controls the plug-in life cycle
 */
public class UIBase extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.ui.base";

	// The shared instance
	private static UIBase plugin;
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UIBase getDefault() {
		return plugin;
	}

	/**
	 * The constructor
	 */
	public UIBase() {
	}
	
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
	
	public static String[] getLighthousePluginIDs() {
		return new String[] { CommonBaseActivator.getPlugin().PLUGIN_ID, CommonBaseActivator.PLUGIN_ID, OperationBase.PLUGIN_ID, CommonBaseActivator.PLUGIN_ID, Security.PLUGIN_ID };
	}
}
