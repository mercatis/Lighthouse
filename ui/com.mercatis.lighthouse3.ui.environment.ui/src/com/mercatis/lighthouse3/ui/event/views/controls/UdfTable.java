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
package com.mercatis.lighthouse3.ui.event.views.controls;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class UdfTable {

	private final class UdfViewerSorter extends ViewerSorter {
		
		@SuppressWarnings("unchecked")
		public int compare(Viewer viewer, Object udf1, Object udf2) {
			if (udf1 == null || udf2 == null) {
				return super.compare(viewer, udf1, udf2);
			}
			final String sortKey1;
			final String sortKey2;
			final UdfEntry udfEntry1 = (UdfEntry) udf1;
			final UdfEntry udfEntry2 = (UdfEntry) udf2;
			if(udfTable.getSortColumn()==null || udfTable.getSortColumn().equals(keyColumn)){
				sortKey1 = udfEntry1.getKey();
				sortKey2 = udfEntry2.getKey();
			}
			else{
				sortKey1 = String.valueOf(udfEntry1.getValue());
				sortKey2 = String.valueOf(udfEntry2.getValue());
			}
			
			final int res = getComparator().compare(sortKey1, sortKey2);		
			
			return udfTable.getSortDirection()==SWT.DOWN ? res : -res;
		}
	}

	private ILabelProvider labelProvider;
	private IContentProvider contentProvider;
	private Table udfTable;
	private TableViewer tableViewer;
	private UdfViewerSorter sorter = new UdfViewerSorter();
	private TableColumn keyColumn;
	/**
	 * @param parent
	 * @param style
	 */
	public UdfTable(Composite parent, int style) {
		createTable(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 */
	private void createTable(Composite parent, int style) {
		udfTable = new Table(parent, style);
		udfTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = new TableViewer(udfTable);

		contentProvider = new UdfTableContentProvider(tableViewer);
		labelProvider = new UdfTableLabelProvider();

		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(labelProvider);
		tableViewer.setSorter(sorter);

		udfTable.setLinesVisible(true);
		udfTable.setHeaderVisible(true);
		udfTable.addMenuDetectListener((UdfTableContentProvider) contentProvider);

		createColumns();
		tableViewer.setInput(parent);
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
			// update data sort order
			tableViewer.getTable().setSortDirection(dir);
			tableViewer.refresh();
		}
	};

	private TableColumn createTableColumn(String text, int width, int index) {
		TableColumn tc = new TableColumn(udfTable, SWT.LEFT, index);
		tc.setText(text);
		tc.setWidth(width);
		tc.setMoveable(true);
		tc.addListener(SWT.Selection, sortListener);
		return tc;
	}

	/**
	 * Creates all columns
	 */
	private void createColumns() {
		keyColumn = createTableColumn("Key", 200, 0);
		createTableColumn("Value", 600, 1);
		udfTable.setSortColumn(keyColumn);
		udfTable.setSortDirection(SWT.DOWN);
	}

	/**
	 * @return
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * @param labelProvider
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
		tableViewer.setLabelProvider(this.labelProvider);
	}

	/**
	 * @return
	 */
	public IContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * @param contentProvider
	 */
	public void setContentProvider(IContentProvider contentProvider) {
		this.contentProvider = contentProvider;
		tableViewer.setContentProvider(this.contentProvider);
	}

	/**
	 * @return
	 */
	public Table getEventTable() {
		return udfTable;
	}

	/**
	 * @param eventTable
	 */
	public void setEventTable(Table eventTable) {
		this.udfTable = eventTable;
	}

	/**
	 * @return
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * @param tableViewer
	 */
	public void setTableViewer(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}
}
