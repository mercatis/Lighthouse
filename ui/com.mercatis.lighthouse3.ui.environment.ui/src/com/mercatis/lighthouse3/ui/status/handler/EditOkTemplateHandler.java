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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.status.handler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.status.ui.wizards.TemplateEditor;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;


public class EditOkTemplateHandler extends AbstractStructuredSelectionHandler {

	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof StatusEditingObject) {
			StatusEditingObject statusEditingObject = (StatusEditingObject) element;
			final Status status = statusEditingObject.getStatus();
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getStatusService().getLighthouseDomainForEntity(status);
			
			TemplateEditor wizard = new TemplateEditor(lighthouseDomain, status.getOkTemplate());
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.create();
			dialog.getShell().setText("Template editor");
			dialog.setTitle("Edit OK template on " + LabelConverter.getLabel(status));
			dialog.setMessage("An event matching this template will result in an ok status.");
			if (dialog.open() == Window.OK) {
				BusyIndicator.showWhile(null, new Runnable(){
					public void run() {
						CommonBaseActivator.getPlugin().getStatusService().updateStatus(status);
					}
				});
			}
		}
	}
}
