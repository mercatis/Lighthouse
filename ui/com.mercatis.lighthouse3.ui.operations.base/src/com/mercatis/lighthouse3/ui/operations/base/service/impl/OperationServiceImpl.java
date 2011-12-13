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
package com.mercatis.lighthouse3.ui.operations.base.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry;
import com.mercatis.lighthouse3.services.OperationRegistryFactoryService;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.model.Category;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationConfiguration;
import com.mercatis.lighthouse3.ui.operations.base.service.OperationService;


public class OperationServiceImpl implements OperationService, LighthouseDomainListener {
	
	private Map<LighthouseDomain, OperationRegistry> operationRegistries;
	
	private BundleContext context;
	
	public OperationServiceImpl(BundleContext context) {
		this.context = context;
		this.operationRegistries = new HashMap<LighthouseDomain, OperationRegistry>();
	}
	
	private OperationRegistry getOperationRegistry(LighthouseDomain lighthouseDomain) {
		ServiceReference ref = context.getServiceReference(OperationRegistryFactoryService.class.getName());
		if (ref != null) {
			OperationRegistry registry = ((OperationRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
			operationRegistries.put(lighthouseDomain, registry);
			return registry;
		}
		
		return null;
	}

	public List<Category<Operation>> findAllCategories(LighthouseDomain lighthouseDomain) {
		List<Category<Operation>> categories = new ArrayList<Category<Operation>>();
		for (String catname : getOperationRegistry(lighthouseDomain).findAllCategories()) {
			categories.add(new Category<Operation>(lighthouseDomain, catname, findByCategory(lighthouseDomain, catname)));
		}
		Collections.sort(categories);
		return categories;
	}

	public List<String> findAllCodes(LighthouseDomain lighthouseDomain) {
		List<String> codes = new ArrayList<String>(getOperationRegistry(lighthouseDomain).findAllCodes());
		Collections.sort(codes);
		return codes;
	}

	public List<Operation> findByCategory(LighthouseDomain lighthouseDomain, String category) {
		List<Operation> operations = new ArrayList<Operation>(getOperationRegistry(lighthouseDomain).findByCategory(category));
		Collections.sort(operations, operationComparator);
		return operations;
	}

	public void persist(LighthouseDomain lighthouseDomain, Operation operation) {
		getOperationRegistry(lighthouseDomain).persist(operation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operation, null, null, null);
	}

	public void update(LighthouseDomain lighthouseDomain, Operation operation) {
		getOperationRegistry(lighthouseDomain).update(operation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operation, null, null, null);
	}

	public Operation findByCode(LighthouseDomain lighthouseDomain, String code) {
		return getOperationRegistry(lighthouseDomain).findByCode(code);
	}
	
	public Operation findInstalled(LighthouseDomain lighthouseDomain, OperationInstallation operationInstallation) {
		return getOperationRegistry(lighthouseDomain).findInstalled(operationInstallation);
	}

	public void delete(LighthouseDomain lighthouseDomain, Operation operation) {
		getOperationRegistry(lighthouseDomain).delete(operation);
		OperationBase.fireOperationsChanged(lighthouseDomain, operation, null, null, null);
	}

	public List<String> findAllCategoryStrings(LighthouseDomain lighthouseDomain) {
		List<String> categories = new ArrayList<String>(getOperationRegistry(lighthouseDomain).findAllCategories());
		Collections.sort(categories);
		return categories;
	}

	public OperationConfiguration getOperationConfiguration(LighthouseDomain lighthouseDomain) {
		return new OperationConfigurationImpl(lighthouseDomain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		operationRegistries.remove(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
	}
	
	private Comparator<Operation> operationComparator = new Comparator<Operation>() {

		public int compare(Operation arg0, Operation arg1) {
			return getDislpayNameForOperation(arg0).compareTo(getDislpayNameForOperation(arg1));
		}
	};
	
	private String getDislpayNameForOperation(Operation operation) {
		return operation.getLongName() != null && operation.getLongName().length() > 0
				? operation.getLongName()
				: operation.getCode();
	}
}
