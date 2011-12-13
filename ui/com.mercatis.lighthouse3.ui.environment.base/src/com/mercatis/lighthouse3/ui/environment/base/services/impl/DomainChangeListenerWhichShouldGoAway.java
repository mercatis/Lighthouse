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
package com.mercatis.lighthouse3.ui.environment.base.services.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class DomainChangeListenerWhichShouldGoAway implements
		LighthouseDomainListener {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		try {
			domain.getProject().close(null);
		} catch (CoreException e) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
		try {
			domain.getProject().open(null);
			domain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(domain.getProject());
		} catch (CoreException e) {
			CommonBaseActivator.getPlugin().getLog().log(
					new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		} catch (Exception e) {
			CommonBaseActivator.getPlugin().getLog().log(
					new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, e.getMessage(), e));
		}
	}
	
}
