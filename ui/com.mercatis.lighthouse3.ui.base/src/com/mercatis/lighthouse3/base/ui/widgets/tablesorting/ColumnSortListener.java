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
package com.mercatis.lighthouse3.base.ui.widgets.tablesorting;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ColumnSortListener extends SelectionAdapter {

	private final TableViewer viewer;

	public ColumnSortListener(TableViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		TableColumn column = (TableColumn) e.getSource();
		Table table = column.getParent();
		boolean toggleCurrentSortColumn = (column == table.getSortColumn());
		if (toggleCurrentSortColumn) {
			table.setSortDirection(table.getSortDirection() == SWT.DOWN ? SWT.UP : SWT.DOWN);
		} else {
			table.setSortColumn(column);
			table.setSortDirection(SWT.UP);
		}
		viewer.refresh();
	}

}
