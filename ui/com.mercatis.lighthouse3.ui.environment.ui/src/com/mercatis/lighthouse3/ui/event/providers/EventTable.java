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
package com.mercatis.lighthouse3.ui.event.providers;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import com.mercatis.lighthouse3.base.ui.widgets.CommonTable;
import com.mercatis.lighthouse3.commons.commons.Tuple;
import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.domainmodel.events.EventBuilder;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.event.actions.AddColumnAsFilterCriteriaAction;
import com.mercatis.lighthouse3.ui.event.actions.CopyEventAsCsvAction;
import com.mercatis.lighthouse3.ui.event.actions.CopyEventXmlAction;
import com.mercatis.lighthouse3.ui.event.actions.CreateFilterFromCriteriaAction;
import com.mercatis.lighthouse3.ui.event.handler.EventDetailHandler;
import com.mercatis.lighthouse3.ui.event.providers.EventTableUIElementsConstants.ColumnType;


public class EventTable {
	
	/**ID of the tables context menu.
	 * Plugins wanting to contribute to this context menu need to use this ID
	 * 
	 */
	public static final String CONTEXT_MENU_ID = "com.mercatis.lighthouse3.ui.event.providers.EventTable.ContextMenu";
	
	/**ID of the section after which plugins should contribute their context menu actions
	 * 
	 */
	public static final String EVENT_TABLE_ADDITIONS = "tableAdditions";
	
	private EventTableContentLabelProvider contentProvider;
	private CommonTable commonTable;
	private TableViewer tableViewer;
	private IWorkbenchPartSite site;
	private LighthouseDomain lighthouseDomain;
	private Menu tableMenu;
	private Point lastMousePosition;
	private EventFilterModel eventFilterModel;
	private Set<Event> events;
	private EventDetailHandler eventDetailHandler = new EventDetailHandler();
	
	/**
	 * @param parent
	 * @param style
	 */
	public EventTable(LighthouseDomain lighthouseDomain, Composite parent, int style,
			IWorkbenchPartSite workbenchPartSite, EventFilterModel eventFilterModel, Set<Event> events) {
		this.lighthouseDomain = lighthouseDomain;
		this.site = workbenchPartSite;
		this.eventFilterModel = eventFilterModel;
		this.events = events;
		createTable(parent, style);
	}
	
	public EventTable(LighthouseDomain lighthouseDomain, Composite parent, int style,
			IWorkbenchPartSite workbenchPartSite, EventFilterModel eventFilterModel) {
		this(lighthouseDomain, parent, style, workbenchPartSite, eventFilterModel, null);
	}
	
	/**
	 * This method will register the table for all incoming events, that match
	 * the given template
	 * 
	 * @param template
	 */
	public void registerForEvents(Event template) {
		clearTable();
		tableViewer.setInput(template);
	}
	
	/**
	 * This method will unregister the table for all incoming events, that match
	 * the given template
	 */
	public void unregisterForEvents() {
		contentProvider.disconnect();
	}

