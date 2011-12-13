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

import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;


public class SoftwareComponentAdapter extends HierarchicalDomainModelEntityAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalDomainModelEntityAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object adaptee) {
		Object[] children = super.getChildren(adaptee);
		List<Object> tmp = new LinkedList<Object>();
		for (Object obj : children) {
			if (hasChildren(obj) || CodeGuard.hasRole(Role.SOFTWARE_COMPONENT_VIEW, obj)) {
				tmp.add(obj);
			}
		}
		//sort
		Collections.sort(tmp, new Comparator<Object>() {

			public int compare(Object o1, Object o2) {
				if(o1 !=null && o2 != null && o1 instanceof SoftwareComponent && o2 instanceof SoftwareComponent){
					return SoftwareComponentContainerAdapter.comparator.compare((SoftwareComponent)o1, (SoftwareComponent)o2);
				}
				return 0;
			}
			
		});
		return tmp.toArray();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalDomainModelEntityAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object adaptee) {
		Object parent = super.getParent(adaptee);
		if (parent != null)
			return parent;
		
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(adaptee).getSoftwareComponentContainer();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalDomainModelEntityAdapter#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object adaptee) {
		return getChildren(adaptee).length > 0;
	}
	
	
}
