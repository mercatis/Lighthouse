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
package com.mercatis.lighthouse3.domainmodel.operations;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;

/**
 * This class provides registry functionality for operations. Note that except
 * the methods
 * <ul>
 * <li><code>findByCode()</code>
 * <li><code>findByCategory()</code>
 * <li><code>findByTemplate()</code>
 * <li><code>getManagedType()</code>
 * <li><code>findAllCodes()</code>
 * <li><code>findAllCategories()</code>
 * </ul>
 * all others are disabled and have to throw an
 * <code>PersistenceException</code> with the exception of the
 * <code>getUnitOfWork()</code> method. <code>getUnitOfWork()</code> must return
 * <code>null</code>.
 * <p/>
 * This is because information on available operations is not kept in a database
 * but is dynamically retrieved from an OSGi container.
 */
public interface OperationRegistry extends CodedDomainModelEntityDAO<Operation> {

    /**
     * This method returns the codes of all operations available through this
     * registry.
     *
     * @return the codes of all registered operations
     */
    public List<String> findAllCodes();

    /**
     * This method returns all categories of the operations available through
     * this registry.
     *
     * @return the operation categories
     */
    public List<String> findAllCategories();

    /**
     * This method returns all operation of a given category.
     *
     * @param category the category of interest
     * @return the operations registered for the given category.
     */
    public List<Operation> findByCategory(String category);


    /**
     * This method returns the operation installed by a given operation installation. Note that the operation
     * may be adapted to the operation installation, e.g., the default values of parameters may be adapted to the installation.
     *
     * To get the untainted structure of an operation, use <code>findByCode()</code>
     *
     * @param operationInstallation the operation installation whose operation we are interested in.
     * @return the operation installed or <code>null</code> if the operation could not be retrieved.
     */
    public Operation findInstalled(OperationInstallation operationInstallation);

}
