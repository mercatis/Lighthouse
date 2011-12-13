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
package com.mercatis.lighthouse3.ui.operations.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;


public class ExecuteJobHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof Job) {
			Job job = (Job) element;
			MessageDialog md = new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Manual Execute", null, "Execute " + LabelConverter.getLabel(job) + "?",
					MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
			int button = md.open();
			if (button == 0) {
				LighthouseDomain lighthouseDomain = OperationBase.getJobService().getLighthouseDomainForJob(job);
				OperationBase.getOperationInstallationService().execute(lighthouseDomain, job.getScheduledCall());
			}
		}
	}
}
