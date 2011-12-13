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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedBackFigureProvider;

/**
 * This policy cares about drag&drop ProcessTasks to other Swimlanes.
 * 
 */
public class ProcessTaskMovePolicy extends ConstrainedLayoutEditPolicy {
	
	private EditPart contentEditPart;
	private ProcessTaskMoveCommand command = null;
	
	public ProcessTaskMovePolicy(EditPart contentEditPart) {
		this.contentEditPart = contentEditPart;
	}

	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		return null;
	}

	@Override
	protected Object getConstraintFor(Point point) {
		return null;
	}

	@Override
	protected Object getConstraintFor(Rectangle rect) {
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		return null;
	}

	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		if (command == null) {
			command = new ProcessTaskMoveCommand(contentEditPart);
			command.setItem(child.getModel());
			command.setOrigin(child.getTargetEditPart(new Request(RequestConstants.REQ_ADD)));
		}
		return command;
	}

	@Override
	protected Command getAddCommand(Request request) {
		if (command != null) {
			command.setTargetEditPart(getTargetEditPart(request));
		}
		return super.getAddCommand(request);
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		EditPart target = getTargetEditPart(request);
		if (target instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) target).getFeedbackFigure().eraseTargetFeedback();
		}
	}

	@Override
	public void showTargetFeedback(Request request) {
		EditPart target = getTargetEditPart(request);
		if (target instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) target).getFeedbackFigure().showTargetFeedback();
		}
	}
}
