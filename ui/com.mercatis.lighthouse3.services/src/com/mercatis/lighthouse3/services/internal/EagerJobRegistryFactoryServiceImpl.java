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
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.services.JobRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerJobRegistryImplementation;


public class EagerJobRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<JobRegistry, EagerJobRegistryImplementation> implements JobRegistryFactoryService {

	/**
	 * @param context
	 */
	public EagerJobRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerJobRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerJobRegistryImplementation(cd.serverUrl, getSoftwareComponentRegistry(project), getDeploymentRegistry(project), getOperationInstallationRegistry(project), cd.user, cd.password);
	}
}
