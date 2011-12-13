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
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedBackFigureProvider;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedbackFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies.ProcessTaskMovePolicy;


public class SwimlaneEditPart extends AbstractGraphicalEditPart implements IFeedBackFigureProvider, IInstanceViewEditPart {

	private SwimlaneModel swimlane;
	private SwimlaneFigure swimlaneFigure;

	public SwimlaneEditPart(EditPart part, SwimlaneModel swimlane) {
		this.setModel(swimlane);
		this.setParent(part);
		this.swimlane = swimlane;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getModelChildren() {
		return swimlane.getRoster();
	}

	@Override
	protected IFigure createFigure() {
		swimlaneFigure = new SwimlaneFigure(swimlane);
		return swimlaneFigure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessTaskMovePolicy(getRoot().getContents()));
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	public IFeedbackFigure getFeedbackFigure() {
		return swimlaneFigure;
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
