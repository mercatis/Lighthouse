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

import java.util.LinkedList;
import java.util.List;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCallException;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.DomainModelEntityDAOImplementation;

/**
 * This class provides an implementation of the
 * <code>OperationInstallationRegistry</code> interface based on the operations
 * REST web service.
 */
public class OperationInstallationRegistryImplementation extends DomainModelEntityDAOImplementation<OperationInstallation> implements OperationInstallationRegistry {

    /**
     * This property keeps a reference to the RESTful deployment registry client
     * implementation to use for resolving operation installations.
     */
    private DeploymentRegistry deploymentRegistry = null;

    @SuppressWarnings("rawtypes")
    @Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new DomainModelEntityDAO[]{this.deploymentRegistry};
    }

    @Override
    protected String urlForEntity(OperationInstallation operationInstallation) {
        return appendPathElementToUrl(appendPathElementToUrl(appendPathElementToUrl("/OperationInstallation", operationInstallation.getInstallationLocation().getLocation()), operationInstallation.getInstallationLocation().getDeployedComponent().getCode()),
                operationInstallation.getInstalledOperationCode());
    }

    /**
     * This method retrieves an operation installation given the code and
     * location of the deployment forming its installation location plus the
     * code of the operation installed.
     *
     * @param code
     *            the code of the software component deployed at the deployment
     * @param location
     *            the location of the deployment
     * @param operation
     *            the code of the operation
     * @param entityResolvers
     *            the registries to use to parse the XML format of operation
     *            installations.
     * @return the operation installation retrieved or <code>null</code> if no
     *         such operation could be found.
     */
    @SuppressWarnings("rawtypes")
    protected OperationInstallation findByCodeLocationAndOperation(String code, String location, String operation,
            DomainModelEntityDAO[] entityResolvers) {
        String xml = null;
        try {
            xml = this.executeHttpMethod(appendPathElementToUrl(appendPathElementToUrl(appendPathElementToUrl("/OperationInstallation", location), code),
                    operation), HttpMethod.GET, null, null);
        } catch (PersistenceException persistenceException) {
            return null;
        }

        OperationInstallation result = new OperationInstallation();
        result.fromXml(xml, entityResolvers);
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<OperationInstallation> resolveWebServiceResultList(String webServiceResultList) {
        DomainModelEntityDAO[] entityResolvers = this.getEntityResolvers();

        List<OperationInstallation> result = new LinkedList<OperationInstallation>();
        List<String> deploymentCodes = XmlMuncher.readValuesFromXml(webServiceResultList, "//:deployedComponentCode");
        List<String> deploymentLocations = XmlMuncher.readValuesFromXml(webServiceResultList, "//:deploymentLocation");
        List<String> operationCodes = XmlMuncher.readValuesFromXml(webServiceResultList, "//:operationCode");

        for (int i = 0; i < deploymentCodes.size(); i++) {
            String code = deploymentCodes.get(i);
            String location = deploymentLocations.get(i);
            String operationCode = operationCodes.get(i);

            result.add(this.findByCodeLocationAndOperation(code, location, operationCode, entityResolvers));
        }

        return result;
    }

    @Override
    public boolean alreadyPersisted(OperationInstallation operationInstallation) {
        return this.findByCodeLocationAndOperation(operationInstallation.getInstallationLocation().getDeployedComponent().getCode(), operationInstallation.getInstallationLocation().getLocation(),
                operationInstallation.getInstalledOperationCode(), this.getEntityResolvers()) != null;
    }

    @Override
    public OperationInstallation find(long id) {
        throw new PersistenceException("Method not supported", null);
    }

    @Override
    public void update(OperationInstallation entityToUpdate) {
        throw new PersistenceException("Method not supported", null);
    }

    @Override
    public void persist(OperationInstallation operationInstallation) {
        if (!this.deploymentRegistry.alreadyPersisted(operationInstallation.getInstallationLocation())) {
            this.deploymentRegistry.persist(operationInstallation.getInstallationLocation());
        }

        super.persist(operationInstallation);
    }

    public void execute(OperationCall operationCall) {
        try {
            this.executeHttpMethod("/OperationCall", HttpMethod.POST, operationCall.toXml(), null);
        } catch (PersistenceException persistenceException) {
            throw new OperationCallException("OperationCall failed", persistenceException);
        }
    }

    public List<OperationInstallation> findAtDeployment(Deployment deployment) {
        String xml = this.executeHttpMethod(
                appendPathElementToUrl(appendPathElementToUrl("/OperationInstallation", deployment.getLocation()),
                deployment.getDeployedComponent().getCode()), HttpMethod.GET, null, null);
        return this.resolveWebServiceResultList(xml);
    }

    public OperationInstallation findByDeploymentAndOperation(Deployment deployment, String operation) {
        return this.findByCodeLocationAndOperation(deployment.getDeployedComponent().getCode(), deployment.getLocation(), operation, this.getEntityResolvers());
    }

    public List<OperationInstallation> findForOperation(String operation) {
        String xml = this.executeHttpMethod(appendPathElementToUrl("/OperationInstallation", operation), HttpMethod.GET, null, null);
        return this.resolveWebServiceResultList(xml);
    }

    /**
     * This is the constructor for the RESTful operation installation registry
     * implementation.
     *
     * @param baseUrl
     *            the URL where the operations REST web service is listening
     * @param deploymentRegistry
     *            the deployment registry to use
     */
    public OperationInstallationRegistryImplementation(String baseUrl, DeploymentRegistry deploymentRegistry) {
        this.setServerUrl(baseUrl);
        this.deploymentRegistry = deploymentRegistry;
    }
    
    public OperationInstallationRegistryImplementation(String baseUrl, DeploymentRegistry deploymentRegistry, String user, String password) {
	this(baseUrl, deploymentRegistry);
	this.user = user;
	this.password = password;
    }

    protected OperationInstallationRegistryImplementation() {
    }
}
