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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.domainmodel.status.ManualStatusClearance;

public class ViewReasonHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof ManualStatusClearance) {
			ManualStatusClearance msc = (ManualStatusClearance) element;
			String message = "Cleared by: " + msc.getClearer() + "\n\n";
			message += msc.getReason() != null && !msc.getReason().equals("") ? msc.getReason() : "No reason defined";
			MessageDialog md = new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Manual Clearance Reason", null, message, MessageDialog.INFORMATION, new String[] { "Ok" }, 0);
			md.open();
		}
	}
}
