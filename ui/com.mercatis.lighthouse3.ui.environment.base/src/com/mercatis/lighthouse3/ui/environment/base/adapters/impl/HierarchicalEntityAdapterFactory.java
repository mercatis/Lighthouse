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

import org.eclipse.core.runtime.IAdapterFactory;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;


@SuppressWarnings("rawtypes")
public class HierarchicalEntityAdapterFactory implements IAdapterFactory {

	private Class[] adapters = new Class[] { HierarchicalEntityAdapter.class };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != HierarchicalEntityAdapter.class) {
			return null;
		}
		if (adaptableObject instanceof LighthouseDomain) {
			return new LighthouseDomainAdapter();
		}
		if (adaptableObject instanceof DeploymentContainer) {
			return new DeploymentContainerAdapter();
		}
		if (adaptableObject instanceof EnvironmentContainer) {
			return new EnvironmentContainerAdapter();
		}
		if (adaptableObject instanceof ProcessTaskContainer) {
			return new ProcessTaskContainerAdapter();
		}
		if (adaptableObject instanceof SoftwareComponentContainer) {
			return new SoftwareComponentContainerAdapter();
		}
		if (adaptableObject instanceof Location) {
			return new LocationAdapter();
		}
		if (adaptableObject instanceof Deployment) {
			return new DeploymentAdapter();
		}
		if (adaptableObject instanceof Environment) {
			return new EnvironmentAdapter();
		}
		if (adaptableObject instanceof ProcessTask) {
			return new ProcessTaskAdapter();
		}
		if (adaptableObject instanceof SoftwareComponent) {
			return new SoftwareComponentAdapter();
		}
		
		return null;
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
