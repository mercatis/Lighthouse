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
package com.mercatis.lighthouse3.ui.swimlaneeditor;

import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import com.mercatis.lighthouse3.base.actions.DropDownAction.SortingMode;
import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstance;
import com.mercatis.lighthouse3.domainmodel.processinstance.ProcessInstanceDefinition;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.processinstance.base.ProcessInstanceBase;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;
import com.mercatis.lighthouse3.ui.swimlaneeditor.actions.InstanceNavigationAction;
import com.mercatis.lighthouse3.ui.swimlaneeditor.actions.ReloadInstancesAction;
import com.mercatis.lighthouse3.ui.swimlaneeditor.actions.SwitchToInstanceViewAction;
import com.mercatis.lighthouse3.ui.swimlaneeditor.actions.ViewInstanceEventsAction;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.ConnectionToolEntry;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.DeleteToolEntry;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.ExtendedCommandStack;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.ExtendedSelectionTool;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.IInstanceViewEditPart;
import com.mercatis.lighthouse3.ui.swimlaneeditor.gef.parts.ProcessTaskEditPartFactory;
import com.mercatis.lighthouse3.ui.swimlaneeditor.providers.UnequalSelection;

/**
 * This is the "SwimlaneEditor" - a GEF editor for editing processes on a ProcessTask.
 * 
 */
public class ProcessTaskSwimlaneEditor extends GraphicalEditorWithFlyoutPalette implements IAdaptable, IInstanceView {

	/**
	 * Editor ID, used to indentify the editor in the plugin.xml
	 */
	public static final String EDITOR_ID = "com.mercatis.lighthouse3.ui.editors.swimlaneeditor";
	
	/**
	 * The editors default context menu
	 */
	public static String contextMenuId = "com.mercatis.lighthouse3.ui.environment.editors.pages.ProcessTaskSwimlaneEditor.contextMenu";
	
	/**
	 * The editors context menu during instance view
	 */
	public static String contextMenuInstanceViewId = "com.mercatis.lighthouse3.ui.environment.editors.pages.ProcessTaskSwimlaneEditor.contextMenuInstanceView";
	
	/**
	 * Action to de-/activate the instance view mode
	 */
	private static final String TOGGLE_INSTANCE_VIEW_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.toggleInstanceView";
	
	/**
	 * Action to navigate to the last instance
	 */
	private static final String BACK_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.instanceBackward";
	
	/**
	 * Action to navigate to the next instance
	 */
	private static final String FORWARD_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.instanceForward";
	
	/**
	 * Action to refresh (reload) the instances from the eventserver
	 */
	private static final String RELOAD_INSTANCES_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.reloadInstances";
	
	/**
	 * Zoom in the current graph
	 */
	private static final String ZOOM_IN_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.zoomIn";
	
	/**
	 * Zoom out the current graph
	 */
	private static final String ZOOM_OUT_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.zoomOut";
	
	/**
	 * Set zoomlevel of the current graph
	 */
	private static final String ZOOM_BOX_ACTION_ID = "com.mercatis.lighthouse3.ui.actions.zoomBox";

	private InstanceNavigationAction backAction;
	private InstanceNavigationAction forwardAction;
	private SwitchToInstanceViewAction instanceViewAction;
	private ReloadInstancesAction reloadInstancesAction;
	private ProcessInstanceDefinition processInstanceDefinition = null;
	private LinkedList<ProcessInstance> instances;
	private ProcessInstance activeInstance;
	
	private ScalableRootEditPart rootEditPart;
	private IEditorPart editor;	
	private DefaultEditDomain editDomain;
	private PaletteRoot paletteRoot;

	private PaletteDrawer connectionDrawer;

	/**
	 * Palette entry for a tool that creates connections
	 */
	private ConnectionToolEntry connectionToolEntry;

	/**
	 * Palette entry for a tool that deletes connections
	 */
	private DeleteToolEntry deleteToolEntry;
	
	/**
	 * A figure, onlay dislpay durning the instance view. It show the current instances label
	 */
	private RectangleFigure activeInstanceFigure;
	
	/**
	 * Displays the currents instances startDate
	 */
	private Label activeInstanceLabel;
	
