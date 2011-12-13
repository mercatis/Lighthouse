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
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPart;

/**
 * Delete the connection between two ProcessTasks
 * 
 */
public class ConnectionDeleteCommand extends Command {

	private ConnectionModel connection;
	private EditPart contentEditPart;

	public ConnectionDeleteCommand(EditPart contentEditPart, ConnectionModel connection) {
		this.contentEditPart = contentEditPart;
		this.connection = connection;
	}

	@Override
	public boolean canExecute() {
		if (this.contentEditPart instanceof ProcessTaskEditPart && CodeGuard.hasRole(Role.PROCESS_TASK_MODEL, ((ProcessTaskEditPart) this.contentEditPart).getProcessTask()))
			return connection != null;
		return false;
	}

	@Override
	public void execute() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.removeTransition(connection.getFrom(), connection.getTo());
			contentEditPart.refresh();
		}
	}

	@Override
	public boolean canUndo() {
		return canExecute();
	}

	@Override
	public void undo() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.setTransition(connection.getFrom(), connection.getTo());
			contentEditPart.refresh();
		}
	}
}
