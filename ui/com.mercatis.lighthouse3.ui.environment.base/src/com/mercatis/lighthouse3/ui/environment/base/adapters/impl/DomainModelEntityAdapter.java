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

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalEntityAdapter;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainServiceImpl;



public class DomainModelEntityAdapter implements HierarchicalEntityAdapter, DomainBoundEntityAdapter {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object adaptee) {
		return EMPTY_ARRAY;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object adaptee) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.HierarchicalAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object adaptee) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.adapters.DomainBoundEntityAdapter#getLighthouseDomain(java.lang.Object)
	 */
	public LighthouseDomain getLighthouseDomain(Object adaptee) {
		return ((DomainServiceImpl) CommonBaseActivator.getPlugin().getDomainService()).getLighthouseDomain(((DomainModelEntity) adaptee).getLighthouseDomain());
	}
}
