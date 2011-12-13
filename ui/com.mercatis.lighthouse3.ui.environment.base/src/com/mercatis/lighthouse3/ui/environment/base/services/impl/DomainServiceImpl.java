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
package com.mercatis.lighthouse3.ui.environment.base.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import com.mercatis.lighthouse3.commons.commons.EnumerationRange;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.RegistryFactoryService;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.util.RegistryFactoryServiceUtil;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.LighthouseDomainNature;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainConfiguration;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;

public class DomainServiceImpl implements DomainService, LighthouseDomainListener {
	private class DomainInfo {
		public final LighthouseDomain domain;
		public DomainConfiguration domainConfig;
		public DeploymentRegistry deploymentRegistry;
		public EnvironmentRegistry environmentRegistry;
		public ProcessTaskRegistry processTaskRegistry;
		public SoftwareComponentRegistry softwareComponentRegistry;
		public final IProject project;

		public DomainInfo(LighthouseDomain domain) {
			this.domain = domain;
			project = domain.getProject();
			domainConfig = new DomainConfigurationImpl(domain);
		}
			
		@SuppressWarnings("unchecked")
		private <T> T createService(Class<? extends RegistryFactoryService<T>> clazz) {
			ServiceReference<?> ref = context.getServiceReference(clazz.getName());
			if (ref == null)
				throw new RuntimeException("error getting "+clazz.getName());
			
			return (T) ((RegistryFactoryService<T>) context.getService(ref)).getRegistry(project);
		}
		
		public void modifyRegistries(boolean create) {
			deploymentRegistry = create ? createService(DeploymentRegistryFactoryService.class) : null;
			environmentRegistry = create ? createService(EnvironmentRegistryFactoryService.class) : null;
			processTaskRegistry = create ? createService(ProcessTaskRegistryFactoryService.class) : null;
			softwareComponentRegistry = create ? createService(SoftwareComponentRegistryFactoryService.class) : null;
		}
	}

	private final BundleContext context;
	private final ListenerList listener = new ListenerList();

	private final Map<String, DomainInfo> domainMap = new HashMap<String, DomainInfo>();
	private final Map<IProject, DomainInfo> projectMap = new HashMap<IProject, DomainInfo>();

