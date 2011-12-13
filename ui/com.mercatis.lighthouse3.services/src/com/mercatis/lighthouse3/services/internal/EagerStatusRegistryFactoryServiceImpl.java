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
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.domainmodel.events.EventRegistry;
import com.mercatis.lighthouse3.domainmodel.status.StatusRegistry;
import com.mercatis.lighthouse3.services.StatusRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerStatusRegistryImplementation;


public class EagerStatusRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<StatusRegistry, EagerStatusRegistryImplementation> implements StatusRegistryFactoryService {

	public EagerStatusRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}

	@Override
	protected EagerStatusRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		EnvironmentRegistry environmentRegistry = getEnvironmentRegistry(project);
		ProcessTaskRegistry processTaskRegistry = getProcessTaskRegistry(project);
		DeploymentRegistry deploymentRegistry = getDeploymentRegistry(project);
		SoftwareComponentRegistry softwareComponentRegistry = getSoftwareComponentRegistry(project);
		EventRegistry eventRegistry = getEventRegistry(project);
		return new EagerStatusRegistryImplementation(cd.serverUrl, environmentRegistry, processTaskRegistry, deploymentRegistry, softwareComponentRegistry, eventRegistry, cd.user, cd.password);
	}
}
