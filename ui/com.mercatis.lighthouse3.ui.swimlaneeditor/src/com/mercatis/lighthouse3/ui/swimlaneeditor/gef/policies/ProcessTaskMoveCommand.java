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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IExtendedCommand;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel.SwimlaneType;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPart;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.SwimlaneEditPart;

/**
 * This move command is used when drag&drop a ProcessTask to some Swimlane
 * 
 */
public class ProcessTaskMoveCommand extends Command implements IExtendedCommand {
	private EditPart target;
	private EditPart origin;
	private ProcessTaskModel itemToMove;
	private boolean executed = false;
	private List<String> swimlanesAvailable = new LinkedList<String>();
	private EditPart contentEditPart;
	
	public ProcessTaskMoveCommand(EditPart contentEditPart) {
		this.contentEditPart = contentEditPart;
	}

	@Override
	public synchronized void execute() {
		if (target.equals(origin)) {
			return;
		}
		String targetName = ((SwimlaneModel) target.getModel()).getName();
		SwimlaneType targetType = ((SwimlaneModel) target.getModel()).getType();
		if (targetType == SwimlaneType.NEWLANE) {
			InputDialog dSwimlaneName = new InputDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Enter name", "Please provide a name for the new swimlane", null, new SwimlaneNameValidator());
			if (dSwimlaneName.open() == Dialog.OK) {
				targetName = dSwimlaneName.getValue();
			} else {
				return;
			}
		}
		if (contentEditPart instanceof ProcessTaskEditPart) {
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.removeFromSwimlane(itemToMove.getProcessTask());
			if (targetType != SwimlaneType.DRYLANE) {
				parent.assignToSwimlane(itemToMove.getProcessTask(), targetName);
			}
			executed = true;
			contentEditPart.refresh();
		}
	}

	@Override
	public void undo() {
		if (executed) {
			String originName = ((SwimlaneModel) origin.getModel()).getName();
			SwimlaneType originType = ((SwimlaneModel) origin.getModel()).getType();
			ProcessTask parent = (ProcessTask) contentEditPart.getModel();
			parent.removeFromSwimlane(itemToMove.getProcessTask());
			if (originType != SwimlaneType.DRYLANE) {
				parent.assignToSwimlane(itemToMove.getProcessTask(), originName);
			}
			contentEditPart.refresh();
		}
	}

	@Override
	public boolean canExecute() {
		if (this.contentEditPart instanceof ProcessTaskEditPart && CodeGuard.hasRole(Role.PROCESS_TASK_MODEL, ((ProcessTaskEditPart) this.contentEditPart).getProcessTask())) {
		return !executed
			&& this.target != null
			&& this.target instanceof SwimlaneEditPart
			&& this.origin != null
			&& this.origin instanceof SwimlaneEditPart
			&& this.itemToMove != null;
		}
		return false;
	}

	@Override
	public boolean canUndo() {
		return executed;
	}

	public void setTargetEditPart(EditPart target) {
		if (this.target != target) {
			this.target = target;
		}
	}

	public EditPart getTargetEditPart() {
		return this.target;
	}

	public void setOrigin(EditPart origin) {
		this.origin = origin;
	}

	public EditPart getOrigin() {
		return this.origin;
	}

	public void setItem(Object object) {
		if (object instanceof ProcessTaskModel && itemToMove != object) {
			this.itemToMove = (ProcessTaskModel) object;
			this.swimlanesAvailable.clear();
			this.swimlanesAvailable.addAll(itemToMove.getProcessTask().getParentEntity().getSwimlanes());
		}
	}

	private class SwimlaneNameValidator implements IInputValidator {

		public String isValid(String newText) {
			if (newText == null || newText.equals("")) {
				return "";
			}
			if (swimlanesAvailable.contains(newText)) {
				return "This swimlane already exists!";
			}
			return null;
		}
	}

	public boolean executedSuccessfully() {
		return executed;
	}
}
