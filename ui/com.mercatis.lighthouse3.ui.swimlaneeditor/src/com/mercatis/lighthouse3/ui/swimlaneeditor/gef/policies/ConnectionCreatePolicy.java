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

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedBackFigureProvider;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;


public class ConnectionCreatePolicy extends GraphicalNodeEditPolicy {

	private ConnectionCreateCommand connectionCommand;
	private EditPart contentEditPart;
	
	public ConnectionCreatePolicy(EditPart contentEditPart) {
		this.contentEditPart = contentEditPart;
		connectionCommand = new ConnectionCreateCommand(contentEditPart);
	}
	
	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		if (!connectionCommand.isBeginSet()) {
			if (request != null && request.getSourceEditPart() != null
					&& request.getSourceEditPart().getModel() instanceof ProcessTaskModel) {
				connectionCommand.setBeginTask((ProcessTaskModel) request.getSourceEditPart().getModel());
			}
		}
		if (!connectionCommand.isEndSet()) {
			if (request != null && request.getTargetEditPart() != null
					&& request.getTargetEditPart().getModel() instanceof ProcessTaskModel) {
				connectionCommand.setEndTask((ProcessTaskModel) request.getTargetEditPart().getModel());
			}
		}
		return connectionCommand;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		return connectionCommand;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		return new ConnectionEditCommand(contentEditPart, request);
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		return new ConnectionEditCommand(contentEditPart, request);
	}

	@Override
	protected Connection createDummyConnection(Request req) {
		PolylineConnection connection = new PolylineConnection();
		PolygonDecoration arrow = new PolygonDecoration();
		arrow.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		arrow.setScale(5, 2.5);
		connection.setTargetDecoration(arrow);
		return connection;
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		if (getTargetEditPart(request) instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) getTargetEditPart(request)).getFeedbackFigure().eraseSourceFeedback();
		}
		super.eraseSourceFeedback(request);
	}

	@Override
	public void eraseTargetFeedback(Request request) {
		if (getTargetEditPart(request) instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) getTargetEditPart(request)).getFeedbackFigure().eraseTargetFeedback();
		}
		super.eraseTargetFeedback(request);
	}

	@Override
	public void showSourceFeedback(Request request) {
		if (getTargetEditPart(request) instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) getTargetEditPart(request)).getFeedbackFigure().showSourceFeedback();
		}
		super.showSourceFeedback(request);
	}

	@Override
	public void showTargetFeedback(Request request) {
		if (getTargetEditPart(request) instanceof IFeedBackFigureProvider) {
			((IFeedBackFigureProvider) getTargetEditPart(request)).getFeedbackFigure().showTargetFeedback();
		}
		super.showTargetFeedback(request);
	}
}
