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
package com.mercatis.lighthouse3.ui.processinstance.base.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.services.ProcessInstanceRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.processinstance.base.services.ProcessInstanceService;


public class ProcessInstanceServiceImpl implements ProcessInstanceService {

	private Map<LighthouseDomain, ProcessInstanceRegistry> registries = new HashMap<LighthouseDomain, ProcessInstanceRegistry>();
	private BundleContext context;
	
	public ProcessInstanceServiceImpl(BundleContext context) {
		this.context = context;
	}

	private ProcessInstanceRegistry getRegistry(LighthouseDomain lighthouseDomain) {
		ProcessInstanceRegistry registry = registries.get(lighthouseDomain);
		if (registry == null) {
			ServiceReference ref = context.getServiceReference(ProcessInstanceRegistryFactoryService.class.getName());
			if (ref != null) {
				registry = ((ProcessInstanceRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
				registries.put(lighthouseDomain, registry);
			}
		}
		return registry;
	}
	
	public List<ProcessInstance> findByProcessInstanceDefinition(ProcessInstanceDefinition definition, int pageSize, int pageNo) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(definition.getProcessTask());
		return getRegistry(lighthouseDomain).findByProcessTask(definition.getProcessTask(), pageSize, pageNo);
	}

	public List<ProcessInstance> findInstancesAfter(ProcessInstance instance, int maxResults) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(instance.getProcessInstanceDefinition().getProcessTask());
		return getRegistry(lighthouseDomain).findAfterInstance(instance, maxResults);
	}

	public List<ProcessInstance> findInstancesBefore(ProcessInstance instance, int maxResults) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(instance.getProcessInstanceDefinition().getProcessTask());
		return getRegistry(lighthouseDomain).findBeforeInstance(instance, maxResults); 
	}
}
