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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ShowHideDialog extends TitleAreaDialog {

	private Map<TableColumn, Boolean> columnConfiguration;
	private List<Button> checkBoxesForColumns;
	private Table table;

	/**
	 * @param parentShell
	 * @param table
	 * @param columnsToShow
	 */
	public ShowHideDialog(Shell parentShell, Table table) {
		super(parentShell);
		this.table = table;
		checkBoxesForColumns = new LinkedList<Button>();
		initColumnNames();
		initColumnConfiguration(null);
	}

	/**
	 * @param columnsToShow
	 */
	private void initColumnConfiguration(List<String> columnsToShow) {
		if (columnsToShow != null) {
			for (TableColumn column : columnConfiguration.keySet()) {
				if (columnsToShow.contains(column.getText())) {
					columnConfiguration.put(column, true);
				} else {
					columnConfiguration.put(column, false);
				}
			}
		} else {
			for (TableColumn column : columnConfiguration.keySet()) {
				if (column.getWidth() == 0) {
					columnConfiguration.put(column, false);
				} else {
					columnConfiguration.put(column, true);
				}
			}
		}
	}

	/**
	 * 
	 */
	private void initColumnNames() {
		if (table != null) {
			columnConfiguration = new HashMap<TableColumn, Boolean>();
			columnConfiguration = new TreeMap<TableColumn, Boolean>(new TableColumnComparator());
			for (int i = 0; i < table.getColumns().length; i++) {
				columnConfiguration.put(table.getColumns()[i], true);
			}
		}

	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Show/Hide Columns");
		setMessage("Please choose the columns to show", IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		return createCheckBoxs(parent);
	}

	/**
	 * Creates the columns
	 * 
	 * @param parent
	 * @return
	 */
	private Control createCheckBoxs(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		for (TableColumn column : columnConfiguration.keySet()) {
			Button checkBox = new Button(parent, SWT.CHECK);
			checkBox.setText(column.getText());
			checkBoxesForColumns.add(checkBox);
		}
		checkUncheckColums();
		return parent;
	}

	/**
	 * 
	 */
	private void checkUncheckColums() {
		for (TableColumn column : columnConfiguration.keySet()) {
			Button checkBox = getCheckBoxByName(column.getText());
			if (checkBox != null) {
				checkBox.setSelection(columnConfiguration.get(column));
			}
		}
	}

	/**
	 * @param checkBoxName
	 * @return
	 */
	private Button getCheckBoxByName(String checkBoxName) {
		for (Button checkBox : checkBoxesForColumns) {
			if (checkBox.getText().equals(checkBoxName)) {
				return checkBox;
			}
		}
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText("OK");
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (TableColumn column : columnConfiguration.keySet()) {
					Button checkBox = getCheckBoxByName(column.getText());
					if (checkBox != null) {
						columnConfiguration.put(column, checkBox.getSelection());
					}
				}
				close();
			}
		});
	}

	/**
	 * @return the columnConfiguration
	 */
	public Map<TableColumn, Boolean> getColumnConfiguration() {
		return columnConfiguration;
	}

	/**
	 * @param columnsToShow
	 *            the columnsToShow to set
	 */
	public void configureDialog(List<String> columnsToShow) {
		initColumnConfiguration(columnsToShow);
		checkUncheckColums();
	}

	private class TableColumnComparator implements Comparator<TableColumn> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(TableColumn o1, TableColumn o2) {
			if (o1 == null || o2 == null || o1.getText() == null || o2.getText() == null) {
				return 0;
			} else {
				return o1.getText().compareTo(o2.getText());
			}
		}

	}
}