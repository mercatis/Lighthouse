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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts;

import java.util.Hashtable;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.ui.IEditorPart;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.swimlaneeditor.IInstanceView;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneRosterModel;


public class ProcessTaskEditPartFactory implements EditPartFactory {

	private IEditorPart creatingEditor;
	public static Hashtable<Object, EditPart> editPartRegistry = new Hashtable<Object, EditPart>();

	public ProcessTaskEditPartFactory setCreatingEditor(IEditorPart editor) {
		this.creatingEditor = editor;
		return this;
	}
	
	public EditPart createEditPart(EditPart part, Object obj) {
		if (obj instanceof ProcessInstanceDefinition) {
			ProcessInstanceDefinitionEditPart p = new ProcessInstanceDefinitionEditPart((ProcessInstanceDefinition) obj);
			if (creatingEditor != null && creatingEditor instanceof IInstanceView) {
				p.setInstanceView((IInstanceView) creatingEditor);
			}
			editPartRegistry.put(obj, p);
			return p;
		}
		if (obj instanceof ProcessTask) {
			EditPart p = new ProcessTaskEditPart((ProcessTask) obj);
			editPartRegistry.put(obj, p);
			return p;
		}
		if (obj instanceof SwimlaneModel) {
			EditPart p = new SwimlaneEditPart(part, (SwimlaneModel) obj);
			editPartRegistry.put(obj, p);
			return p;
		}
		if (obj instanceof SwimlaneRosterModel) {
			EditPart p = new SwimlaneRosterEditPart(part, (SwimlaneRosterModel) obj);
			editPartRegistry.put(obj, p);
			return p;
		}
		if (obj instanceof ProcessTaskModel) {
			ProcessTaskModelEditPart p = new ProcessTaskModelEditPart(part, (ProcessTaskModel) obj);
			if (((ProcessTaskModel) obj).getProcessTask() != null)
				editPartRegistry.put(((ProcessTaskModel) obj).getProcessTask(), p);
			if (creatingEditor != null)
				p.setCreatingEditor(creatingEditor);
			return p;
		}
		if (obj instanceof ConnectionModel) {
			ConnectionEditPart p;
			if (editPartRegistry.get(obj) != null) {
				p = (ConnectionEditPart) editPartRegistry.get(obj);
			} else {
				p = new ConnectionEditPart(part, (ConnectionModel) obj);
				editPartRegistry.put(obj, p);
			}
			return p;
		}
		return null;
	}
}
