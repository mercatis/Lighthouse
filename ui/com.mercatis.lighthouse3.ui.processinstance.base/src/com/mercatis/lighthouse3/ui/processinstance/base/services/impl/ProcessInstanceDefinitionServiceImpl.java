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
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinitionRegistry;
import com.mercatis.lighthouse3.services.ProcessInstanceDefinitionRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.processinstance.base.services.ProcessInstanceDefinitionService;


public class ProcessInstanceDefinitionServiceImpl implements ProcessInstanceDefinitionService {

	private Map<LighthouseDomain, ProcessInstanceDefinitionRegistry> registries = new HashMap<LighthouseDomain, ProcessInstanceDefinitionRegistry>();
	private BundleContext context;
	
	public ProcessInstanceDefinitionServiceImpl(BundleContext context) {
		this.context = context;
	}
	
	private ProcessInstanceDefinitionRegistry getRegistry(LighthouseDomain lighthouseDomain) {
		ProcessInstanceDefinitionRegistry registry = registries.get(lighthouseDomain);
		if (registry == null) {
			ServiceReference ref = context.getServiceReference(ProcessInstanceDefinitionRegistryFactoryService.class.getName());
			if (ref != null) {
				registry = ((ProcessInstanceDefinitionRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
				registries.put(lighthouseDomain, registry);
			}
		}
		return registry;
	}
	
	public ProcessInstanceDefinition findByProcessTask(ProcessTask processTask) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(processTask);
		return getRegistry(lighthouseDomain).findByProcessTask(processTask);
	}
}
