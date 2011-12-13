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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.swimlaneeditor.ProcessTaskSwimlaneEditor;
import com.mercatis.lighthouse3.ui.swimlaneeditor.SwimlaneEditorPlugin;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskModelEditPart;

/**
 * This SelectionTool enhances the superclass by a DoubleClickAction that opens a ProcessTaskEditor
 * when doubleclicking on a ProcessTask.
 * 
 */
public class ExtendedSelectionTool extends SelectionTool {

	@Override
	public void mouseDoubleClick(MouseEvent e, EditPartViewer viewer) {
		if (e != null && e.button == 1) {
			EditPart part = viewer.findObjectAt(new Point(e.x, e.y));
			if (part instanceof ProcessTaskModelEditPart) {
				ProcessTask task = ((ProcessTaskModel) part.getModel()).getProcessTask();
				CommonBaseActivator.getPlugin().getPlugin();
				LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(task);
				GenericEditorInput<ProcessTask> input = new GenericEditorInput<ProcessTask>(lighthouseDomain, task);
				input.setPayload(ProcessTaskSwimlaneEditor.class);

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, input, "com.mercatis.lighthouse3.ui.environment.editors.ProcessTaskEditor");
				} catch (PartInitException ex) {
					SwimlaneEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, SwimlaneEditorPlugin.PLUGIN_ID, ex.getMessage(), ex));
				}
			}
		}
	}
}
