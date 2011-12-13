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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class EnvironmentContainerAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {
	
	private IAdapterManager adapterManager = Platform.getAdapterManager();
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		//return ((EnvironmentContainer) adaptee).getEnvironments().toArray();
		
		List<Environment> environments = ((EnvironmentContainer) adaptee).getEnvironments();
		List<Environment> results = new LinkedList<Environment>();
		for (Environment environment : environments) {
			
			if (CodeGuard.hasRole(Role.ENVIRONMENT_VIEW, environment)) {
				results.add(environment);
			} else {
				HierarchicalEntityAdapter adapter = ((HierarchicalEntityAdapter) adapterManager.getAdapter(environment, HierarchicalEntityAdapter.class));

				if (adapter.hasChildren(environment)) {
					results.add(environment);
				}
				
			}
			
		}
		Collections.sort(results, comparator);
		return results.toArray();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object adaptee) {
		return getChildren(adaptee).length > 0;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return ((EnvironmentContainer) adaptee).getLighthouseDomain();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter#getLighthouseDomain(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return ((EnvironmentContainer) adaptee).getLighthouseDomain();
	}

	private Comparator<Environment> comparator = new Comparator<Environment>() {

		public int compare(Environment o1, Environment o2) {
			return getLabel(o1).compareTo(getLabel(o2));
		}
		
		private String getLabel(Environment environment) {
			return environment.getLongName() == null || environment.getLongName().length() == 0
				? environment.getCode()
				: environment.getLongName();
		}
	};
}
