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
package com.mercatis.lighthouse3.services.internal;

import org.eclipse.core.resources.IProject;
import org.osgi.framework.BundleContext;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceRegistry;
import com.mercatis.lighthouse3.services.ProcessInstanceRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerProcessInstanceRegistryImplementation;


public class EagerProcessInstanceFactoryServiceImpl extends AbstractEagerRegistryFactoryService<ProcessInstanceRegistry, EagerProcessInstanceRegistryImplementation> implements ProcessInstanceRegistryFactoryService {

	public EagerProcessInstanceFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerProcessInstanceRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerProcessInstanceRegistryImplementation(cd.serverUrl, getProcessInstanceDefinitionRegistry(project), getEventRegistry(project), cd.user, cd.password);
	}
}
