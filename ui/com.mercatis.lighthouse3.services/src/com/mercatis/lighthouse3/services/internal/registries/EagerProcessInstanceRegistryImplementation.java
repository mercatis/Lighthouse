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

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.persistence.processinstance.rest.ProcessInstanceRegistryImplementation;


public class EagerProcessInstanceRegistryImplementation extends EagerDomainModelEntityDAOImplementation<ProcessInstance>
		implements ProcessInstanceRegistry {

	private ProcessInstanceRegistry delegateRegistry;
	
	public EagerProcessInstanceRegistryImplementation(String serverUrl, ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry, EventRegistry eventRegistry, String user, String password) {
		delegateRegistry = new ProcessInstanceRegistryImplementation(serverUrl, processInstanceDefinitionRegistry, eventRegistry, user, password);
	}
	
	public EagerProcessInstanceRegistryImplementation(String serverUrl, ProcessInstanceDefinitionRegistry processInstanceDefinitionRegistry, EventRegistry eventRegistry) {
		this(serverUrl, processInstanceDefinitionRegistry, eventRegistry, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<ProcessInstance> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries.EagerDomainModelEntityDAOImplementation#keyForEntity(com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity)
	 */
	@Override
	protected String keyForEntity(ProcessInstance entity) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry#findByProcessInstanceDefinition(com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition)
	 */
	public List<ProcessInstance> findByProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		return delegateRegistry.findByProcessInstanceDefinition(processInstanceDefinition);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry#findByProcessTask(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask)
	 */
	public List<ProcessInstance> findByProcessTask(ProcessTask processTask) {
		return delegateRegistry.findByProcessTask(processTask);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class<?> getManagedType() {
		return ProcessInstance.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry#findByProcessTask(com.mercatis.lighthouse3.domainmodel.environment.ProcessTask, int, int)
	 */
	public List<ProcessInstance> findByProcessTask(ProcessTask processInstanceDefinition, int pageSize, int pageNo) {
		return delegateRegistry.findByProcessTask(processInstanceDefinition, pageSize, pageNo);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry#findAfterInstance(com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance, int)
	 */
	public List<ProcessInstance> findAfterInstance(ProcessInstance instance, int maxResults) {
		return delegateRegistry.findAfterInstance(instance, maxResults);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry#findBeforeInstance(com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance, int)
	 */
	public List<ProcessInstance> findBeforeInstance(ProcessInstance instance, int maxResults) {
		return delegateRegistry.findBeforeInstance(instance, maxResults);
	}
}
