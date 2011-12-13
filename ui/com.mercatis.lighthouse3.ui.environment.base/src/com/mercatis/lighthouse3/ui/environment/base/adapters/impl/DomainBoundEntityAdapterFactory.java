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
package com.mercatis.lighthouse3.ui.environment.base.adapters.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;


@SuppressWarnings("rawtypes")
public class DomainBoundEntityAdapterFactory implements IAdapterFactory {

	private Class[] adapters = new Class[] { DomainBoundEntityAdapter.class };
	
	private Map<Class<?>, DomainBoundEntityAdapter> adapterLookup;

	public DomainBoundEntityAdapterFactory() {
		this.adapterLookup = new HashMap<Class<?>, DomainBoundEntityAdapter>();
		this.adapterLookup.put(LighthouseDomain.class, new LighthouseDomainAdapter());
		this.adapterLookup.put(DeploymentContainer.class, new DeploymentContainerAdapter());
		this.adapterLookup.put(EnvironmentContainer.class, new EnvironmentContainerAdapter());
		this.adapterLookup.put(ProcessTaskContainer.class, new ProcessTaskContainerAdapter());
		this.adapterLookup.put(SoftwareComponentContainer.class, new SoftwareComponentContainerAdapter());
		this.adapterLookup.put(Location.class, new LocationAdapter());
		this.adapterLookup.put(Deployment.class, new DeploymentAdapter());
		this.adapterLookup.put(Environment.class, new EnvironmentAdapter());
		this.adapterLookup.put(ProcessTask.class, new ProcessTaskAdapter());
		this.adapterLookup.put(SoftwareComponent.class, new SoftwareComponentAdapter());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != DomainBoundEntityAdapter.class) {
			return null;
		}
		
		return adapterLookup.get(adaptableObject.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return adapters;
	}

}
