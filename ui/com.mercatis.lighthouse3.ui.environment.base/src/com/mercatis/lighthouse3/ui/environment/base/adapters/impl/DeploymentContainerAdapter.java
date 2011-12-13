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

import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class DeploymentContainerAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		List<Location> locations = ((DeploymentContainer) adaptee).getLocations();
		List<Location> tmp = new LinkedList<Location>();
		for (Location location : locations) {
			HierarchicalEntityAdapter adapter = (HierarchicalEntityAdapter) location.getAdapter(HierarchicalEntityAdapter.class);
			if (CodeGuard.hasRole(Role.DEPLOYMENT_VIEW, location) || adapter.hasChildren(location)) {
				tmp.add(location);
			}
		}
		Collections.sort(tmp, comparator);
		return tmp.toArray();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object adaptee) {
		return getChildren(adaptee).length > 0;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return ((DeploymentContainer) adaptee).getLighthouseDomain();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter#getLighthouseDomain(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return ((DeploymentContainer) adaptee).getLighthouseDomain();
	}

	private Comparator<Location> comparator = new Comparator<Location>() {

		public int compare(Location o1, Location o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}
	};
}
