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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeEvent;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainChangeListener;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ConnectionModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.ProcessTaskModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model.SwimlaneModel;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.policies.ProcessTaskMovePolicy;

/**
 * This is the EditPart representing the entity that is being edited.
 * All displayed childs are wrapped into ProcessTaskModels and are being displayed be
 * the ProcessTaskModelEditparts.
 * 
 * @see ProcessTaskModel
 * @see ProcessTaskModelEditPart
 */
public class ProcessTaskEditPart extends AbstractGraphicalEditPart implements IPropertyChangeListener, DomainChangeListener, IInstanceViewEditPart {

	public static ConnectionRegistry connectionRegistry = new ConnectionRegistry();
	
	/**
	 * The entity that is being edited.
	 */
	private ProcessTask rootTask;
	
	/**
	 * Maps a SwimlaneModel to one of the ProcessTasks it contains.
	 */
	private Hashtable<ProcessTask, SwimlaneModel> taskAtLane = new Hashtable<ProcessTask, SwimlaneModel>();
	
	/**
	 * Tasks that are currently not placed in any Swimlane.
	 * <i>(Only fillded during init. Tasks without a Swimlane will be placed in the DRYLANE)</i>
	 */
	private LinkedList<ProcessTask> unplacedTasks = new LinkedList<ProcessTask>();
	
	/**
	 * Maps a ProcessTaskModel to it real ProcessTask to find the model for a specific task.
	 */
	private Hashtable<ProcessTask, ProcessTaskModel> taskToModel = new Hashtable<ProcessTask, ProcessTaskModel>();
	
	/**
	 * Used during init to store all successors that are not placed yet.
	 */
	private HashSet<ProcessTask> placeableSuccessors = new HashSet<ProcessTask>();
	
	/**
	 * The swimlanes this EditPart will show as its children.
	 */
	private ArrayList<SwimlaneModel> swimlanes = new ArrayList<SwimlaneModel>();
	
	/**
	 * I currently don't know if this is still (or ever) needed.
	 */
	private List<ProcessTask> registeredAdapters = new LinkedList<ProcessTask>();

	/**
	 * If the editor is switched to the InstanceView mode, this will be filled with the current instance.
	 */
	private ProcessInstanceDefinition processInstanceDefinition;
	
	public ProcessTaskEditPart(ProcessTask rootTask) {
		this.setModel(rootTask);
		this.rootTask = rootTask;
	}
	
