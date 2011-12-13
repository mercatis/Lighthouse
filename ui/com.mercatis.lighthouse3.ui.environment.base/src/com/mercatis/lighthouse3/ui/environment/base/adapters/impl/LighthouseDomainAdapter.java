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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;



public class LighthouseDomainAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		LighthouseDomain lighthouseDomain = (LighthouseDomain) adaptee;
		Object[] children = new Object[4];
		children[0] = lighthouseDomain.getDeploymentContainer();
		children[1] = lighthouseDomain.getEnvironmentContainer();
		children[2] = lighthouseDomain.getProcessTaskContainer();
		children[3] = lighthouseDomain.getSoftwareComponentContainer();
		
		return children;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object adaptee) {
		IProject project = ((LighthouseDomain) adaptee).getProject(); 
		if (project.isOpen())
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter#getLighthouseDomain(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return (LighthouseDomain) adaptee;
	}
}