	/**
	 * Show either the ProcessTaskEditor default context menu or the one from the ProcessInstance
	 */
	private MenuManager contextMenu;

	@SuppressWarnings("unchecked")
	public ProcessTaskSwimlaneEditor(IEditorPart editor) {
		this.editor = editor;
		ProcessTask rootTask = ((GenericEditorInput<ProcessTask>) editor.getEditorInput()).getEntity();
		setEditDomain(new DefaultEditDomain(this));
		getEditDomain().setCommandStack(new ExtendedCommandStack());
		
		processInstanceDefinition = ProcessInstanceBase.getProcessInstanceDefinitionService().findByProcessTask(rootTask);
		createInstanceViewActions();
	}
	
	@Override
	public void setFocus() {
		if (editor instanceof AbstractExtendableFormEditor) {
			((AbstractExtendableFormEditor)editor).setSelectionProvider(getGraphicalViewer());
		}
		updateInstanceViewActions();
		updateZoomActions();
		super.setFocus();
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		setProcessTask(getContent());
		super.initializeGraphicalViewer();
	}

	public ProcessTask getContent() {
		IEditorInput input = getEditorInput();
		if (input instanceof GenericEditorInput<?>) {
			return (ProcessTask) ((GenericEditorInput<?>)input).getEntity();
		} else {
			return null;
		}
	}
	
	private void setProcessTask(ProcessTask pt) {
		if (CodeGuard.hasRole(Role.PROCESS_TASK_MODEL, pt)) {
			getEditDomain().getPaletteViewer().getControl().setEnabled(true);
		} else {
			getEditDomain().getPaletteViewer().getControl().setEnabled(false);
		}
		if (processInstanceDefinition != null && getContent().getParentEntity() == null) {
			getGraphicalViewer().setContents(processInstanceDefinition);
		} else {
			getGraphicalViewer().setContents(pt);
		}
		updateContextMenu();
		createZoomActions();
	}
	
	private void updateContextMenu() {
		if (contextMenu != null)
			contextMenu.dispose();
		if (instanceViewActive) {
			contextMenu = new MenuManager("Contextmenu", contextMenuInstanceViewId);
			contextMenu.add(new ViewInstanceEventsAction(this));
			getSite().registerContextMenu(contextMenuInstanceViewId, contextMenu, getGraphicalViewer());
		} else {
			contextMenu = new MenuManager("ContextMenu", contextMenuId);
			getSite().registerContextMenu(contextMenuId, contextMenu, getGraphicalViewer());
		}
		getGraphicalViewer().setContextMenu(contextMenu);
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		rootEditPart = (ScalableRootEditPart) getGraphicalViewer().getRootEditPart();

		GraphicalViewer graphicalViewer = getGraphicalViewer();
		graphicalViewer.setEditPartFactory(new ProcessTaskEditPartFactory().setCreatingEditor(this));
		graphicalViewer.getControl().setBackground(new Color(null, 255, 255, 255));

		LayerManager manager = (LayerManager) rootEditPart;
		ConnectionLayer cLayer = (ConnectionLayer) manager.getLayer(LayerConstants.CONNECTION_LAYER);
		cLayer.setAntialias(1);
		
		Layer feedbackLayer = (Layer) manager.getLayer(LayerConstants.FEEDBACK_LAYER);
		activeInstanceFigure = new RectangleFigure();
		activeInstanceFigure.setSize(270, 18);
		activeInstanceFigure.setBackgroundColor(new Color(null, 238, 238, 100));
		activeInstanceFigure.setVisible(false);
		activeInstanceFigure.setLayoutManager(new ToolbarLayout());
		feedbackLayer.add(activeInstanceFigure);
		
		activeInstanceLabel = new Label();
		activeInstanceLabel.setLabelAlignment(PositionConstants.CENTER | PositionConstants.MIDDLE);
		activeInstanceLabel.setTextAlignment(PositionConstants.CENTER | PositionConstants.MIDDLE);
		activeInstanceLabel.setIconAlignment(PositionConstants.CENTER | PositionConstants.MIDDLE);
		activeInstanceLabel.setFont(new Font(null, "Arial", 9, SWT.BOLD));
		activeInstanceLabel.setOpaque(false);
		activeInstanceFigure.add(activeInstanceLabel, null, -1);
	}
	
