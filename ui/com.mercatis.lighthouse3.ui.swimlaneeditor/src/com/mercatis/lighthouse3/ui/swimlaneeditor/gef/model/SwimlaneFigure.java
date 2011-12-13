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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedbackFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel.SwimlaneType;


public class SwimlaneFigure extends Figure implements IFeedbackFigure {
	private Color backgroundColor = new Color(null, 238, 238, 238);
	private Color feedbackColor = new Color(null, 230, 230, 230);
	private Figure lane = new Figure();
	private Label label;
	private SwimlaneType type;

	public SwimlaneFigure(SwimlaneModel swimlane) {
		this.type = swimlane.getType();
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		this.setLayoutManager(layout);
		label = new Label(swimlane.getName());
		label.setLabelAlignment(PositionConstants.ALWAYS_LEFT);
		label.setTextAlignment(PositionConstants.ALWAYS_LEFT);
		label.setIconAlignment(PositionConstants.ALWAYS_LEFT);
		label.setOpaque(true);
		super.add(label, null, -1);

		if (type == SwimlaneType.DEFAULT || type == SwimlaneType.DRYLANE) {
			Font f = new Font(null, "Arial", 13, SWT.BOLD);
			label.setFont(f);
			ToolbarLayout laneLayout = new ToolbarLayout();
			laneLayout.setVertical(false);
			laneLayout.setSpacing(25);
			lane.setLayoutManager(laneLayout);
			lane.setBackgroundColor(backgroundColor);
			lane.setOpaque(true);
			laneLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
			lane.setBorder(new LineBorder());
			super.add(lane, null, -1);
		}
		if (type == SwimlaneType.NEWLANE) {
			ToolbarLayout labelLayout = new ToolbarLayout(false);
			RectangleFigure rf = new RectangleFigure();
			rf.setSize(50, 50);
			rf.setVisible(false);
			label.setLayoutManager(labelLayout);
			label.add(rf);
			Font f = new Font(null, "Arial", 10, SWT.BOLD);
			label.setFont(f);
			label.setBorder(new LineBorder());
			label.setBackgroundColor(backgroundColor);
		}
	}

	@Override
	public void add(IFigure figure, Object constraint, int index) {
		lane.add(figure);
	}

	public void eraseSourceFeedback() {
	}

	public void eraseTargetFeedback() {
		if (type == SwimlaneType.DEFAULT || type == SwimlaneType.DRYLANE) {
			lane.setBackgroundColor(backgroundColor);
		}
		if (type == SwimlaneType.NEWLANE) {
			label.setBackgroundColor(backgroundColor);
		}
		this.repaint();
	}

	public void showSourceFeedback() {
	}

	public void showTargetFeedback() {
		if (type == SwimlaneType.DEFAULT || type == SwimlaneType.DRYLANE) {
			lane.setBackgroundColor(feedbackColor);
		}
		if (type == SwimlaneType.NEWLANE) {
			label.setBackgroundColor(feedbackColor);
		}
		this.repaint();
	}
}
