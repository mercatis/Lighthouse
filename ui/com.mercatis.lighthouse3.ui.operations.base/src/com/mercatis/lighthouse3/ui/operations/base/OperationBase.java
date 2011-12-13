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
package com.mercatis.lighthouse3.ui.operations.base;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.adapters.impl.ContextAdapterFactory;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.operations.base.service.JobService;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationInstallationService;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationService;
import com.mercatis.lighthouse3.ui.operations.base.service.impl.JobServiceImpl;
import com.mercatis.lighthouse3.ui.operations.base.service.impl.OperationInstallationServiceImpl;
import com.mercatis.lighthouse3.ui.operations.base.service.impl.OperationServiceImpl;


public class OperationBase extends Plugin {

	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.ui.operations.base";
	
	private static OperationBase plugin;
	
	private OperationService operationService;
	private OperationInstallationService operationInstallationService;
	private JobService jobService;
	private List<OperationsChangedListener> operationsChangedListener;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		operationService = new OperationServiceImpl(context);
		operationInstallationService = new OperationInstallationServiceImpl(context);
		jobService = new JobServiceImpl(context);
		operationsChangedListener = new ArrayList<OperationsChangedListener>();
		registerAdapters();
		plugin = this;
	}
	
	private ContextAdapterFactory contextAdapterFactory;
	
	protected void registerAdapters() {
		contextAdapterFactory = new ContextAdapterFactory();
		Platform.getAdapterManager().registerAdapters(contextAdapterFactory, Job.class);
		Platform.getAdapterManager().registerAdapters(contextAdapterFactory, OperationInstallation.class);
		Platform.getAdapterManager().registerAdapters(contextAdapterFactory, OperationInstallationWrapper.class);
	}
	
	protected void unregisterAdapters() {
		Platform.getAdapterManager().unregisterAdapters(contextAdapterFactory);
		contextAdapterFactory = null;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		unregisterAdapters();
		operationsChangedListener = null;
		jobService = null;
		operationInstallationService = null;
		operationService = null;
		super.stop(context);
	}
	
	public static OperationService getOperationService() {
		if (plugin.operationService == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		return plugin.operationService;
	}
	
	public static OperationInstallationService getOperationInstallationService() {
		if (plugin.operationInstallationService == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		return plugin.operationInstallationService;
	}
	
	public static JobService getJobService() {
		if (plugin.jobService == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		return plugin.jobService;
	}
	
	public static void fireOperationsChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue, Object newValue) {
		if (plugin.operationsChangedListener == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		for (OperationsChangedListener listener : plugin.operationsChangedListener) {
			listener.operationsChanged(lighthouseDomain, source, property, oldValue, newValue);
		}
	}
	
	public static OperationBase getPlugin() {
		return plugin;
	}
	
	public static void addOperationsChangedListener(OperationsChangedListener listener) {
		if (plugin.operationsChangedListener == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		plugin.operationsChangedListener.add(listener);
	}

	public static void removeOperationsChangedListener(OperationsChangedListener listener) {
		if (plugin.operationsChangedListener == null)
			throw new IllegalStateException("OperationsPlugin stopped.");
		plugin.operationsChangedListener.remove(listener);
	}
}
