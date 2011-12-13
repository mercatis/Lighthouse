package com.mercatis.lighthouse3.ui.operations.base.listener;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;

public class LighthouseDomainListener implements com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		((com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener)OperationBase.getJobService()).closeDomain(domain);
		((com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener)OperationBase.getOperationService()).closeDomain(domain);
		((com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener)OperationBase.getOperationInstallationService()).closeDomain(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {

	}

}