	public void setProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition) {
		this.processInstanceDefinition = processInstanceDefinition;
	}
	
	public ProcessInstanceDefinition getProcessInstanceDefinition() {
		return this.processInstanceDefinition;
	}

	@Override
	public void activate() {
		CommonBaseActivator.getPlugin().getDomainService().addDomainChangeListener(this);
		refreshListeners();
		super.activate();
	}

	@Override
	public void deactivate() {
		CommonBaseActivator.getPlugin().getDomainService().removeDomainChangeListener(this);
		removeListeners();
		super.deactivate();
	}

	private void refreshListeners() {
		removeListeners();
		registeredAdapters.add(rootTask);
		registeredAdapters.addAll(rootTask.getDirectSubEntities());
	}

	private void removeListeners() {
		registeredAdapters.clear();
	}

	@Override
	public void refresh() {
		init();
		refreshListeners();
		super.refresh();
	}

	private void init() {
		taskAtLane.clear();
		unplacedTasks.clear();
		taskToModel.clear();
		placeableSuccessors.clear();
		swimlanes.clear();

		unplacedTasks.addAll(rootTask.getDirectSubEntities());

		LinkedList<ProcessTask> dryProcesses = new LinkedList<ProcessTask>();
		dryProcesses.addAll(rootTask.getDirectSubEntities());
		for (String swimlaneName : this.rootTask.getSwimlanes()) {
			SwimlaneModel swimlane = new SwimlaneModel(SwimlaneModel.SwimlaneType.DEFAULT, swimlaneName);
			swimlanes.add(swimlane);
			for (ProcessTask processTask : rootTask.getSwimlane(swimlaneName)) {
				taskAtLane.put(processTask, swimlane);
				taskToModel.put(processTask, new ProcessTaskModel(processTask));
				dryProcesses.remove(processTask);
			}
		}
		SwimlaneModel drylane = new SwimlaneModel(SwimlaneModel.SwimlaneType.DRYLANE, "Unassigned");
		swimlanes.add(drylane);
		for (ProcessTask dryProcess : dryProcesses) {
			taskAtLane.put(dryProcess, drylane);
			taskToModel.put(dryProcess, new ProcessTaskModel(dryProcess));
		}
		swimlanes.add(new SwimlaneModel(SwimlaneModel.SwimlaneType.NEWLANE, "New swimlane"));
		calculateSwimlanes();
		calculateConnections();
	}

	@Override
	protected List<SwimlaneModel> getModelChildren() {
		Collections.sort(swimlanes);
		return swimlanes;
	}

	@Override
	protected IFigure createFigure() {
		Figure figure = new FreeformLayer();
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setSpacing(20);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);

		figure.setLayoutManager(layout);
		return figure;
	}

	/**
	 * This is the main part of the autolayouting of the SwimlaneEditor.
	 * It sorts the ProcessTasks, respecting their connections to each other.
	 */
	private void calculateSwimlanes() {
		int roster = 0;
		HashMap<String, SwimlaneModel> swimlanesRoster = new HashMap<String, SwimlaneModel>();
		for (SwimlaneModel swimlane : swimlanes) {
			swimlanesRoster.put(swimlane.getName(), swimlane);
		}

		// Place the starters
		Set<ProcessTask> starters = rootTask.getStarters();
		for (ProcessTask starter : starters) {
			ProcessTaskModel starterModel = taskToModel.get(starter);
			calculatePlacement(starterModel);
			starterModel.setStarter(true);
			swimlanesRoster.get(taskAtLane.get(starter).getName()).addTaskToRoster(roster, starterModel);
			unplacedTasks.remove(starter);
			placeableSuccessors.addAll(rootTask.getSuccessors(starter));
		}

		// Place the rest
		ProcessTask forcedToPlace = null;
		if (starters.size() > 0) {
			roster++;
		}
		while (!unplacedTasks.isEmpty()) {
			Set<ProcessTask> placedTasks = new HashSet<ProcessTask>();
			Iterator<ProcessTask> taskIterator = placeableSuccessors.iterator();
			while (taskIterator.hasNext()) {
				ProcessTask processTask = taskIterator.next();
				if (!processTask.equals(forcedToPlace)
						&& (!unplacedTasks.contains(processTask) || !isPredecessorsPlaced(processTask))) {
					continue;
				}
				forcedToPlace = null;
				ProcessTaskModel taskModel = taskToModel.get(processTask);
				taskModel.setStop(rootTask.isFinal(processTask));
				calculatePlacement(taskModel);
				swimlanesRoster.get(taskAtLane.get(processTask).getName()).addTaskToRoster(roster, taskModel);
				placedTasks.add(processTask);
			}
			unplacedTasks.removeAll(placedTasks);
			placeableSuccessors.removeAll(placedTasks);
			for (ProcessTask placed : placedTasks) {
				placeableSuccessors.addAll(rootTask.getSuccessors(placed));
			}
			// force placement
			if (placedTasks.size() == 0) {
				for (ProcessTask unplaced : unplacedTasks) {
					if (!rootTask.isFinal(unplaced)) {
						// set flag to prevent serarching for placed successors,
						// this will cause system to go bye bye...
						// skip finals, they could always be placed
						forcedToPlace = unplacedTasks.element();
						break;
					}
				}
				placeableSuccessors.add(forcedToPlace);
				continue;
			}
			roster++;
		}
	}

	/**
	 * Check if all predecessors of a ProcessTask are placed in the layout.
	 * It should be placed if this method returns true, to give the whole swimlane thingie some logical ordering.
	 * 
	 * @param processTask
	 * @return
	 */
	private boolean isPredecessorsPlaced(ProcessTask processTask) {
		Set<ProcessTask> predecessors = rootTask.getPredecessors(processTask);
		if (!unplacedTasks.containsAll(predecessors)) {
			return true;
		} else {
			for (ProcessTask predecessor : predecessors) {
				if (searchInSuccessors(processTask, predecessor))
					return true;
			}
		}
		return false;
	}

	/**
	 * Check if the PorcessTask <i>needle</i> is somehow a successor <i>(recursive)</i> of the given ProcessTask.
	 * 
	 * @param processTask the task to be rummaged
	 * @param needle the task to find
	 * @return true if the task is found
	 */
	private boolean searchInSuccessors(ProcessTask processTask, ProcessTask needle) {
		Set<ProcessTask> successors = rootTask.getSuccessors(processTask);
		if (successors.contains(needle)) {
			return true;
		} else {
			for (ProcessTask successor : successors) {
				if (!rootTask.isFinal(successor) && searchInSuccessors(successor, needle)) {
					return true;
				}
			}
		}
		return false;
	}

	private void calculatePlacement(ProcessTaskModel task) {
		int orderNumber = 0;
		SwimlaneModel swimlane = taskAtLane.get(task.getProcessTask());
		for (ProcessTask pre : rootTask.getPredecessors(task.getProcessTask())) {
			SwimlaneModel otherLane = taskAtLane.get(pre);
			orderNumber += swimlane.compareTo(otherLane);
		}
		for (ProcessTask post : rootTask.getSuccessors(task.getProcessTask())) {
			SwimlaneModel otherLane = taskAtLane.get(post);
			orderNumber += swimlane.compareTo(otherLane);
		}
		task.setOrderNumber(orderNumber);
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessTaskMovePolicy(getRoot()));
	}

	private void calculateConnections() {
		connectionRegistry.clear();
		for (ProcessTask processTask : rootTask.getDirectSubEntities()) {
			if (!rootTask.isStarter(processTask)) {
				for (ProcessTask pre : rootTask.getPredecessors(processTask)) {
					connectionRegistry.addConnection(new ConnectionModel(pre, processTask));
				}
			}
		}
	}

	/**
	 * Just an enhanced Vector
	 * //TODO this may look better by just subclassing Vector
	 */
	public static class ConnectionRegistry {

		private Vector<ConnectionModel> connections = new Vector<ConnectionModel>();

		public List<ConnectionModel> searchBySuccessor(ProcessTask task) {
			List<ConnectionModel> result = new ArrayList<ConnectionModel>();
			if (task == null) {
				return result;
			}
			for (ConnectionModel con : connections) {
				if (con.getTo().equals(task)) {
					result.add(con);
				}
			}
			return result;
		}

		public List<ConnectionModel> searchByPredecessor(ProcessTask task) {
			List<ConnectionModel> result = new ArrayList<ConnectionModel>();
			if (task == null) {
				return result;
			}
			for (ConnectionModel con : connections) {
				if (con.getFrom().equals(task)) {
					result.add(con);
				}
			}
			return result;
		}

		public void addConnection(ConnectionModel con) {
			connections.add(con);
		}

		public void clear() {
			connections.clear();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	public void domainChange(DomainChangeEvent event) {
		refresh();
	}
	
	public ProcessTask getProcessTask() {
		return this.rootTask;
	}
	
	public void setProcessTask(ProcessTask pt) {
		rootTask = pt;
		refresh();
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.swimlaneeditor.gef.IDisplayOnlyEditPart#switchToDisplayOnlyMode()
	 */
	public void setInstanceViewMode(boolean instanceViewActive) {
		if (instanceViewActive) {
			removeEditPolicy(EditPolicy.COMPONENT_ROLE);
			removeEditPolicy(EditPolicy.LAYOUT_ROLE);
		} else {
			createEditPolicies();
		}
	}
}
