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

import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class SoftwareComponentContainerAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		List<SoftwareComponent> components = ((SoftwareComponentContainer) adaptee).getSoftwareComponents();
		List<SoftwareComponent> tmp = new LinkedList<SoftwareComponent>();
		for (SoftwareComponent softwareComponent : components) {
			HierarchicalEntityAdapter adapter = (HierarchicalEntityAdapter) Platform.getAdapterManager().getAdapter(softwareComponent, HierarchicalEntityAdapter.class);
			if (CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_VIEW, softwareComponent) || adapter.hasChildren(softwareComponent)) {
				tmp.add(softwareComponent);
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
		return ((SoftwareComponentContainer) adaptee).getLighthouseDomain();
	}
	
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return ((SoftwareComponentContainer) adaptee).getLighthouseDomain();
	}

	public static Comparator<SoftwareComponent> comparator = new Comparator<SoftwareComponent>() {

		public int compare(SoftwareComponent o1, SoftwareComponent o2) {
			return getLabel(o1).compareTo(getLabel(o2));
		}
		
		private String getLabel(SoftwareComponent component) {
			return component.getLongName() == null || component.getLongName().length() == 0
				? component.getCode()
				: component.getLongName();
		}
	};
}
