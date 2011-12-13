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
package com.mercatis.lighthouse3.persistence.operations.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import java.util.List;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;

/**
 * This class provides an implementation of the <code>JobRegistry</code>
 * interface based on the operations REST web service.
 */
public class JobRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Job> implements JobRegistry {

    /**
     * This property keeps a reference to the RESTful software component
     * registry client implementation to use for resolving job operation calls.
     */
    private SoftwareComponentRegistry softwareComponentRegistry = null;
    /**
     * This property keeps a reference to the RESTful deployment registry client
     * implementation to use for resolving job operation calls.
     */
    private DeploymentRegistry deploymentRegistry = null;
    /**
     * This property keeps a reference to the RESTful operation installation
     * registry client implementation to use for resolving job operation calls.
     */
    private OperationInstallationRegistry operationInstallationRegistry = null;

    @SuppressWarnings("rawtypes")
    @Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
    	return new DomainModelEntityDAO[]{
                this.softwareComponentRegistry,
                this.deploymentRegistry,
                this.operationInstallationRegistry};
    }

    @Override
    public void persist(Job jobToPersist) {
        if (!this.operationInstallationRegistry.alreadyPersisted(jobToPersist.getScheduledCall().getTarget())) {
            this.operationInstallationRegistry.persist(jobToPersist.getScheduledCall().getTarget());
        }

        super.persist(jobToPersist);
    }

    @Override
    public void update(Job jobToUpdate) {
        if (!this.operationInstallationRegistry.alreadyPersisted(jobToUpdate.getScheduledCall().getTarget())) {
            this.operationInstallationRegistry.persist(jobToUpdate.getScheduledCall().getTarget());
        }

        super.update(jobToUpdate);
    }

    public List<Job> findAtDeployment(Deployment deployment) {
        String result = null;
        try {
            String url = appendPathElementToUrl(appendPathElementToUrl(this.urlForEntityClass() + "/Deployment", deployment.getLocation()), deployment.getDeployedComponent().getCode());

            result = this.executeHttpMethod(url, HttpMethod.GET, null, null);
        } catch (PersistenceException ex) {
            return null;
        }
        return this.resolveWebServiceResultList(result);
    }

    /**
     * This is the constructor for REST-based job registries.
     *
     * @param baseUrl
     *            the URL on which the operations web service is listening
     * @param softwareComponentRegistry
     *            the software component registry to use for resolving operation
     *            calls tied to jobs.
     * @param deploymentRegistry
     *            the deployment registry to use for resolving operation calls
     *            tied to jobs.
     * @param operationInstallationRegistry
     *            the operation installation registry to use for resolving
     *            operation calls tied to jobs.
     */
    public JobRegistryImplementation(String baseUrl,
            SoftwareComponentRegistry softwareComponentRegistry,
            DeploymentRegistry deploymentRegistry,
            OperationInstallationRegistry operationInstallationRegistry) {

        this.setServerUrl(baseUrl);
        this.softwareComponentRegistry = softwareComponentRegistry;
        this.deploymentRegistry = deploymentRegistry;
        this.operationInstallationRegistry = operationInstallationRegistry;
    }
    
    public JobRegistryImplementation(String baseUrl,
            SoftwareComponentRegistry softwareComponentRegistry,
            DeploymentRegistry deploymentRegistry,
            OperationInstallationRegistry operationInstallationRegistry,
	    String user,
	    String password) {
	this(baseUrl, softwareComponentRegistry, deploymentRegistry, operationInstallationRegistry);
	this.user = user;
	this.password = password;
    }
}
