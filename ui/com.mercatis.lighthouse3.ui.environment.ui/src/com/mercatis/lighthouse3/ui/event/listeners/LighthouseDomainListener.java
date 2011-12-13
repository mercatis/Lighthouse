/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.ui.event.listeners;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.editors.EventEditorInput;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.editors.EventEditor;


public class LighthouseDomainListener implements com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener {

	/**
	 * 
	 */
	public LighthouseDomainListener() {
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		closeRelatedEditors(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
	}
	
	private void closeRelatedEditors(LighthouseDomain domain) {
		IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			try {
				if(editors[i].getEditorInput() instanceof EventEditorInput) {
					EventEditorInput editor = (EventEditorInput) editors[i].getEditorInput();
					if(editor.getLighthouseDomain().equals(domain)) {
						EventEditor e = (EventEditor)editors[i].getEditor(false);
						e.stopEventReceipt();
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(e,false);
					}
				}
			}catch (Exception e) {
				CommonBaseActivator.getPlugin().getLog().log(
						new Status(IStatus.ERROR, CommonBaseActivator.OLD_EVENT_PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

}
