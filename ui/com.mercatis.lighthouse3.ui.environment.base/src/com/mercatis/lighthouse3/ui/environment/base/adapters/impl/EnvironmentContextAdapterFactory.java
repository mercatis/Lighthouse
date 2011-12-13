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
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


@SuppressWarnings("rawtypes")
public class EnvironmentContextAdapterFactory implements IAdapterFactory {

	private Class[] adapters = new Class[] { ContextAdapter.class };
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!adapterType.equals(ContextAdapter.class))
			return null;
		
		if (adaptableObject instanceof LighthouseDomain)
			return new LighthouseDomainContextAdapter();
		
		if (adaptableObject instanceof DeploymentContainer)
			return new DeploymentContainerContextAdapter();
		
		if (adaptableObject instanceof Location)
			return new LocationContextAdapter();
		
		if (adaptableObject instanceof EnvironmentContainer)
			return new EnvironmentContainerContextAdapter();
		
		if (adaptableObject instanceof ProcessTaskContainer)
			return new ProcessTaskContainerContextAdapter();
		
		if (adaptableObject instanceof SoftwareComponentContainer)
			return new SoftwareComponentContainerContextAdapter();
		
		if (adaptableObject instanceof Deployment)
			return new DeploymentContextAdapter();
		
		if (adaptableObject instanceof Environment)
			return new EnvironmentContextAdapter();
		
		if (adaptableObject instanceof ProcessTask)
			return new ProcessTaskContextAdapter();
		
		if (adaptableObject instanceof SoftwareComponent)
			return new SoftwareComponentContextAdapter();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return adapters;
	}

}
