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
package com.mercatis.lighthouse3.persistence.environment.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import java.util.LinkedList;
import java.util.List;

import com.mercatis.lighthouse3.commons.commons.HttpException;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation;

/**
 * This class provides a deployment registry implementation. The implementation
 * acts as an HTTP client to a RESTful web service providing the DAO storage
 * functionality.
 */
public class DeploymentRegistryImplementation extends DomainModelEntityDAOImplementation<Deployment> implements DeploymentRegistry {

	/**
	 * This property maintains a references to a suitable software component
	 * registry to use for reference resolving.
	 */
	private SoftwareComponentRegistry softwareComponentRegistry = null;

	/**
	 * This method sets the software component registry to use for reference
	 * resolving.
	 * 
	 * @param softwareComponentRegistry
	 *            the registry to use.
	 */
	public void setSoftwareComponentRegistry(SoftwareComponentRegistry softwareComponentRegistry) {
		this.softwareComponentRegistry = softwareComponentRegistry;
	}

	/**
	 * Returns the software component registry implementation used by the
	 * deployment registry implementation.
	 * 
	 * @return the software component registry.
	 */
	public SoftwareComponentRegistry getSoftwareComponentRegistry() {
		return this.softwareComponentRegistry;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected DomainModelEntityDAO[] getRealEntityResolvers() {
		return new DomainModelEntityDAO[] { this.softwareComponentRegistry };
	}

	/**
	 * Constructs the URL of a deployment out of the location and the code of
	 * the deployed component.
	 * 
	 * @param code
	 *            the code of the deployed software component
	 * @param location
	 *            the location of the deployed software component.
	 * @return the URL
	 */
	protected String urlForDeploymentCodeAndLocation(String code, String location) {
		String url = appendPathElementToUrl(appendPathElementToUrl("/Deployment", location), code);
		return url;
	}

	@Override
	protected String urlForEntity(Deployment deployment) {
		return this.urlForDeploymentCodeAndLocation(deployment.getDeployedComponent().getCode(), deployment.getLocation());
	}

	@Override
	protected List<Deployment> resolveWebServiceResultList(String webServiceResultList) {
		List<Deployment> result = new LinkedList<Deployment>();
		List<String> deploymentCodes = XmlMuncher.readValuesFromXml(webServiceResultList, "//:code");
		List<String> deploymentLocations = XmlMuncher.readValuesFromXml(webServiceResultList, "//:location");

		for (int i = 0; i < deploymentCodes.size(); i++) {
			String code = deploymentCodes.get(i);
			String location = deploymentLocations.get(i);

			result.add(this.findByComponentCodeAndLocation(code, location));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry#
	 * findByComponentCodeAndLocation(java.lang.String, java.lang.String)
	 */
	public Deployment findByComponentCodeAndLocation(String componentCode, String location) {
		String xml = null;
		try {
			xml = this.executeHttpMethod(this.urlForDeploymentCodeAndLocation(componentCode, location), HttpMethod.GET, null, null);
		} catch (PersistenceException persistenceException) {
			return null;
		}
		Deployment result = new Deployment();
		result.fromXml(xml, this.getEntityResolvers());
		return result;
	}

	@Override
	public boolean alreadyPersisted(Deployment deployment) {
		return this.findByComponentCodeAndLocation(deployment.getDeployedComponent().getCode(), deployment.getLocation()) != null;
	}

	@Override
	public void persist(Deployment deploymentToPersist) {
		if (!this.softwareComponentRegistry.alreadyPersisted(deploymentToPersist.getDeployedComponent())) {
			this.softwareComponentRegistry.persist(deploymentToPersist.getDeployedComponent());
		}
		super.persist(deploymentToPersist);
	}

	public Deployment deploy(SoftwareComponent component, String location, String description, String contact, String contactEmail) {
		Deployment deployment = new Deployment();
		deployment.setLocation(location);
		deployment.setDescription(description);
		deployment.setContact(contact);
		deployment.setContactEmail(contactEmail);
		deployment.setDeployedComponent(component);
		try {
			this.persist(deployment);
		} catch (HttpException ex) {
			throw new PersistenceException("Could not persist Deployment", ex);
		}

		return deployment;
	}

	@Override
	public Deployment find(long id) {
		throw new PersistenceException("Method not supported", null);
	}

	public List<String> findAllLocations() {
		String xml = this.executeHttpMethod("/Deployment/Location/all", HttpMethod.GET, null, null);

		return new LinkedList<String>(XmlMuncher.readValuesFromXml(xml, "//:location"));
	}

	public List<Deployment> findAtLocation(String location) {
		String xml = this.executeHttpMethod(appendPathElementToUrl("/Deployment", location), HttpMethod.GET, null, null);
		return this.resolveWebServiceResultList(xml);
	}

	public List<Deployment> findByComponent(SoftwareComponent component) {
		String xml = this.executeHttpMethod(appendPathElementToUrl("/Deployment/SoftwareComponent", component.getCode()), HttpMethod.GET, null, null);
		return this.resolveWebServiceResultList(xml);
	}

	public Deployment findByComponentAndLocation(SoftwareComponent component, String location) {
		return findByComponentCodeAndLocation(component.getCode(), location);
	}

	public void undeploy(Deployment deployment) {
		this.delete(deployment);
	}

	public DeploymentRegistryImplementation() {
		super();
	}

	public DeploymentRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry) {
		super();
		this.setServerUrl(serverUrl);
		this.setSoftwareComponentRegistry(softwareComponentRegistry);
	}
	
	public DeploymentRegistryImplementation(String serverUrl, SoftwareComponentRegistry softwareComponentRegistry, String user, String password) {
	    this(serverUrl, softwareComponentRegistry);
	    this.user = user;
	    this.password = password;
	}
}