	/**
	 * @param parent
	 * @param style
	 */
	private void createTable(Composite parent, int style) {
		commonTable = new CommonTable(parent, style | SWT.VIRTUAL);
		tableViewer = new TableViewer(commonTable.getTable());
		tableViewer.getTable().getVerticalBar().addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				ScrollBar scrollBar = tableViewer.getTable().getVerticalBar();
				
				int minimum = scrollBar.getMinimum();
				int selection = scrollBar.getSelection();
				contentProvider.setRevealLatestElement(selection == minimum);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					eventDetailHandler.execute(selection.getFirstElement());
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
		
		site.setSelectionProvider(tableViewer);

		Map<Color, List<Event>> rowBackGroundColorConfiguration = new HashMap<Color, List<Event>>();
		
		List<Event> eventsFatal = new LinkedList<Event>();
		eventsFatal.add(EventBuilder.template().setLevel(Event.FATAL).done());
		rowBackGroundColorConfiguration.put(new Color(parent.getDisplay(),205,0,0), eventsFatal);
		
		List<Event> eventsError = new LinkedList<Event>();
		eventsError.add(EventBuilder.template().setLevel(Event.ERROR).done());
		rowBackGroundColorConfiguration.put(new Color(parent.getDisplay(),255,116,76), eventsError);
		
		List<Event> eventsWarning = new LinkedList<Event>();
		eventsWarning.add(EventBuilder.template().setLevel(Event.WARNING).done());
		rowBackGroundColorConfiguration.put(new Color(parent.getDisplay(),255,255,0), eventsWarning);
		
		contentProvider = new EventTableContentLabelProvider(lighthouseDomain, rowBackGroundColorConfiguration, parent.getDisplay());

		ColumnViewerToolTipSupport.enableFor(tableViewer,ToolTip.NO_RECREATE);
		
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(contentProvider);
		
		if (EventTableContentLabelProvider.isLegacyWindows)
			commonTable.getTable().setLinesVisible(true);
		commonTable.getTable().setHeaderVisible(true);

		commonTable.getTable().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					lastMousePosition = new Point(e.x, e.y);
				}
			}
		});

		createColumns();
		createMenu();
		hideInitialColumns();
		commonTable.setTableMenu(tableMenu);
		tableViewer.setInput(events);
		contentProvider.setSortColumn((ColumnType) tableViewer.getTable().getSortColumn().getData(), tableViewer.getTable().getSortDirection());
	}
	
	private void hideInitialColumns() {
		commonTable.hideColumn(EventTableUIElementsConstants.getColumnText(ColumnType.TAGS));
		commonTable.hideColumn(EventTableUIElementsConstants.getColumnText(ColumnType.TRANSACTION));
	}
	
	/**
	 * 
	 */
	private void createMenu() {
		MenuManager menuMgr = new MenuManager("Events", CONTEXT_MENU_ID);
		
		ActionContributionItem aItem = new ActionContributionItem(new AddColumnAsFilterCriteriaAction(site.getWorkbenchWindow(), this));
		aItem.setId("lighthouse.events.actions.filterCriteria");
		menuMgr.add(aItem);
		ActionContributionItem bItem = new ActionContributionItem(new CreateFilterFromCriteriaAction(site.getWorkbenchWindow(), lighthouseDomain));
		bItem.setId("lighthouse.events.actions.eventToFilter");
		menuMgr.add(bItem);
		menuMgr.add(new Separator());
		ActionContributionItem cItem = new ActionContributionItem(new CopyEventXmlAction(site.getWorkbenchWindow()));
		cItem.setId(CopyEventXmlAction.ID);
		menuMgr.add(cItem);
		ActionContributionItem dItem = new ActionContributionItem(new CopyEventAsCsvAction(site.getWorkbenchWindow(), this));
		dItem.setId(CopyEventAsCsvAction.ID);
		menuMgr.add(dItem);
		menuMgr.add(new Separator(EVENT_TABLE_ADDITIONS));
		tableMenu = menuMgr.createContextMenu(site.getShell());
		
		tableViewer.getControl().setMenu(menuMgr.getMenu());
		site.registerContextMenu(CONTEXT_MENU_ID, menuMgr, tableViewer);
	}

	public void addSelectedColumnAsFilterCriteria() {
		ViewerCell cell = (ViewerCell)tableViewer.getCell(lastMousePosition);
		int columnIndex = cell.getColumnIndex();
		ColumnType columnType = (ColumnType) tableViewer.getTable().getColumn(columnIndex).getData();

		Event event = (Event) ((StructuredSelection)tableViewer.getSelection()).getFirstElement();
		List<Object> values = new LinkedList<Object>();
		int propertyIndex = EventTableUIElementsConstants.transformColumnNameToFilterPropertyIndex(columnType);
		
		switch (propertyIndex) {
			case EventFilterModel.UDF:
				for (Entry<String, Object> entry : event.getUdfs().entrySet()) {
					values.add(new Tuple<String, Object>(entry.getKey(), entry.getValue()));
				}
				values.addAll(eventFilterModel.getValuesFor(propertyIndex));
				break;
			case EventFilterModel.DATE:
				values.add(new Tuple<Date, Object>(event.getDateOfOccurrence(), null));
				break;
			case EventFilterModel.TRANSACTION_ID:
				for (String transActionId : event.getTransactionIds()) {
					values.add((Object)transActionId);
				}
				values.addAll(eventFilterModel.getValuesFor(propertyIndex));
				break;
			default:
				values.add(cell.getText());
				values.addAll(eventFilterModel.getValuesFor(propertyIndex));
		}
		eventFilterModel.setValuesFor(values, propertyIndex);
	}
	
	private Listener sortListener = new Listener() {
		public void handleEvent(org.eclipse.swt.widgets.Event e) {
			// determine new sort column and direction
			TableColumn sortColumn = tableViewer.getTable().getSortColumn();
			TableColumn currentColumn = (TableColumn) e.widget;
			int dir = tableViewer.getTable().getSortDirection();
			if (sortColumn == currentColumn) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				tableViewer.getTable().setSortColumn(currentColumn);
				dir = SWT.UP;
			}
			// sort the data based on column and direction
			contentProvider.setSortColumn((ColumnType) currentColumn.getData(), dir);
			// update data displayed in table
			tableViewer.getTable().setSortDirection(dir);
			tableViewer.getTable().clearAll();
		}
	};
	
	private TableColumn createTableColumn(ColumnType type, int width) {
		TableColumn tc = new TableColumn(commonTable.getTable(), SWT.LEFT);
		tc.setText(EventTableUIElementsConstants.getColumnText(type));
		tc.setWidth(width);
		tc.setMoveable(true);
		tc.addListener(SWT.Selection, sortListener);
		tc.setData(type);
		return tc;
	}
	
	/**
	 * Creates all columns
	 * 
	 */
	private void createColumns() {
		TableColumn sortColumn = createTableColumn(ColumnType.TIMESTAMP, 130);
		createTableColumn(ColumnType.CONTEXT, 100);
		createTableColumn(ColumnType.CODE, 100);
		createTableColumn(ColumnType.LEVEL, 70);
		createTableColumn(ColumnType.ORIGIN, 100);
		createTableColumn(ColumnType.MESSAGE, 300);		
		createTableColumn(ColumnType.TRANSACTION, 200);
		createTableColumn(ColumnType.UDF, 300);
		createTableColumn(ColumnType.TAGS, 300);
		tableViewer.getTable().setSortDirection(SWT.DOWN);
		tableViewer.getTable().setSortColumn(sortColumn);
	}

	/**
	 * @return
	 */
	public Table getEventTable() {
		return commonTable.getTable();
	}

	/**
	 * @return
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * @return the labelProvider
	 */
	public ILabelProvider getLabelProvider() {
		return contentProvider;
	}

	/**
	 * @return the contentProvider
	 */
	public IContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * @return the rowBackGroundColorConfiguration
	 */
	public Map<Color, List<Event>> getRowBackGroundColorConfiguration() {
		return contentProvider.getRowBackGroundColorConfiguration();
	}

	/**
	 * @param rowBackGroundColorConfiguration
	 *            the rowBackGroundColorConfiguration to set
	 */
	public void setRowBackGroundColorConfiguration(Map<Color, List<Event>> rowBackGroundColorConfiguration) {
		contentProvider.setRowBackGroundColorConfiguration(rowBackGroundColorConfiguration);
	}

	/**
	 * @param maxNumberOfEventsToDisplay
	 *            the maxNumberOfEventsToDisplay to set
	 */
	public void setMaxNumberOfEventsToDisplay(int maxNumberOfEventsToDisplay) {
		contentProvider.setMaxNumberOfEvents(maxNumberOfEventsToDisplay);
	}

	
	/**Resume displaying events
	 * ATTENTION:
	 * This will not register the table as a eventlistener at the eventdispatcher
	 * 
	 */
	public void resume() {
		contentProvider.connect();
	}
	
	/**Clear events from table
	 * 
	 */
	public void clearTable() {
		contentProvider.clear();
	}
	
	/**Packs all columns
	 * 
	 */
	public void packAllColumns() {
		TableColumn [] columns = commonTable.getTable().getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
	}

	public String getIdentifier() {
		return tableViewer.toString();
	}
}
