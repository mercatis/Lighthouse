package com.mercatis.lighthouse3.ui.processinstance.base;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.processinstance.base.services.ProcessInstanceDefinitionService;
import com.mercatis.lighthouse3.ui.processinstance.base.services.ProcessInstanceService;
import com.mercatis.lighthouse3.ui.processinstance.base.services.impl.ProcessInstanceDefinitionServiceImpl;
import com.mercatis.lighthouse3.ui.processinstance.base.services.impl.ProcessInstanceServiceImpl;
import com.mercatis.lighthouse3.ui.processinstance.base.services.impl.ProcessInstanceStatusServiceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProcessInstanceBase extends AbstractUIPlugin {

	private ProcessInstanceService processInstanceService;
	private ProcessInstanceDefinitionService processInstanceDefinitionService;
	private Map<IEditorPart, ProcessInstanceStatusServiceImpl> processInstanceStatusServices = new HashMap<IEditorPart, ProcessInstanceStatusServiceImpl>();
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.ui.processinstance.base";

	// The shared instance
	private static ProcessInstanceBase plugin;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		processInstanceService = new ProcessInstanceServiceImpl(context);
		processInstanceDefinitionService = new ProcessInstanceDefinitionServiceImpl(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		processInstanceService = null;
		processInstanceDefinitionService = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ProcessInstanceBase getDefault() {
		return plugin;
	}

	public static ProcessInstanceService getProcessInstanceService() {
		if (plugin == null || plugin.processInstanceService == null) {
			throw new IllegalStateException("ProcessInstance plugin stopped");
		}
		return plugin.processInstanceService;
	}

	public static ProcessInstanceDefinitionService getProcessInstanceDefinitionService() {
		if (plugin == null || plugin.processInstanceDefinitionService == null) {
			throw new IllegalStateException("ProcessInstance plugin stopped");
		}
		return plugin.processInstanceDefinitionService;
	}
	
	public static ProcessInstanceStatusServiceImpl getInstanceAggregationService(IEditorPart editor) {
		if (plugin == null) {
			throw new IllegalStateException("ProcessInstance plugin stopped");
		}
		ProcessInstanceStatusServiceImpl service = plugin.processInstanceStatusServices.get(editor);
		if (service == null) {
			throw new IllegalStateException("No aggregationService registered for " + editor);
		}
		return service;
	}
	
	public static void registerInstanceAggregationService(IEditorPart editor, ProcessInstanceDefinition definition) {
		if (plugin == null) {
			throw new IllegalStateException("ProcessInstance plugin stopped");
		}
		if (plugin.processInstanceStatusServices.get(editor) == null) {
			ProcessInstanceStatusServiceImpl aggregationService = new ProcessInstanceStatusServiceImpl(definition);
			plugin.processInstanceStatusServices.put(editor, aggregationService);
		}
	}
	
	public static void unregisterinstanceAggregationService(IEditorPart editor) {
		if (plugin == null) {
			throw new IllegalStateException("ProcessInstance plugin stopped");
		}
		plugin.processInstanceStatusServices.remove(editor);
	}
}
