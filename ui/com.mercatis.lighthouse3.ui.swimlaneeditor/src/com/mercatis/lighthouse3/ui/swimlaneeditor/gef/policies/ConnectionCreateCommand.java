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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPart;


public class ConnectionCreateCommand extends Command {
	private EditPart contentEditPart;
	private ProcessTaskModel beginTask;
	private ProcessTaskModel endTask;

	public ConnectionCreateCommand(EditPart contentEditPart) {
		this.contentEditPart = contentEditPart;
	}
	
	public void setEndTask(ProcessTaskModel endTask) {
		this.endTask = endTask;
	}

	public void setBeginTask(ProcessTaskModel beginTask) {
		this.beginTask = beginTask;
	}

	public boolean isBeginSet() {
		return this.beginTask != null;
	}

	public boolean isEndSet() {
		return this.endTask != null;
	}

	@Override
	public boolean canExecute() {
		if (this.contentEditPart instanceof ProcessTaskEditPart && CodeGuard.hasRole(Role.PROCESS_TASK_MODEL, ((ProcessTaskEditPart) this.contentEditPart).getProcessTask()))
			return ((isBeginSet() && isEndSet()) && !beginTask.equals(endTask));
		return false;
	}

	@Override
	public void execute() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.setTransition(beginTask.getProcessTask(), endTask.getProcessTask());
			contentEditPart.refresh();
		}
	}

	@Override
	public void undo() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.removeTransition(beginTask.getProcessTask(), endTask.getProcessTask());
			contentEditPart.refresh();
		}
	}

	@Override
	public boolean canUndo() {
		return (isBeginSet() && isEndSet());
	}
}
