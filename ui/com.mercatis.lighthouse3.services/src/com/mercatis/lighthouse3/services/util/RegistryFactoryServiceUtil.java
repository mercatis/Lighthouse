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
package com.mercatis.lighthouse3.services.util;

import org.eclipse.core.resources.IProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.mercatis.lighthouse3.services.RegistryFactoryService;


@SuppressWarnings("unchecked")
public class RegistryFactoryServiceUtil {

	public static <R, F extends RegistryFactoryService<R>> R getRegistryFactoryService(Class<F> registryFactoryServiceClass, String lighthouseDomain, Object callingObject) {
		Bundle bundle = FrameworkUtil.getBundle(callingObject.getClass());
		ServiceReference<?> reference = bundle.getBundleContext().getServiceReference(registryFactoryServiceClass.getName());
		F registryFactoryService = (F) bundle.getBundleContext().getService(reference);
		return (R) registryFactoryService.getRegistry(lighthouseDomain);
	}
	
	public static <R, F extends RegistryFactoryService<R>> R getRegistryFactoryService(Class<F> registryFactoryServiceClass, IProject project, Object callingObject) {
		Bundle bundle = FrameworkUtil.getBundle(callingObject.getClass());
		ServiceReference<?> reference = bundle.getBundleContext().getServiceReference(registryFactoryServiceClass.getName());
		F registryFactoryService = (F) bundle.getBundleContext().getService(reference);
		return (R) registryFactoryService.getRegistry(project);
	}
}
