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
package com.mercatis.lighthouse3.ui.status.base.adapters.impl;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class StatusContextAdapter implements ContextAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.ContextAdapter#toContext(java.lang.Object)
	 */
	public String toContext(Object entity) {
		Status status = null;
		if (entity instanceof Status)
			status = (Status) entity;
		
		if (entity instanceof StatusEditingObject)
			status = ((StatusEditingObject) entity).getStatus();
		
		if (status == null)
			throw new IllegalArgumentException("Unknown entity.");
		
		IAdapterManager manager = Platform.getAdapterManager();
		ContextAdapter adapter = (ContextAdapter) manager.getAdapter(status.getContext(), ContextAdapter.class);
		
		return new StringBuilder(adapter.toContext(status.getContext())).append("/Status(").append(status.getCode()).append(")").toString();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.ContextAdapter#toShortContext(java.lang.Object)
	 */
	public String toShortContext(Object entity) {
		return this.toContext(entity);
	}

}
