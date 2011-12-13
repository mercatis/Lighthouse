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
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.processinstance.base.ProcessInstanceBase;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedBackFigureProvider;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IFeedbackFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModelFigure;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModelFigure.ProcessTaskState;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies.ConnectionCreatePolicy;

/**
 * This EditPart controls the wrapped ProcessTasks - children of the current edited ProcessTask
 * 
 */
public class ProcessTaskModelEditPart extends AbstractGraphicalEditPart implements NodeEditPart,
		IFeedBackFigureProvider, EventHandler, IInstanceViewEditPart {

	private LighthouseDomain lighthouseDomain;
	
	/**
	 * Keeps the currently used status service. This depends on the status of the InstanceView mode.
	 * The InstanceView needs specific histograms, that are provided by the ProcessInstanceStatusServiceImpl
	 * 
	 * @see com.mercatis.lighthouse3.ui.processinstance.base.services.impl.ProcessInstanceStatusServiceImpl
	 */
	private StatusService statusService;
	private ProcessTaskModel task;
	private ProcessTaskModelFigure figure;
	private IEditorPart creatingEditor;

	public ProcessTaskModelEditPart(EditPart part, ProcessTaskModel task) {
		this.setModel(task);
		this.setParent(part);
		this.task = task;
		registerDefaultAggregationChanged();
	}
	
	private LighthouseDomain getLighthouseDomain() {
		if (lighthouseDomain == null && task.getProcessTask() != null) {
			lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(task.getProcessTask());
		}
		return lighthouseDomain;
	}
	
	private void registerDefaultAggregationChanged() {
		Services.unregisterEventHandler(this);
		statusService = CommonBaseActivator.getPlugin().getStatusService();
		if (task.getProcessTask() != null) {
			String domainServerKey = "" + task.getProcessTask().getLighthouseDomain().hashCode();
			String filter = "(type=statusAggregationChanged)";
			Services.registerEventHandler(this, "com/mercatis/lighthouse3/event/" + domainServerKey + "/*", filter);
		}
	}
	
	private void registerInstanceViewAggregationChanged() {
		Services.unregisterEventHandler(this);
		statusService = ProcessInstanceBase.getInstanceAggregationService(creatingEditor);
		if (task.getProcessTask() != null) {
			String filter = "(type=instanceAggregationChanged)";
			Services.registerEventHandler(this, "com/mercatis/lighthouse3/event/instanceViewChange", filter);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
		Services.unregisterEventHandler(this);
	}

	@Override
	public boolean isSelectable() {
		return task.getProcessTask() != null;
	}

	@Override
	protected IFigure createFigure() {
		figure = new ProcessTaskModelFigure(task);
		if (task.getProcessTask() != null) {
			LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(task.getProcessTask());
			StatusHistogram sh = statusService.getStatusHistogramForObject(lighthouseDomain, task.getProcessTask());
			if (sh != null) {
				if (sh.getError() > 0) {
					figure.setProcessTaskState(ProcessTaskState.ERROR);
					figure.setMouseOverText(sh.getError() + " status errorneous");
				}
				else if (sh.getStale() > 0) {
					figure.setProcessTaskState(ProcessTaskState.STALE);
					figure.setMouseOverText(sh.getStale() + " status stale");
				}
				else if (sh.getOk() > 0) {
					figure.setProcessTaskState(ProcessTaskState.OK);
					figure.setMouseOverText(sh.getOk() + " status ok");
				}
				else {
					figure.setProcessTaskState(ProcessTaskState.NONE);
					figure.setMouseOverText(null);
				}
			}
			Label toolTip = new Label(LabelConverter.getLabel(task.getProcessTask()));
			figure.setToolTip(toolTip);
		}
		return figure;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getModelSourceConnections() {
		return ProcessTaskEditPart.connectionRegistry.searchByPredecessor(task.getProcessTask());
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getModelTargetConnections() {
		return ProcessTaskEditPart.connectionRegistry.searchBySuccessor(task.getProcessTask());
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnectionCreatePolicy(getRoot().getContents()));
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart arg0) {
		return figure.getSuccessorAnchor();
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request arg0) {
		return figure.getPredecessorAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart arg0) {
		return figure.getPredecessorAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request arg0) {
		return figure.getPredecessorAnchor();
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		if (request.getType() == RequestConstants.REQ_ADD) {
			return getParent().getTargetEditPart(request);
		}
		return super.getTargetEditPart(request);
	}

	public IFeedbackFigure getFeedbackFigure() {
		return figure;
	}
	
	public ProcessTask getProcessTask() {
		return task.getProcessTask();
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(Event event) {
		if (task.getProcessTask() == null)
			return;
		String eventType = (String) event.getProperty("type");
		if (eventType.equals("statusAggregationChanged")) {
			String statusPath = (String) event.getProperty("statusPath");
			String serverDomainKey = (String) event.getProperty("serverDomainKey");
			if (statusPath.contains(task.getProcessTask().getCode())
					&& serverDomainKey.equals(task.getProcessTask().getLighthouseDomain())) {
				refreshFigure();
			}
		} else if (eventType.equals("instanceAggregationChanged")) {
			refreshFigure();
		}
	}
	
	private void refreshFigure() {
		if (task.getProcessTask() != null) {
			StatusHistogram sh = statusService.getStatusHistogramForObject(getLighthouseDomain(), task.getProcessTask());
			final String hint;
			final ProcessTaskState state;
			if (sh.getError() > 0) {
				state = ProcessTaskState.ERROR;
				hint = sh.getError() + " status errorneous";
			} 
			else if (sh.getStale() > 0) {
				state = ProcessTaskState.STALE;
				hint = sh.getStale() + " status stale";
			}
			else if (sh.getOk() > 0) {
				state = ProcessTaskState.OK;
				hint = sh.getOk() + " status ok";
			}
			else {
				state = ProcessTaskState.NONE;
				hint = null;
			}

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					figure.setProcessTaskState(state);
					figure.setMouseOverText(hint);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IDisplayOnlyEditPart#switchToDisplayOnlyMode()
	 */
	public void setInstanceViewMode(boolean instanceViewActive) {
		if (instanceViewActive) {
			removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
			registerInstanceViewAggregationChanged();
		} else {
			createEditPolicies();
			registerDefaultAggregationChanged();
		}
		refreshFigure();
	}

	public void setCreatingEditor(IEditorPart creatingEditor) {
		this.creatingEditor = creatingEditor;
	}
}
