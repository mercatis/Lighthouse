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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneRosterFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneRosterModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies.ProcessTaskMovePolicy;


public class SwimlaneRosterEditPart extends AbstractGraphicalEditPart implements IInstanceViewEditPart {

	public SwimlaneRosterEditPart(EditPart part, SwimlaneRosterModel roster) {
		this.setModel(roster);
		this.setParent(part);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getModelChildren() {
		return ((SwimlaneRosterModel) getModel()).getTasks();
	}

	@Override
	protected IFigure createFigure() {
		return new SwimlaneRosterFigure(((SwimlaneRosterModel) getModel()));
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessTaskMovePolicy(getRoot().getContents()));
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request.getType() == RequestConstants.REQ_ADD) {
			return getParent().getTargetEditPart(request);
		}
		return super.getTargetEditPart(request);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IDisplayOnlyEditPart#switchToDisplayOnlyMode()
	 */
	public void setInstanceViewMode(boolean readonly) {
		if (readonly) {
			removeEditPolicy(EditPolicy.LAYOUT_ROLE);
		} else {
			createEditPolicies();
		}
	}
}
