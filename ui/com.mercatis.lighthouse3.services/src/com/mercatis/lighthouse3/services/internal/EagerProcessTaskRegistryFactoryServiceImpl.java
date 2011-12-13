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
import com.mercatis.lighthouse3.domainmodel.environment.DeploymentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.services.ProcessTaskRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerProcessTaskRegistryImplementation;

public class EagerProcessTaskRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<ProcessTaskRegistry, EagerProcessTaskRegistryImplementation> implements ProcessTaskRegistryFactoryService {

	/**
	 * @param context
	 */
	public EagerProcessTaskRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerProcessTaskRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		DeploymentRegistry deploymentRegistry = getDeploymentRegistry(project);
		return new EagerProcessTaskRegistryImplementation(cd.serverUrl, deploymentRegistry, cd.user, cd.password);
	}

}
