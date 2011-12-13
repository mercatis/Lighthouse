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

/**
 * This interface has to be provided by all implementations of operations. Note
 * that one operation implementation can implement several operations.  Furthermore, provided operations can be
 * distinguish into general ones (the normal type) and operation installation specific ones -- those that are available
 * only on a certain installation / deployment or that adapt themselves to the deployment.
 */
public interface OperationImplementation {
    /**
     * This method gets invoked by the operation executor when an operation call
     * is finally made. Here, the logic of the operation must be placed.
     *
     * @param operationCall the call of the operation to execute.
     * @throws OperationCallException in case of trouble.
     */
    public void execute(OperationCall operationCall);

    /**
     * This method returns all operations provided by the present implementation
     *
     * @return the operations.
     */
    public List<Operation> getProvidedOperations();

    /**
     * This method returns the operation given by an installation, potentially adapted to that installation.
     * For example, the operation installation may influence default values of parameters.
     *
     * @param operationInstallation the deployment potentially influencing the operations.
     * @return the installation-specific operation or <code>null</code> in the operation cannot be retrieved.
     */
    public Operation getProvidedOperations(OperationInstallation operationInstallation);
}
