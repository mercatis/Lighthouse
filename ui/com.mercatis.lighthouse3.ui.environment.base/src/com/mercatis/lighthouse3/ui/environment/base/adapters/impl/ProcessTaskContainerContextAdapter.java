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

import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class ProcessTaskContainerContextAdapter implements ContextAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.security.ContextAdapter#toContext(java.lang.Object)
	 */
	public String toContext(Object entity) {
		ProcessTaskContainer container = (ProcessTaskContainer) entity;
		String domain = container.getLighthouseDomain().getServerDomainKey();
		
		return new StringBuilder("//LH3/").append(domain).append("/ProcessTask").toString();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.security.ContextAdapter#toShortContext(java.lang.Object)
	 */
	public String toShortContext(Object entity) {
		return new StringBuilder("/ProcessTask").toString();
	}

}
