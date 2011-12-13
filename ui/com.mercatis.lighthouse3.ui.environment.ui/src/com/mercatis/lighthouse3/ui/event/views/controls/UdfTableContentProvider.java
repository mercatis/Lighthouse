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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;

import com.mercatis.lighthouse3.domainmodel.events.Event;
import com.mercatis.lighthouse3.ui.event.util.ClipboardHelper;

public class UdfTableContentProvider implements IStructuredContentProvider, MenuDetectListener {

	private List<UdfEntry> udfEntryList;
	private TableViewer tableViewer;
	private Menu menu;

	public UdfTableContentProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		udfEntryList = new ArrayList<UdfEntry>();
	}

	public Object[] getElements(Object inputElement) {
		return udfEntryList.toArray();
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}

	public void setEvent(Event event) {
		udfEntryList = new ArrayList<UdfEntry>();

		for (String key : event.getUdfs().keySet()) {
			udfEntryList.add(new UdfEntry(key, event.getUdf(key)));
		}

		tableViewer.refresh();
	}

	public void menuDetected(MenuDetectEvent e) {
		if (menu==null) {
			menu = new Menu((Control) e.getSource());
			MenuItem item = new MenuItem (menu, SWT.PUSH);
			item.setText("Copy");
			item.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					Table t = tableViewer.getTable();
					int idx = t.getSelectionIndex();
					UdfEntry ue = (UdfEntry) tableViewer.getElementAt(idx);
					ClipboardHelper.copyToClipboard(e.display, ue.getValue().toString());
				}				
				public void widgetDefaultSelected(SelectionEvent e) { /* ignore */ }
			});
		}
		if (menu!=null) {
			menu.setLocation(e.x, e.y);
			menu.setVisible(true);
		}
	}
}