	private void updateInstanceInfoBox() {
		if (getActiveProcessInstance() == null || !instanceViewActive) {
			activeInstanceFigure.setVisible(false);
		} else {
			activeInstanceLabel.setText("Current instance: " + LabelConverter.getLabel(getActiveProcessInstance()));
			activeInstanceFigure.setVisible(true);
			if (!getActiveProcessInstance().isClosed()) {
				activeInstanceFigure.setBackgroundColor(new Color(null, 230, 230, 230));
			} else if (getActiveProcessInstance().isErroneous()) {
				activeInstanceFigure.setBackgroundColor(new Color(null, 230, 100, 100));
			} else {
				activeInstanceFigure.setBackgroundColor(new Color(null, 100, 230, 100));
			}
		}
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null) {
			paletteRoot = new PaletteRoot();
			paletteRoot.setLabel("Tools");
			PaletteGroup toolsGroup = new PaletteGroup("Tools");

			SelectionToolEntry selectionToolEntry = new SelectionToolEntry("Select", "Select");
			selectionToolEntry.setToolClass(ExtendedSelectionTool.class);
			selectionToolEntry.setSmallIcon(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/cursor.png")));
			toolsGroup.add(selectionToolEntry);
			
			connectionDrawer = new PaletteDrawer("Connection");
			connectionDrawer.setSmallIcon(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/connect.png")));
			toolsGroup.add(connectionDrawer);

			connectionToolEntry = new ConnectionToolEntry("Create", "Create Connection", null, ImageDescriptor.createFromURL(this.getClass().getResource("/icons/connect.png")), null);
			connectionDrawer.add(connectionToolEntry);

			deleteToolEntry = new DeleteToolEntry("Delete", "Delete Connection", ImageDescriptor.createFromURL(this.getClass().getResource("/icons/delete.gif")));
			connectionDrawer.add(deleteToolEntry);

			paletteRoot.setDefaultEntry(selectionToolEntry);
			paletteRoot.add(toolsGroup);
		}
		return paletteRoot;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// we do not test if this is the active editor, we just update the
		// actions
		updateActions(getSelectionActions());
	}

	@Override
	protected DefaultEditDomain getEditDomain() {
		if (editDomain == null)
			editDomain = new DefaultEditDomain(this);
		return editDomain;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		editDomain.getCommandStack().markSaveLocation();
	}

	@Override
	public boolean isDirty() {
		return editDomain.getCommandStack().isDirty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void commandStackChanged(EventObject event) {
		ProcessTask task = ((GenericEditorInput<ProcessTask>) getEditorInput()).getEntity();
		firePropertyChange(PROP_DIRTY);
		CommonBaseActivator.getPlugin().getDomainService().notifyDomainChange(task);
		super.commandStackChanged(event);
	}

	public void updateModel() {
		editDomain.getCommandStack().markSaveLocation();
	}

	public void undoAll() {
		while (editDomain.getCommandStack().canUndo() && editDomain.getCommandStack().isDirty()) {
			editDomain.getCommandStack().undo();
		}
	}

	@Override
	public void dispose() {
		if (isDirty())
			undoAll();
		ProcessInstanceBase.unregisterinstanceAggregationService(this);
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == EditPartViewer.class) {
			return getGraphicalViewer();
		} else if (adapter == EditDomain.class) {
			return getEditDomain();
		} else if (adapter == IWorkbenchPage.class) {
			return getSite().getPage();
		} else if (adapter == ISelectionProvider.class) {
			return getSite().getSelectionProvider();
		} else if (adapter == ProcessTaskSwimlaneEditor.class) {
			return this;
		} else if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		} else if (adapter == ZoomManager.class) {
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		}
		return super.getAdapter(adapter);
	}
	
