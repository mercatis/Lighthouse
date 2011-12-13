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
package com.mercatis.lighthouse3.services.internal;

import org.eclipse.core.resources.IProject;
import org.osgi.framework.BundleContext;
import com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry;
import com.mercatis.lighthouse3.services.OperationRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerOperationRegistryImplementation;


public class EagerOperationRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<OperationRegistry, EagerOperationRegistryImplementation> implements OperationRegistryFactoryService {

	/**
	 * @param context
	 */
	public EagerOperationRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerOperationRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerOperationRegistryImplementation(cd.serverUrl, cd.user, cd.password);
	}
}
