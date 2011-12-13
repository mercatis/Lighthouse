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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ConnectionEditPart;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies.ConnectionDeleteCommand;


public class DeleteConnectionTool extends AbstractTool {

	ConnectionEditPart cp = null;

	public DeleteConnectionTool() {
		super();
		setDefaultCursor(new Cursor(null, ImageDescriptor.createFromURL(getClass().getResource("/icons/delete.gif"))
				.getImageData(), 8, 7));
	}

	@Override
	protected String getCommandName() {
		return "deleteConnection";
	}

	@Override
	public void mouseMove(MouseEvent me, EditPartViewer viewer) {
		EditPart part = viewer.findObjectAt(new Point(me.x, me.y));
		if (part instanceof ConnectionEditPart && part != cp) {
			if (cp != null) {
				((PolylineConnection) cp.getFigure()).setForegroundColor(ColorConstants.black);
			}
			cp = (ConnectionEditPart) part;
			((PolylineConnection) cp.getFigure()).setForegroundColor(ColorConstants.red);
		} else if (cp != null && part != cp) {
			((PolylineConnection) cp.getFigure()).setForegroundColor(ColorConstants.black);
			cp = null;
		}
	}

	@Override
	public void mouseUp(MouseEvent me, EditPartViewer viewer) {
		mouseMove(me, viewer);
		if (cp != null) {
			ConnectionModel cm = (ConnectionModel) cp.getModel();
			getDomain().getCommandStack().execute(new ConnectionDeleteCommand(viewer.getRootEditPart().getContents(), cm));
		}
	}
}
