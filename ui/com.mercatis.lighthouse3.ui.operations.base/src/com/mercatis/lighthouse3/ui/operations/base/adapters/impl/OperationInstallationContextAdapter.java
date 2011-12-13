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

import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class OperationInstallationContextAdapter implements ContextAdapter {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.internal.ContextAdapter#toContext(java.lang.Object)
	 */
	public String toContext(Object entity) {
		OperationInstallation operationInstallation  = null;
		
		if (entity instanceof OperationInstallation) {
			operationInstallation = (OperationInstallation) entity;
		}
	
		if (entity instanceof OperationInstallationWrapper) {
			operationInstallation = ((OperationInstallationWrapper) entity).getOperationInstallation();
		}
		
		if (operationInstallation == null) {
				throw new IllegalArgumentException("Unknown entity type.");
		}
	
		Deployment deployment = operationInstallation.getInstallationLocation();
		ContextAdapter deploymentContextAdapter = (ContextAdapter) Platform.getAdapterManager().getAdapter(deployment, ContextAdapter.class);
		return new StringBuilder(deploymentContextAdapter.toContext(deployment))
			.append(String.format("/OperationInstallation(%s)", operationInstallation.getInstalledOperationCode())).toString();
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.security.internal.ContextAdapter#toShortContext(java.lang.Object)
	 */
	public String toShortContext(Object entity) {
		OperationInstallation operationInstallation  = null;
		
		if (entity instanceof OperationInstallation) {
			operationInstallation = (OperationInstallation) entity;
		}
	
		if (entity instanceof OperationInstallationWrapper) {
			operationInstallation = ((OperationInstallationWrapper) entity).getOperationInstallation();
		}
		
		if (operationInstallation == null) {
				throw new IllegalArgumentException("Unknown entity type.");
		}
	
		Deployment deployment = operationInstallation.getInstallationLocation();
		ContextAdapter deploymentContextAdapter = (ContextAdapter) Platform.getAdapterManager().getAdapter(deployment, ContextAdapter.class);
		return new StringBuilder(deploymentContextAdapter.toShortContext(deployment))
			.append(String.format("/OperationInstallation(%s)", operationInstallation.getInstalledOperationCode())).toString();
	}

}
