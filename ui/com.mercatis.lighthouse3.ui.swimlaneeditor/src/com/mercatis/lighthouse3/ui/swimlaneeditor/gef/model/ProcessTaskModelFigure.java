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

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedbackFigure;

public class ProcessTaskModelFigure extends Figure implements IFeedbackFigure {

	public static enum ProcessTaskState {
		NONE, OK, ERROR, STALE
	}
	
	public static enum ProcessTaskType {
		TASK_START, TASK_STOP, TASK_STEP, GROUP_START, GROUP_STOP, GROUP_STEP
	}
	
	private static ImageDescriptor TASK_STEP_NONE = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/task_step_none.png"));
	private static ImageDescriptor TASK_STEP_ERROR = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/task_step_error.png"));
	private static ImageDescriptor TASK_STEP_STALE = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/task_step_stale.png"));
	private static ImageDescriptor TASK_STEP_OK = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/task_step_ok.png"));

	private static ImageDescriptor GROUP_STEP_NONE = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/group_step_none.png"));
	private static ImageDescriptor GROUP_STEP_ERROR = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/group_step_error.png"));
	private static ImageDescriptor GROUP_STEP_STALE = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/group_step_stale.png"));
	private static ImageDescriptor GROUP_STEP_OK = ImageDescriptor.createFromURL(ProcessTaskModelFigure.class.getResource("/icons/group_step_ok.png"));

	
	private Label label;
	
	private Image image;
	
	private ConnectionAnchor successorAnchor;
	private ConnectionAnchor predecessorAnchor;
	
	private Label mouseOverHint;
	
	public ProcessTaskModelFigure(ProcessTaskModel taskModel) {
		if (taskModel.getProcessTask() == null)
			return;
		
		this.figure = new ImageFigure();
		this.add(this.figure);
		this.figure.setAlignment(PositionConstants.CENTER);
		
		if (taskModel.getProcessTask().isComplexEntity()) {
			if (taskModel.isStarter()) {
				type = ProcessTaskType.GROUP_START;
			} else if (taskModel.isStopper()) {
				type = ProcessTaskType.GROUP_STOP;
			} else {
				type = ProcessTaskType.GROUP_STEP;			
			}
		} else {
			if (taskModel.isStarter()) {
				type = ProcessTaskType.TASK_START;
			} else if (taskModel.isStopper()) {
				type = ProcessTaskType.TASK_STOP;
			} else {
				type = ProcessTaskType.TASK_STEP;			
			}
		}
		setImage(type, ProcessTaskState.NONE);
		setText(LabelConverter.getLabel(taskModel.getProcessTask()));
	}

	public ConnectionAnchor getSuccessorAnchor() {
		return successorAnchor;
	}

	public ConnectionAnchor getPredecessorAnchor() {
		return predecessorAnchor;
	}

	public void eraseSourceFeedback() {
	}

	public void eraseTargetFeedback() {
	}

	public void showSourceFeedback() {
	}

	public void showTargetFeedback() {
	}
	
	private ProcessTaskState state;
	
	private ProcessTaskType type;
	
	private ImageFigure figure;
	
	public void setProcessTaskType(ProcessTaskType type) {
		if (this.type == type)
			return;
		
		setImage(type, state);
	}
	
	public void setProcessTaskState(ProcessTaskState state) {
		if (this.state == state)
			return;

		setImage(type, state);
	}
	
	private void setImage(ProcessTaskType type, ProcessTaskState state) {
		Image oldImage = this.image;
		this.image = createImage(type, state);
		this.type = type;
		this.state = state;
		
		figure.setImage(image);
		figure.setSize(image.getImageData().width, image.getImageData().height);
		this.setSize(image.getBounds().width, image.getBounds().height);
		createConnectionAnchors();
		
		if (oldImage != null)
			oldImage.dispose();
	}
	
	private RectangleFigure anchorFigure;
	
	/**
	 * 
	 */
	private void createConnectionAnchors() {
		if (anchorFigure == null) {
			anchorFigure = new RectangleFigure();
			Point p = new Point(18, 13);
			anchorFigure.setLocation(p);
			anchorFigure.setVisible(false);
			add(anchorFigure);
		}
		
		Dimension d = new Dimension(image.getImageData().width - 36, image.getImageData().height - 26);		
		anchorFigure.setSize(d);

		successorAnchor = new ChopboxAnchor(anchorFigure);
		predecessorAnchor = new ChopboxAnchor(anchorFigure);
	}

	private Image createImage(ProcessTaskType type, ProcessTaskState state) {
		switch (type) {
		case TASK_START:
		case TASK_STOP:
		case TASK_STEP:
			switch (state) {
			case NONE:
				return TASK_STEP_NONE.createImage();
			case OK:
				return TASK_STEP_OK.createImage();
			case ERROR:
				return TASK_STEP_ERROR.createImage();
			case STALE:
				return TASK_STEP_STALE.createImage();
			}
		case GROUP_START:
		case GROUP_STOP:
		case GROUP_STEP:
			switch (state) {
			case NONE:
				return GROUP_STEP_NONE.createImage();
			case OK:
				return GROUP_STEP_OK.createImage();
			case ERROR:
				return GROUP_STEP_ERROR.createImage();
			case STALE:
				return GROUP_STEP_STALE.createImage();
			}
		}
		return ImageDescriptor.getMissingImageDescriptor().createImage();
	}
	
	public void dispose() {
		if (image != null)
			image.dispose();
	}
	
	public void setMouseOverText(String text) {
		if ((text == null || text.equals("")) && this.mouseOverHint != null) {
			remove(this.mouseOverHint);
			this.mouseOverHint = null;
			return;
		}
		
		if(text != null && !text.equals("")) {
			if (this.mouseOverHint == null) {
				this.mouseOverHint = new Label();
				this.mouseOverHint.setBorder(new LineBorder());
				this.mouseOverHint.setFont(new Font(null, "Arial", 7, SWT.ITALIC));
				this.mouseOverHint.setForegroundColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				this.mouseOverHint.setBackgroundColor(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				this.mouseOverHint.setOpaque(true);
				add(this.mouseOverHint);
			}
			this.mouseOverHint.setText(text);
			this.mouseOverHint.setSize(this.mouseOverHint.getPreferredSize());
			int x = getLocation().x + 2;
			int y = getLocation().y + 2;
			this.mouseOverHint.setLocation(new Point(x, y));
		}
	}
}
