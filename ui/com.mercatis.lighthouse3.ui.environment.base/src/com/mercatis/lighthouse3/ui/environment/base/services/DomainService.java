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

import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;

public interface DomainService {
	
	/**
	 * @param project
	 * @return
	 */
	public LighthouseDomain getLighthouseDomain(IProject project);
	
	/**
	 * @param project
	 * @return
	 */
	public LighthouseDomain createLighthouseDomain(IProject project);
	
	/**
	 * @param lighthouseDomain
	 * @return
	 */
	public DomainConfiguration getDomainConfiguration(LighthouseDomain lighthouseDomain);
	
	/**
	 * @param lighthouseDomain
	 */
	public void addLighthouseDomainNature(LighthouseDomain lighthouseDomain);

	/**
	 * @param container
	 * @return
	 */
	public List<SoftwareComponent> getSoftwareComponents(SoftwareComponentContainer container);
	
	/**
	 * @param entity1
	 * @param entity2
	 * @return
	 */
	public boolean inSameDomain(Object entity1, Object entity2);
	
	/**
	 * @param location
	 * @return
	 */
	public List<Deployment> getDeployments(Location location);
	
	/**
	 * @param deploymentContainer
	 * @return
	 */
	public List<Location> getLocations(DeploymentContainer deploymentContainer);
	
	/**
	 * @param processTaskContainer
	 * @return
	 */
	public List<ProcessTask> getProcessTasks(ProcessTaskContainer processTaskContainer);
	
	/**
	 * @param environmentContainer
	 * @return
	 */
	public List<Environment> getEnvironments(EnvironmentContainer environmentContainer);
	
	/**
	 * @param entity
	 * @return
	 */
	public LighthouseDomain getLighthouseDomainByEntity(Object entity);

	/**
	 * @param lighthouseDomain
	 * @param component
	 */
	public void persistSoftwareComponent(LighthouseDomain lighthouseDomain, SoftwareComponent component);
	
	/**
	 * @param component
	 */
	public void updateSoftwareComponent(SoftwareComponent component);

	/**
	 * @param lighthouseDomain
	 * @param processTask
	 */
	public void persistProcessTask(LighthouseDomain lighthouseDomain, ProcessTask processTask);

	/**
	 * @param parent
	 */
	public void updateProcessTask(ProcessTask parent);

	/**
	 * @param lighthouseDomain
	 * @param environment
	 */
	public void persistEnvironment(LighthouseDomain lighthouseDomain, Environment environment);

	/**
	 * @param parentEnvironment
	 */
	public void updateEnvironment(Environment parentEnvironment);

	/**
	 * @param lighthouseDomain
	 * @param deployment
	 */
	public void persistDeployment(LighthouseDomain lighthouseDomain, Deployment deployment);

	/**
	 * @param deployment
	 */
	public void updateDeployment(Deployment deployment);
	
	/**
	 * @param element 
	 */
	public void update(Object element);

	/**
	 * @param entity
	 * @return
	 */
	public Object getParentForEntity(Object entity);
	
	/**
	 * @param lighthouseDomain
	 * @return
	 */
	public List<Deployment> getAllDeployments(LighthouseDomain lighthouseDomain);

	/**
	 * @param deployment
	 */
	public void deleteDeployment(Deployment deployment);

	/**
	 * @param environment
	 */
	public void deleteEnvironment(Environment environment);

	/**
	 * @param processTask
	 */
	public void deleteProcessTask(ProcessTask processTask);

	/**
	 * @param softwareComponent
	 */
	public void deleteSoftwareComponent(SoftwareComponent softwareComponent);

	/**
	 * @param deployment
	 * @return
	 */
	public Location getLocation(Deployment deployment);

	public void addDomainChangeListener(DomainChangeListener listener);
	
	public void removeDomainChangeListener(DomainChangeListener listener);

	public void notifyDomainChange(Object source);
	
	public SoftwareComponentRegistry getSoftwareComponentRegistry(Object entity);
	
	public DeploymentRegistry getDeploymentRegistry(Object entity);
	
	public EnvironmentRegistry getEnvironmentRegistry(Object entity);
	
	public ProcessTaskRegistry getProcessTaskRegistry(Object entity);
	
	public LighthouseDomain getLighthouseDomain(String key);
	
	/**
	 * Read the domain configuration and return it as java properties.
	 * 
	 * @param lighthouseDomain
	 * @param pluginId
	 * @return
	 */
	public Properties exportDomainConfiguration(LighthouseDomain lighthouseDomain, String pluginId);
	
	/**
	 * Currently not used. The ImportDomainSettingsHandler opens a wizard to do this job.
	 * 
	 * @param domainConfiguration
	 */
	public void importDomainConfiguration(Properties domainConfiguration);
	
	/**
	 * Check if a deployment is somehow necessary for a status template.
	 * This is needed to determine the safety of detaching a deployment from a DeploymentCarrier.
	 * 
	 * @param currentCarrier
	 * @param deployment
	 * @return true if any status template on this carrier (or even parent carriers) uses this deployment
	 */
	public boolean isDeploymentPartOfStatusTemplate(DeploymentCarryingDomainModelEntity<?> currentCarrier, Deployment deployment);
}
