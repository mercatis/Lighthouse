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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.operations.base.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;

/**
 * This class wraps the OperationInstallation to provide some action (eg remove) on installations
 * even if the operation server is unreachable.
 */
public class OperationInstallationWrapper {

	private OperationInstallation operationInstallation;
	private LighthouseDomain lighthouseDomain;
	private Operation operation;
	
	public OperationInstallationWrapper(OperationInstallation operationInstallation) {
		this.operationInstallation = operationInstallation;
		this.lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(operationInstallation.getInstallationLocation());
		load();
	}
	
	public OperationInstallation getOperationInstallation() {
		return operationInstallation;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public LighthouseDomain getLighthouseDomain() {
		return lighthouseDomain;
	}
	
	public String getCategory() {
		if (operation == null)
			return "DISABLED";
		else
			return operation.getCategory();
	}
	
	private void load() {
		try {
			operation = OperationBase.getOperationService().findInstalled(lighthouseDomain, operationInstallation);
		}
		catch (Exception e) {
			OperationBase.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationBase.PLUGIN_ID, e.getMessage(), e));
		}
	}
}
