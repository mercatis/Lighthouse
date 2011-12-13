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
package com.mercatis.lighthouse3.ui.operations.base.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.services.OperationInstallationRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationInstallationService;


public class OperationInstallationServiceImpl implements OperationInstallationService, LighthouseDomainListener {

	private Map<LighthouseDomain, OperationInstallationRegistry> operationInstallationRegistries;

	private BundleContext context;
	
	public OperationInstallationServiceImpl(BundleContext context) {
		this.context = context;
		this.operationInstallationRegistries = new HashMap<LighthouseDomain, OperationInstallationRegistry>();
	}
	
	public OperationInstallationRegistry getOperationInstallationRegistry(LighthouseDomain lighthouseDomain) {
		ServiceReference ref = context.getServiceReference(OperationInstallationRegistryFactoryService.class.getName());
		if (ref != null) {
			OperationInstallationRegistry registry = ((OperationInstallationRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
			operationInstallationRegistries.put(lighthouseDomain, registry);
			return registry;
		}
		
		return null;
	}

	public void delete(OperationInstallation operationInstallation) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForOperationInstallation(operationInstallation);
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		registry.delete(operationInstallation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operationInstallation, null, null, null);
	}

	public List<OperationInstallation> findAtDeployment(Deployment deployment) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(deployment);
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		List<OperationInstallation> installations = registry.findAtDeployment(deployment);
		Collections.sort(installations, operationInstallationComparator);
		return installations;
	}

	public OperationInstallation findByDeploymentAndOperation(Deployment deployment, Operation operation) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(deployment);
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		return registry.findByDeploymentAndOperation(deployment, operation.getCode());
	}

	public List<OperationInstallation> findForOperation(LighthouseDomain lighthouseDomain, Operation operation) {
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		List<OperationInstallation> installations = registry.findForOperation(operation.getCode());
		Collections.sort(installations, operationInstallationComparator);
		return installations;
	}

	public void persist(OperationInstallation operationInstallation) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForOperationInstallation(operationInstallation);
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		registry.persist(operationInstallation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operationInstallation, null, null, null);
	}

	public void update(OperationInstallation operationInstallation) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForOperationInstallation(operationInstallation);
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		registry.update(operationInstallation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operationInstallation, null, null, null);
	}
	
	private LighthouseDomain getLighthouseDomainForOperationInstallation(OperationInstallation operationInstallation) {
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(operationInstallation.getInstallationLocation());
	}
	
	public void execute(LighthouseDomain lighthouseDomain, OperationCall operationCall) {
		OperationInstallationRegistry registry = getOperationInstallationRegistry(lighthouseDomain);
		registry.execute(operationCall);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		operationInstallationRegistries.remove(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
	}
	
	private Comparator<OperationInstallation> operationInstallationComparator = new Comparator<OperationInstallation>() {
		public int compare(OperationInstallation o1, OperationInstallation o2) {
			return o1.getInstalledOperationCode().compareTo(o2.getInstalledOperationCode());
		}
	};
}
