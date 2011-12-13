package com.mercatis.lighthouse3.services;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

import com.mercatis.lighthouse3.services.internal.EagerDeploymentRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerEnvironmentRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerEventRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerJobRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerOperationInstallationRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerOperationRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerProcessInstanceDefinitionFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerProcessInstanceFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerProcessTaskRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerSoftwareComponentRegistryFactoryServiceImpl;
import com.mercatis.lighthouse3.services.internal.EagerStatusRegistryFactoryServiceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("rawtypes")
public class Services extends Plugin {

	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.services";
	
	private ServiceRegistration softwareComponentRegistryFactoryServiceRegistration;
	
	private ServiceRegistration deploymentRegistryFactoryServiceRegistration;
	
	private ServiceRegistration environmentRegistryFactoryServiceRegistration;
	
	private ServiceRegistration processTaskRegistryFactoryServiceRegistration;
	
	private ServiceRegistration operationInstallationRegistryFactoryServiceRegistration;
	
	private ServiceRegistration jobRegistryFactoryServiceRegistration;
	
	private ServiceRegistration operationRegistryFactoryServiceRegistration;
	
	private ServiceRegistration statusRegistryFactoryServiceRegistration;
	
	private ServiceRegistration eventRegistryFactoryServiceRegistration;
	
	private ServiceRegistration processInstanceRegistryFactoryServiceRegistration;
	
	private ServiceRegistration processInstanceDefinitionRegistryFactoryServiceRegistration;

	private EventAdmin eventAdmin = null;
	
	private Map<EventHandler, List<ServiceRegistration>> registeredEventHandlers = new HashMap<EventHandler, List<ServiceRegistration>>();

	private ServiceTracker serviceTracker;
	
	private static Services plugin;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		plugin = this;
		
		Object service = new EagerSoftwareComponentRegistryFactoryServiceImpl(context);
		softwareComponentRegistryFactoryServiceRegistration = context.registerService(SoftwareComponentRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerDeploymentRegistryFactoryServiceImpl(context);
		deploymentRegistryFactoryServiceRegistration = context.registerService(DeploymentRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerEnvironmentRegistryFactoryServiceImpl(context);
		environmentRegistryFactoryServiceRegistration = context.registerService(EnvironmentRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerProcessTaskRegistryFactoryServiceImpl(context);
		processTaskRegistryFactoryServiceRegistration = context.registerService(ProcessTaskRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerOperationInstallationRegistryFactoryServiceImpl(context);
		operationInstallationRegistryFactoryServiceRegistration = context.registerService(OperationInstallationRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerJobRegistryFactoryServiceImpl(context);
		jobRegistryFactoryServiceRegistration = context.registerService(JobRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerOperationRegistryFactoryServiceImpl(context);
		operationRegistryFactoryServiceRegistration = context.registerService(OperationRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerStatusRegistryFactoryServiceImpl(context);
		statusRegistryFactoryServiceRegistration = context.registerService(StatusRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerEventRegistryFactoryServiceImpl(context);
		eventRegistryFactoryServiceRegistration = context.registerService(EventRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerProcessInstanceDefinitionFactoryServiceImpl(context);
		processInstanceDefinitionRegistryFactoryServiceRegistration = context.registerService(ProcessInstanceDefinitionRegistryFactoryService.class.getName(), service, null);
		
		service = new EagerProcessInstanceFactoryServiceImpl(context);
		processInstanceRegistryFactoryServiceRegistration = context.registerService(ProcessInstanceRegistryFactoryService.class.getName(), service, null);

		loadEventAdmin();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		processTaskRegistryFactoryServiceRegistration.unregister();
		environmentRegistryFactoryServiceRegistration.unregister();
		deploymentRegistryFactoryServiceRegistration.unregister();
		softwareComponentRegistryFactoryServiceRegistration.unregister();
		operationInstallationRegistryFactoryServiceRegistration.unregister();
		jobRegistryFactoryServiceRegistration.unregister();
		operationRegistryFactoryServiceRegistration.unregister();
		statusRegistryFactoryServiceRegistration.unregister();
		eventRegistryFactoryServiceRegistration.unregister();
		processInstanceDefinitionRegistryFactoryServiceRegistration.unregister();
		processInstanceRegistryFactoryServiceRegistration.unregister();

		eventAdmin = null;
		serviceTracker.close();
		
		super.stop(context);
	}
	
	/**
	 * Loads the OSGi EventAdmin
	 */
	@SuppressWarnings("unchecked")
	private void loadEventAdmin() {
		try {
			serviceTracker = new ServiceTracker(plugin.getBundle().getBundleContext(), EventAdmin.class.getName(), null);
			serviceTracker.open();
			eventAdmin = (EventAdmin)serviceTracker.getService();
		}
		catch (Exception e) {
			System.err.println("Error while loading event admin");
			e.printStackTrace();
		}
		if (eventAdmin == null) {
			throw new RuntimeException("Could not obtain an event admin.");
		}
	}
	
	/**
	 * Post an event to the EventAdmin <b>(non-blocking)</b>
	 * 
	 * @param event
	 */
	public static void postEvent(Event event) {
		if (plugin == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		plugin.eventAdmin.postEvent(event);
	}
	
	/**
	 * Send an event to the EventAdmin <b>(blocking)</b>
	 * 
	 * @param event
	 */
	public static void sendEvent(Event event) {
		if (plugin == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		plugin.eventAdmin.sendEvent(event);
	}
	
	/**
	 * Regiter an EventHanlder to the OSGi EventAdmin
	 * 
	 * @param eventHandler the handler to register
	 * @param topic the topic the listener will listen
	 * @param filter filters messages with specific properties from the topic (LDAP filter string)
	 */
	public static void registerEventHandler(EventHandler eventHandler, String topic, String filter) {
		registerEventHandler(eventHandler, new String[] {topic}, filter);
	}
	
	/**
	 * Regiter an EventHanlder to the OSGi EventAdmin as Listener
	 * 
	 * @param eventHandler the handler to register
	 * @param topics the topics the listener will listen
	 * @param filter filters messages with specific properties from the topic (LDAP filter string)
	 */
	public static void registerEventHandler(EventHandler eventHandler, String[] topics, String filter) {
		if (plugin == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		BundleContext context = plugin.getBundle().getBundleContext();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(EventConstants.EVENT_TOPIC, topics);
		if (filter != null) {
			properties.put(EventConstants.EVENT_FILTER, filter);
		}
		ServiceRegistration registration = context.registerService(EventHandler.class.getName(), eventHandler, properties);
		List<ServiceRegistration> registratrions = plugin.registeredEventHandlers.get(eventHandler);
		if (registratrions == null) {
			registratrions = new LinkedList<ServiceRegistration>();
			plugin.registeredEventHandlers.put(eventHandler, registratrions);
		}
		registratrions.add(registration);
	}
	
	/**
	 * Removes the given EventHanlder from the OSGi EventAdmin
	 * 
	 * @param eventHandler
	 */
	public static void unregisterEventHandler(EventHandler eventHandler) {
		if (plugin == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		List<ServiceRegistration> registrations = plugin.registeredEventHandlers.get(eventHandler);
		if (registrations != null) {
			for (ServiceRegistration registration : registrations) {
				registration.unregister();
			}
		}
		plugin.registeredEventHandlers.remove(eventHandler);
	}
}
