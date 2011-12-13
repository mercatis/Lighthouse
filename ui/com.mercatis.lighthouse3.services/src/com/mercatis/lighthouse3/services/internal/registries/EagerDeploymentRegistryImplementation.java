/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.persistence.environment.rest.DeploymentRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerDeploymentRegistryImplementation extends EagerDomainModelEntityDAOImplementation<Deployment>
	implements DeploymentRegistry {

	private DeploymentRegistryImplementation delegateRegistry = null;
	
	public EagerDeploymentRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry, String user, String password) {
		this.delegateRegistry = new DeploymentRegistryImplementation(serverUrl, softwareComponentRegistry, user, password);
		invalidate();
	}
	
	public EagerDeploymentRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry) {
		this(serverUrl, softwareComponentRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Deployment> delegateRegistry() {
		return this.delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#keyForEntity(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	@Override
	protected String keyForEntity(Deployment entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.getDeployedComponent().getCode());
		builder.append("@");
		builder.append(entity.getLocation());
		
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#deploy(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Deployment deploy(SoftwareComponent component, String location, String description, String contact, String contactEmail) {
		Deployment deployment = delegateRegistry.deploy(component, location, description, contact, contactEmail);
		if (deployment != null)
			cache.put(keyForEntity(deployment), deployment);
		
		return deployment;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#findAllLocations()
	 */
	public List<String> findAllLocations() {
		HashSet<String> result = new HashSet<String>();
		for (Deployment deployment : cache.values()) {
			result.add(deployment.getLocation());
		}
		
		return new ArrayList<String>(result);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#findAtLocation(java.lang.String)
	 */
	public List<Deployment> findAtLocation(String location) {
		ArrayList<Deployment> result = new ArrayList<Deployment>();
		for (Deployment deployment : cache.values()) {
			if (deployment.getLocation().equals(location)) {
				result.add(deployment);
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#findByComponent(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent)
	 */
	public List<Deployment> findByComponent(SoftwareComponent component) {
		ArrayList<Deployment> result = new ArrayList<Deployment>();
		for (Deployment deployment : cache.values()) {
			if (deployment.getDeployedComponent().equals(component)) {
				result.add(deployment);
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#findByComponentAndLocation(com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent, java.lang.String)
	 */
	public Deployment findByComponentAndLocation(SoftwareComponent component, String location) {
		StringBuilder builder = new StringBuilder();
		builder.append(component.getCode());
		builder.append("@");
		builder.append(location);
		
		String locationAndCode = builder.toString();
		Deployment deployment = cache.get(locationAndCode);
		if (deployment == null) {
			deployment = delegateRegistry.findByComponentAndLocation(component, location);
			cache.put(locationAndCode, deployment);
		}
		
		return deployment;
	}
	
	public Deployment findByComponentCodeAndLocation(String componentCode, String location) {
		StringBuilder builder = new StringBuilder();
		builder.append(componentCode);
		builder.append("@");
		builder.append(location);
		
		String locationAndCode = builder.toString();
		Deployment deployment = cache.get(locationAndCode);
		if (deployment == null) {
			deployment = delegateRegistry.findByComponentCodeAndLocation(componentCode, location);
			if (deployment != null)
				cache.put(locationAndCode, deployment);
		}
		
		return deployment;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#undeploy(com.mercatis.lighthouse3.domainmodel.environment.Deployment)
	 */
	public void undeploy(Deployment deployment) {
		delegateRegistry.undeploy(deployment);
		cache.remove(keyForEntity(deployment));
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return Deployment.class;
	}

}
