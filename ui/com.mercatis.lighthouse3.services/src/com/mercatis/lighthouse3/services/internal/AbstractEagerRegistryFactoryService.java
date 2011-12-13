package com.mercatis.lighthouse3.services.internal;

import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EventRegistryFactoryService;
import com.mercatis.lighthouse3.services.OperationInstallationRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessInstanceDefinitionRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;

public abstract class AbstractEagerRegistryFactoryService<S, T extends S> {
	protected Map<String, T> registries = new TreeMap<String, T>();

	private BundleContext context;
	
	protected class CommonData {
		public final String user;
		public final String serverUrl;
		public final String password;
		
		private CommonData(IProject project) {
			String lhDomain = getServerDomainKey(project);
			user = Security.getLoginName(lhDomain);
			serverUrl = getServerUrl(project);
			password = new String(Security.getLoginPassword(lhDomain));
		}
	}

	public AbstractEagerRegistryFactoryService(BundleContext context) {
		this.context = context;
	}

	public S getRegistry(String lighthouseDomain) {
		return registries.get(getContextKey("//LH3/".concat(lighthouseDomain)));
	}

	public S getRegistry(IProject project) {
		String serverDomainKey = getServerDomainKey(project);
		String context = getContextKey(serverDomainKey);
		T registry = registries.get(context);
		if (registry == null) {
			registry = createImpl(project);
			registries.put(context, registry);
		}
		return registry;
	}
	
	protected abstract T createImpl(IProject project);

	protected CommonData getData(IProject project) {
		return new CommonData(project);
	}
	
	private String getServerDomainKey(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
		return "//LH3/".concat(preferences.get("DOMAIN_SERVER_DOMAIN_KEY", "").trim());
	}

	private String getContextKey(String lighthouseDomain) {
		String user = Security.getLoginName(lighthouseDomain);
		return lighthouseDomain + "#|#" + user;
	}
	
	private String getServerUrl(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
		return preferences.get("DOMAIN_URL", "").trim();
	}

	protected SoftwareComponentRegistry getSoftwareComponentRegistry(IProject project) {
		SoftwareComponentRegistry softwareComponentRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(SoftwareComponentRegistryFactoryService.class.getName());
			if (reference != null) {
				SoftwareComponentRegistryFactoryService service = (SoftwareComponentRegistryFactoryService) context.getService(reference);
				softwareComponentRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return softwareComponentRegistry;
	}

	protected EventRegistry getEventRegistry(IProject project) {
		EventRegistry eventRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(EventRegistryFactoryService.class.getName());
			if (reference != null) {
				EventRegistryFactoryService service = (EventRegistryFactoryService) context.getService(reference);
				eventRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return eventRegistry;
	}

	/**
	 * @param project
	 * @return
	 */
	protected EnvironmentRegistry getEnvironmentRegistry(IProject project) {
		EnvironmentRegistry environmentRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(EnvironmentRegistryFactoryService.class.getName());
			if (reference != null) {
				EnvironmentRegistryFactoryService service = (EnvironmentRegistryFactoryService) context.getService(reference);
				environmentRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return environmentRegistry;
	}

	/**
	 * @param project
	 * @return
	 */
	protected ProcessTaskRegistry getProcessTaskRegistry(IProject project) {
		ProcessTaskRegistry processTaskRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(ProcessTaskRegistryFactoryService.class.getName());
			if (reference != null) {
				ProcessTaskRegistryFactoryService service = (ProcessTaskRegistryFactoryService) context.getService(reference);
				processTaskRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return processTaskRegistry;
	}

	/**
	 * @param project
	 * @return
	 */
	protected DeploymentRegistry getDeploymentRegistry(IProject project) {
		DeploymentRegistry deploymentRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(DeploymentRegistryFactoryService.class.getName());
			if (reference != null) {
				DeploymentRegistryFactoryService service = (DeploymentRegistryFactoryService) context.getService(reference);
				deploymentRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return deploymentRegistry;
	}
	
	protected ProcessInstanceDefinitionRegistry getProcessInstanceDefinitionRegistry(IProject project) {
		ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(ProcessInstanceDefinitionRegistryFactoryService.class.getName());
			if (reference != null) {
				ProcessInstanceDefinitionRegistryFactoryService service = (ProcessInstanceDefinitionRegistryFactoryService) context.getService(reference);
				processInstanceDefinitionRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return processInstanceDefinitionRegistry;
	}

	protected OperationInstallationRegistry getOperationInstallationRegistry(IProject project) {
		OperationInstallationRegistry operationInstallationRegistry = null;
		ServiceReference<?> reference = null;
		try {
			reference = context.getServiceReference(OperationInstallationRegistryFactoryService.class.getName());
			if (reference != null) {
				OperationInstallationRegistryFactoryService service = (OperationInstallationRegistryFactoryService) context.getService(reference);
				operationInstallationRegistry = service.getRegistry(project);
			}
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
		return operationInstallationRegistry;
	}
}
