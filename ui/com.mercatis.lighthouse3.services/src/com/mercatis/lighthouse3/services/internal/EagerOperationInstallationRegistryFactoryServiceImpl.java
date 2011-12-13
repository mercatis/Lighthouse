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
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.services.OperationInstallationRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerOperationInstallationRegistryImplementation;


public class EagerOperationInstallationRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<OperationInstallationRegistry, EagerOperationInstallationRegistryImplementation> implements OperationInstallationRegistryFactoryService {

	public EagerOperationInstallationRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerOperationInstallationRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerOperationInstallationRegistryImplementation(cd.serverUrl, getDeploymentRegistry(project), cd.user, cd.password);
	}
}
