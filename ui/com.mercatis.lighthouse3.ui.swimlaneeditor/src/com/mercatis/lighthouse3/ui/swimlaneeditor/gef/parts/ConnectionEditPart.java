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

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;


public class ConnectionEditPart extends AbstractConnectionEditPart implements IInstanceViewEditPart {

	public ConnectionEditPart(EditPart part, ConnectionModel connection) {
		this.setParent(part);
		this.setModel(connection);
	}

	@Override
	protected IFigure createFigure() {
		PolylineConnection connection = new PolylineConnection();
		PolygonDecoration arrow = new PolygonDecoration();
		arrow.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		arrow.setScale(10, 4);
		connection.setTargetDecoration(arrow);
		connection.setLineWidth(2);

		ConnectionLayer connectionLayer = (ConnectionLayer) ((LayerManager) getRoot()).getLayer(LayerConstants.CONNECTION_LAYER);
		connectionLayer.add(connection, null, -1);
		return connection;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IDisplayOnlyEditPart#switchToDisplayOnlyMode()
	 */
	public void setInstanceViewMode(boolean readonly) {
		if (readonly) {
			removeEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE);
		} else {
			createEditPolicies();
		}
	}
}
