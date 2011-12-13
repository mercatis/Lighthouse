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
import com.mercatis.lighthouse3.services.DeploymentRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerDeploymentRegistryImplementation;


public class EagerDeploymentRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<DeploymentRegistry, EagerDeploymentRegistryImplementation> implements DeploymentRegistryFactoryService {

	/**
	 * @param context
	 */
	public EagerDeploymentRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerDeploymentRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerDeploymentRegistryImplementation(cd.serverUrl, getSoftwareComponentRegistry(project), cd.user, cd.password);
	}
}
