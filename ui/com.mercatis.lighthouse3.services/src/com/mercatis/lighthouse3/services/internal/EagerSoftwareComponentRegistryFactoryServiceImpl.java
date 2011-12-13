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
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponentRegistry;
import com.mercatis.lighthouse3.services.SoftwareComponentRegistryFactoryService;
import com.mercatis.lighthouse3.services.internal.registries.EagerSoftwareComponentRegistryImplementation;


public class EagerSoftwareComponentRegistryFactoryServiceImpl extends AbstractEagerRegistryFactoryService<SoftwareComponentRegistry, EagerSoftwareComponentRegistryImplementation>  implements SoftwareComponentRegistryFactoryService {

	public EagerSoftwareComponentRegistryFactoryServiceImpl(BundleContext context) {
		super(context);
	}

	@Override
	protected EagerSoftwareComponentRegistryImplementation createImpl(IProject project) {
		CommonData cd = getData(project);
		return new EagerSoftwareComponentRegistryImplementation(cd.serverUrl, cd.user, cd.password);
	}
}
