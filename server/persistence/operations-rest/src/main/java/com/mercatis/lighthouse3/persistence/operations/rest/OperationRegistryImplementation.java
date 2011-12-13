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

import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry;
import com.mercatis.lighthouse3.persistence.commons.rest.CodedDomainModelEntityDAOImplementation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a REST-based client implementation to the operation
 * registry web service in the operation executor.
 *
 */
public class OperationRegistryImplementation extends CodedDomainModelEntityDAOImplementation<Operation> implements
        OperationRegistry {

    @SuppressWarnings("rawtypes")
    protected DomainModelEntityDAO[] getRealEntityResolvers() {
        return new DomainModelEntityDAO[]{};
    }

    @Override
    public void delete(Operation entityToDelete) {
        throw new PersistenceException("Delete not supported for operations", null);
    }

    @Override
    public void persist(Operation entityToPersist) {
        throw new PersistenceException("Persist not supported for operations", null);
    }

    @Override
    public void update(Operation entityToUpdate) {
        throw new PersistenceException("Update not supported for operations", null);
    }

    public OperationRegistryImplementation() {
        super();
    }

    public OperationRegistryImplementation(String serverUrl) {
        super();
        this.setServerUrl(serverUrl);
    }
    
    public OperationRegistryImplementation(String serverUrl, String user, String password) {
	this(serverUrl);
	this.user = user;
	this.password = password;
    }

    public List<String> findAllCategories() {
        String result = this.executeHttpMethod(this.urlForEntityClass() + "/Category/all", HttpMethod.GET, null, null);
        return new ArrayList<String>(XmlMuncher.readValuesFromXml(result, "//:code"));
    }

    public List<String> findAllCodes() {
        String result = this.executeHttpMethod(this.urlForEntityClass(), HttpMethod.GET, null, null);
        return new ArrayList<String>(XmlMuncher.readValuesFromXml(result, "//:code"));
    }

    public List<Operation> findByCategory(String category) {
        Operation template = new Operation();
        template.setCategory(category);

        return this.findByTemplate(template);
    }

    public Operation findInstalled(OperationInstallation operationInstallation) {
        try {
            String result = this.executeHttpMethod(
                    appendPathElementToUrl(appendPathElementToUrl(appendPathElementToUrl("/Operation", operationInstallation.getInstalledOperationCode()), operationInstallation.getInstallationLocation().getLocation()), operationInstallation.getInstallationLocation().getDeployedComponent().getCode()),
                    HttpMethod.GET, null, null);

            Operation operation = new Operation();
            operation.fromXml(result, this.getEntityResolvers());

            return operation;
        } catch (PersistenceException ex) {
            return null;
        }
    }
}
