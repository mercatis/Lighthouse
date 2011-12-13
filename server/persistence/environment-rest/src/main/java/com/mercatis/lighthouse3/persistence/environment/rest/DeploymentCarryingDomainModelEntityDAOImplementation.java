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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentCarryingDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.HierarchicalDomainModelEntityDAOImplementation;

/**
 * This abstract class provides common functionality for RESTful web service
 * client implementations of deployment carrying domain model registries.
 */
public abstract class DeploymentCarryingDomainModelEntityDAOImplementation<Entity extends DeploymentCarryingDomainModelEntity<Entity>>
		  extends HierarchicalDomainModelEntityDAOImplementation<Entity> implements
        DeploymentCarryingDomainModelEntityDAO<Entity> {

    /**
     * This property maintains a reference to a suitable deployment registry to
     * use for reference resolving.
     */
    private DeploymentRegistry deploymentRegistry = null;

    /**
     * This method returns a suitable deployment registry for reference
     * resolving purposes.
     *
     * @return the deployment registry
     */
    protected DeploymentRegistry getDeploymentRegistry() {
        return deploymentRegistry;
    }

    /**
     * This method sets the deployment registry to use for reference resolving
     * purposes.
     *
     * @param deploymentRegistry
     *            the deployment registry to use
     */
    public void setDeploymentRegistry(DeploymentRegistry deploymentRegistry) {
        this.deploymentRegistry = deploymentRegistry;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new DomainModelEntityDAO[]{this, this.deploymentRegistry};
    }

    public List<Entity> findForDeployment(Deployment deployment) {
        String xml = null;
        try {
            String url = appendPathElementToUrl(appendPathElementToUrl(this.urlForEntityCode("Deployment"), deployment.getLocation()), deployment.getDeployedComponent().getCode());
            xml = this.executeHttpMethod(url, HttpMethod.GET, null, null);
        } catch (PersistenceException ex) {
            return new LinkedList<Entity>();
        }
        return this.resolveWebServiceResultList(xml);
    }

    @Override
    public void persist(Entity entityToPersist) {
        Set<Deployment> attachedDeployments = new HashSet<Deployment>(entityToPersist.getDeployments());

        boolean needsUpdate = false;

        for (Deployment deployment : attachedDeployments) {
            entityToPersist.detachDeployment(deployment);
            needsUpdate = true;
            if (!this.deploymentRegistry.alreadyPersisted(deployment)) {
                this.deploymentRegistry.persist(deployment);
            }
        }

        super.persist(entityToPersist);

        for (Deployment deployment : attachedDeployments) {
            entityToPersist.attachDeployment(deployment);
        }

        if (needsUpdate) {
            this.update(entityToPersist);
        }
    }
}
