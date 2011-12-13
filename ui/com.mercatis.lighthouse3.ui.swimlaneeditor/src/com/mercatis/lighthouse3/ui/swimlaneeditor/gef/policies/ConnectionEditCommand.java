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
import org.eclipse.gef.requests.ReconnectRequest;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPart;

/**
 * This command is used to reconnect a Connection (replace start- or endpoint).
 * 
 */
public class ConnectionEditCommand extends Command {
	
	private EditPart contentEditPart;
	private ConnectionModel oldConnection;
	private EditPart newSource = null;
	private EditPart newTarget = null;

	public ConnectionEditCommand(EditPart contentEditPart, ReconnectRequest request) {
		this.contentEditPart = contentEditPart;
		if (request.isMovingStartAnchor()) {
			newSource = request.getTarget();
		} else {
			newTarget = request.getTarget();
		}
		oldConnection = ((ConnectionModel) request.getConnectionEditPart().getModel());
	}

	public void setNetSource(EditPart part) {
		this.newSource = part;
	}

	public void setNewTarget(EditPart part) {
		this.newTarget = part;
	}

	@Override
	public boolean canExecute() {
		if (this.contentEditPart instanceof ProcessTaskEditPart && CodeGuard.hasRole(Role.PROCESS_TASK_MODEL, ((ProcessTaskEditPart) this.contentEditPart).getProcessTask()))
			return oldConnection != null && (newSource != null || newTarget != null);
		return false;
	}

	@Override
	public boolean canUndo() {
		return canExecute();
	}

	@Override
	public void undo() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart .getModel();
			ProcessTask source = null;
			ProcessTask target = null;
			if (newSource != null) {
				source = ((ProcessTaskModel) newSource.getModel()).getProcessTask();
				target = oldConnection.getTo();
			}
			if (newTarget != null) {
				source = oldConnection.getFrom();
				target = ((ProcessTaskModel) newTarget.getModel()).getProcessTask();
			}
			parent.removeTransition(source, target);
			parent.setTransition(oldConnection.getFrom(), oldConnection.getTo());
			contentEditPart.refresh();
		}
	}

	@Override
	public void execute() {
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			ProcessTask source = null;
			ProcessTask target = null;
			if (newSource != null) {
				source = ((ProcessTaskModel) newSource.getModel()).getProcessTask();
				target = oldConnection.getTo();
			}
			if (newTarget != null) {
				source = oldConnection.getFrom();
				target = ((ProcessTaskModel) newTarget.getModel()).getProcessTask();
			}
			parent.removeTransition(oldConnection.getFrom(), oldConnection.getTo());
			parent.setTransition(source, target);
			contentEditPart.refresh();
		}
	}
}
