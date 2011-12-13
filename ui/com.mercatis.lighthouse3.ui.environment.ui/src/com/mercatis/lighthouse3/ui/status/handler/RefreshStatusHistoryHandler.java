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
package com.mercatis.lighthouse3.ui.status.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.status.ui.views.StatusHistoryView;


public class RefreshStatusHistoryHandler extends AbstractHandler implements IHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IViewReference [] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (int i = 0; i < views.length; i++) {
			if(views[i].getId().equals(StatusHistoryView.ID)) {
				refreshView(views[i]);
				return null;
			}
		}
		return null;
	}

	/**
	 * @param viewReference
	 */
	private void refreshView(IViewReference viewReference) {
		StatusHistoryView view = (StatusHistoryView) viewReference.getView(false);
		
		if(view != null) {
			view.refreshStatus();
		}
	}
	

}
