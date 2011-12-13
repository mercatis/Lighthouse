/**
 * (c) Copyright 2010 mercatis technologies AG
 *
 * All rights reserved.
 *
 * Part of Lighthouse 3
 *
 * This source code is proprietary trade secret information of
 * mercatis information systems GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * mercatis information systems GmbH.
 */
package com.mercatis.lighthouse3.services.internal.registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry;
import com.mercatis.lighthouse3.persistence.operations.rest.OperationRegistryImplementation;


@SuppressWarnings("rawtypes")
public class EagerOperationRegistryImplementation extends EagerCodedDomainModelEntityDAOImplementation<Operation>
	implements OperationRegistry {

	private OperationRegistryImplementation delegateRegistry = null;
	
	public EagerOperationRegistryImplementation(String serverUrl, String user, String password) {
		this.delegateRegistry = new OperationRegistryImplementation(serverUrl, user, password);
		this.invalidate();
	}
	
	public EagerOperationRegistryImplementation(String serverUrl) {
		this(serverUrl, null, null);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.services.internal.registries2.EagerDomainModelEntityDAOImplementation#delegateRegistry()
	 */
	@Override
	protected DomainModelEntityDAO<Operation> delegateRegistry() {
		return delegateRegistry;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO#getManagedType()
	 */
	public Class getManagedType() {
		return Operation.class;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry#findAllCategories()
	 */
	public List<String> findAllCategories() {
		HashSet<String> categories = new HashSet<String>();
		for (Operation operation : cache.values()) {
			categories.add(operation.getCategory());
		}
		
		return new ArrayList<String>(categories);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry#findAllCodes()
	 */
	public List<String> findAllCodes() {
		return new ArrayList<String>(cache.keySet());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry#findByCategory(java.lang.String)
	 */
	public List<Operation> findByCategory(String category) {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		for (Operation operation : cache.values()) {
			if (operation.getCategory() != null && operation.getCategory().equals(category)) {
				operations.add(operation);
			}
		}
		
		return operations;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry#findInstalled(com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation)
	 */
	public Operation findInstalled(OperationInstallation operationInstallation) {
		return delegateRegistry.findInstalled(operationInstallation);
	}
}
