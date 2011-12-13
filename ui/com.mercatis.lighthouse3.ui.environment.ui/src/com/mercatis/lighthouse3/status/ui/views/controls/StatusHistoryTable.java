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
package com.mercatis.lighthouse3.status.ui.views.controls;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;

import com.mercatis.lighthouse3.base.getterchain.GetterChain;
import com.mercatis.lighthouse3.base.ui.widgets.CommonTable;
import com.mercatis.lighthouse3.base.ui.widgets.tablesorting.ColumnComparator;
import com.mercatis.lighthouse3.base.ui.widgets.tablesorting.ColumnGenerator;
import com.mercatis.lighthouse3.domainmodel.status.EventTriggeredStatusChange;
import com.mercatis.lighthouse3.domainmodel.status.ManualStatusClearance;
import com.mercatis.lighthouse3.domainmodel.status.StalenessChange;
import com.mercatis.lighthouse3.domainmodel.status.StatusChange;
import com.mercatis.lighthouse3.status.ui.model.StatusGetter;


public class StatusHistoryTable {

	/**
	 * ID of the tables context menu. Plugins wanting to contribute to this
	 * context menu need to use this ID
	 * 
	 */
	public static final String STATUS_HISTORY_MENU_ID = "com.mercatis.lighthouse3.status.ui.views.controls.StatusHistoryTable.ContextMenu";

	/**
	 * ID of the section after which plugins should contribute their context
	 * menu actions
	 * 
	 */
	public static final String STATUS_HISTORY_TABLE_ADDITIONS = "tableAdditions";

	private ILabelProvider labelProvider;
	private IContentProvider contentProvider;
	private CommonTable commonTable;
	private TableViewer tableViewer;
	private IWorkbenchPartSite site;
	private Menu tableMenu;
	private ColumnGenerator columnGenerator;
	
	private int[] colWidths = {180, 100, 50, 50, 50, 120};
	
	/**
	 * @param parent
	 * @param style
	 */
	public StatusHistoryTable(Composite parent, int style, IWorkbenchPartSite site) {
		this.site = site;
		createTable(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 */
	private void createTable(Composite parent, int style) {
		commonTable = new CommonTable(parent, style);
		commonTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		columnGenerator = new ColumnGenerator(commonTable.getTable());
		
		tableViewer = new TableViewer(commonTable.getTable());
		site.setSelectionProvider(tableViewer);
		
		contentProvider = new StatusHistoryTableContentProvider(tableViewer);
		labelProvider = new StatusHistoryTableLabelProvider();

		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(labelProvider);

		commonTable.getTable().setLinesVisible(true);
		commonTable.getTable().setHeaderVisible(true);

		createColumns();
		createMenu(parent);
		commonTable.setTableMenu(tableMenu);
		tableViewer.setInput(parent);
		
		tableViewer.setComparator(new ColumnComparator());
		commonTable.enableSorting(tableViewer, 0, SWT.DOWN);
	}

	/**
	 * 
	 */
	private void createMenu(Composite parent) {

		MenuManager menuMgr = new MenuManager("Status History", STATUS_HISTORY_MENU_ID);

		menuMgr.add(new Separator(STATUS_HISTORY_TABLE_ADDITIONS));
		menuMgr.createContextMenu(parent);

		tableMenu = menuMgr.createContextMenu(site.getShell());
		
		tableViewer.getControl().setMenu(menuMgr.getMenu());
		site.registerContextMenu(STATUS_HISTORY_MENU_ID, menuMgr, tableViewer);
	}

	
	private void addColumn(ColumnGenerator gen, String property, int flags, String label) {
		GetterChain chain = new GetterChain();
		chain.registerGetter(EventTriggeredStatusChange.class, StatusGetter.class);
		chain.registerGetter(ManualStatusClearance.class, StatusGetter.class);
		chain.registerGetter(StalenessChange.class, StatusGetter.class);
		chain.registerGetter(StatusChange.class, StatusGetter.class);
		chain.buildGetterChain(StatusChange.class, property);
		
		int i = commonTable.getTable().getColumnCount();
		TableColumn column = gen.createTableColumnWithSortProperty(chain, flags);
		column.setText(label);
		if (colWidths[i] > 0)
			column.setWidth(colWidths[i]);
		else
			commonTable.hideColumn(column);
	}
	
	/**
	 * Creates all columns
	 */
	private void createColumns() {
		addColumn(columnGenerator, "dateOfChange", SWT.LEFT, "Date");
		addColumn(columnGenerator, "newStatus", SWT.LEFT, "Current Status");
		addColumn(columnGenerator, "errorCounter", SWT.LEFT, "#Error");
		addColumn(columnGenerator, "okCounter", SWT.LEFT, "#OK");
		addColumn(columnGenerator, "staleCounter", SWT.LEFT, "#Stale");
		addColumn(columnGenerator, "causedBy", SWT.LEFT, "Caused by");
	}

	/**
	 * @return
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	/**
	 * @return
	 */
	public IContentProvider getContentProvider() {
		return contentProvider;
	}
}
