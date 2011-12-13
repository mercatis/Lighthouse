package com.mercatis.lighthouse3.ui.environment.base.message.listener;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public interface LighthouseDomainListener {

	/**Is called when the a lighthouse 3 domain is closed
	 * 
	 */
	public void closeDomain(LighthouseDomain domain);
	
	/**Is called when a lighthouse 3 domain is opened
	 * 
	 */
	public void openDomain(LighthouseDomain domain);
}
