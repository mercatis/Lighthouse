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
import com.mercatis.lighthouse3.domainmodel.environment.EnvironmentRegistry;
import com.mercatis.lighthouse3.services.EnvironmentRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerEnvironmentRegistryImplementation;


public class EagerEnvironmentRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<EnvironmentRegistry, EagerEnvironmentRegistryImplementation> implements EnvironmentRegistryFactoryService {
	
	/**
	 * @param context
	 */
	public EagerEnvironmentRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}
	
	@Override
	protected EagerEnvironmentRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerEnvironmentRegistryImplementation(cd.serverUrl, getDeploymentRegistry(project), cd.user, cd.password);
	}
}
