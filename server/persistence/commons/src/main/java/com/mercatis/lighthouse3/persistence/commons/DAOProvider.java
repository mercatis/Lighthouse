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
package com.mercatis.lighthouse3.persistence.commons;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;

/**
 * Implement this interface to let the DAOs know who created them and retrieve other DAOs.
 */
public interface DAOProvider {

    /**
     * Get a DAO by according interface.
     *
     * @param registryInterface
     * @return The DAO that was registered for the given interface
     */
    public <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface);

    /**
     * Get a DAO by according interface with possibility to use the hibernate implementation of the DAO interface in each case.
     * 
     * @param registryInterface
     * @param forceHibernate
     * @return The DAO that was registered for the given interface
     */
    public <R extends DomainModelEntityDAO<? extends DomainModelEntity>> R getDAO(Class<R> registryInterface, boolean forceHibernate);
}
