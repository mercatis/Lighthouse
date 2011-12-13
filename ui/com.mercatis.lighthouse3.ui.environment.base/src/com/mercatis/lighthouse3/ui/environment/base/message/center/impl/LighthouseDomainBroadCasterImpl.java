
package com.mercatis.lighthouse3.ui.environment.base.message.center.impl;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.ui.environment.base.message.center.ILighthouseDomainBroadCaster;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class LighthouseDomainBroadCasterImpl implements ILighthouseDomainBroadCaster {

	private static String LISTENER_ID = "com.mercatis.lighthouse3.ui.base.Domainlistener";
	private List<LighthouseDomainListener> listeners;
	
	public LighthouseDomainBroadCasterImpl() {
		listeners = new LinkedList<LighthouseDomainListener>();
		getAllRegisteredListeners();
	}
	
	private void getAllRegisteredListeners() {
		IConfigurationElement[] decls = Platform.getExtensionRegistry().getConfigurationElementsFor(LISTENER_ID);
		for (int i = 0; i < decls.length; i++) {
			try {
				LighthouseDomainListener extension = (LighthouseDomainListener) decls[i].createExecutableExtension("class");
				listeners.add(extension);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addListener(LighthouseDomainListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(LighthouseDomainListener listener)  {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.message.center.core.LighthouseDomainBroadCaster#notifyDomainClosed(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void notifyDomainClosed(LighthouseDomain domain) {
		for (LighthouseDomainListener listener : listeners) {
			listener.closeDomain(domain);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.message.center.core.LighthouseDomainBroadCaster#notifyDomainOpened(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void notifyDomainOpened(LighthouseDomain domain) {
		for (LighthouseDomainListener listener : listeners) {
			listener.openDomain(domain);
		}
	}
	
}
