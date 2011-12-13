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
package com.mercatis.lighthouse3.base.ui.widgets;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.mercatis.lighthouse3.base.ui.widgets.tablesorting.ColumnSortListener;

/**
 * Common Table is a wrapper class around the swt table.
 * It automatically provides a header-context menu with the following functionality:
 * -show/hide selected column
 * -show/hide any of the tables columns
 * -pack selected column (autor resize column width)
 * -pack all visible columns
 * 
 * If the table context menu is provided via {@code setTableMenu(menu)}
 * it will be displayed as context menu for the table
 */
public class CommonTable {

	private Table table;
	private Menu headerMenu;
	private Menu tableMenu;
	
	private SelectionAdapter selectionAdaptor;
	
	/**Creates a table 
	 * If a menu is passed, this menu is displayed as table-menu
	 * 
	 * @param parent
	 * @param style
	 * @param tableMenu
	 */
	public CommonTable(Composite parent, int style) {
		table = new Table(parent, style);
		init();
	}
	
	/**
	 * 
	 */
	private void init() {
		//now add a listener which displays
		//the header menu or the normal context menu
		//depending on whether the user performed
		//a context click on the table header or in the table
		table.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent event) {
				Point pt = Display.getCurrent().map(null, table, new Point(event.x, event.y));
				Rectangle clientArea = table.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
				if (header) {
					createHeaderMenu(getSelectedColumn(pt));
				}
				table.setMenu(header ? headerMenu : tableMenu);
			}});
	}
	
	/**
	 * @param selectedColumn
	 */
	private void createHeaderMenu(final TableColumn selectedColumn) {
		headerMenu = new Menu(table.getShell(), SWT.POP_UP);
		if (selectedColumn != null) {
			final MenuItem hideSelectedColumn = new MenuItem(headerMenu, SWT.PUSH);
			hideSelectedColumn.setText("Hide Column: " + selectedColumn.getText());
			hideSelectedColumn.addListener(SWT.Selection, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					selectedColumn.setWidth(0);
					selectedColumn.setResizable(false);
				}
			});

			new MenuItem(headerMenu, SWT.SEPARATOR);

		}
		TableColumn[] comlumns = table.getColumns();
		for (int i = 0; i < comlumns.length; i++) {
			createCheckMenuItem(headerMenu, comlumns[i]);
		}
		new MenuItem(headerMenu, SWT.SEPARATOR);
		if (selectedColumn != null) {
			final MenuItem packSelectedColumn = new MenuItem(headerMenu, SWT.PUSH);
			packSelectedColumn.setText("Pack Column: " + selectedColumn.getText());
			packSelectedColumn.addListener(SWT.Selection, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					selectedColumn.pack();
				}
			});
		}
		final MenuItem packAllColumns = new MenuItem(headerMenu, SWT.PUSH);
		packAllColumns.setText("Pack all Columns");
		packAllColumns.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				TableColumn [] columns =  table.getColumns();
				System.out.println("Pack all columns");
				for (int i = 0; i < columns.length; i++) {
					//only pack visible columns
					if(columns[i].getWidth() != 0) {
						columns[i].pack();
					}
				}
			}
		});
	}
	
	/**If sorting is not yet enabled on the table, it will be enabled
	 * 
	 * If TableViewer.getComparator() returns null, the default comparator {@link ViewerComparator} will be set
	 * Otherwise the ViewerComparator which is set will be used
	 * 
	 * The default sorting column is the first column
	 * 
	 * @param viewer
	 * @param sortingColumn the initial sorting column. Must be in the range of the available sort columns, otherwise the first column will be used. Zero based start
	 * @param sortDirection can be SWT.UP or SWT.DOWN according to the initial sort direction
	 */
	public void enableSorting(TableViewer viewer, int sortingColumn, int sortDirection) {
		if(selectionAdaptor == null) {
			selectionAdaptor = new ColumnSortListener(viewer);
		}
			
		TableColumn[] columns = table.getColumns();
		int sColumn = 0;
		if(0 <= sortingColumn && sortingColumn < columns.length) {
			sColumn = sortingColumn;
		}
		if(columns != null) {
			for (int i = 0; i < columns.length; i++) {
				columns[i].addSelectionListener(selectionAdaptor);
			}
			
			table.setSortColumn(columns[sColumn]);
		}
		table.setSortDirection(sortDirection);
			
	}
	
	/**
	 * @param parent
	 * @param column
	 */
	private void createCheckMenuItem(Menu parent, final TableColumn column) {
		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getResizable());
		itemName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (itemName.getSelection()) {
					showColumn(column);
				} else {
					hideColumn(column);
				}
			}
		});
	}
	
	
	/**Return the selected column in a table
	 * @param pt the mouse point
	 * @return the selected column or null
	 */
	private TableColumn getSelectedColumn(Point pt) {

		TableColumn selectedColumn = null;

		int[] columnOrder = table.getColumnOrder();
		int totalWidthSoFar = 0;
		for (int i = 0; i < columnOrder.length; i++) {
			TableColumn temp = table.getColumns()[columnOrder[i]];
			if (totalWidthSoFar <= pt.x && pt.x <= (totalWidthSoFar + temp.getWidth())) {
				selectedColumn = temp;
				break;
			}

			totalWidthSoFar += temp.getWidth();
		}
		return selectedColumn;
	}
	
	/*
	 * Column show/hide stuff
	 */

	/**
	 * Returns the column which matches the given name
	 * 
	 * @param columName
	 * @return TableColumn or null if column not found
	 */
	public TableColumn getColumnByName(String columName) {
		for (int i = 0; i < table.getColumns().length; i++) {
			if (table.getColumns()[i].getText().equals(columName)) {
				return table.getColumns()[i];
			}
		}
		return null;
	}

	/**
	 * Returns the column index of the column which matches the given name
	 * 
	 * @param columName
	 * @return index or -1 if column not found
	 */
	public int getColumnIndexByName(String columName) {
		int index = -1;
		for (int i = 0; i < table.getColumns().length; i++) {
			if (table.getColumns()[i].equals(columName)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**Returns the column for the given index
	 * @param columnIndex
	 * @return
	 */
	public TableColumn getColumnNameByIndex(int columnIndex) {
		return table.getColumn(columnIndex);
	}
	
	/**
	 * Hides the given column
	 * 
	 * @param column
	 */
	public void hideColumn(final TableColumn column) {
		column.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (column != null) {
					System.out.println("hiding column "+column.getText());
					column.setWidth(0);
					column.setResizable(false);
				}
			}
		});
	}

	/**
	 * Hides the column, which has the given name
	 * 
	 * @param columnName
	 */
	public void hideColumn(String columnName) {
		TableColumn column = getColumnByName(columnName);
		hideColumn(column);
	}
	
	/**Set all given columns visible visible 
	 * @param visibleColumns
	 */
	public void setColumnsVisible(List<String> visibleColumns) {
		TableColumn [] col = table.getColumns();
		for (int i = 0; i < col.length; i++) {
			hideColumn(col[i]);
		}
		for (String tableColumnName : visibleColumns) {
			showColumn(tableColumnName);
		}
	}

	/**
	 * Shows the given column
	 * 
	 * @param column
	 */
	public void showColumn(final TableColumn column) {
		column.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (column != null) {
					if (column.getWidth() == 0) {
						column.pack();
					}
					column.setResizable(true);
				}
			}
		});
	}

	/**
	 * shows the column, which has the given name
	 * 
	 * @param columnName
	 */
	public void showColumn(String columnName) {
		TableColumn column = getColumnByName(columnName);
		showColumn(column);
	}


	/**
	 * @return
	 */
	public Table getTable() {
		return table;
	}
	
	/**
	 * @return
	 */
	public Menu getTableMenu() {
		return tableMenu;
	}

	/**
	 * @param tableMenu
	 */
	public void setTableMenu(Menu tableMenu) {
		this.tableMenu = tableMenu;
	}

	/**
	 * @return the columnSortListener
	 */
	public SelectionAdapter getColumnSortListener() {
		return selectionAdaptor;
	}

	/**
	 * @param columnSortListener the columnSortListener to set
	 */
	public void setColumnSortListener(SelectionAdapter selectionAdaptor) {
		this.selectionAdaptor = selectionAdaptor;
	}
}
