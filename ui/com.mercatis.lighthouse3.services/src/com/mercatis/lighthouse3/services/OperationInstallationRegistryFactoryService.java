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
package com.mercatis.lighthouse3.services;

import org.eclipse.core.resources.IProject;

import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;


public interface OperationInstallationRegistryFactoryService extends RegistryFactoryService<OperationInstallationRegistry> {

	public OperationInstallationRegistry getRegistry(String lighthouseDomain);
	
	public OperationInstallationRegistry getRegistry(IProject project);
	
}
