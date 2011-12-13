
package com.mercatis.lighthouse3.ui.environment.base.message.center;

import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public interface ILighthouseDomainBroadCaster {

	/**
	 * @param domain
	 */
	public void notifyDomainClosed(LighthouseDomain domain);
	/**
	 * @param domain
	 */
	public void notifyDomainOpened(LighthouseDomain domain);
	
	public void addListener(LighthouseDomainListener listener);
	
	public void removeListener(LighthouseDomainListener listener);

}
