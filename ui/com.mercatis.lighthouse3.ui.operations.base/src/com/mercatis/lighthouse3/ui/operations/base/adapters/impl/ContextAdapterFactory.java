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
package com.mercatis.lighthouse3.ui.operations.base.adapters.impl;

import org.eclipse.core.runtime.IAdapterFactory;

import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


@SuppressWarnings("rawtypes")
public class ContextAdapterFactory implements IAdapterFactory {

	private Class[] adapters = new Class[] { ContextAdapter.class };
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!adapterType.equals(ContextAdapter.class))
			return null;
		
		if (adaptableObject instanceof Job)
			return new JobContextAdapter();
		
		if (adaptableObject instanceof OperationInstallation)
			return new OperationInstallationContextAdapter();
		
		if (adaptableObject instanceof OperationInstallationWrapper)
			return new OperationInstallationContextAdapter();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return adapters;
	}

}
