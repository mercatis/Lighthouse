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
package com.mercatis.lighthouse3.persistence.commons.rest;

import static com.mercatis.lighthouse3.commons.commons.HttpRequest.appendPathElementToUrl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.commons.commons.HttpRequest.HttpMethod;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.PersistenceException;

/**
 * This class provides an abstract implementation of common methods for coded
 * domain model entity DAOs. The implementation acts as an HTTP client to a
 * RESTful web service providing the DAO storage functionality.
 */
public abstract class CodedDomainModelEntityDAOImplementation<Entity extends CodedDomainModelEntity>
	extends DomainModelEntityDAOImplementation<Entity>
	implements CodedDomainModelEntityDAO<Entity> {

    /**
     * This method returns the resource URL for the coded domain model entity
     * with the given code.
     *
     * @param code
     *            the code of the coded domain model entity
     * @return the URL
     */
    protected String urlForEntityCode(String code) {
        return appendPathElementToUrl(appendPathElementToUrl("", this.getManagedType().getSimpleName()), code);
    }

    private class ByCodeFinder implements Callable<Entity> {

        private String code = null;

        public Entity call() throws Exception {
            return findByCode(this.code);
        }

        public ByCodeFinder(String code) {
            this.code = code;
        }
    }

    @Override
    protected List<Entity> resolveWebServiceResultList(String webServiceResultList) {
        List<Entity> result = new LinkedList<Entity>();
        List<String> entityCodes = XmlMuncher.readValuesFromXml(webServiceResultList, "//:code");

        List<Callable<Entity>> jobs = new ArrayList<Callable<Entity>>();

        for (String entityCode : entityCodes) {
            jobs.add(new ByCodeFinder(entityCode));
        }

        ExecutorService pooledExecutor = Executors.newFixedThreadPool(this.getResolveWebServiceResultThreadPoolSize());
        try {
            List<Future<Entity>> jobResults = pooledExecutor.invokeAll(jobs);
            for (Future<Entity> jobResult : jobResults) {
                result.add(jobResult.get());
            }

        } catch (Exception ex) {
            throw new PersistenceException("Encountered problem while resolving entity code result list", ex);
        }

        return result;
    }

    @Override
    protected String urlForEntity(Entity entity) {
        return this.urlForEntityCode(entity.getCode());
    }

    @Override
    public boolean alreadyPersisted(Entity entity) {
        return this.findByCode(entity.getCode()) != null;
    }

    public Entity findByCode(String code) {
        try {
            String result = this.executeHttpMethod(this.urlForEntityCode(code), HttpMethod.GET, null, null);

            Entity entity = newEntity();
            entity.fromXml(result, this.getEntityResolvers());
            
            return entity;
        } catch (PersistenceException ex) {
            return null;
        }
    }

    @Override
    public Entity find(long id) {
        throw new PersistenceException("Method not supported", null);
    }
}
