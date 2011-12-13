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

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class DeploymentContextAdapter implements ContextAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.internal.ContextTranslator#toContext(java.lang.Object)
	 */
	public String toContext(Object entity) {
		Deployment deployment = (Deployment) entity;
		String domain = deployment.getLighthouseDomain();
		String location = deployment.getLocation();
		String componentCode = deployment.getDeployedComponent().getCode();
		
		return new StringBuilder("//LH3/").append(domain).append("/Deployment").append("/Location(").append(location).append(")").append("/Deployment(").append(componentCode).append(")").toString();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.internal.ContextTranslator#toShortContext(java.lang.Object)
	 */
	public String toShortContext(Object entity) {
		Deployment deployment = (Deployment) entity;
		String location = deployment.getLocation();
		String componentCode = deployment.getDeployedComponent().getCode();
		
		return new StringBuilder("/Deployment").append("/Location(").append(location).append(")").append("/Deployment(").append(componentCode).append(")").toString();
	}

}