	/**
	 * @param domainLookupService
	 */
	public DomainServiceImpl(BundleContext context) {
		this.context = context;
		CommonBaseActivator.getPlugin().getLighthouseDomainBroadCaster().addListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# addLighthouseDomainNature(org.eclipse.core.resources.IProject)
	 */
	public void addLighthouseDomainNature(LighthouseDomain lighthouseDomain) {
		CommonBaseActivator.getPlugin().getNatureService().addNature(lighthouseDomain, LighthouseDomainNature.class.getName());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# createLighthouseDomain(org.eclipse.core.resources.IProject)
	 */
	public LighthouseDomain createLighthouseDomain(IProject project) {
		return addLighthouseDomain(project);
	}
	
	private LighthouseDomain addLighthouseDomain(IProject project) {
		LighthouseDomain lighthouseDomain = new LighthouseDomain(project);
		DomainInfo di = new DomainInfo(lighthouseDomain);
		domainMap.put(lighthouseDomain.getServerDomainKey(), di);
		projectMap.put(project, di);
		return lighthouseDomain;
	}
	
	private void removeLighthouseDomain(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.remove(lighthouseDomain);
		if (di!=null)
			projectMap.remove(di.project);
	}

	/*
	 * (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.DomainService# deleteDeployment (com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public void deleteDeployment(Deployment deployment) {
		getDeploymentRegistry(deployment).delete(deployment);
	}

	/*
	 * (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.DomainService# deleteEnvironment
	 * (com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void deleteEnvironment(Environment environment) {
		getEnvironmentRegistry(environment).delete(environment);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# deleteProcessTask (com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void deleteProcessTask(ProcessTask processTask) {
		getProcessTaskRegistry(processTask).delete(processTask);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# deleteSoftwareComponent
	 * (com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void deleteSoftwareComponent(SoftwareComponent softwareComponent) {
		getSoftwareComponentRegistry(softwareComponent).delete(softwareComponent);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getAllDeployments
	 * (com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public List<Deployment> getAllDeployments(LighthouseDomain lighthouseDomain) {
		List<Location> locations = lighthouseDomain.getDeploymentContainer().getLocations();
		Set<Deployment> deployments = new HashSet<Deployment>();
		for (Location location : locations) {
			deployments.addAll(location.getDeployments());
		}
		return new LinkedList<Deployment>(deployments);
	}

	public DeploymentRegistry getDeploymentRegistry(Object entity) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainByEntity(entity);
		return getDeploymentRegistry(lighthouseDomain);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getDeployments (com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public List<Deployment> getDeployments(Environment environment) {
		return new LinkedList<Deployment>(environment.getAllDeployments());
	}

	public List<Deployment> getDeployments(Location location) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainByEntity(location);
		DeploymentRegistry registry = getDeploymentRegistry(lighthouseDomain);

		return registry.findAtLocation(location.getLabel());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getDomainConfiguration
	 * (com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public DomainConfiguration getDomainConfiguration(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.get(lighthouseDomain.getServerDomainKey());
		return di==null ? null : di.domainConfig;
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getEntityByPath(java.lang.String)
	 */
	public Object getEntityByPath(String path) {
		// TODO implement
		throw new RuntimeException("YET TO BE IMPLEMENTED");
	}

	public EnvironmentRegistry getEnvironmentRegistry(Object entity) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainByEntity(entity);
		return getEnvironmentRegistry(lighthouseDomain);
	}

	public List<Environment> getEnvironments(Environment environment) {
		return new LinkedList<Environment>(environment.getDirectSubEntities());
	}

	public List<Environment> getEnvironments(EnvironmentContainer environmentContainer) {
		LighthouseDomain lighthouseDomain = environmentContainer.getLighthouseDomain();
		EnvironmentRegistry registry = getEnvironmentRegistry(lighthouseDomain);
		List<String> codes = registry.findAllTopLevelComponentCodes();
		List<Environment> environments = new ArrayList<Environment>(codes.size());
		for (String code : codes) {
			Environment environment = registry.findByCode(code);
			environments.add(environment);
		}
		return environments;
	}

	protected String findServerUrlForProject(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode("com.mercatis.lighthouse3.ui.environment.base");
		return preferences.get("DOMAIN_URL", "").trim();
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getLighthouseDomain(org.eclipse.core.resources.IProject)
	 */
	public LighthouseDomain getLighthouseDomain(IProject project) {
		DomainInfo di = projectMap.get(project);
		LighthouseDomain lighthouseDomain;
		if (di==null) {
			lighthouseDomain = addLighthouseDomain(project);
			di = projectMap.get(project);
		} else {
			lighthouseDomain = di.domain;
		}
		try {
			if (!project.isOpen() || !project.hasNature(LighthouseDomainNature.class.getName())) {
				return lighthouseDomain;
			}
		} catch (CoreException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
		}
		return lighthouseDomain;
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getLighthouseDomainByEntity(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomainByEntity(Object entity) {
		if (entity instanceof LighthouseDomain)
			return (LighthouseDomain) entity;

		return ((DomainBoundEntityAdapter) Platform.getAdapterManager().getAdapter(entity, DomainBoundEntityAdapter.class)).getLighthouseDomain(entity);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getLocation(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public Location getLocation(Deployment deployment) {
		return (Location) getParentForEntity(deployment);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getLocations
	 * (com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer)
	 */
	public List<Location> getLocations(DeploymentContainer deploymentContainer) {
		LighthouseDomain lighthouseDomain = deploymentContainer.getLighthouseDomain();
		DeploymentRegistry registry = getDeploymentRegistry(lighthouseDomain);
		Set<String> codes = new HashSet<String>(registry.findAllLocations());
		List<Location> locations = new ArrayList<Location>(codes.size());
		for (String code : codes) {
			Location location = new Location(deploymentContainer, code);
			locations.add(location);
		}
		return locations;
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getParentForEntity(java.lang.Object)
	 */
	public Object getParentForEntity(Object entity) {
		return ((HierarchicalEntityAdapter) Platform.getAdapterManager().getAdapter(entity, HierarchicalEntityAdapter.class)).getParent(entity);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getPathForEntity(java.lang.Object)
	 */
	public String getPathForEntity(Object entity) {
		// TODO later
		throw new RuntimeException("YET TO BE IMPLEMENTED");
	}

	public ProcessTaskRegistry getProcessTaskRegistry(Object entity) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainByEntity(entity);
		return getProcessTaskRegistry(lighthouseDomain);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getProcessTasks (com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public List<ProcessTask> getProcessTasks(ProcessTask processTask) {
		return new LinkedList<ProcessTask>(processTask.getDirectSubEntities());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getProcessTasks
	 * (com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer)
	 */
	public List<ProcessTask> getProcessTasks(ProcessTaskContainer processTaskContainer) {
		LighthouseDomain lighthouseDomain = processTaskContainer.getLighthouseDomain();
		ProcessTaskRegistry registry = getProcessTaskRegistry(lighthouseDomain);
		List<String> codes = registry.findAllTopLevelComponentCodes();
		List<ProcessTask> processTasks = new ArrayList<ProcessTask>(codes.size());
		for (String code : codes) {
			ProcessTask processTask = registry.findByCode(code);
			processTasks.add(processTask);
		}
		return processTasks;
	}

	public SoftwareComponentRegistry getSoftwareComponentRegistry(Object entity) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainByEntity(entity);
		return getSoftwareComponentRegistry(lighthouseDomain);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getSoftwareComponents
	 * (com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public List<SoftwareComponent> getSoftwareComponents(SoftwareComponent softwareComponent) {
		return new LinkedList<SoftwareComponent>(softwareComponent.getDirectSubEntities());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# getSoftwareComponents(com.mercatis.lighthouse3.ui.environment.base.model.
	 * SoftwareComponentContainer)
	 */
	public List<SoftwareComponent> getSoftwareComponents(SoftwareComponentContainer softwareComponentContainer) {
		LighthouseDomain lighthouseDomain = softwareComponentContainer.getLighthouseDomain();
		SoftwareComponentRegistry registry = getSoftwareComponentRegistry(lighthouseDomain);
		List<String> codes = registry.findAllTopLevelComponentCodes();
		List<SoftwareComponent> softwareComponents = new ArrayList<SoftwareComponent>(codes.size());
		for (String code : codes) {
			SoftwareComponent softwareComponent = registry.findByCode(code);
			softwareComponents.add(softwareComponent);
		}
		return softwareComponents;
	}

	public boolean inSameDomain(Object entity1, Object entity2) {
		LighthouseDomain domain1 = getLighthouseDomainByEntity(entity1);
		LighthouseDomain domain2 = getLighthouseDomainByEntity(entity2);
		if ((domain1 == null) || (domain2 == null))
			return false;
		return domain1.equals(domain2);
	}

	public void persistDeployment(LighthouseDomain lighthouseDomain, Deployment deployment) {
		getDeploymentRegistry(lighthouseDomain).persist(deployment);
		deployment.setLighthouseDomain(lighthouseDomain.getServerDomainKey());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# persistEnvironment
	 * (com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void persistEnvironment(LighthouseDomain lighthouseDomain, Environment environment) {
		getEnvironmentRegistry(lighthouseDomain).persist(environment);
		environment.setLighthouseDomain(lighthouseDomain.getServerDomainKey());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# persistProcessTask
	 * (com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void persistProcessTask(LighthouseDomain lighthouseDomain, ProcessTask processTask) {
		getProcessTaskRegistry(lighthouseDomain).persist(processTask);
		processTask.setLighthouseDomain(lighthouseDomain.getServerDomainKey());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# persistSoftwareComponent
	 * (com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void persistSoftwareComponent(LighthouseDomain lighthouseDomain, SoftwareComponent softwareComponent) {
		getSoftwareComponentRegistry(lighthouseDomain).persist(softwareComponent);
		softwareComponent.setLighthouseDomain(lighthouseDomain.getServerDomainKey());
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# updateDeployment (com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public void updateDeployment(Deployment deployment) {
		getDeploymentRegistry(deployment).update(deployment);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# updateEnvironment (com.mercatis.lighthouse3.domainmodel.environment.Environment)
	 */
	public void updateEnvironment(Environment environment) {
		getEnvironmentRegistry(environment).update(environment);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# updateProcessTask (com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public void updateProcessTask(ProcessTask processTask) {
		getProcessTaskRegistry(processTask).update(processTask);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# updateSoftwareComponent
	 * (com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public void updateSoftwareComponent(SoftwareComponent softwareComponent) {
		getSoftwareComponentRegistry(softwareComponent).update(softwareComponent);
	}

	public void update(Object element) {
		if (element instanceof Deployment) {
			updateDeployment((Deployment) element);
		} else if (element instanceof Environment) {
			updateEnvironment((Environment) element);
		} else if (element instanceof ProcessTask) {
			updateProcessTask((ProcessTask) element);
		} else if (element instanceof SoftwareComponent) {
			updateSoftwareComponent((SoftwareComponent) element);
		}
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# addPropertyChangeListener
	 * (org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	public void addDomainChangeListener(DomainChangeListener listener) {
		this.listener.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.mercatis.lighthouse3.ui.environment.base.services.DomainService# removePropertyChangeListener
	 * (org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	public void removeDomainChangeListener(DomainChangeListener listener) {
		this.listener.remove(listener);
	}

	/**
	 * Only public, because this package is not exported anyway (and we need to access this method from within the registries package).
	 * 
	 * @param lighthouseDomain
	 * @param source
	 * @param property
	 * @param oldValue
	 * @param newValue
	 */
	public void fireDomainChange(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue, Object newValue) {
		DomainChangeEvent event = null;
		Object[] listener = this.listener.getListeners();
		for (int i = 0; i < listener.length; i++) {
			if (event == null)
				event = new DomainChangeEvent(lighthouseDomain, source, property, oldValue, newValue);
			((DomainChangeListener) listener[i]).domainChange(event);
		}
	}

	public void notifyDomainChange(Object source) {
		System.out.println("domainChange");
		fireDomainChange(null, source, null, null, null);
	}

	public LighthouseDomain getLighthouseDomain(String key) {
		DomainInfo di = domainMap.get(key);
		if (di == null)
			throw new RuntimeException("Found no lighthousedomain for key " + key);
		return di.domain;
	}

	public DeploymentRegistry getDeploymentRegistry(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.get(lighthouseDomain.getServerDomainKey());
		return di==null ? null : di.deploymentRegistry;
	}

	public EnvironmentRegistry getEnvironmentRegistry(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.get(lighthouseDomain.getServerDomainKey());
		return di==null ? null : di.environmentRegistry;
	}

	public ProcessTaskRegistry getProcessTaskRegistry(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.get(lighthouseDomain.getServerDomainKey());
		return di==null ? null : di.processTaskRegistry;
	}

	public SoftwareComponentRegistry getSoftwareComponentRegistry(LighthouseDomain lighthouseDomain) {
		DomainInfo di = domainMap.get(lighthouseDomain.getServerDomainKey());
		return di==null ? null : di.softwareComponentRegistry;
	}

	public String getSoftwareComponentCodeForLongNameOrCode(String componentCodeOrLongName, LighthouseDomain lighthouseDomain) {
		SoftwareComponentRegistry softwareComponentRegistry = getSoftwareComponentRegistry(lighthouseDomain);
		String trimmedCodeOrLongName = componentCodeOrLongName.substring(1, componentCodeOrLongName.length() - 1).trim();
		if (softwareComponentRegistry.findByCode(trimmedCodeOrLongName) != null) {
			return trimmedCodeOrLongName;
		} else {
			SoftwareComponent template = new SoftwareComponent();
			template.setLongName(componentCodeOrLongName);
			List<SoftwareComponent> matches = softwareComponentRegistry.findByTemplate(template);
			if (matches != null && !matches.isEmpty()) {
				return matches.get(0).getCode();
			} else {
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model
	 * .LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		try {
			DomainInfo di = domainMap.get(domain.getServerDomainKey());
			di.modifyRegistries(false);
			di.project.close(null);
		} catch (CoreException e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		}
		removeLighthouseDomain(domain);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.
	 * LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
		try {
			DomainInfo di = domainMap.get(domain.getServerDomainKey());
			di.project.open(null);
			final String securityContext = ((ContextAdapter) domain.getAdapter(ContextAdapter.class)).toContext(domain);
			if (di.project.isOpen() && Security.isAuthenticated(securityContext))
				di.modifyRegistries(true);
		} catch (CoreException e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		} catch (Exception e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.services.DomainService#exportDomainConfiguration(com.mercatis.lighthouse3.ui.environment.base.model.
	 * LighthouseDomain)
	 */
	public Properties exportDomainConfiguration(LighthouseDomain lighthouseDomain, String pluginId) {
		IProject project = lighthouseDomain.getProject();
		Properties properties = new Properties();
		boolean wasOpen = project.isOpen();
		//This has to be done in order to be able to get preferences even if the iproject is closed
		try {
			if (!wasOpen)
				project.open(null);
			IScopeContext projectScope = new ProjectScope(project);
			IEclipsePreferences preferences = projectScope.getNode(pluginId);
			preferences.sync();
			for (String key : preferences.keys()) {
				properties.put(key, preferences.get(key, ""));
			}
		} catch (CoreException e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, "Error opening project for export: "+e.getMessage(), e));
		} catch (BackingStoreException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, "Error synchronizing preferences: "+ex.getMessage(), ex));
		}
		if (!wasOpen) {
			try {
				project.close(null);
			} catch (CoreException e) { /* ignore */ }
		}
		return properties;
	}

	public boolean isDeploymentPartOfStatusTemplate(DeploymentCarryingDomainModelEntity<?> carrier, Deployment deployment) {
		boolean selectedDeploymentPartOfStatus = false;
		if (carrier instanceof StatusCarrier) {
			StatusCarrier statusCarrier = (StatusCarrier) carrier;
			StatusRegistry registry = RegistryFactoryServiceUtil.getRegistryFactoryService(StatusRegistryFactoryService.class, deployment.getLighthouseDomain(), this);
			List<com.mercatis.lighthouse3.domainmodel.status.Status> statuus = registry.getStatusForCarrier(statusCarrier);
			for (com.mercatis.lighthouse3.domainmodel.status.Status status : statuus) {
				if (contextContainsDeployment(status.getOkTemplate(), deployment)) {
					selectedDeploymentPartOfStatus = true;
					break;
				}
				if (contextContainsDeployment(status.getErrorTemplate(), deployment)) {
					selectedDeploymentPartOfStatus = true;
					break;
				}
			}
		}
		if (!selectedDeploymentPartOfStatus && carrier.getParentEntity() != null) {
			DeploymentCarryingDomainModelEntity<?> parent = carrier.getParentEntity();
			selectedDeploymentPartOfStatus = isDeploymentPartOfStatusTemplate(parent, deployment) && !isDeploymentReduntantAvailableInChildEntities(parent, deployment);
		} else if (selectedDeploymentPartOfStatus) {
			selectedDeploymentPartOfStatus = !isDeploymentReduntantAvailableInChildEntities(carrier, deployment);
		}
		return selectedDeploymentPartOfStatus;
	}

	/**
	 * Check if the context of an event contains the given deployment.
	 * 
	 * @param template
	 * @param deployment
	 * @return true when the event context contains the deployment
	 */
	@SuppressWarnings("unchecked")
	private boolean contextContainsDeployment(Event template, Deployment deployment) {
		Deployment context = template.getContext();
		if (context instanceof EnumerationRange<?>) {
			EnumerationRange<Deployment> deployments = (EnumerationRange<Deployment>) context;
			return deployments.contains(deployment);
		}

		return context.equals(deployment);
	}

	/**
	 * Checks if one deployment is attached multiple time below a deployment carrier. This is true, even if the deployment is attached to an entity that is a
	 * grandchild of the given parent.
	 * 
	 * @param parent
	 * @param deployment
	 * @return
	 */
	private boolean isDeploymentReduntantAvailableInChildEntities(DeploymentCarryingDomainModelEntity<?> parent, Deployment deployment) {
		int matchCounter = 0;
		if (parent.getDeployments().contains(deployment)) {
			matchCounter++;
		}
		for (DeploymentCarryingDomainModelEntity<?> child : parent.getSubEntities()) {
			if (child.getDeployments().contains(deployment)) {
				matchCounter++;
			}
			if (matchCounter > 1) {
				break;
			}
		}
		return matchCounter > 1;
	}

}
