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

import com.mercatis.lighthouse3.domainmodel.operations.OperationRegistry;


public interface OperationRegistryFactoryService extends RegistryFactoryService<OperationRegistry> {

	public OperationRegistry getRegistry(String lighthouseDomain);
	
	public OperationRegistry getRegistry(IProject project);
	
}
