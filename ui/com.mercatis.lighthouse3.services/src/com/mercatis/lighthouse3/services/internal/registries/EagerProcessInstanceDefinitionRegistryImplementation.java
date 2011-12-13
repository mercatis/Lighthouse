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
package com.mercatis.lighthouse3.services.internal.registries;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.persistence.processinstance.rest.ProcessInstanceDefinitionRegistryImplementation;


public class EagerProcessInstanceDefinitionRegistryImplementation extends
		EagerCodedDomainModelEntityDAOImplementation<ProcessInstanceDefinition> implements ProcessInstanceDefinitionRegistry {

	private ProcessInstanceDefinitionRegistry delegateRegistry;
	
	public EagerProcessInstanceDefinitionRegistryImplementation(String serverUrl, ProcessTaskRegistry processTaskRegistry, String user, String password) {
		delegateRegistry = new ProcessInstanceDefinitionRegistryImplementation(serverUrl, processTaskRegistry, user, password);
	}
	
	public EagerProcessInstanceDefinitionRegistryImplementation(String serverUrl, ProcessTaskRegistry processTaskRegistry) {
		this(serverUrl, processTaskRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<ProcessInstanceDefinition> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry#findByProcessTask(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public ProcessInstanceDefinition findByProcessTask(ProcessTask processTask) {
		return delegateRegistry.findByProcessTask(processTask);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class<?> getManagedType() {
		return ProcessInstanceDefinition.class;
	}
}
