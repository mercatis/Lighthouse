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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.security.Security;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class ClearStatusHandler extends AbstractStructuredSelectionHandler {

	protected void execute(Object element) throws ExecutionException {
		Status s = null;
		if (element instanceof Status) {
			s = (Status) element;
		}
		else if (element instanceof StatusEditingObject) {
			s = ((StatusEditingObject)element).getStatus();
		}
		if (s != null) {
			InputDialog reason = new InputDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Enter a reason", "Please provide a reason for clearing", null, null);
			if (reason.open() == Dialog.OK) {
				LighthouseDomain domain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomain(s.getLighthouseDomain());
				String context = ((ContextAdapter) domain.getAdapter(ContextAdapter.class)).toContext(domain);
				String loginName = Security.getLoginName(context);
				CommonBaseActivator.getPlugin().getStatusService().clearStatusManually(s, reason.getValue(), loginName);
			}
		}
	}
}