	private boolean instanceViewActive = false;
	public void setInstanceViewEnabled(boolean enabled) {
		instanceViewActive = enabled;
		getPaletteRoot().remove(connectionDrawer);
		getPaletteRoot().setVisible(!enabled);
		((ExtendedCommandStack)getEditDomain().getCommandStack()).setEnabled(!enabled);
		getEditDomain().getPaletteViewer().getControl().setEnabled(!enabled);
		updateInstanceViewActions();
		if (enabled) {
			ProcessInstanceBase.registerInstanceAggregationService(this, processInstanceDefinition);
			loadProcessInstances(processInstanceDefinition, false);
		}
		setInstanceViewModeOnEditParts(rootEditPart, enabled);
		updateInstanceInfoBox();
		updateContextMenu();
	}
	
	@SuppressWarnings("unchecked")
	private void setInstanceViewModeOnEditParts(EditPart parent, boolean readonly) {
		if (parent==null)
			return;
		for (EditPart child : (List<EditPart>)parent.getChildren()) {
			if (child instanceof IInstanceViewEditPart) {
				((IInstanceViewEditPart)child).setInstanceViewMode(readonly);
			}
			setInstanceViewModeOnEditParts(child, readonly);
		}
	}
	
	private void createInstanceViewActions() {
		instanceViewAction = new SwitchToInstanceViewAction(this);
		instanceViewAction.setId(TOGGLE_INSTANCE_VIEW_ACTION_ID);
		instanceViewAction.setEnabled(processInstanceDefinition != null);
		
		backAction = new InstanceNavigationAction(this);
		backAction.setId(BACK_ACTION_ID);
		backAction.setText("Previous Instance");
		backAction.setImageDescriptor(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/back.gif")));
		backAction.setSortingMode(SortingMode.DESCENDING);
		backAction.setEnabled(false);
		
		forwardAction = new InstanceNavigationAction(this);
		forwardAction.setId(FORWARD_ACTION_ID);
		forwardAction.setText("Next Instance");
		forwardAction.setImageDescriptor(ImageDescriptor.createFromURL(this.getClass().getResource("/icons/forward.gif")));
		forwardAction.setSortingMode(SortingMode.ASCENDING);
		forwardAction.setEnabled(false);
		
		reloadInstancesAction = new ReloadInstancesAction(this);
		reloadInstancesAction.setId(RELOAD_INSTANCES_ACTION_ID);
	}
	
	private void updateInstanceViewActions() {
		IToolBarManager manager = this.getEditorSite().getActionBars().getToolBarManager();
		manager.remove(TOGGLE_INSTANCE_VIEW_ACTION_ID);
		manager.add(instanceViewAction);
		instanceViewAction.setEnabled(processInstanceDefinition != null);
		if (instanceViewActive) {
			manager.remove(BACK_ACTION_ID);
			manager.remove(FORWARD_ACTION_ID);
			manager.remove(RELOAD_INSTANCES_ACTION_ID);
			if (manager.find(BACK_ACTION_ID) == null) {
				manager.add(backAction);
			}
			if (manager.find(FORWARD_ACTION_ID) == null) {
				manager.add(forwardAction);
			}
			if (manager.find(RELOAD_INSTANCES_ACTION_ID) == null) {
				manager.add(reloadInstancesAction);
			}
		} else {
			manager.remove(BACK_ACTION_ID);
			manager.remove(FORWARD_ACTION_ID);
			manager.remove(RELOAD_INSTANCES_ACTION_ID);
		}
		manager.update(false);
	}
	
	public void setActiveProcessInstance(ProcessInstance processInstance) {
		//used to refresh StatusHistograms, so the UI displays thingies in the right color
		ProcessInstanceBase.getInstanceAggregationService(this).setProcessInstance(processInstance);
		
		activeInstance = processInstance;
		backAction.clearMenu();
		forwardAction.clearMenu();
		boolean backEnabled = false;
		boolean forwardEnabled = false;
		boolean isBefore = true;
		List<ProcessInstance> loadedInstancesBefore = new LinkedList<ProcessInstance>();
		for (ProcessInstance instance : instances) {
			if (processInstanceComparator.compare(instance, activeInstance) == 0) {
				isBefore = false;
				if (backEnabled == false) {
					//there are no more instances before the current one
					//lets load some more and put them into the menu
					loadedInstancesBefore.addAll(ProcessInstanceBase.getProcessInstanceService()
					.findInstancesBefore(instance, 20));
					for (ProcessInstance before : loadedInstancesBefore) {
						backAction.addMenuItem(before);
						backEnabled = true;
					}
				}
				continue;
			}
			if (isBefore) {
				backAction.addMenuItem(instance);
				backEnabled = true;
			} else {
				forwardAction.addMenuItem(instance);
				forwardEnabled = true;
			}
		}
		if (!loadedInstancesBefore.isEmpty()) {
			for (ProcessInstance loadedInstance : loadedInstancesBefore) {
				instances.add(loadedInstance);
			}
			Collections.sort(instances, processInstanceComparator);
		}
		backAction.setEnabled(backEnabled);
		forwardAction.setEnabled(forwardEnabled);
		updateInstanceInfoBox();
		fireDirtySelectionChanged();
	}
	
	/**
	 * Fires a selection changed event, to update the properties view.
	 */
	private void fireDirtySelectionChanged() {
		setFocus();
		ISelection selection = new UnequalSelection(ProcessTaskEditPartFactory.editPartRegistry.get(processInstanceDefinition));
		getEditorSite().getSelectionProvider().setSelection(selection);
	}
	
	/**
	 * Load and cache the newest 20 processInstances from the eventserver.
	 * No reload happens, if there are already cached instances, exept froceReload is set to <code>true</code>
	 * 
	 * @param definition
	 * @param forceReload if false, no reload when there are instances present
	 */
	private void loadProcessInstances(ProcessInstanceDefinition definition, boolean forceReload) {
		if (instances == null || forceReload) {
			instances = new LinkedList<ProcessInstance> (ProcessInstanceBase.getProcessInstanceService().findByProcessInstanceDefinition(definition, 20, 0));
			Collections.sort(instances, processInstanceComparator);
			if (activeInstance == null) {
				setActiveProcessInstance(instances.getLast());
			} else {
				setActiveProcessInstance(activeInstance);
			}
		}
	}
	
	public ProcessInstance getActiveProcessInstance() {
		return activeInstance;
	}
	
	/**
	 * Compares processInstances by startDate
	 */
	private Comparator<ProcessInstance> processInstanceComparator = new Comparator<ProcessInstance>() {
		public int compare(ProcessInstance o1, ProcessInstance o2) {
			if (o1.getStartDate().equals(o2.getStartDate())) {
				return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
			}
			return o1.getStartDate().compareTo(o2.getStartDate());
		}
	};

	public void reloadInstances() {
		loadProcessInstances(processInstanceDefinition, true);
	}
	
	private ZoomInAction zoomInAction;
	private ZoomOutAction zoomOutAction;
	private ZoomComboContributionItem zoomItem;
	protected void createZoomActions() {
		super.createActions();

		RootEditPart root = getGraphicalViewer().getRootEditPart();
		if (root instanceof ScalableRootEditPart) {
			ScalableRootEditPart rootEditPart = (ScalableRootEditPart) root;
			ActionRegistry registry = getActionRegistry();
			
			rootEditPart.getZoomManager().setZoomLevels(new double[] {0.05, 0.10, 0.25, 0.50, 0.75, 1});
			zoomItem = new ZoomComboContributionItem(getEditorSite().getPage());
			zoomItem.setId(ZOOM_BOX_ACTION_ID);

			zoomInAction = new ZoomInAction(rootEditPart.getZoomManager());
			zoomInAction.setEnabled(false);
			zoomInAction.setId(ZOOM_IN_ACTION_ID);
			
			zoomOutAction = new ZoomOutAction(rootEditPart.getZoomManager());
			zoomOutAction.setId(ZOOM_OUT_ACTION_ID);

			registry.registerAction(zoomInAction);
			registry.registerAction(zoomOutAction);
		}
	}
	
	private void updateZoomActions() {
		IToolBarManager manager = this.getEditorSite().getActionBars().getToolBarManager();
		manager.remove(ZOOM_BOX_ACTION_ID);
		manager.remove(ZOOM_IN_ACTION_ID);
		manager.remove(ZOOM_OUT_ACTION_ID);
		manager.add(zoomItem);
		manager.add(zoomInAction);
		manager.add(zoomOutAction);
		manager.update(false);
	}
}
