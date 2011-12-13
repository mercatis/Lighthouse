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
package com.mercatis.lighthouse3.status.listener;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.status.ui.views.StatusHistoryView;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


@SuppressWarnings("rawtypes")
public class DomainListener implements LighthouseDomainListener {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		closeAllViews(StatusHistoryView.class);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {

	}

	private void closeAllViews(final Class viewType) {
	    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		
	    for (int w = 0; w < windows.length; w++) {
	        final IWorkbenchPage[] pages = windows[w].getPages();
				
	        for (int p = 0; p < pages.length; p++) {
	            final IWorkbenchPage page = pages[p];
	            final IViewReference[] viewRefs = page.getViewReferences();
					
	            // of a given workbench window
	            for (int v = 0; v < viewRefs.length; v++) {
	                final IViewReference viewRef = viewRefs[v];
	                final IWorkbenchPart viewPart = viewRef.getPart(false);
	                final Class partType = (viewPart != null) ? viewPart.getClass() : null;
						
	                if (viewType == null ||  viewType.equals(partType)) {
	                    page.hideView(viewRef);
	                }
	            }
	        }
	    }
	}
	
}
